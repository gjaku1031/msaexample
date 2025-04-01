package com.example.orderservice.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.oauth2.server.resource.web.access.BearerTokenAccessDeniedHandler;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http, HandlerMappingIntrospector introspector)
                        throws Exception {
                MvcRequestMatcher.Builder mvcMatcherBuilder = new MvcRequestMatcher.Builder(introspector);

                http
                                // CSRF 보호 비활성화 (RESTful API에서는 일반적)
                                .csrf(csrf -> csrf.disable())

                                // CORS 설정 활성화
                                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                                // 요청 권한 설정
                                .authorizeHttpRequests(authorize -> authorize
                                                .requestMatchers(mvcMatcherBuilder.pattern("/actuator/**"),
                                                                mvcMatcherBuilder.pattern("/h2-console/**"))
                                                .permitAll()
                                                .requestMatchers(mvcMatcherBuilder.pattern(HttpMethod.GET,
                                                                "/api/orders/**"))
                                                .hasAnyAuthority("SCOPE_read", "SCOPE_order:read")
                                                .requestMatchers(mvcMatcherBuilder.pattern(HttpMethod.POST,
                                                                "/api/orders/**"))
                                                .hasAnyAuthority("SCOPE_write", "SCOPE_order:write")
                                                .requestMatchers(mvcMatcherBuilder.pattern(HttpMethod.PATCH,
                                                                "/api/orders/**"))
                                                .hasAnyAuthority("SCOPE_write", "SCOPE_order:write")
                                                .anyRequest().authenticated())

                                // 세션 관리 - STATELESS로 설정
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                                // OAuth2 Resource Server JWT 설정
                                .oauth2ResourceServer(oauth2 -> oauth2
                                                .jwt(jwt -> jwt.jwtAuthenticationConverter(
                                                                jwtAuthenticationConverter())))

                                // 예외 처리 향상
                                .exceptionHandling(exceptions -> exceptions
                                                .authenticationEntryPoint(new BearerTokenAuthenticationEntryPoint())
                                                .accessDeniedHandler(new BearerTokenAccessDeniedHandler()))

                                // 보안 헤더 설정
                                .headers(headers -> headers
                                                .contentSecurityPolicy(csp -> csp
                                                                .policyDirectives(
                                                                                "default-src 'self'; frame-ancestors 'self'; form-action 'self'; script-src 'self' 'unsafe-inline' 'unsafe-eval'; style-src 'self' 'unsafe-inline';"))
                                                .xssProtection(Customizer.withDefaults())
                                                .contentTypeOptions(Customizer.withDefaults())
                                                .referrerPolicy(referrer -> referrer
                                                                .policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.SAME_ORIGIN))
                                                .frameOptions(frame -> frame.sameOrigin())
                                                .crossOriginEmbedderPolicy(Customizer.withDefaults())
                                                .crossOriginOpenerPolicy(Customizer.withDefaults())
                                                .crossOriginResourcePolicy(Customizer.withDefaults()));

                return http.build();
        }

        /**
         * JWT 토큰에서 권한 정보를 추출하기 위한 커스텀 컨버터
         * 'roles' claim을 사용하고 'ROLE_' 접두사를 추가
         */
        @Bean
        public JwtAuthenticationConverter jwtAuthenticationConverter() {
                JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
                grantedAuthoritiesConverter.setAuthoritiesClaimName("authorities");
                grantedAuthoritiesConverter.setAuthorityPrefix(""); // 접두어 없음 (이미 SCOPE_ 또는 ROLE_이 있는 경우)

                JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
                jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);

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