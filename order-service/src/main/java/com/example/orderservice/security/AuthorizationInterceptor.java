package com.example.orderservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.HandlerInterceptor;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Secured 어노테이션을 처리하는 인터셉터 구현
 * 
 * 이 클래스는 MSA 환경에서 마이크로서비스별 세분화된 권한 체크 메커니즘을 제공합니다.
 * Spring Security의 기본 인증 메커니즘과 함께 작동하여 메서드 수준의 권한 검사를 수행합니다.
 */
@Component
public class AuthorizationInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true; // 메서드 핸들러가 아닌 경우 통과
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        Method method = handlerMethod.getMethod();

        // Secured 어노테이션이 없는 경우 통과
        if (!method.isAnnotationPresent(Secured.class)) {
            return true;
        }

        // Spring Security 컨텍스트에서 Authentication 객체 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.getWriter().write("인증되지 않은 요청입니다.");
            return false;
        }

        // 필요한 권한 가져오기
        Secured secured = method.getAnnotation(Secured.class);
        String[] requiredPermissions = secured.value();

        // 사용자의 권한 목록 가져오기
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        List<String> userPermissions = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        // 필요한 권한 중 하나라도 있는지 확인
        boolean hasPermission = Arrays.stream(requiredPermissions)
                .anyMatch(userPermissions::contains);

        if (!hasPermission) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.getWriter().write("이 작업을 수행할 권한이 없습니다.");
            return false;
        }

        return true;
    }
}