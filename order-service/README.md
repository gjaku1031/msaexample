# 주문 서비스 (Order Service)

주문 정보를 관리하는 마이크로서비스입니다.

## 주요 기능
- 주문 생성, 조회, 상태 변경
- 주문 목록 조회 및 검색
- 고객 서비스 및 상품 서비스와 연동

## 실행 방법
```bash
./gradlew bootRun
```

## 서버 정보
- 포트: 8083
- API 엔드포인트:
  - GET /api/orders: 주문 목록 조회
  - GET /api/orders/{id}: 주문 상세 조회
  - POST /api/orders: 주문 생성
  - PATCH /api/orders/{id}/status: 주문 상태 변경 