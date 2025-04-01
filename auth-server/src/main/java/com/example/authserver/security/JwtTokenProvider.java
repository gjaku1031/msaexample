package com.example.authserver.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.stream.Collectors;

/**
 * JWT 토큰 생성 및 검증을 담당하는 컴포넌트
 * JWT 토큰의 생성, 검증, 파싱 등 토큰 관련 기능을 제공합니다.
 */
@Slf4j
@Component
public class JwtTokenProvider {

    /**
     * JWT 서명에 사용할 비밀키
     */
    private final Key key;

    /**
     * 액세스 토큰 유효 기간 (초)
     */
    private final long accessTokenValidityInSeconds;

    /**
     * 리프레시 토큰 유효 기간 (초)
     */
    private final long refreshTokenValidityInSeconds;

    /**
     * JwtTokenProvider 생성자
     * 
     * @param secret                        JWT 서명에 사용할 비밀키 문자열
     * @param accessTokenValidityInSeconds  액세스 토큰 유효 기간 (초)
     * @param refreshTokenValidityInSeconds 리프레시 토큰 유효 기간 (초)
     */
    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-validity-in-seconds}") long accessTokenValidityInSeconds,
            @Value("${jwt.refresh-token-validity-in-seconds}") long refreshTokenValidityInSeconds) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.accessTokenValidityInSeconds = accessTokenValidityInSeconds;
        this.refreshTokenValidityInSeconds = refreshTokenValidityInSeconds;
    }

    /**
     * 액세스 토큰을 생성합니다.
     * 
     * @param authentication 인증 정보
     * @return String 생성된 JWT 액세스 토큰
     */
    public String createAccessToken(Authentication authentication) {
        return createToken(authentication, accessTokenValidityInSeconds);
    }

    /**
     * 리프레시 토큰을 생성합니다.
     * 
     * @param authentication 인증 정보
     * @return String 생성된 JWT 리프레시 토큰
     */
    public String createRefreshToken(Authentication authentication) {
        return createToken(authentication, refreshTokenValidityInSeconds);
    }

    /**
     * JWT 토큰을 생성합니다.
     * 
     * @param authentication    인증 정보
     * @param validityInSeconds 토큰 유효 기간 (초)
     * @return String 생성된 JWT 토큰
     */
    private String createToken(Authentication authentication, long validityInSeconds) {
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        long now = (new Date()).getTime();
        Date validity = new Date(now + validityInSeconds * 1000);

        return Jwts.builder()
                .setSubject(authentication.getName())
                .claim("auth", authorities)
                .setIssuedAt(new Date(now))
                .setExpiration(validity)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * 토큰에서 이메일을 추출합니다.
     * 
     * @param token JWT 토큰
     * @return String 토큰에서 추출한 이메일
     */
    public String getEmailFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    /**
     * 토큰의 유효성을 검증합니다.
     * 
     * @param token 검증할 JWT 토큰
     * @return boolean 토큰 유효성 여부
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            log.error("잘못된 JWT 서명입니다.");
        } catch (ExpiredJwtException e) {
            log.error("만료된 JWT 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            log.error("지원되지 않는 JWT 토큰입니다.");
        } catch (IllegalArgumentException e) {
            log.error("JWT 토큰이 잘못되었습니다.");
        }
        return false;
    }

    /**
     * 토큰을 무효화합니다.
     * 
     * @param token 무효화할 JWT 토큰
     */
    public void invalidateToken(String token) {
        // 실제 구현에서는 Redis나 데이터베이스를 사용하여 블랙리스트에 토큰을 추가
    }

    /**
     * 액세스 토큰 유효 기간을 반환합니다.
     * 
     * @return long 액세스 토큰 유효 기간 (초)
     */
    public long getAccessTokenValidityInSeconds() {
        return accessTokenValidityInSeconds;
    }
}