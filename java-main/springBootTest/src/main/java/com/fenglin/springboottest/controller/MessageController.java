package com.fenglin.springboottest.controller;

import com.fenglin.springboottest.service.MessageService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 好友私信接口（与前端 login-vue/src/api/message.js 的约定一一对应）：
 *
 *  GET  /api/content/messages/conversations?owner=      -> { ok, items: [Conversation] }
 *  GET  /api/content/messages?owner=&peer=&peerName=    -> { ok, conversation }
 *  POST /api/content/messages                           { owner, peer, peerName, text, fromName } -> { ok, message }
 *  POST /api/content/messages/read                      { owner, peer } -> { ok }
 *  DELETE /api/content/messages?owner=&peer=             -> { ok }
 *
 * Conversation: { peerKey, peerName, messages: [Message], updatedAt }
 * Message     : { id, from, fromName, text, createdAt, read }
 */
@RestController
@RequestMapping("/api/content")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    /** 会话列表 */
    @GetMapping("/messages/conversations")
    public Map<String, Object> conversations(@RequestParam(required = false) String owner) {
        List<Map<String, Object>> items = messageService.conversations(owner);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("ok", true);
        m.put("items", items);
        return m;
    }

    /** 单个会话的聊天记录 */
    @GetMapping("/messages")
    public Map<String, Object> conversation(@RequestParam(required = false) String owner,
                                           @RequestParam(required = false) String peer,
                                           @RequestParam(required = false) String peerName) {
        Map<String, Object> c = messageService.conversation(owner, peer, peerName);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("ok", c != null);
        m.put("conversation", c);
        return m;
    }

    /** 发送消息 */
    @PostMapping("/messages")
    public Map<String, Object> send(@RequestBody Map<String, String> body) {
        Map<String, Object> msg = messageService.send(
                body.get("owner"), body.get("peer"), body.get("peerName"),
                body.get("text"), body.get("fromName"));
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("ok", msg != null);
        m.put("message", msg);
        return m;
    }

    /** 标记会话已读 */
    @PostMapping("/messages/read")
    public Map<String, Object> read(@RequestBody Map<String, String> body) {
        messageService.markRead(body.get("owner"), body.get("peer"));
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("ok", true);
        return m;
    }

    /** 删除会话 */
    @DeleteMapping("/messages")
    public Map<String, Object> remove(@RequestParam(required = false) String owner,
                                      @RequestParam(required = false) String peer) {
        messageService.remove(owner, peer);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("ok", true);
        return m;
    }
}
