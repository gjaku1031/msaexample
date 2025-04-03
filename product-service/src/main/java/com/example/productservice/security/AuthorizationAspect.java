package com.example.productservice.security;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

/**
 * 메소드 레벨에서 세밀한 권한 검사를 수행하는 AOP 구성요소
 * 
 * 컨트롤러 메소드에 @RequirePermission 어노테이션을 적용하여 필요한 권한 지정 가능
 */
@Aspect
@Component
public class AuthorizationAspect {

    @Value("${jwt.secret}")
    private String jwtSecret;

    /**
     * @RequirePermission 어노테이션이 있는 메소드 실행 전에 호출되어 권한 검사 수행
     */
    @Around("@annotation(com.example.productservice.security.RequirePermission)")
    public Object checkPermission(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        RequirePermission requirePermission = method.getAnnotation(RequirePermission.class);
        String[] requiredPermissions = requirePermission.value();

        // Spring Security 컨텍스트에서 Authentication 객체 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증되지 않은 요청입니다.");
        }

        // 사용자의 권한 목록 가져오기
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        List<String> userPermissions = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        // 요구되는 권한 중 하나라도 가지고 있는지 확인
        boolean hasPermission = Arrays.stream(requiredPermissions)
                .anyMatch(userPermissions::contains);

        if (!hasPermission) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "이 작업을 수행할 권한이 없습니다.");
        }

        // 권한 체크 통과, 원래 메서드 실행
        return joinPoint.proceed();
    }
}