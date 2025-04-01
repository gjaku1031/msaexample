package com.example.authserver.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 사용자 엔티티 클래스
 * 시스템의 사용자 정보를 저장하고 관리하는 JPA 엔티티입니다.
 * 
 * MSA에서의 사용자 관리:
 * - 중앙 집중식 사용자 저장소: 인증 서버는 모든 마이크로서비스에서 공통으로 사용되는 사용자 정보를 관리합니다.
 * - 토큰 기반 인증: 사용자 정보는 JWT 토큰에 포함되어 마이크로서비스 간에 전달됩니다.
 * - 역할 기반 접근 제어: 사용자의 역할(Role)은 각 마이크로서비스에서 권한 검사에 사용됩니다.
 * 
 * Spring Security와의 통합:
 * - 이 클래스는 Spring Security의 UserDetails 인터페이스를 구현하여 인증 시스템과 통합됩니다.
 * - UserDetailsService는 이 엔티티를 로드하여 인증 과정에서 사용합니다.
 */
@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails {

    /**
     * 사용자의 고유 식별자
     * 
     * MSA 고려사항:
     * - 이 ID는 인증 서버 내에서만 사용되는 내부 식별자입니다.
     * - 마이크로서비스 간에는 보통 이메일이나 UUID와 같은 비즈니스 식별자가 사용됩니다.
     * - 서비스 간 통신 시 사용자 식별을 위해 JWT 토큰에 적절한 식별자가 포함되어야 합니다.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 사용자의 이메일 주소
     * 로그인 시 사용자명으로 사용됩니다.
     * 
     * MSA 고려사항:
     * - 이메일은 시스템 전체에서 사용자를 고유하게 식별하는 비즈니스 키로 사용됩니다.
     * - JWT 토큰의 'sub' 클레임으로 사용되어 마이크로서비스 간에 사용자를 식별합니다.
     * - 이메일 중복 방지를 위해 unique 제약조건이 설정되어 있습니다.
     */
    @Email
    @Column(unique = true, nullable = false)
    private String email;

    /**
     * 사용자의 비밀번호
     * BCrypt로 해시화되어 저장됩니다.
     * 
     * MSA 고려사항:
     * - 비밀번호는 인증 서버에서만 처리되며, 다른 마이크로서비스로 전달되지 않습니다.
     * - 보안을 위해 비밀번호는 평문이 아닌 해시된 형태로만 저장됩니다.
     * - 인증 후에는 JWT 토큰이 발급되어 이후 요청에 사용됩니다.
     */
    @NotBlank
    @Column(nullable = false)
    private String password;

    /**
     * 사용자의 이름
     * 
     * MSA 고려사항:
     * - 사용자 이름은 UI 표시용으로 사용되며, JWT 토큰의 'name' 클레임에 포함될 수 있습니다.
     * - 다른 마이크로서비스에서 사용자 정보 조회 없이 토큰에서 직접 이름을 사용할 수 있습니다.
     */
    @Column(nullable = false)
    private String name;

    /**
     * 사용자의 역할 목록
     * ROLE_USER, ROLE_ADMIN 등의 권한을 저장합니다.
     * 
     * MSA에서의 역할:
     * - 역할은 JWT 토큰의 'roles' 또는 'authorities' 클레임에 포함되어 전파됩니다.
     * - 각 마이크로서비스는 이 역할 정보를 기반으로 권한 결정을 내립니다.
     * - 역할 기반 접근 제어(RBAC)를 통해 서비스별로 세분화된 권한 관리가 가능합니다.
     * - FetchType.EAGER는 사용자 조회 시 항상 역할 정보도 함께 로드되도록 합니다.
     */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    /**
     * 계정 활성화 여부
     * 
     * MSA 고려사항:
     * - 계정 상태는 인증 과정에서 확인되며, 비활성화된 계정은 인증이 거부됩니다.
     * - 계정 상태 변경은 인증 서버에서만 이루어지고, 다른 마이크로서비스에는 JWT 토큰을 통해 전파됩니다.
     */
    @Column(nullable = false)
    @Builder.Default
    private boolean enabled = true;

    /**
     * 계정 잠금 여부
     * 
     * MSA 고려사항:
     * - 계정 잠금은 보안 정책(예: 로그인 실패 횟수 초과)에 따라 적용될 수 있습니다.
     * - 잠긴 계정은 인증 서버에서 로그인이 차단되므로 JWT 토큰이 발급되지 않습니다.
     */
    @Column(nullable = false)
    @Builder.Default
    private boolean accountNonLocked = true;

    /**
     * 자격 증명(비밀번호) 만료 여부
     * 
     * MSA 고려사항:
     * - 비밀번호 정책에 따라 주기적인 비밀번호 변경을 강제할 수 있습니다.
     * - 자격 증명이 만료된 경우, 인증 서버는 사용자에게 비밀번호 변경을 요구할 수 있습니다.
     */
    @Column(nullable = false)
    @Builder.Default
    private boolean credentialsNonExpired = true;

    /**
     * 계정 만료 여부
     * 
     * MSA 고려사항:
     * - 계정 만료는 임시 계정이나 구독 기반 서비스에서 활용될 수 있습니다.
     * - 만료된 계정은 인증 서버에서 로그인이 차단되므로 JWT 토큰이 발급되지 않습니다.
     */
    @Column(nullable = false)
    @Builder.Default
    private boolean accountNonExpired = true;

    /**
     * Spring Security의 UserDetails 인터페이스 구현
     * 사용자의 로그인 식별자를 반환합니다.
     * 
     * MSA 고려사항:
     * - 이메일을 사용자명으로 사용하는 것은 마이크로서비스 간에 일관된 사용자 식별 방식을 제공합니다.
     * - 이 값은 JWT 토큰의 'sub' 클레임과 일치해야 합니다.
     * 
     * @return 사용자명(이메일)
     */
    @Override
    public String getUsername() {
        return email; // 이메일을 사용자명으로 사용
    }

    /**
     * Spring Security의 UserDetails 인터페이스 구현
     * 사용자의 권한 목록을 반환합니다.
     * 
     * MSA 고려사항:
     * - 이 메서드가 반환하는 권한 목록은 JWT 토큰의 'authorities' 또는 'roles' 클레임에 포함됩니다.
     * - 각 마이크로서비스는 이 권한 정보를 기반으로 접근 제어 결정을 내립니다.
     * - 권한은 "ROLE_USER", "ROLE_ADMIN"과 같은 형식의 문자열로 표현됩니다.
     * 
     * @return 사용자의 권한 목록
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toList());
    }

    /**
     * Spring Security의 UserDetails 인터페이스 구현
     * 계정의 만료 여부를 반환합니다.
     * 
     * @return true: 계정이 만료되지 않음, false: 계정이 만료됨
     */
    @Override
    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }

    /**
     * Spring Security의 UserDetails 인터페이스 구현
     * 계정의 잠금 여부를 반환합니다.
     * 
     * @return true: 계정이 잠기지 않음, false: 계정이 잠김
     */
    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    /**
     * Spring Security의 UserDetails 인터페이스 구현
     * 자격 증명(비밀번호)의 만료 여부를 반환합니다.
     * 
     * @return true: 자격 증명이 만료되지 않음, false: 자격 증명이 만료됨
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }

    /**
     * Spring Security의 UserDetails 인터페이스 구현
     * 계정의 활성화 여부를 반환합니다.
     * 
     * @return true: 계정이 활성화됨, false: 계정이 비활성화됨
     */
    @Override
    public boolean isEnabled() {
        return enabled;
    }
}