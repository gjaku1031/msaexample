package com.example.authserver.service;

import com.example.authserver.dto.LoginRequest;
import com.example.authserver.dto.TokenResponse;
import com.example.authserver.entity.User;
import com.example.authserver.repository.UserRepository;
import com.example.authserver.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 인증 서비스 클래스
 * 사용자 인증 및 토큰 관리와 관련된 비즈니스 로직을 처리합니다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;

    /**
     * 사용자 로그인을 처리하고 토큰을 발급합니다.
     * 
     * @param loginRequest 로그인 요청 정보
     * @return TokenResponse 발급된 토큰 정보
     * @throws org.springframework.security.core.AuthenticationException 인증 실패 시 발생
     */
    public TokenResponse login(LoginRequest loginRequest) {
        // 인증 처리
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()));

        // 권한 정보 추출
        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        // 토큰 생성
        String accessToken = tokenProvider.createAccessToken(authentication);
        String refreshToken = tokenProvider.createRefreshToken(authentication);

        // 사용자 정보 조회
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("인증된 사용자를 찾을 수 없습니다."));

        // 토큰 응답 생성
        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(tokenProvider.getAccessTokenValidityInSeconds())
                .email(user.getEmail())
                .roles(roles.toArray(new String[0]))
                .build();
    }

    /**
     * 리프레시 토큰을 사용하여 새로운 액세스 토큰을 발급합니다.
     * 
     * @param refreshToken 리프레시 토큰
     * @return TokenResponse 새로 발급된 토큰 정보
     * @throws IllegalArgumentException 유효하지 않은 리프레시 토큰인 경우 발생
     */
    public TokenResponse refreshToken(String refreshToken) {
        // 리프레시 토큰 검증
        if (!tokenProvider.validateToken(refreshToken)) {
            throw new IllegalArgumentException("유효하지 않은 리프레시 토큰입니다.");
        }

        // 토큰에서 사용자 정보 추출
        String email = tokenProvider.getEmailFromToken(refreshToken);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 새로운 액세스 토큰 생성
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                user.getEmail(),
                null,
                user.getAuthorities());
        String newAccessToken = tokenProvider.createAccessToken(authentication);

        // 토큰 응답 생성
        return TokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken) // 기존 리프레시 토큰 유지
                .tokenType("Bearer")
                .expiresIn(tokenProvider.getAccessTokenValidityInSeconds())
                .email(user.getEmail())
                .roles(user.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .toArray(String[]::new))
                .build();
    }

    /**
     * 토큰을 무효화하여 로그아웃을 처리합니다.
     * 
     * @param token 무효화할 토큰
     */
    public void logout(String token) {
        tokenProvider.invalidateToken(token);
    }
}