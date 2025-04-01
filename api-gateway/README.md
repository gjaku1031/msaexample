# API 게이트웨이 (API Gateway)

Spring Cloud Gateway를 기반으로 한 API 게이트웨이 서비스입니다.

## 주요 기능
- 모든 마이크로서비스에 대한 단일 진입점
- 라우팅 및 로드 밸런싱
- 인증 및 권한 위임

## 실행 방법
```bash
./gradlew bootRun
```

## 서버 정보
- 포트: 8080
- 라우팅:
  - /api/auth/*: 인증 서버
  - /api/customers/*: 고객 서비스
  - /api/products/*: 상품 서비스
  - /api/orders/*: 주문 서비스 