package com.example.authserver.config;

import com.example.authserver.security.AuthEntryPointJwt;
import com.example.authserver.security.AuthTokenFilter;
import com.example.authserver.security.JwtUtils;
import com.example.authserver.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy;

import java.util.Arrays;
import java.util.List;

/**
 * Spring Security 기본 설정 클래스
 * 이 클래스는 인증 서버의 전반적인 보안 설정을 담당합니다.
 * 
 * <p>
 * MSA 아키텍처에서 보안 설정의 중요성:
 * 
 * - 각 마이크로서비스는 독립적인 보안 정책을 가지지만, 인증 서버는 중앙 집중식 인증을 제공
 * - 인증 서버와 다른 마이크로서비스 간의 통신을 위한 토큰 기반 인증 구현
 * - 서비스 간 안전한 통신을 위한 CORS 및 CSRF 정책 설정
 * 
 * 
 * @Configuration: Spring의 설정 클래스임
 * @EnableWebSecurity: Spring Security를 활성화
 * @EnableMethodSecurity: 메서드 레벨 보안을 활성화합니다 (예: @PreAuthorize)
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

        /**
         * 사용자 정보를 로드하고 인증을 처리하는 서비스
         * UserDetailsService 인터페이스를 구현한 서비스입니다.
         */
        private final UserService userService;

        /**
         * 인증되지 않은 요청을 처리하는 핸들러
         * 인증 실패 시 적절한 HTTP 응답을 반환합니다.
         */
        private final AuthEntryPointJwt unauthorizedHandler;

        /**
         * JWT 토큰 생성, 검증 및 처리를 담당하는 유틸리티 클래스
         */
        private final JwtUtils jwtUtils;

        /**
         * JWT 토큰 기반 인증을 위한 필터를 구성합니다.
         * 이 필터는 요청 헤더에서 JWT 토큰을 추출하고 검증합니다.
         * 
         * @return 구성된 JWT 인증 필터
         */
        @Bean
        public AuthTokenFilter authTokenFilter() {
                return new AuthTokenFilter(jwtUtils, userService);
        }

        /**
         * 사용자 인증을 처리하는 인증 제공자를 구성합니다.
         * DaoAuthenticationProvider는 UserDetailsService를 통해 사용자 정보를 로드하고
         * PasswordEncoder를 사용하여 비밀번호를 검증합니다.
         * 
         * @return 구성된 인증 제공자
         */
        @Bean
        public DaoAuthenticationProvider authenticationProvider() {
                DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
                authProvider.setUserDetailsService(userService);
                authProvider.setPasswordEncoder(passwordEncoder());
                return authProvider;
        }

        /**
         * 인증 관리자를 제공합니다.
         * 이는 인증 요청을 처리하는 중앙 컴포넌트입니다.
         * 
         * @param authConfig 인증 구성 객체
         * @return 인증 관리자
         * @throws Exception 인증 관리자 생성 중 발생할 수 있는 예외
         */
        @Bean
        public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
                return authConfig.getAuthenticationManager();
        }

        /**
         * 기본 보안 필터 체인 설정
         * 일반적인 웹 보안 설정을 담당하며, OAuth2 엔드포인트 이외의 요청을 처리합니다.
         * 
         * <p>
         * MSA에서의 역할:
         * - 인증 서버의 API 엔드포인트에 대한 접근 제어
         * - WT 토큰 기반의 상태 없는(stateless) 인증 구현
         * - 다른 마이크로서비스와의 통신을 위한 CORS 구성
         * 
         * @param http HttpSecurity 객체
         * @return SecurityFilterChain 인스턴스
         * @throws Exception 보안 설정 중 발생할 수 있는 예외
         */
        @Bean
        @Order(2)
        public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
                http
                                // CSRF 보호 비활성화 (RESTful API에서는 일반적)
                                // MSA에서는 서비스 간 통신이 많으므로 CSRF 토큰 관리가 복잡해질 수 있음
                                .csrf(AbstractHttpConfigurer::disable)

                                // CORS 설정 활성화
                                // 다른 도메인이나 포트에서 실행 중인 마이크로서비스 간의 통신을 허용
                                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                                // 세션 관리 설정
                                // MSA에서는 상태 없는(stateless) 서비스가 선호됨
                                // 각 요청은 자체적으로 인증 정보(JWT)를 포함
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                                // 인증 예외 처리
                                // 인증 오류 발생 시 클라이언트에게 적절한 응답 제공
                                .exceptionHandling(exception -> exception
                                                .authenticationEntryPoint(unauthorizedHandler))

                                // 요청 권한 설정
                                // 특정 엔드포인트에 대한 접근 권한 정의
                                .authorizeHttpRequests(authorize -> authorize
                                                // 공개 엔드포인트 설정 - 인증 없이 접근 가능
                                                .requestMatchers(
                                                                new AntPathRequestMatcher("/api/auth/register"),
                                                                new AntPathRequestMatcher("/api/auth/login"),
                                                                new AntPathRequestMatcher("/actuator/**"),
                                                                new AntPathRequestMatcher(
                                                                                "/.well-known/openid-configuration"),
                                                                new AntPathRequestMatcher("/oauth2/jwks"))
                                                .permitAll()
                                                // 나머지 요청은 인증 필요
                                                .anyRequest().authenticated())

                                // 폼 로그인 비활성화 (REST API이므로)
                                // MSA에서는 일반적으로 폼 로그인 대신 토큰 기반 인증 사용
                                .formLogin(AbstractHttpConfigurer::disable)

                                // JWT 필터 추가
                                // 요청 헤더에서 JWT 토큰을 추출하고 검증하는 필터
                                // 이 필터는 UsernamePasswordAuthenticationFilter 전에 실행됨
                                .addFilterBefore(authTokenFilter(), UsernamePasswordAuthenticationFilter.class)

                                // 보안 헤더 설정
                                // 브라우저에게 특정 보안 정책을 따르도록 지시하는 HTTP 헤더 설정
                                .headers(headers -> headers
                                                .contentSecurityPolicy(csp -> csp
                                                                .policyDirectives(
                                                                                "default-src 'self'; frame-ancestors 'self'; form-action 'self'"))
                                                .frameOptions(frame -> frame.sameOrigin())
                                                .referrerPolicy(referrer -> referrer
                                                                .policy(ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)));

                return http.build();
        }


        @Bean
        public PasswordEncoder passwordEncoder() {
                // 작업 계수 12는 보안과 성능 사이의 균형점
                // 값이 클수록 더 안전하지만 CPU 부하가 증가
                return new BCryptPasswordEncoder(12);
        }

        /**
         * CORS 설정
         * - 각 마이크로서비스는 서로 다른 도메인/포트에서 실행될 수 있음
         * - 브라우저의 동일 출처 정책(Same-Origin Policy)으로 인해 CORS 설정 필요
         * - 프론트엔드와 백엔드 서비스 간의 통신 활성화
         */
        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration configuration = new CorsConfiguration();

                // 허용할 오리진 설정
                // MSA에서는 API 게이트웨이와 프론트엔드 애플리케이션의 오리진을 허용
                configuration.setAllowedOrigins(List.of(
                                "http://localhost:8080", // API Gateway
                                "http://localhost:3000" // 프론트엔드
                ));

                // 허용할 HTTP 메서드 설정
                // RESTful API를 위한 모든 표준 HTTP 메서드 허용
                configuration.setAllowedMethods(Arrays.asList(
                                "GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD"));

                // 허용할 헤더 설정
                // 클라이언트에서 보낼 수 있는 HTTP 헤더 지정
                configuration.setAllowedHeaders(Arrays.asList(
                                "Authorization", // JWT 토큰 전송용
                                "Content-Type",
                                "X-Requested-With",
                                "Accept",
                                "Origin",
                                "Access-Control-Request-Method",
                                "Access-Control-Request-Headers"));

                // 노출할 헤더 설정
                // 브라우저에게 응답에서 접근 가능한 헤더 지정
                configuration.setExposedHeaders(List.of(
                                "Access-Control-Allow-Origin",
                                "Access-Control-Allow-Credentials",
                                "Authorization",
                                "X-Auth-Token"));

                // 자격 증명 허용 (쿠키 등)
                // 요청에 인증 정보(쿠키, HTTP 인증, 클라이언트 측 SSL 인증서)를 포함할 수 있도록 함
                configuration.setAllowCredentials(true);

                // preflight 요청 캐시 시간 (1시간)
                // 브라우저가 preflight 요청 결과를 캐시하는 시간(초)
                configuration.setMaxAge(3600L);

                // 모든 경로에 대해 CORS 설정 적용
                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration);

                return source;
        }
}