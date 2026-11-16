package com.fenglin.springboottest.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 私信实时推送处理器（/ws/chat，2026-11 新增）。
 *
 * 连接鉴权：握手拦截器（WebSocketConfig）把 HttpSession 里的登录用户名
 * 放进 WebSocket 会话属性 "user"，未登录直接拒绝握手（401）。
 *
 * 推送模型：MessageServiceImpl.send() 落库成功后调用 notifyUser()，
 * 给收件人（以及发件人自己的其他标签页）推一条 {type:'message'} 事件，
 * 前端收到后刷新会话列表。历史消息仍走 HTTP 接口，WS 只做「提醒」。
 */
@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(ChatWebSocketHandler.class);

    /** 登录名 -> 该用户的全部 WS 连接（同一用户可开多个标签页） */
    private final Map<String, Set<WebSocketSession>> sessions = new ConcurrentHashMap<>();

    private final ObjectMapper om = new ObjectMapper();

    /** 给某个用户的全部在线连接推一条 JSON 消息（不在线则静默忽略） */
    public void notifyUser(String username, Map<String, Object> payload) {
        if (username == null || username.isBlank()) return;
        Set<WebSocketSession> set = sessions.get(username);
        if (set == null || set.isEmpty()) return;
        String json;
        try {
            json = om.writeValueAsString(payload);
        } catch (Exception e) {
            return;
        }
        for (WebSocketSession s : set) {
            try {
                if (s.isOpen()) {
                    synchronized (s) {
                        s.sendMessage(new TextMessage(json));
                    }
                }
            } catch (Exception e) {
                log.debug("[WS] 推送失败 user={}：{}", username, e.getMessage());
            }
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String user = (String) session.getAttributes().get("user");
        if (user == null || user.isBlank()) {
            session.close(CloseStatus.NOT_ACCEPTABLE);
            return;
        }
        sessions.computeIfAbsent(user, k -> ConcurrentHashMap.newKeySet()).add(session);
        log.info("[WS] 上线 {}（当前在线用户数 {}）", user, sessions.size());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        // 客户端心跳：{"type":"ping"} -> {"type":"pong"}；其余消息忽略（业务全走 HTTP）
        String body = message.getPayload();
        if (body != null && body.contains("ping")) {
            try {
                synchronized (session) {
                    session.sendMessage(new TextMessage("{\"type\":\"pong\"}"));
                }
            } catch (Exception ignored) {
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String user = (String) session.getAttributes().get("user");
        if (user == null) return;
        Set<WebSocketSession> set = sessions.get(user);
        if (set != null) {
            set.remove(session);
            if (set.isEmpty()) sessions.remove(user);
        }
        log.info("[WS] 下线 {}（剩余在线用户数 {}）", user, sessions.size());
    }
}
