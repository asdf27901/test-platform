package com.lmj.platformserver.interceptor;

import com.lmj.platformserver.context.UserContextHolder;
import com.lmj.platformserver.exception.TokenValidFailException;
import com.lmj.platformserver.result.ResultCodeEnum;
import com.lmj.platformserver.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthenticationInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("token");
        if (token == null) {
            throw new TokenValidFailException(ResultCodeEnum.TOKEN_INVALID);
        }
        Claims claims = JwtUtil.parseToken(token);
        Long userId = claims.get("userId", Long.class);
        // 将userId存入到ThreadLocal中
        UserContextHolder.setUserId(userId);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 清除ThreadLocal，避免造成OOM
        UserContextHolder.clear();
    }
}
