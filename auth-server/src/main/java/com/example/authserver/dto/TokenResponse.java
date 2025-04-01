package com.example.authserver.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 토큰 응답 DTO 클래스
 * 인증 서버가 클라이언트에게 발급한 토큰 정보를 전달하는 데이터 전송 객체입니다.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenResponse {

    /**
     * 액세스 토큰
     * 보호된 리소스에 접근하기 위한 JWT 토큰입니다.
     */
    private String accessToken;

    /**
     * 리프레시 토큰
     * 액세스 토큰이 만료되었을 때 새로운 액세스 토큰을 발급받기 위한 토큰입니다.
     */
    private String refreshToken;

    /**
     * 토큰 타입
     * 일반적으로 "Bearer"를 사용합니다.
     */
    private String tokenType;

    /**
     * 액세스 토큰 만료 시간
     * 초 단위로 표시됩니다.
     */
    private long expiresIn;

    /**
     * 사용자 이메일
     * 토큰이 발급된 사용자의 이메일 주소입니다.
     */
    private String email;

    /**
     * 사용자 권한 목록
     * 토큰에 포함된 사용자의 권한 정보입니다.
     */
    private String[] roles;
}