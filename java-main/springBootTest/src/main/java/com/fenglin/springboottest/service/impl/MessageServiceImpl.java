package com.fenglin.springboottest.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.fenglin.springboottest.entity.PetMessage;
import com.fenglin.springboottest.mapper.PetMessageMapper;
import com.fenglin.springboottest.service.MessageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 好友私信实现（表 pet_message）。
 *
 * 存储策略：一次查询取回 owner 名下全部消息（演示级数据量很小），
 * 在内存里按 peer_key 分组，避免按会话逐个查库造成 N+1。
 * 会话的最后时间取该组内最新一条消息的 created_at。
 */
@Service
public class MessageServiceImpl implements MessageService {

    private final PetMessageMapper messageMapper;

    public MessageServiceImpl(PetMessageMapper messageMapper) {
        this.messageMapper = messageMapper;
    }

    @Override
    public List<Map<String, Object>> conversations(String ownerKey) {
        if (ownerKey == null || ownerKey.isBlank()) return new ArrayList<>();
        List<PetMessage> all = allOf(ownerKey);

        // 按对方分组（peer_key 语义：会话里「另一个人」的标识）
        Map<String, List<PetMessage>> grouped = new LinkedHashMap<>();
        Map<String, String> names = new LinkedHashMap<>();
        for (PetMessage m : all) {
            String peer = peerOf(m, ownerKey);
            grouped.computeIfAbsent(peer, k -> new ArrayList<>()).add(m);
            String name = m.getPeerName();
            if (name != null && !name.isBlank()) names.put(peer, name);
        }

        List<Map<String, Object>> out = new ArrayList<>(grouped.size());
        for (Map.Entry<String, List<PetMessage>> e : grouped.entrySet()) {
            String peer = e.getKey();
            List<PetMessage> list = e.getValue();
            list.sort((a, b) -> {
                LocalDateTime x = a.getCreatedAt();
                LocalDateTime y = b.getCreatedAt();
                if (x == null && y == null) return 0;
                if (x == null) return -1;
                if (y == null) return 1;
                return x.compareTo(y);
            });

            List<Map<String, Object>> msgs = new ArrayList<>(list.size());
            for (PetMessage m : list) msgs.add(msgView(m, ownerKey));

            Map<String, Object> c = new LinkedHashMap<>();
            c.put("peerKey", peer);
            c.put("peerName", names.getOrDefault(peer, peer));
            c.put("messages", msgs);
            PetMessage last = list.get(list.size() - 1);
            c.put("updatedAt", last.getCreatedAt());
            out.add(c);
        }

        // 会话按最后一条消息倒序
        out.sort((a, b) -> {
            LocalDateTime x = (LocalDateTime) a.get("updatedAt");
            LocalDateTime y = (LocalDateTime) b.get("updatedAt");
            if (x == null && y == null) return 0;
            if (x == null) return 1;
            if (y == null) return -1;
            return y.compareTo(x);
        });
        return out;
    }

    @Override
    public Map<String, Object> conversation(String ownerKey, String peerKey, String peerName) {
        if (ownerKey == null || peerKey == null) return null;
        List<PetMessage> all = allOf(ownerKey);
        List<Map<String, Object>> msgs = new ArrayList<>();
        for (PetMessage m : all) {
            if (peerKey.equals(peerOf(m, ownerKey))) msgs.add(msgView(m, ownerKey));
        }
        Map<String, Object> c = new LinkedHashMap<>();
        c.put("peerKey", peerKey);
        c.put("peerName", peerName == null || peerName.isBlank() ? peerKey : peerName);
        c.put("messages", msgs);
        return c;
    }

    @Override
    @Transactional
    public Map<String, Object> send(String ownerKey, String peerKey, String peerName,
                                    String content, String fromName) {
        if (ownerKey == null || peerKey == null) return null;
        if (content == null || content.isBlank()) return null;

        LocalDateTime now = LocalDateTime.now();

        // 1) 我这一侧：from_key = 我，已读
        PetMessage mine = new PetMessage();
        mine.setOwnerKey(ownerKey);
        mine.setPeerKey(peerKey);
        mine.setPeerName(peerName);
        mine.setFromKey(ownerKey);
        mine.setFromName(fromName);
        mine.setContent(content);
        mine.setReadFlag(true);
        mine.setCreatedAt(now);
        messageMapper.insert(mine);

        // 2) 对方那一侧：from_key = 我，未读（演示里对方登录后能看到）
        PetMessage theirs = new PetMessage();
        theirs.setOwnerKey(peerKey);
        theirs.setPeerKey(ownerKey);
        theirs.setPeerName(fromName);
        theirs.setFromKey(ownerKey);
        theirs.setFromName(fromName);
        theirs.setContent(content);
        theirs.setReadFlag(false);
        theirs.setCreatedAt(now);
        messageMapper.insert(theirs);

        return msgView(mine, ownerKey);
    }

    @Override
    @Transactional
    public void markRead(String ownerKey, String peerKey) {
        if (ownerKey == null || peerKey == null) return;
        messageMapper.update(null, new LambdaUpdateWrapper<PetMessage>()
                .eq(PetMessage::getOwnerKey, ownerKey)
                .eq(PetMessage::getPeerKey, peerKey)
                .ne(PetMessage::getFromKey, ownerKey)
                .set(PetMessage::getReadFlag, true));
    }

    @Override
    @Transactional
    public void remove(String ownerKey, String peerKey) {
        if (ownerKey == null || peerKey == null) return;
        messageMapper.delete(new LambdaQueryWrapper<PetMessage>()
                .eq(PetMessage::getOwnerKey, ownerKey)
                .eq(PetMessage::getPeerKey, peerKey));
    }

    /* ---------------- 内部辅助 ---------------- */

    private List<PetMessage> allOf(String ownerKey) {
        return messageMapper.selectList(new LambdaQueryWrapper<PetMessage>()
                .eq(PetMessage::getOwnerKey, ownerKey));
    }

    /**
     * 会话里「对方」的标识。
     * peer_key 已经是对端口径存好的，直接用；为空时回退到 from_key。
     */
    private String peerOf(PetMessage m, String ownerKey) {
        String peer = m.getPeerKey();
        if (peer != null && !peer.isBlank()) return peer;
        return ownerKey.equals(m.getFromKey()) ? "" : m.getFromKey();
    }

    /** 输出结构与前端 api/message.js 约定一致 */
    private Map<String, Object> msgView(PetMessage m, String ownerKey) {
        Map<String, Object> v = new LinkedHashMap<>();
        v.put("id", m.getId());
        v.put("from", m.getFromKey());
        v.put("fromName", m.getFromName());
        v.put("text", m.getContent());
        v.put("createdAt", m.getCreatedAt());
        v.put("read", Boolean.TRUE.equals(m.getReadFlag()) || ownerKey.equals(m.getFromKey()));
        return v;
    }
}
