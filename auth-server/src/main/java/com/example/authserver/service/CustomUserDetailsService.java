package com.example.authserver.service;

import com.example.authserver.entity.User;
import com.example.authserver.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 커스텀 사용자 세부 정보 서비스
 * Spring Security의 UserDetailsService 인터페이스를 구현하여 사용자 인증에 필요한 정보를 제공합니다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * 사용자명으로 사용자 정보를 조회합니다.
     * 
     * @param username 조회할 사용자의 사용자명 (이메일)
     * @return UserDetails 인터페이스를 구현한 사용자 정보 객체
     * @throws UsernameNotFoundException 사용자를 찾을 수 없는 경우 발생하는 예외
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .roles(user.getRoles().toArray(new String[0]))
                .build();
    }
}