package com.example.authserver.init;

import com.example.authserver.entity.Role;
import com.example.authserver.entity.User;
import com.example.authserver.repository.RoleRepository;
import com.example.authserver.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * 초기 데이터 생성기
 * 애플리케이션 시작 시 기본 역할과 사용자를 생성합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        log.info("초기 데이터 생성을 시작합니다...");
        initRoles();
        initUsers();
        log.info("초기 데이터 생성이 완료되었습니다!");
    }

    private void initRoles() {
        if (roleRepository.count() == 0) {
            log.info("기본 역할을 생성합니다...");

            Role roleUser = Role.builder()
                    .name("ROLE_USER")
                    .description("일반 사용자 역할")
                    .build();

            Role roleAdmin = Role.builder()
                    .name("ROLE_ADMIN")
                    .description("관리자 역할")
                    .build();

            roleRepository.save(roleUser);
            roleRepository.save(roleAdmin);
        }
    }

    private void initUsers() {
        if (userRepository.count() == 0) {
            log.info("기본 사용자를 생성합니다...");

            Role roleUser = roleRepository.findByName("ROLE_USER")
                    .orElseThrow(() -> new IllegalStateException("사용자 역할을 찾을 수 없습니다."));
            Role roleAdmin = roleRepository.findByName("ROLE_ADMIN")
                    .orElseThrow(() -> new IllegalStateException("관리자 역할을 찾을 수 없습니다."));

            // 일반 사용자
            Set<Role> userRoles = new HashSet<>();
            userRoles.add(roleUser);

            User user = User.builder()
                    .email("user@example.com")
                    .password(passwordEncoder.encode("password"))
                    .name("일반 사용자")
                    .roles(userRoles)
                    .enabled(true)
                    .accountNonLocked(true)
                    .credentialsNonExpired(true)
                    .accountNonExpired(true)
                    .build();

            userRepository.save(user);

            // 관리자
            Set<Role> adminRoles = new HashSet<>();
            adminRoles.add(roleUser);
            adminRoles.add(roleAdmin);

            User admin = User.builder()
                    .email("admin@example.com")
                    .password(passwordEncoder.encode("password"))
                    .name("관리자")
                    .roles(adminRoles)
                    .enabled(true)
                    .accountNonLocked(true)
                    .credentialsNonExpired(true)
                    .accountNonExpired(true)
                    .build();

            userRepository.save(admin);
        }
    }
}