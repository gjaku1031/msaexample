package com.example.authserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Spring Authorization Server의 메인 애플리케이션 클래스.
 * 이 클래스는 애플리케이션의 진입점(Entry Point)이며 Spring Boot 애플리케이션을 시작합니다.
 * 
 * @EnableDiscoveryClient: 이 서비스가 Service Registry(Eureka)에 등록되어
 * 다른 서비스들이 이 인증 서버를 발견 -> MSA에서 중요한 부분입니다
 * 
 * MSA에서 인증 서버의 역할:
 * - 중앙 집중식 인증과 인가 제공
 * - 사용자 자격 증명(Credentials) 검증
 * - JWT 토큰 발급 및 관리
 * - OAuth2.0 및 OpenID Connect 프로토콜 구현
 */
@SpringBootApplication
@EnableDiscoveryClient
public class AuthServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthServerApplication.class, args);
    }
}