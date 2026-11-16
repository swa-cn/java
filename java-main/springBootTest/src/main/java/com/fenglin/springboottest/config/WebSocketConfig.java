package com.fenglin.springboottest.config;

import com.fenglin.springboottest.auth.CurrentUser;
import com.fenglin.springboottest.websocket.ChatWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.HandshakeInterceptor;

import jakarta.servlet.http.HttpSession;
import java.util.Map;

/**
 * WebSocket 配置：注册 /ws/chat 端点。
 *
 * 鉴权：握手阶段检查 HttpSession 里的登录用户（CurrentUser.SESSION_USER），
 * 未登录返回 401 拒绝握手；已登录则把用户名写入 WS 会话属性，供处理器路由推送。
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final ChatWebSocketHandler chatWebSocketHandler;

    public WebSocketConfig(ChatWebSocketHandler chatWebSocketHandler) {
        this.chatWebSocketHandler = chatWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(chatWebSocketHandler, "/ws/chat")
                .addInterceptors(new HandshakeInterceptor() {
                    @Override
                    public boolean beforeHandshake(
                            org.springframework.http.server.ServerHttpRequest request,
                            org.springframework.http.server.ServerHttpResponse response,
                            org.springframework.web.socket.WebSocketHandler wsHandler,
                            Map<String, Object> attributes) {
                        HttpSession hs = (request instanceof ServletServerHttpRequest sr)
                                ? sr.getServletRequest().getSession(false) : null;
                        Object user = hs == null ? null : hs.getAttribute(CurrentUser.SESSION_USER);
                        if (user == null || String.valueOf(user).isBlank()) {
                            // 未登录：拒绝握手
                            if (response instanceof ServletServerHttpResponse sr2) {
                                sr2.getServletResponse().setStatus(HttpStatus.UNAUTHORIZED.value());
                            }
                            return false;
                        }
                        attributes.put("user", String.valueOf(user));
                        return true;
                    }

                    @Override
                    public void afterHandshake(
                            org.springframework.http.server.ServerHttpRequest request,
                            org.springframework.http.server.ServerHttpResponse response,
                            org.springframework.web.socket.WebSocketHandler wsHandler,
                            Exception exception) {
                        // 无需处理
                    }
                })
                .setAllowedOriginPatterns("*");
    }
}
