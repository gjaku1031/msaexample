package com.example.authserver.config;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.web.SecurityFilterChain;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * OAuth2 인증 서버 설정 클래스
 * Spring Authorization Server의 핵심 설정을 담당
 * 
 * MSA 환경에서의 OAuth2 서버의 역할:
 * - 중앙 집중식 인증/인가 제공: 모든 마이크로서비스가 동일한 인증 서버를 통해 보안을 처리
 * - 서비스 간 통신 보안: 서비스-대-서비스(Service-to-Service) 통신을 위한 클라이언트 자격 증명 흐름 지원
 * - 사용자 인증: 최종 사용자 로그인 및 권한 부여를 위한 권한 코드 흐름 지원
 * - 토큰 발급 및 검증: JWT 토큰을 사용한 상태 없는(stateless) 인증 메커니즘 제공
 * 
 * 주요 컴포넌트:
 * - OAuth2 보안 필터 체인: OAuth2 프로토콜 엔드포인트 보호
 * - 클라이언트 등록: 각 마이크로서비스와 API 게이트웨이를 위한 OAuth2 클라이언트 설정
 * - JWT 토큰 설정: 토큰 서명, 검증, 만료 시간 등 설정
 * - 인증 서버 설정: 서버 발급자(Issuer) URL 등 설정
 */
@Configuration
@RequiredArgsConstructor
public class AuthorizationServerConfig {

        /**
         * OAuth2 인증 서버의 보안 필터 체인 설정
         * OAuth2 및 OpenID Connect 프로토콜 엔드포인트에 대한 보안을 구성
         * 
         * MSA에서의 의미:

         * - 이 필터 체인은 OAuth2 관련 엔드포인트(/oauth2/authorize, /oauth2/token 등)에 적용
         * - 일반 API 엔드포인트보다 높은 우선순위(HIGHEST_PRECEDENCE)를 가져 먼저 처리
         * - 다른 마이크로서비스가 이 인증 서버와 통신할 때 이 필터 체인을 통과
         */
        @Bean
        @Order(Ordered.HIGHEST_PRECEDENCE)
        public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
                OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);
                http.getConfigurer(OAuth2AuthorizationServerConfigurer.class)
                                .oidc(Customizer.withDefaults()); // OpenID Connect 1.0 구성 활성화
                return http.build();
        }

        /**
         * 클라이언트 등록 저장소 설정
         * OAuth2 클라이언트 애플리케이션의 등록 정보를 관리
         * 
         * MSA에서의 의미:
         * - 각 마이크로서비스는 인증 서버에 등록된 클라이언트로 인식
         * - API 게이트웨이는 사용자 인증을 위한 클라이언트로 설정
         * - 각 서비스별로 고유한 클라이언트 ID, 시크릿, 스코프(권한 범위)가 부여
         * - 서비스 간 통신(Service-to-Service)은 주로 Client Credentials 그랜트 타입을 사용
         */
        @Bean
        public RegisteredClientRepository registeredClientRepository() {
                // gateway 클라이언트
                // API 게이트웨이를 위한 클라이언트 설정
                // 이 클라이언트는 사용자 인증을 위한 Authorization Code 흐름과
                // 서비스 간 통신을 위한 Client Credentials 흐름을 모두 지원합니다.
                RegisteredClient gatewayClient = RegisteredClient.withId(UUID.randomUUID().toString())
                                .clientId("gateway-client")
                                .clientSecret("{noop}gateway-secret")
                                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                                .redirectUri("http://localhost:8080/login/oauth2/code/gateway")
                                .scope(OidcScopes.OPENID)
                                .scope(OidcScopes.PROFILE)
                                .scope("read")
                                .scope("write")
                                .tokenSettings(TokenSettings.builder()
                                                .accessTokenTimeToLive(Duration.ofHours(1))
                                                .refreshTokenTimeToLive(Duration.ofDays(1))
                                                .build())
                                .clientSettings(ClientSettings.builder().requireAuthorizationConsent(true).build())
                                .build();

                // 주문 서비스 클라이언트
                // 주문 서비스를 위한 클라이언트 설정
                // 이 클라이언트는 다른 서비스와의 통신을 위한 Client Credentials 흐름만 지원합니다.
                RegisteredClient orderServiceClient = RegisteredClient.withId(UUID.randomUUID().toString())
                                .clientId("order-service")
                                .clientSecret("{noop}order-service-secret")
                                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                                .scope("order:read") // 주문 데이터 읽기 권한
                                .scope("order:write") // 주문 데이터 쓰기 권한
                                .tokenSettings(TokenSettings.builder()
                                                .accessTokenTimeToLive(Duration.ofHours(1))
                                                .build())
                                .build();

                // 상품 서비스 클라이언트
                // 상품 서비스를 위한 클라이언트 설정
                RegisteredClient productServiceClient = RegisteredClient.withId(UUID.randomUUID().toString())
                                .clientId("product-service")
                                .clientSecret("{noop}product-service-secret")
                                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                                .scope("product:read") // 상품 데이터 읽기 권한
                                .scope("product:write") // 상품 데이터 쓰기 권한
                                .tokenSettings(TokenSettings.builder()
                                                .accessTokenTimeToLive(Duration.ofHours(1))
                                                .build())
                                .build();

                // 고객 서비스 클라이언트
                // 고객 서비스를 위한 클라이언트 설정
                RegisteredClient customerServiceClient = RegisteredClient.withId(UUID.randomUUID().toString())
                                .clientId("customer-service")
                                .clientSecret("{noop}customer-service-secret")
                                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                                .scope("customer:read") // 고객 데이터 읽기 권한
                                .scope("customer:write") // 고객 데이터 쓰기 권한
                                .tokenSettings(TokenSettings.builder()
                                                .accessTokenTimeToLive(Duration.ofHours(1))
                                                .build())
                                .build();

                // 인메모리 저장소에 모든 클라이언트 등록
                return new InMemoryRegisteredClientRepository(
                                gatewayClient, orderServiceClient, productServiceClient, customerServiceClient);
        }

        /**
         * JWK 소스 설정
         * JWT 서명에 사용되는 키 쌍을 관리합니다.
    
         * MSA에서의 의미:
         * - 인증 서버가 발급하는 JWT 토큰은 이 키로 서명
         * - 모든 마이크로서비스는 이 공개 키를 사용하여 토큰의 유효성
         * - 중앙 집중식 키 관리를 통해 마이크로서비스 간 일관된 인증이 가능
         * - 실제 프로덕션 환경에서는 이 키를 안전하게 저장하고 관리
         */
        @Bean
        public JWKSource<SecurityContext> jwkSource() throws NoSuchAlgorithmException {
                KeyPair keyPair = generateRsaKey();
                RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
                RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
                RSAKey rsaKey = new RSAKey.Builder(publicKey)
                                .privateKey(privateKey)
                                .keyID(UUID.randomUUID().toString())
                                .build();
                JWKSet jwkSet = new JWKSet(rsaKey);
                return new ImmutableJWKSet<>(jwkSet);
        }

        /**
         * RSA 키 쌍 생성
         * JWT 서명에 사용될 2048비트 RSA 키 쌍을 생성
         * 
         * MSA에서의 참고사항
         * - 프로덕션 환경에서는 애플리케이션 재시작 시마다 새 키 쌍이 생성되지 않도록 해야 함
         * - 키 저장소(KeyStore)를 사용하여 영구적으로 키를 저장하는 것이 좋음
         * - 키 로테이션 정책을 구현하여 주기적으로 키를 갱신하는 것이 보안상 권장
         */
        private static KeyPair generateRsaKey() throws NoSuchAlgorithmException {
                KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
                keyPairGenerator.initialize(2048); // 2048비트 키 강도는 보안과 성능의 균형점입니다
                return keyPairGenerator.generateKeyPair();
        }

        /**
         * JWT 디코더 설정
         * 토큰 검증에 사용될 JWT 디코더를 구성합니다.
         * 
         * MSA에서의 의미:
         * - 인증 서버 자체도 JWT 토큰을 검증할 수 있어야 함
         * - 다른 마이크로서비스들도 이와 유사한 디코더를 사용하여 토큰을 검증
         * - 이 디코더는 OAuth2AuthorizationServerConfiguration에서 제공하는 기본 설정을 사용
         */
        @Bean
        public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
                return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
        }

        /**
         * 인증 서버 설정
         * 인증 서버의 기본 설정을 구성
         * 
         * MSA에서의 의미:
         * - issuer URL은 토큰의 발급자를 식별하는 중요한 정보
         * - 모든 마이크로서비스는 이 issuer URL을 신뢰하고 이로부터 발급된 토큰만 수락
         * - 프로덕션 환경에서는 이 URL이 안전한 HTTPS 프로토콜을 사용해야 함
         * - service registry(Eureka)와 함께 사용할 때는 이 URL이 일관되게 유지되어야 함
         */
        @Bean
        public AuthorizationServerSettings authorizationServerSettings() {
                return AuthorizationServerSettings.builder()
                                .issuer("http://localhost:9000") // 토큰 발급자 URL
                                .build();
        }

        /**
         * JWT 토큰 커스터마이저
         * 생성되는 JWT 토큰에 추가 정보를 삽입
         * 
         * MSA에서의 의미:
         * - 사용자 권한(authorities)을 토큰에 포함시켜 마이크로서비스가 권한 기반 결정
         * - 각 마이크로서비스는 토큰의 "roles" 클레임을 통해 사용자 권한을 확인
         * - 이를 통해 각 서비스는 중앙 집중식 인증 서버에 재검증 요청 없이 권한 검사를 수행
         * - 토큰에 필요한 정보를 포함시켜 서비스 간 통신 횟수를 줄이는 것은 MSA의 성능 최적화에 중요
         */
        @Bean
        public OAuth2TokenCustomizer<JwtEncodingContext> jwtCustomizer() {
                return context -> {
                        if (context.getTokenType() == OAuth2TokenType.ACCESS_TOKEN) {
                                Authentication principal = context.getPrincipal();
                                Set<String> authorities = principal.getAuthorities().stream()
                                                .map(GrantedAuthority::getAuthority)
                                                .collect(Collectors.toSet());
                                context.getClaims().claim("roles", authorities);
                        }
                };
        }
}