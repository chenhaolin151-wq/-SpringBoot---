package com.work.attendance.config;

import com.work.attendance.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class JwtInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 1. 从请求头获取 token
        String token = request.getHeader("token");
        if (token == null || token.isEmpty()) {
            throw new RuntimeException("无Token，请重新登录");
        }
        try {
            // 2. 校验 Token
            JwtUtils.parseToken(token);
            return true;
        } catch (Exception e) {
            throw new RuntimeException("Token无效，请重新登录");
        }
    }
}