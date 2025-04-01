package com.example.authserver.repository;

import com.example.authserver.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 사용자 저장소 인터페이스
 * 사용자 엔티티에 대한 데이터베이스 작업을 처리합니다.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 이메일로 사용자를 조회합니다.
     * 
     * @param email 조회할 사용자의 이메일
     * @return Optional<User> 조회된 사용자 정보를 담은 Optional 객체
     */
    Optional<User> findByEmail(String email);

    /**
     * 이메일로 사용자 존재 여부를 확인합니다.
     * 
     * @param email 확인할 사용자의 이메일
     * @return boolean 사용자 존재 여부
     */
    boolean existsByEmail(String email);
}