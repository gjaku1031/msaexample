package com.example.authserver.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "oauth2_clients")
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String clientId;

    private String clientSecret;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "client_redirect_uris", joinColumns = @JoinColumn(name = "client_id"))
    @Column(name = "redirect_uri")
    private Set<String> redirectUris = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "client_grant_types", joinColumns = @JoinColumn(name = "client_id"))
    @Column(name = "grant_type")
    private Set<String> authorizedGrantTypes = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "client_scopes", joinColumns = @JoinColumn(name = "client_id"))
    @Column(name = "scope")
    private Set<String> scopes = new HashSet<>();

    private Integer accessTokenValiditySeconds;
    private Integer refreshTokenValiditySeconds;

    private boolean autoApprove = false;

    /**
     * 빌더 패턴을 이용한 클라이언트 생성자
     * 필수 필드를 검증하고 초기화합니다.
     */
    @Builder
    public Client(String clientId, String clientSecret, Set<String> redirectUris,
            Set<String> authorizedGrantTypes, Set<String> scopes,
            Integer accessTokenValiditySeconds, Integer refreshTokenValiditySeconds,
            Boolean autoApprove) {
        // 필수 필드 검증
        if (clientId == null || clientId.isBlank()) {
            throw new IllegalArgumentException("클라이언트 ID는 필수입니다");
        }
        if (clientSecret == null || clientSecret.isBlank()) {
            throw new IllegalArgumentException("클라이언트 시크릿은 필수입니다");
        }

        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUris = redirectUris != null ? redirectUris : new HashSet<>();
        this.authorizedGrantTypes = authorizedGrantTypes != null ? authorizedGrantTypes : new HashSet<>();
        this.scopes = scopes != null ? scopes : new HashSet<>();
        this.accessTokenValiditySeconds = accessTokenValiditySeconds;
        this.refreshTokenValiditySeconds = refreshTokenValiditySeconds;
        this.autoApprove = autoApprove != null ? autoApprove : false;
    }

    /**
     * 리다이렉트 URI 추가 메서드
     */
    public void addRedirectUri(String redirectUri) {
        if (redirectUri != null && !redirectUri.isBlank()) {
            this.redirectUris.add(redirectUri);
        }
    }

    /**
     * 권한 부여 타입 추가 메서드
     */
    public void addAuthorizedGrantType(String grantType) {
        if (grantType != null && !grantType.isBlank()) {
            this.authorizedGrantTypes.add(grantType);
        }
    }

    /**
     * 스코프 추가 메서드
     */
    public void addScope(String scope) {
        if (scope != null && !scope.isBlank()) {
            this.scopes.add(scope);
        }
    }

    /**
     * 액세스 토큰 유효 시간 설정 메서드
     */
    public void setAccessTokenValiditySeconds(Integer accessTokenValiditySeconds) {
        this.accessTokenValiditySeconds = accessTokenValiditySeconds;
    }

    /**
     * 리프레시 토큰 유효 시간 설정 메서드
     */
    public void setRefreshTokenValiditySeconds(Integer refreshTokenValiditySeconds) {
        this.refreshTokenValiditySeconds = refreshTokenValiditySeconds;
    }

    /**
     * 자동 승인 여부 설정 메서드
     */
    public void setAutoApprove(boolean autoApprove) {
        this.autoApprove = autoApprove;
    }
}