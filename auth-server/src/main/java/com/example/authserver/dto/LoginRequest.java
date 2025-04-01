package com.example.authserver.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 로그인 요청 DTO 클래스
 * 클라이언트로부터 받은 로그인 정보를 전달하는 데이터 전송 객체입니다.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    /**
     * 사용자 이메일
     * 유효한 이메일 형식이어야 합니다.
     */
    @Email(message = "유효한 이메일 주소를 입력해주세요.")
    @NotBlank(message = "이메일은 필수 입력 항목입니다.")
    private String email;

    /**
     * 사용자 비밀번호
     * 공백이 아니어야 합니다.
     */
    @NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
    private String password;
}