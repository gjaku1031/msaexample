package com.example.authserver.service;

import com.example.authserver.dto.UserRegistrationDto;
import com.example.authserver.entity.Role;
import com.example.authserver.entity.User;
import com.example.authserver.repository.RoleRepository;
import com.example.authserver.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;

/**
 * 사용자 서비스 클래스
 * 사용자 관련 비즈니스 로직을 처리합니다.
 * 
 * MSA에서의 사용자 서비스의 역할:
 * - 중앙 집중식 사용자 관리: 모든 마이크로서비스에서 공통으로 사용하는 사용자 정보를 관리합니다.
 * - 인증 서버 통합: Spring Security의 UserDetailsService를 구현하여 인증 프로세스와 통합됩니다.
 * - 사용자 데이터 소유권: 사용자 도메인에 대한 단일 진실 공급원(Single Source of Truth)이며,
 * - 다른 마이크로서비스는 이 서비스를 통해 또는 JWT 토큰을 통해 사용자 정보에 접근합니다.
 * - 사용자 상태 관리: 사용자의 활성화, 잠금, 자격 증명 등의 상태를 중앙에서 관리합니다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService implements UserDetailsService {

    /**
     * 사용자 정보에 접근하기 위한 저장소
     * 
     * MSA 고려사항:
     * - 사용자 정보는 인증 서버에서만 직접 접근하고 수정할 수 있습니다.
     * - 다른 마이크로서비스는 사용자 정보를 직접 조회하지 않고, JWT 토큰이나 API 호출을 통해 접근해야 합니다.
     */
    private final UserRepository userRepository;

    /**
     * 역할 정보에 접근하기 위한 저장소
     * 
     * MSA 고려사항:
     * - 역할 정보는 권한 관리의 기초가 되며, 모든 마이크로서비스에서 일관되게 사용됩니다.
     * - 역할 정보는 JWT 토큰을 통해 다른 서비스로 전파됩니다.
     */
    private final RoleRepository roleRepository;

    /**
     * 비밀번호 암호화를 위한 인코더
     * 
     * MSA 고려사항:
     * - 비밀번호 처리는 인증 서버에서만 이루어져야 합니다.
     * - 다른 마이크로서비스는 암호화된 비밀번호에 접근하거나 처리해서는 안 됩니다.
     */
    private final PasswordEncoder passwordEncoder;

    /**
     * 사용자명(이메일)으로 사용자 정보를 로드합니다.
     * Spring Security의 인증 과정에서 호출됩니다.
     * 
     * MSA 고려사항:
     * - 이 메서드는 인증 과정의 핵심으로, 토큰 발급 전에 사용자 자격 증명을 확인합니다.
     * - 사용자 정보 로딩 실패(사용자 없음, 비활성화 등)는 인증 실패로 이어집니다.
     * - 로드된 사용자 정보에는 권한 정보가 포함되어, 토큰 생성 시 이 정보가 포함될 수 있습니다.
     * 
     * @param username 사용자명(이메일)
     * @return 사용자 상세 정보
     * @throws UsernameNotFoundException 사용자를 찾을 수 없는 경우
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username));
    }

    /**
     * 새로운 사용자를 등록합니다.
     * 
     * MSA 고려사항:
     * - 사용자 등록은 중앙화된 인증 서버에서만 처리되어야 합니다.
     * - 신규 사용자 등록 후, 필요시 이벤트를 발행하여 다른 마이크로서비스에게 알릴 수 있습니다(이벤트 기반 아키텍처).
     * - 사용자별 추가 프로필 정보는 해당 도메인의 마이크로서비스에서 관리할 수 있으나,
     * 사용자 ID로 연결되어야 합니다.
     * 
     * @param registrationDto 사용자 등록 정보
     * @return 등록된 사용자 엔티티
     * @throws IllegalArgumentException 이메일이 이미 등록된 경우
     * @throws IllegalStateException    기본 사용자 역할이 설정되지 않은 경우
     */
    @Transactional
    public User registerUser(UserRegistrationDto registrationDto) {
        if (userRepository.existsByEmail(registrationDto.getEmail())) {
            throw new IllegalArgumentException("이미 등록된 이메일입니다: " + registrationDto.getEmail());
        }

        // 기본 사용자 역할 조회
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new IllegalStateException("기본 사용자 역할이 설정되지 않았습니다."));

        // 사용자 엔티티 생성
        User user = User.builder()
                .email(registrationDto.getEmail())
                .password(passwordEncoder.encode(registrationDto.getPassword()))
                .name(registrationDto.getName())
                .roles(new HashSet<>(Collections.singletonList(userRole)))
                .enabled(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .accountNonExpired(true)
                .build();

        return userRepository.save(user);
    }

    /**
     * 이메일로 사용자를 조회합니다.
     * 
     * MSA 고려사항:
     * - 이 메서드는 인증 서버 내부에서만 사용되어야 합니다.
     * - 다른 마이크로서비스에서 필요한 사용자 정보는 별도의 API로 제공하거나,
     * JWT 토큰에 필요한 정보를 포함시켜야 합니다.
     * - 민감한 사용자 정보는 서비스 간 직접 전송을 최소화해야 합니다.
     * 
     * @param email 사용자 이메일
     * @return 조회된 사용자 엔티티
     * @throws IllegalArgumentException 사용자를 찾을 수 없는 경우
     */
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + email));
    }

    /**
     * 사용자 계정을 활성화/비활성화합니다.
     * 
     * MSA 고려사항:
     * - 사용자 계정 상태 변경은 인증 서버에서만 직접 처리해야 합니다.
     * - 상태 변경 후 이벤트를 발행하여 다른 마이크로서비스에 알릴 수 있습니다.
     * - 계정 비활성화는 해당 사용자의 모든 토큰이 무효화되도록 처리해야 합니다(예: 토큰 블랙리스트).
     * 
     * @param email   사용자 이메일
     * @param enabled 활성화 여부
     */
    @Transactional
    public void setUserEnabled(String email, boolean enabled) {
        User user = getUserByEmail(email);
        user.setEnabled(enabled);
        userRepository.save(user);
    }

    /**
     * 사용자 계정을 잠금/잠금해제합니다.
     * 
     * MSA 고려사항:
     * - 계정 잠금은 보안 정책에 따라 자동화될 수 있습니다(예: 로그인 실패 횟수 초과).
     * - 잠금 상태는 인증 과정에서 확인되며, 잠긴 계정은 토큰 발급이 거부됩니다.
     * - 계정 잠금 시 활성 토큰의 무효화를 고려해야 합니다.
     * 
     * @param email  사용자 이메일
     * @param locked 잠금 여부(true: 잠금, false: 잠금해제)
     */
    @Transactional
    public void setAccountLocked(String email, boolean locked) {
        User user = getUserByEmail(email);
        user.setAccountNonLocked(!locked);
        userRepository.save(user);
    }
}