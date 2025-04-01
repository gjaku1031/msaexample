## 프로젝트 구조

해당 프로젝트는 독립적으로 실행 가능한 6개의 마이크로서비스로 구성되어 있습니다:

- **service-registry**: 유레카 서버를 사용한 서비스 디스커버리 (포트: 8761)
- **api-gateway**: 모든 마이크로서비스에 대한 진입점 역할 (포트: 8080)
- **auth-server**: Spring Authorization Server를 사용한 중앙 집중식 인증/인가 서버 (포트: 9000)
- **customer-service**: 고객 정보 관리 서비스 (포트: 8081)
- **product-service**: 상품 정보 및 재고 관리 서비스 (포트: 8082)
- **order-service**: 주문 관리 서비스 (포트: 8083)


## 기술 스택

- **Spring Boot**: 애플리케이션 기본 프레임워크
- **Spring Cloud Netflix Eureka**: 서비스 디스커버리
- **Spring Cloud Gateway**: API 게이트웨이
- **Spring Security + OAuth2 + JWT**: 인증 및 권한 부여
- **Spring Data JPA**: 데이터 액세스
- **QueryDSL**: 타입 안전한 쿼리 작성
- **H2 Database**: 인메모리 데이터베이스 (개발 및 테스트용)
- **Feign Client**: 서비스 간 통신

## 인증 흐름

1. 사용자는 인증을 위해 API 게이트웨이를 통해 인증 서버로 접근
2. 인증 서버는 JWT 토큰 발급 (access token, refresh token)
3. 사용자는 발급받은 토큰으로 다른 마이크로서비스 접근
4. 각 마이크로서비스는 인증 서버를 통해 토큰 검증

## 실행 순서

서비스들을 다음 순서로 실행하는 것을 권장합니다:

1. 서비스 레지스트리 (service-registry)
2. 인증 서버 (auth-server)
3. API 게이트웨이 (api-gateway) 
4. 비즈니스 서비스 (customer-service, product-service, order-service)

각 서비스 디렉토리에서 다음 명령어로 실행할 수 있습니다:

```bash
cd [서비스 디렉토리]
./gradlew bootRun
``` 

