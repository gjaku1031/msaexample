# Spring Authorization Server

이 서비스는 OAuth2.0과 OpenID Connect 프로토콜을 구현한 인증/인가 서버입니다.

## MSA에서의 인증 서버 역할

### 중앙 집중식 인증/인가

마이크로서비스 아키텍처(MSA)에서 인증 서버는 다음과 같은 중요한 역할을 담당합니다:

- **통합된 사용자 저장소**: 모든 마이크로서비스가 공유하는 중앙 집중식 사용자 정보 관리
- **토큰 기반 인증**: 상태 없는(Stateless) JWT 토큰을 통한 확장 가능한 인증 메커니즘 제공
- **서비스 간 인증**: 마이크로서비스 간 통신을 위한 클라이언트 자격 증명 흐름 지원
- **권한 관리 통합**: 일관된 역할 기반 접근 제어(RBAC) 제공

### JWT 토큰과 MSA

JWT(JSON Web Token)는 MSA 환경에서 다음과 같은 이유로 널리 사용됩니다:

- **상태 없는 통신**: 토큰에 필요한 모든 정보가 포함되어 세션 상태를 유지할 필요가 없음
- **확장성**: 수평적 확장이 용이하며 서버 간 공유 세션 관리가 필요 없음
- **서비스 간 전파**: 토큰을 통해 인증 정보와 사용자 권한이 모든 마이크로서비스에 전파됨
- **검증 독립성**: 각 마이크로서비스가 공개 키를 사용하여 독립적으로 토큰을 검증할 수 있음

### 보안 아키텍처

인증 서버는 다음과 같은 보안 아키텍처를 구현합니다:

- **API 게이트웨이 통합**: 모든 인증 요청은 API 게이트웨이를 통해 인증 서버로 라우팅
- **토큰 계층화**: 짧은 수명의 액세스 토큰과 긴 수명의 리프레시 토큰 사용
- **클라이언트 등록**: 각 마이크로서비스는 인증 서버에 클라이언트로 등록
- **스코프 기반 접근 제어**: 토큰에 포함된 스코프를 통해 서비스별 권한 제한

### MSA 구성 요소와의 상호작용

- **서비스 디스커버리**: 인증 서버는 Eureka에 등록되어 다른 서비스에서 동적으로 발견
- **설정 서버**: 환경별 보안 설정이 중앙 설정 서버를 통해 관리될 수 있음
- **API 게이트웨이**: 모든 인증 요청의 진입점이자 토큰 검증의 첫 번째 지점
- **비즈니스 서비스**: 각 마이크로서비스는 토큰을 검증하고 권한을 확인하여 접근 제어

### 토큰 기반 인증 흐름

1. **사용자 로그인**: 사용자가 자격 증명을 API 게이트웨이를 통해 인증 서버에 제출
2. **토큰 발급**: 인증 서버가 액세스 토큰과 리프레시 토큰을 발급
3. **리소스 요청**: 클라이언트가 액세스 토큰을 포함하여 마이크로서비스에 요청
4. **토큰 검증**: 각 마이크로서비스가 토큰을 검증하고 권한을 확인
5. **토큰 갱신**: 액세스 토큰 만료 시 리프레시 토큰을 사용하여 새 액세스 토큰 발급

## 주요 기능

### 1. OAuth2.0 프로토콜 지원
- Authorization Code Grant
- Client Credentials Grant
- Refresh Token Grant
- PKCE(Proof Key for Code Exchange) 지원

### 2. OpenID Connect 기능
- ID 토큰 발급
- UserInfo Endpoint
- Discovery Endpoint

### 3. 토큰 관리
- JWT 형식의 액세스 토큰
- 리프레시 토큰 관리
- 토큰 폐기(Revocation)

### 4. 클라이언트 관리
- 동적 클라이언트 등록
- 클라이언트 인증
- 클라이언트 범위(Scope) 제한

## 기술 스택

- Spring Authorization Server 1.2.1
- Spring Security 6.2
- Spring Boot 3.2.3
- JPA/Hibernate
- H2 Database (개발용)

## 주요 컴포넌트

### 1. 설정 클래스
- `SecurityConfig`: 기본 보안 설정
- `AuthorizationServerConfig`: OAuth2 서버 설정
- `JwtConfig`: JWT 토큰 설정

### 2. 인증 관련
- `CustomUserDetailsService`: 사용자 인증 처리
- `JwtTokenCustomizer`: JWT 토큰 커스터마이징
- `OAuth2TokenCustomizer`: OAuth2 토큰 커스터마이징

### 3. 데이터 모델
- `User`: 사용자 엔티티
- `Role`: 역할 엔티티
- `OAuth2AuthorizationConsent`: 인증 동의 정보

## API 엔드포인트

### OAuth2 엔드포인트
```
POST /oauth2/token              # 토큰 발급
POST /oauth2/authorize          # 인증 코드 발급
POST /oauth2/revoke            # 토큰 폐기
GET  /.well-known/openid-configuration  # OIDC Discovery
GET  /userinfo                 # 사용자 정보
```

### 사용자 관리 API
```
POST   /api/auth/register      # 회원가입
POST   /api/auth/login         # 로그인
GET    /api/auth/me            # 내 정보 조회
PUT    /api/auth/password      # 비밀번호 변경
```

## 설정 가이드

### 1. application.yml 설정
```yaml
spring:
  security:
    oauth2:
      authorizationserver:
        client:
          client-id: client
          client-secret: "{noop}secret"
          client-authentication-methods:
            - client_secret_basic
          authorization-grant-types:
            - authorization_code
            - refresh_token
            - client_credentials
          redirect-uris:
            - http://localhost:8080/login/oauth2/code/client
          scopes:
            - openid
            - profile
            - read
            - write
```

### 2. 키 저장소 설정
```bash
# JWT 서명용 키 생성
keytool -genkeypair -alias auth-server-key \
  -keyalg RSA -keysize 2048 \
  -keystore auth-server.jks \
  -validity 3650
```

### 3. CORS 설정
```yaml
spring:
  security:
    cors:
      allowed-origins:
        - http://localhost:8080
      allowed-methods:
        - GET
        - POST
        - PUT
        - DELETE
```

## 보안 설정 상세

### 1. 암호화 설정
- BCrypt 암호화 사용
- Salt 자동 생성
- 작업 계수(Work Factor): 12

### 2. 토큰 설정
- 액세스 토큰 만료: 1시간
- 리프레시 토큰 만료: 30일
- JWT 서명 알고리즘: RS256

### 3. 세션 관리
- 무상태(Stateless) 세션
- CSRF 보호
- XSS 방지

## 개발 가이드

### 1. 로컬 개발 환경 설정
```bash
# 프로젝트 빌드
./gradlew clean build

# 서버 실행
./gradlew bootRun
```

### 2. 테스트
```bash
# 단위 테스트 실행
./gradlew test

# 통합 테스트 실행
./gradlew integrationTest
```

### 3. API 테스트

#### 토큰 발급 테스트
```bash
# Authorization Code 발급
curl -X POST http://localhost:9000/oauth2/authorize \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "response_type=code" \
  -d "client_id=client" \
  -d "redirect_uri=http://localhost:8080/login/oauth2/code/client" \
  -d "scope=openid profile"

# 토큰 발급
curl -X POST http://localhost:9000/oauth2/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -u "client:secret" \
  -d "grant_type=authorization_code" \
  -d "code=..." \
  -d "redirect_uri=http://localhost:8080/login/oauth2/code/client"
```

## 모니터링

### 1. 액추에이터 엔드포인트
- /actuator/health: 서버 상태
- /actuator/metrics: 메트릭 정보
- /actuator/loggers: 로깅 설정

### 2. 로깅
- 접근 로그
- 인증 실패 로그
- 토큰 발급 로그

## 트러블슈팅

### 일반적인 문제

1. **토큰 발급 실패**
   - 클라이언트 인증 정보 확인
   - 리다이렉트 URI 일치 여부 확인
   - 요청된 스코프 권한 확인

2. **Invalid_grant 에러**
   - 인증 코드 재사용 여부 확인
   - 인증 코드 만료 여부 확인
   - PKCE 파라미터 확인

3. **CORS 에러**
   - 허용된 오리진 설정 확인
   - 프리플라이트 요청 처리 확인
   - 헤더 설정 확인

## 보안 고려사항

1. **토큰 보안**
   - 짧은 액세스 토큰 수명
   - 안전한 리프레시 토큰 저장
   - 토큰 폐기 관리

2. **클라이언트 보안**
   - 안전한 클라이언트 시크릿 관리
   - 적절한 스코프 제한
   - 리다이렉트 URI 검증

3. **일반 보안**
   - TLS 사용 필수
   - 적절한 CORS 정책
   - 레이트 리미팅 적용

## 참고 자료

- [Spring Authorization Server 문서](https://docs.spring.io/spring-authorization-server/docs/current/reference/html/)
- [OAuth 2.0 명세](https://tools.ietf.org/html/rfc6749)
- [OpenID Connect Core 1.0](https://openid.net/specs/openid-connect-core-1_0.html)

# MSA 예제 프로젝트

이 프로젝트는 Spring Boot, Spring Cloud, Spring Security, OAuth2 및 JPA를 활용한 마이크로서비스 아키텍처(MSA) 예제입니다.

## 프로젝트 구조

이 프로젝트는 독립적으로 실행 가능한 6개의 마이크로서비스로 구성되어 있습니다:

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

## QueryDSL 사용

이 프로젝트에서는 각 마이크로서비스에서 타입 안전한 쿼리를 위해 QueryDSL을 사용합니다.

- **@QueryEntity 어노테이션 사용**: MSA 환경에서는 독립적인 빌드 설정으로 인해 엔티티 클래스에 `@QueryEntity` 어노테이션을 명시적으로 추가하여 Q클래스 생성을 보장합니다.
- **Q클래스 생성 위치**: 각 서비스의 `src/main/generated` 디렉토리에 Q클래스가 생성됩니다.
- **빌드 설정**: 각 서비스의 `build.gradle`에 QueryDSL 관련 설정이 포함되어 있습니다.

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

## 토큰 기반 인증 아키텍처

MSA 환경에서 이 프로젝트는 다음과 같은 토큰 기반 인증 아키텍처를 사용합니다:

1. **중앙 집중식 인증/인가**: 모든 인증 요청은 auth-server에서 처리됩니다.
2. **상태 없는(Stateless) 통신**: JWT 토큰을 사용하여 서버 측 세션을 유지하지 않습니다.
3. **토큰 계층화**: 짧은 수명의 액세스 토큰과 긴 수명의 리프레시 토큰을 사용합니다.
4. **서비스 간 전파**: 토큰을 통해 인증 정보와 권한이 모든 마이크로서비스에 전달됩니다.
5. **검증 독립성**: 각 마이크로서비스는 공개 키를 사용하여 독립적으로 토큰을 검증합니다.

## 라이센스

이 프로젝트는 MIT 라이센스 하에 배포됩니다. 