package com.lmj.platformserver.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;

@Component
@Slf4j
public class WebSocketEventListener {

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        // 从事件中获取 StompHeaderAccessor，它包含了所有与连接相关的信息
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

        // 从 StompHeaderAccessor 中获取我们之前在 AuthChannelInterceptor 中设置的 Principal
        Principal userPrincipal = headerAccessor.getUser();

        if (userPrincipal != null) {
            String userId = userPrincipal.getName();
            log.info("WebSocket 连接已建立: UserID = {}, SessionID = {}", userId, headerAccessor.getSessionId());
        } else {
            // 如果 userPrincipal 为 null，可能是因为认证失败或者是非 STOMP 的连接
            // 在我们的架构中，因为有 AuthChannelInterceptor，理论上不应该出现这种情况
            log.warn("一个 WebSocket 连接已建立，但无法获取用户信息. SessionID = {}", headerAccessor.getSessionId());
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        // 同样，从事件中获取 StompHeaderAccessor
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

        // 获取用户信息
        Principal userPrincipal = headerAccessor.getUser();

        if (userPrincipal != null) {
            String userId = userPrincipal.getName();
            log.info("WebSocket 连接已断开: UserID = {}, SessionID = {}", userId, headerAccessor.getSessionId());
        } else {
            // 这种情况可能发生在一个连接在认证成功之前就断开了
            log.warn("一个 WebSocket 连接已断开，但无法获取用户信息. SessionID = {}", headerAccessor.getSessionId());
        }
        // 打印断开状态码和原因
        log.info("断开详情: CloseStatus = {}, Message = {}", event.getCloseStatus(), event.getMessage());
    }
}
