package com.example.authserver.config;

import com.example.authserver.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 사용자 인증 설정 클래스
 * Spring Security의 사용자 인증 메커니즘을 구성합니다.
 */
@Configuration
@RequiredArgsConstructor
public class UserAuthenticationConfig {

    private final CustomUserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    /**
     * 인증 관리자 설정
     * Spring Security의 인증 프로세스를 관리하는 AuthenticationManager를 구성합니다.
     * 
     * @param config 인증 설정 객체
     * @return AuthenticationManager 인스턴스
     * @throws Exception 인증 관리자 생성 중 발생할 수 있는 예외
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * 인증 제공자 설정
     * 사용자 인증을 처리하는 DaoAuthenticationProvider를 구성합니다.
     * 
     * @return AuthenticationProvider 인스턴스
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();

        // 커스텀 UserDetailsService 설정
        authProvider.setUserDetailsService(userDetailsService);

        // 패스워드 인코더 설정
        authProvider.setPasswordEncoder(passwordEncoder);

        return authProvider;
    }
}