package com.fenglin.springboottest.controller;

import com.fenglin.springboottest.auth.CurrentUser;
import com.fenglin.springboottest.service.MessageService;
import jakarta.servlet.http.HttpServletRequest;
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
 *  GET    /api/content/messages/conversations   -> { ok, items: [Conversation] }
 *  GET    /api/content/messages?peer=&peerName= -> { ok, conversation }
 *  POST   /api/content/messages                 { peer, peerName, text, fromName } -> { ok, message }
 *  POST   /api/content/messages/read            { peer } -> { ok }
 *  DELETE /api/content/messages?peer=           -> { ok }
 *
 * Conversation: { peerKey, peerName, messages: [Message], updatedAt }
 * Message     : { id, from, fromName, text, createdAt, read }
 *
 * 身份（2026-11 起）：owner 一律取登录会话用户，前端传参仅作兼容、忽略 —— 私信只能看/发自己的。
 */
@RestController
@RequestMapping("/api/content")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    /** 会话列表（当前登录用户的所有会话） */
    @GetMapping("/messages/conversations")
    public Map<String, Object> conversations(HttpServletRequest request) {
        List<Map<String, Object>> items = messageService.conversations(CurrentUser.require(request));
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("ok", true);
        m.put("items", items);
        return m;
    }

    /** 单个会话的聊天记录 */
    @GetMapping("/messages")
    public Map<String, Object> conversation(@RequestParam(required = false) String peer,
                                            @RequestParam(required = false) String peerName,
                                            HttpServletRequest request) {
        Map<String, Object> c = messageService.conversation(CurrentUser.require(request), peer, peerName);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("ok", c != null);
        m.put("conversation", c);
        return m;
    }

    /** 发送消息（发送方 = 登录用户；fromName 仅作展示昵称） */
    @PostMapping("/messages")
    public Map<String, Object> send(@RequestBody Map<String, String> body, HttpServletRequest request) {
        Map<String, Object> msg = messageService.send(
                CurrentUser.require(request), body.get("peer"), body.get("peerName"),
                body.get("text"), body.get("fromName"));
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("ok", msg != null);
        m.put("message", msg);
        return m;
    }

    /** 标记会话已读 */
    @PostMapping("/messages/read")
    public Map<String, Object> read(@RequestBody Map<String, String> body, HttpServletRequest request) {
        messageService.markRead(CurrentUser.require(request), body.get("peer"));
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("ok", true);
        return m;
    }

    /** 删除会话 */
    @DeleteMapping("/messages")
    public Map<String, Object> remove(@RequestParam(required = false) String peer,
                                      HttpServletRequest request) {
        messageService.remove(CurrentUser.require(request), peer);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("ok", true);
        return m;
    }
}
