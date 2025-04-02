package com.example.apigateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * API 게이트웨이에서 모든 요청에 대한 JWT 인증을 처리하는 글로벌 필터
 * 
 * 토큰의 존재 여부와 기본적인 유효성을 검증합니다.
 * 자세한 권한 검사는 각 마이크로서비스에서 수행됩니다.
 */
@Component
@Slf4j
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    // 인증이 필요 없는 공개 경로 목록
    private final List<String> openEndpoints = List.of(
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/refreshtoken",
            "/oauth2/token",
            "/actuator",
            "/h2-console");

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // 공개 API는 토큰 검증 없이 통과
        if (isOpenEndpoint(path)) {
            return chain.filter(exchange);
        }

        // Authorization 헤더에서 토큰 확인
        HttpHeaders headers = request.getHeaders();
        if (!headers.containsKey(HttpHeaders.AUTHORIZATION)) {
            return onError(exchange, "Authorization 헤더가 존재하지 않습니다", HttpStatus.UNAUTHORIZED);
        }

        String authorizationHeader = headers.getFirst(HttpHeaders.AUTHORIZATION);
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return onError(exchange, "잘못된 토큰 형식입니다", HttpStatus.UNAUTHORIZED);
        }

        // 토큰 추출 및 기본 검증 (형식만 체크, 상세 검증은 각 서비스에서 수행)
        String token = authorizationHeader.substring(7);
        if (token.isEmpty()) {
            return onError(exchange, "토큰이 비어있습니다", HttpStatus.UNAUTHORIZED);
        }

        // JWT 토큰 기본 구조 검증 (헤더.페이로드.서명)
        String[] tokenParts = token.split("\\.");
        if (tokenParts.length != 3) {
            return onError(exchange, "유효하지 않은 JWT 토큰 형식입니다", HttpStatus.UNAUTHORIZED);
        }

        log.debug("JWT 토큰 기본 검증 완료: {}", path);

        // 인증 정보와 함께 요청 전달
        return chain.filter(exchange);
    }

    /**
     * 인증 오류 발생 시 클라이언트에 에러 응답 반환
     */
    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status) {
        log.error("인증 오류: {}", message);
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        return response.setComplete();
    }

    /**
     * 주어진 경로가 공개 엔드포인트인지 확인
     */
    private boolean isOpenEndpoint(String path) {
        return openEndpoints.stream().anyMatch(path::startsWith);
    }

    @Override
    public int getOrder() {
        // 필터 체인에서의 실행 순서 (낮을수록 먼저 실행)
        return -1;
    }
}