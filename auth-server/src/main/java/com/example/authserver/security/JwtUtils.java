package com.example.authserver.security;

import com.example.authserver.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;

import java.security.Key;
import java.util.Date;

/**
 * JWT 토큰 유틸리티 클래스
 * JWT 토큰 생성, 검증 및 처리와 관련된 기능을 제공합니다.
 * 
 * MSA에서의 JWT 토큰의 중요성:
 * - 서비스 간 인증: JWT 토큰은 마이크로서비스 간에 인증 정보를 안전하게 전달하는 수단입니다.
 * - 상태 없는(Stateless) 통신: 토큰 기반 인증은 서버 측 세션 상태를 유지할 필요가 없어 수평적 확장에 유리합니다.
 * - 분산 시스템 인증: 중앙 집중식 인증 서버에서 발급된 토큰이 모든 마이크로서비스에서 유효합니다.
 * - 단일 진실 공급원(Single Source of Truth): 모든 마이크로서비스가 동일한 토큰을 검증하여 일관된 인증을 보장합니다.
 */
@Slf4j
@Component
public class JwtUtils {

    /**
     * JWT 토큰 서명에 사용되는 비밀 키
     * 이 키는 인증 서버에만 알려져 있어야 하며, 안전하게 보관되어야 합니다.
     * 
     * MSA 고려사항:
     * - 프로덕션 환경에서는 이 값을 설정 파일이 아닌 환경 변수나 비밀 관리 서비스에서 로드해야 합니다.
     * - 키가 노출되면 모든 마이크로서비스의 보안이 훼손될 수 있습니다.
     */
    @Value("${jwt.secret}")
    private String jwtSecret;

    /**
     * 액세스 토큰의 유효 기간(초)
     * 
     * MSA 고려사항:
     * - 짧은 유효 기간은 보안을 강화하지만 사용자가 자주 다시 로그인해야 할 수 있습니다.
     * - 리프레시 토큰과 함께 사용하여 사용자 경험과 보안의 균형을 유지합니다.
     */
    @Value("${jwt.access-token-validity}")
    private int jwtExpirationMs;

    /**
     * 리프레시 토큰의 유효 기간(초)
     * 
     * MSA 고려사항:
     * - 리프레시 토큰은 액세스 토큰보다 훨씬 긴 유효 기간을 가져 사용자가 자주 재인증하지 않도록 합니다.
     * - 리프레시 토큰의 안전한 관리는 MSA 환경에서 장기적인 보안을 유지하는 데 중요합니다.
     */
    @Value("${jwt.refresh-token-validity}")
    private int refreshTokenExpirationMs;

    /**
     * JWT 서명에 사용할 키를 생성합니다.
     * 
     * @return HMAC-SHA 알고리즘을 위한 키
     */
    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    /**
     * 사용자 정보를 기반으로 JWT 액세스 토큰을 생성합니다.
     * 
     * MSA 고려사항:
     * - 이 토큰은 모든 마이크로서비스에서 사용자 인증에 사용됩니다.
     * - 토큰에는 사용자 식별자(이메일)만 포함됩니다. 필요에 따라 권한 등의 추가 정보를 포함할 수 있습니다.
     * 
     * @param userPrincipal 사용자 정보
     * @return 생성된 JWT 액세스 토큰
     */
    public String generateJwtToken(User userPrincipal) {
        return generateTokenFromUsername(userPrincipal.getUsername());
    }

    /**
     * 사용자명을 기반으로 JWT 액세스 토큰을 생성합니다.
     * 
     * MSA 고려사항:
     * - 이 메서드는 리프레시 토큰을 통한 새 액세스 토큰 발급 시 유용합니다.
     * - 사용자 객체 전체가 아닌 사용자명만 필요하여 서비스 간 데이터 전송량을 줄일 수 있습니다.
     * 
     * @param username 사용자명(이메일)
     * @return 생성된 JWT 액세스 토큰
     */
    public String generateTokenFromUsername(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs * 1000))
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 사용자 정보를 기반으로 JWT 리프레시 토큰을 생성합니다.
     * 
     * MSA 고려사항:
     * - 리프레시 토큰은 액세스 토큰이 만료되었을 때 새 액세스 토큰을 발급받는 데 사용됩니다.
     * - 리프레시 토큰의 만료 기간은 액세스 토큰보다 훨씬 길게 설정합니다.
     * - 보안을 위해 필요시 리프레시 토큰을 폐기할 수 있는 메커니즘이 필요합니다.
     * 
     * @param userPrincipal 사용자 정보
     * @return 생성된 JWT 리프레시 토큰
     */
    public String generateRefreshToken(User userPrincipal) {
        return Jwts.builder()
                .setSubject(userPrincipal.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + refreshTokenExpirationMs * 1000))
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 사용자 정보를 기반으로 JWT 토큰을 포함한 HTTP 쿠키를 생성합니다.
     * 
     * MSA 고려사항:
     * - 쿠키 기반 토큰 전송은 웹 애플리케이션에서 유용하지만, 서비스 간 통신에는 일반적으로 사용되지 않습니다.
     * - API 게이트웨이나 BFF(Backend For Frontend) 패턴에서 클라이언트와의 통신에 사용될 수 있습니다.
     * - 쿠키는 도메인에 종속적이므로, 여러 도메인에 걸친 마이크로서비스 환경에서는 제한이 있을 수 있습니다.
     * 
     * @param userPrincipal 사용자 정보
     * @return JWT 토큰이 포함된 HTTP 응답 쿠키
     */
    public ResponseCookie generateJwtCookie(User userPrincipal) {
        String jwt = generateJwtToken(userPrincipal);
        return ResponseCookie.from("accessToken", jwt)
                .path("/")
                .maxAge(24 * 60 * 60)
                .httpOnly(true)
                .build();
    }

    /**
     * HTTP 요청에서 JWT 토큰을 추출합니다.
     * 
     * @param request HTTP 요청
     * @return 쿠키에서 추출한 JWT 토큰, 쿠키가 없으면 null
     */
    public String getJwtFromCookies(HttpServletRequest request) {
        Cookie cookie = WebUtils.getCookie(request, "accessToken");
        if (cookie != null) {
            return cookie.getValue();
        }
        return null;
    }

    /**
     * JWT 쿠키를 제거하는 응답 쿠키를 생성합니다.
     * 
     * @return 쿠키를 제거하기 위한 HTTP 응답 쿠키
     */
    public ResponseCookie getCleanJwtCookie() {
        return ResponseCookie.from("accessToken", "")
                .path("/")
                .maxAge(0)
                .httpOnly(true)
                .build();
    }

    /**
     * JWT 토큰에서 사용자명(이메일)을 추출합니다.
     * 
     * MSA 고려사항:
     * - 이 메서드는 모든 마이크로서비스에서 토큰으로부터 사용자 식별자를 추출하는 데 사용됩니다.
     * - 일관된 사용자 식별을 위해 모든 서비스가 동일한 방식으로 토큰을 파싱해야 합니다.
     * 
     * @param token JWT 토큰
     * @return 토큰에서 추출한 사용자명(이메일)
     */
    public String getUserNameFromJwtToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    /**
     * JWT 토큰의 유효성을 검증합니다.
     * 
     * MSA 고려사항:
     * - 이 메서드는 모든 마이크로서비스에서 토큰 검증에 사용됩니다.
     * - 모든 서비스는 동일한 비밀 키를 사용하여 토큰을 검증해야 합니다.
     * - 토큰 검증은 각 마이크로서비스에서 독립적으로 수행되므로, 중앙 인증 서버에 의존하지 않고 인증 상태를 확인할 수 있습니다.
     * - 이는 시스템의 확장성과 복원력을 향상시킵니다.
     * 
     * @param authToken 검증할 JWT 토큰
     * @return 토큰이 유효하면 true, 그렇지 않으면 false
     */
    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parserBuilder().setSigningKey(key()).build().parseClaimsJws(authToken);
            return true;
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        }

        return false;
    }
}