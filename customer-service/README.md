# 고객 서비스 (Customer Service)

고객 정보를 관리하는 마이크로서비스입니다.

## 주요 기능
- 고객 정보 등록, 조회, 수정, 삭제
- 고객 목록 조회 및 검색

## 실행 방법
```bash
./gradlew bootRun
```

## 서버 정보
- 포트: 8081
- API 엔드포인트:
  - GET /api/customers: 고객 목록 조회
  - GET /api/customers/{id}: 고객 상세 조회
  - POST /api/customers: 고객 등록
  - PUT /api/customers/{id}: 고객 정보 수정
  - DELETE /api/customers/{id}: 고객 삭제 