package com.lmj.platformserver.interceptor;

import com.lmj.platformserver.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.auth.BasicUserPrincipal;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.security.Principal;

@Slf4j
@Component
public class AuthChannelInterceptor implements ChannelInterceptor {
    
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {

        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String token = accessor.getFirstNativeHeader("token");

            if (!StringUtils.hasText(token)) {
                log.error("WebSocket connection rejected: Token is missing.");
                return message;
            }

            try {
                Claims claims = JwtUtil.parseToken(token);
                Long userId = claims.get("userId", Long.class);
                if (userId != null) {
                    Principal userPrincipal = new BasicUserPrincipal(userId.toString());
                    accessor.setUser(userPrincipal);
                }
                log.info("WebSocket authenticated for user: " + userId);
            } catch (Exception e) {
                log.error("WebSocket connection rejected: Token error - ", e.getCause());
            }
        }
        return message;
    }
}
