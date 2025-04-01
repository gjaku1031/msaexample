package com.example.apigateway.config;

import java.util.Arrays;
import java.util.List;
import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.ServerAuthenticationEntryPointFailureHandler;
import org.springframework.security.web.server.authentication.ServerAuthenticationFailureHandler;
import org.springframework.security.web.server.header.ReferrerPolicyServerHttpHeadersWriter;
import org.springframework.security.web.server.header.XFrameOptionsServerHttpHeadersWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import reactor.core.publisher.Flux;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

        @Bean
        public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
                // 인증 실패 처리기
                ServerAuthenticationFailureHandler authenticationFailureHandler = new ServerAuthenticationEntryPointFailureHandler(
                                (exchange, exception) -> exchange.getResponse().setComplete());

                http
                                // CSRF 보호 비활성화 (RESTful API에서는 일반적)
                                .csrf(ServerHttpSecurity.CsrfSpec::disable)

                                // CORS 설정 활성화
                                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                                // 요청 권한 설정
                                .authorizeExchange(exchanges -> exchanges
                                                .pathMatchers("/api/auth/**", "/actuator/**", "/oauth2/**", "/login/**")
                                                .permitAll()
                                                .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                                                .anyExchange().authenticated())

                                // OAuth2 로그인 설정
                                .oauth2Login(oauth2Login -> oauth2Login
                                                .authenticationFailureHandler(authenticationFailureHandler))

                                // OAuth2 리소스 서버 JWT 설정
                                .oauth2ResourceServer(oauth2 -> oauth2
                                                .jwt(jwt -> jwt.jwtAuthenticationConverter(
                                                                jwtAuthenticationConverter())))

                                // 보안 응답 헤더 설정
                                .headers(headers -> headers
                                                .frameOptions(frameOptions -> frameOptions
                                                                .mode(XFrameOptionsServerHttpHeadersWriter.Mode.SAMEORIGIN))
                                                .contentSecurityPolicy(csp -> csp
                                                                .policyDirectives(
                                                                                "default-src 'self'; frame-ancestors 'self'; form-action 'self'; script-src 'self' 'unsafe-inline' 'unsafe-eval'; style-src 'self' 'unsafe-inline';"))
                                                .referrerPolicy(referrerPolicy -> referrerPolicy
                                                                .policy(ReferrerPolicyServerHttpHeadersWriter.ReferrerPolicy.SAME_ORIGIN))
                                                .hsts(hsts -> hsts
                                                                .includeSubdomains(true)
                                                                .maxAge(Duration.ofDays(365))
                                                                .preload(true)));

                return http.build();
        }

        /**
         * JWT 토큰에서 권한 정보를 추출하기 위한 커스텀 컨버터
         */
        @Bean
        public ReactiveJwtAuthenticationConverter jwtAuthenticationConverter() {
                JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
                grantedAuthoritiesConverter.setAuthoritiesClaimName("authorities");
                grantedAuthoritiesConverter.setAuthorityPrefix("");

                ReactiveJwtAuthenticationConverter jwtAuthenticationConverter = new ReactiveJwtAuthenticationConverter();
                jwtAuthenticationConverter
                                .setJwtGrantedAuthoritiesConverter(
                                                jwt -> Flux.fromIterable(grantedAuthoritiesConverter.convert(jwt)));

                return jwtAuthenticationConverter;
        }

        /**
         * CORS 설정
         * 운영 환경에서는 더 제한적인 설정 사용 권장
         */
        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration configuration = new CorsConfiguration();
                configuration.setAllowedOrigins(List.of("http://localhost:8080", "https://yourdomain.com"));
                configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
                configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));
                configuration.setExposedHeaders(List.of("X-Auth-Token"));
                configuration.setAllowCredentials(true);
                configuration.setMaxAge(3600L);

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration);
                return source;
        }
}