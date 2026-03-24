# Backend 프로젝트 구조

Spring Boot 기반 백엔드의 패키지 구조와 각 레이어의 역할을 정의한다.

## 패키지 구조

```
com.vibemyself/
├── controller/
│   ├── member/
│   ├── goods/
│   ├── cart/
│   ├── order/
│   └── prom/
├── service/
│   ├── member/
│   ├── goods/
│   ├── cart/
│   ├── order/
│   └── prom/
├── mapper/
│   ├── member/
│   ├── goods/
│   ├── cart/
│   ├── order/
│   └── prom/
├── model/                     # 도메인 모델 (DB 엔티티)
│   ├── member/
│   ├── goods/
│   ├── cart/
│   ├── order/
│   └── prom/
├── dto/                       # 요청/응답 DTO
│   ├── member/
│   ├── goods/
│   ├── cart/
│   ├── order/
│   └── prom/
└── config/                    # Spring 설정 (Security, Web 등)
```

## 레이어 역할

| 레이어 | 위치 | 역할 |
|--------|------|------|
| Controller | `controller/` | HTTP 요청 수신, DTO 변환, Service 호출 |
| Service | `service/` | 비즈니스 로직, 트랜잭션 처리 |
| Mapper | `mapper/` | MyBatis SQL 매퍼 인터페이스 |
| DTO | `dto/` | 요청/응답 데이터 구조 정의 |
| Domain | `domain/` | DB 테이블과 매핑되는 모델 클래스 |

## 네이밍 컨벤션

| 구분 | 형식 | 예시 |
|------|------|------|
| Controller | `{Module}Controller` | `OrderController` |
| Service | `{Module}Service` | `OrderService` |
| Mapper | `{Module}Mapper` | `OrderMapper` |
| Request DTO | `{Action}{Module}Request` | `CreateOrderRequest` |
| Response DTO | `{Module}{Detail}Response` | `OrderDetailResponse` |
| Domain | `{Module}` | `Order` |

## URL 구조

```
/api/{module}/...          # 쇼핑몰 API
/api/admin/{module}/...    # 어드민 API
```

## MyBatis XML

SQL XML 파일은 `src/main/resources/mapper/{module}/` 하위에 위치한다.

```
resources/
└── mapper/
    ├── order/
    │   └── OrderMapper.xml
    └── goods/
        └── GoodsMapper.xml
```
