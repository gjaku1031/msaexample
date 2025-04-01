package com.example.authserver.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * 사용자 역할 엔티티 클래스
 * 시스템의 권한 정보를 저장하고 관리하는 JPA 엔티티입니다.
 * 
 * MSA에서의 역할 관리:
 * - 중앙 집중식 권한 관리: 모든 마이크로서비스에서 참조되는 역할 정보를 인증 서버에서 관리합니다.
 * - 권한 전파: 사용자의 역할 정보는 JWT 토큰에 포함되어 각 마이크로서비스로 전달됩니다.
 * - 세분화된 접근 제어: 서비스별, 리소스별 다양한 역할을 정의하여 세밀한 접근 제어가 가능합니다.
 * 
 * 추가적인 고려사항:
 * - 역할 기반 접근 제어(RBAC)는 복잡한 마이크로서비스 환경에서 일관된 보안 정책을 적용하는 데 유용합니다.
 * - 역할 이름은 일관된 명명 규칙(예: 'ROLE_' 접두사)을 사용하여 Spring Security와의 호환성을 유지합니다.
 */
@Entity
@Table(name = "roles")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Role {

    /**
     * 역할의 고유 식별자
     * 
     * MSA 고려사항:
     * - 이 ID는 인증 서버 내부에서만 사용되는 식별자입니다.
     * - 마이크로서비스 간에는 역할 이름(name)이 식별자로 사용됩니다.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 역할 이름
     * 예: ROLE_USER, ROLE_ADMIN, ROLE_PRODUCT_ADMIN, ROLE_ORDER_MANAGER 등
     * Spring Security에서 권한 체크에 사용됩니다.
     * 
     * MSA 고려사항:
     * - 역할 이름은 JWT 토큰의 authorities/roles 클레임에 포함되어 마이크로서비스로 전달됩니다.
     * - 각 마이크로서비스는 이 역할 이름을 기반으로 접근 제어를 수행합니다.
     * - 마이크로서비스별 역할(예: ROLE_PRODUCT_ADMIN, ROLE_ORDER_MANAGER)을 정의하여 서비스별 접근 제어가
     * 가능합니다.
     * - 중복을 방지하기 위해 unique 제약조건이 설정되어 있습니다.
     */
    @Column(unique = true, nullable = false)
    private String name;

    /**
     * 역할에 대한 설명
     * 
     * MSA 고려사항:
     * - 설명은 관리 목적으로만 사용되며, JWT 토큰에는 포함되지 않습니다.
     * - 복잡한 MSA 환경에서 다양한 역할의 목적과 권한 범위를 문서화하는 데 유용합니다.
     */
    @Column
    private String description;
}