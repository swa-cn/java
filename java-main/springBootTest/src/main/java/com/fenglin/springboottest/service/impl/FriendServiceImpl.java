package com.fenglin.springboottest.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fenglin.springboottest.entity.AppUser;
import com.fenglin.springboottest.entity.PetFriend;
import com.fenglin.springboottest.mapper.AppUserMapper;
import com.fenglin.springboottest.mapper.PetFriendMapper;
import com.fenglin.springboottest.service.FriendService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 好友关系实现（表 pet_friend）。
 *
 * 两条记录才叫好友：
 *   申请时     ：只写 A→B（status=pending）。B 的列表里看到「待处理申请」。
 *   通过时     ：A→B 改成 friend，同时补写 B→A（friend），双方互相可见。
 * 这样能清楚区分「我发出的申请」和「别人发给我的申请」，
 * 也不会出现「只有单向好友」的问题。
 */
@Service
public class FriendServiceImpl implements FriendService {

    private final PetFriendMapper friendMapper;
    private final AppUserMapper userMapper;

    public FriendServiceImpl(PetFriendMapper friendMapper, AppUserMapper userMapper) {
        this.friendMapper = friendMapper;
        this.userMapper = userMapper;
    }

    @Override
    public List<Map<String, Object>> list(String ownerKey, String status) {
        if (ownerKey == null || ownerKey.isBlank()) return new ArrayList<>();
        LambdaQueryWrapper<PetFriend> q = new LambdaQueryWrapper<PetFriend>()
                .eq(PetFriend::getOwnerKey, ownerKey);
        if (status != null && !status.isBlank()) {
            q.eq(PetFriend::getStatus, status);
        }
        q.orderByDesc(PetFriend::getCreatedAt);
        List<PetFriend> rows = friendMapper.selectList(q);
        List<Map<String, Object>> out = new ArrayList<>(rows.size());
        for (PetFriend f : rows) out.add(view(f, ownerKey));
        return out;
    }

    @Override
    public List<Map<String, Object>> listPending(String ownerKey) {
        // 我发出的申请（可撤回）
        List<Map<String, Object>> all = list(ownerKey, "pending");
        List<Map<String, Object>> out = new ArrayList<>();
        for (Map<String, Object> r : all) {
            r.put("incoming", false);
            out.add(r);
        }
        return out;
    }

    @Override
    public List<Map<String, Object>> listIncoming(String ownerKey) {
        if (ownerKey == null || ownerKey.isBlank()) return new ArrayList<>();
        // 别人发给我的申请：方向是 target → 我，且仍是 pending
        List<PetFriend> rows = friendMapper.selectList(new LambdaQueryWrapper<PetFriend>()
                .eq(PetFriend::getFriendKey, ownerKey)
                .eq(PetFriend::getStatus, "pending")
                .orderByDesc(PetFriend::getCreatedAt));
        List<Map<String, Object>> out = new ArrayList<>(rows.size());
        for (PetFriend f : rows) {
            // 从「我」的视角输出：对方是 owner_key
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("key", f.getOwnerKey());
            m.put("name", displayNameOf(f.getOwnerKey()));
            m.put("status", "pending");
            m.put("from", f.getSource());
            m.put("createdAt", f.getCreatedAt());
            m.put("incoming", true);
            out.add(m);
        }
        return out;
    }

    @Override
    public String relation(String ownerKey, String targetKey) {
        if (ownerKey == null || targetKey == null) return "none";
        PetFriend mine = find(ownerKey, targetKey);
        if (mine != null) {
            return "friend".equals(mine.getStatus()) ? "friend" : "pending_out";
        }
        PetFriend theirs = find(targetKey, ownerKey);
        if (theirs != null && "pending".equals(theirs.getStatus())) return "pending_in";
        return "none";
    }

    @Override
    @Transactional
    public Map<String, Object> add(String ownerKey, String targetKey, String targetName, String source) {
        if (ownerKey == null || ownerKey.isBlank() || targetKey == null || targetKey.isBlank()) {
            return null;
        }
        if (ownerKey.equals(targetKey)) return null;

        PetFriend exist = find(ownerKey, targetKey);
        if (exist != null) {
            // 已经是好友 / 已发过申请：幂等返回
            return view(exist, ownerKey);
        }

        // 如果对方已经先给我发过申请，这次「添加」等价于同意，直接建立好友关系
        PetFriend reverse = find(targetKey, ownerKey);
        if (reverse != null && "pending".equals(reverse.getStatus())) {
            return accept(ownerKey, targetKey);
        }

        PetFriend f = new PetFriend();
        f.setOwnerKey(ownerKey);
        f.setFriendKey(targetKey);
        f.setFriendName(targetName);
        f.setStatus("pending");
        f.setSource(source == null || source.isBlank() ? "manual" : source);
        f.setCreatedAt(LocalDateTime.now());
        friendMapper.insert(f);
        return view(f, ownerKey);
    }

    @Override
    @Transactional
    public Map<String, Object> accept(String ownerKey, String targetKey) {
        // 场景：target 之前给我（ownerKey）发过申请，我现在同意
        PetFriend reverse = find(targetKey, ownerKey);
        LocalDateTime now = LocalDateTime.now();

        if (reverse == null) {
            // 没有对方的申请记录，但仍允许直接建立（例如双方同时点添加）
            PetFriend f = new PetFriend();
            f.setOwnerKey(ownerKey);
            f.setFriendKey(targetKey);
            f.setFriendName(displayNameOf(targetKey));
            f.setStatus("friend");
            f.setSource("manual");
            f.setCreatedAt(now);
            f.setAcceptedAt(now);
            friendMapper.insert(f);
        } else {
            reverse.setStatus("friend");
            reverse.setAcceptedAt(now);
            friendMapper.updateById(reverse);
        }

        // 补上反向记录，保证双向可见
        PetFriend mine = find(ownerKey, targetKey);
        if (mine == null) {
            PetFriend m = new PetFriend();
            m.setOwnerKey(ownerKey);
            m.setFriendKey(targetKey);
            m.setFriendName(reverse == null ? targetKey : displayNameOf(targetKey));
            m.setStatus("friend");
            m.setSource(reverse == null ? "manual" : reverse.getSource());
            m.setCreatedAt(now);
            m.setAcceptedAt(now);
            friendMapper.insert(m);
        } else {
            mine.setStatus("friend");
            if (mine.getAcceptedAt() == null) mine.setAcceptedAt(now);
            friendMapper.updateById(mine);
        }

        PetFriend result = find(ownerKey, targetKey);
        return result == null ? null : view(result, ownerKey);
    }

    @Override
    @Transactional
    public void remove(String ownerKey, String targetKey) {
        if (ownerKey == null || targetKey == null) return;
        // 双向都删：删除好友时两条一起清；撤回申请时反向本就可能不存在
        friendMapper.delete(new LambdaQueryWrapper<PetFriend>()
                .eq(PetFriend::getOwnerKey, ownerKey)
                .eq(PetFriend::getFriendKey, targetKey));
        friendMapper.delete(new LambdaQueryWrapper<PetFriend>()
                .eq(PetFriend::getOwnerKey, targetKey)
                .eq(PetFriend::getFriendKey, ownerKey));
    }

    @Override
    public List<Map<String, Object>> searchUsers(String ownerKey, String keyword) {
        String kw = keyword == null ? "" : keyword.trim();
        if (kw.isEmpty()) return new ArrayList<>();

        List<AppUser> users = userMapper.selectList(new LambdaQueryWrapper<AppUser>()
                .and(w -> w.like(AppUser::getUsername, kw).or().like(AppUser::getNickname, kw))
                .last("LIMIT 20"));

        List<Map<String, Object>> out = new ArrayList<>();
        for (AppUser u : users) {
            if (u.getUsername() == null || u.getUsername().equals(ownerKey)) continue;
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("key", u.getUsername());
            m.put("name", u.getNickname() == null || u.getNickname().isBlank()
                    ? u.getUsername() : u.getNickname());
            m.put("status", relation(ownerKey, u.getUsername()));
            out.add(m);
        }
        return out;
    }

    /* ---------------- 内部辅助 ---------------- */

    private PetFriend find(String ownerKey, String targetKey) {
        if (ownerKey == null || targetKey == null) return null;
        return friendMapper.selectOne(new LambdaQueryWrapper<PetFriend>()
                .eq(PetFriend::getOwnerKey, ownerKey)
                .eq(PetFriend::getFriendKey, targetKey)
                .last("LIMIT 1"));
    }

    /** 取某个用户名的展示昵称 */
    private String displayNameOf(String username) {
        if (username == null) return "";
        AppUser u = userMapper.selectOne(new LambdaQueryWrapper<AppUser>()
                .eq(AppUser::getUsername, username)
                .last("LIMIT 1"));
        if (u == null) return username;
        return u.getNickname() == null || u.getNickname().isBlank() ? username : u.getNickname();
    }

    /** 输出结构与前端 api/friend.js 约定一致 */
    private Map<String, Object> view(PetFriend f, String ownerKey) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("key", f.getFriendKey());
        m.put("name", f.getFriendName() == null || f.getFriendName().isBlank()
                ? f.getFriendKey() : f.getFriendName());
        m.put("status", f.getStatus());
        m.put("from", f.getSource());
        m.put("createdAt", f.getCreatedAt());
        m.put("incoming", false);
        return m;
    }
}
