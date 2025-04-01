# 상품 서비스 (Product Service)

상품 정보 및 재고를 관리하는 마이크로서비스입니다.

## 주요 기능
- 상품 정보 등록, 조회, 수정, 삭제
- 상품 목록 조회 및 검색
- 상품 재고 관리

## 실행 방법
```bash
./gradlew bootRun
```

## 서버 정보
- 포트: 8082
- API 엔드포인트:
  - GET /api/products: 상품 목록 조회
  - GET /api/products/{id}: 상품 상세 조회
  - POST /api/products: 상품 등록
  - PUT /api/products/{id}: 상품 정보 수정
  - DELETE /api/products/{id}: 상품 삭제
  - PATCH /api/products/{id}/stock: 상품 재고 수정 