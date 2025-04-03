package com.example.customerservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JWT 토큰에서 권한 정보를 추출하고 Spring Security 컨텍스트에 저장하는 필터
 * 
 * 모든 HTTP 요청에 대해 JWT 검증을 수행하고 SecurityContext에 인증 정보를 설정합니다.
 */
@Component
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private final String jwtSecret = "jwt_secret_key_for_customer_service_from_config";

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        // 공개 경로는 토큰 검증 없이 통과
        String path = request.getRequestURI();
        if (isPublicPath(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Authorization 헤더 확인
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "인증 정보가 없거나 잘못되었습니다");
            return;
        }

        try {
            // 토큰에서 JWT 추출
            String token = authHeader.substring(7);

            // JWT 검증 및 클레임 추출
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(jwtSecret.getBytes()))
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            // 사용자 이름과 권한 추출
            String username = claims.getSubject();
            List<String> roles = claims.get("roles", List.class);

            if (username != null && roles != null) {
                // Spring Security 인증 객체 생성
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        username,
                        null,
                        roles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList()));

                // SecurityContext에 인증 정보 설정
                SecurityContextHolder.getContext().setAuthentication(auth);
            }

            filterChain.doFilter(request, response);
        } catch (Exception e) {
            sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "유효하지 않은 토큰: " + e.getMessage());
        }
    }

    /**
     * 인증이 필요 없는 공개 경로인지 확인
     */
    private boolean isPublicPath(String path) {
        return path.startsWith("/api/customers/public/") ||
                path.contains("/h2-console") ||
                path.contains("/actuator");
    }

    /**
     * 오류 응답 전송
     */
    private void sendError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\":\"" + message + "\"}");
        response.getWriter().flush();
    }
}