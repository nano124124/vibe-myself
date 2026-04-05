# Backend 프로젝트 구조

Spring Boot 기반 백엔드의 패키지 구조와 각 레이어의 역할을 정의한다.

## 패키지 구조

```
com.vibemyself/
├── common/                    # 공통 인프라 (모듈 아님): jwt/, redis/, security/, util/
├── global/                    # 전역 설정: exception/, GlobalExceptionHandler
├── controller/
│   ├── member/
│   ├── goods/
│   ├── cart/
│   ├── order/
│   ├── prom/
│   └── system/
├── service/
│   ├── member/
│   ├── goods/
│   ├── cart/
│   ├── order/
│   ├── prom/
│   └── system/
├── mapper/
│   ├── member/
│   ├── goods/
│   ├── cart/
│   ├── order/
│   ├── prom/
│   └── system/
├── entity/                    # DB 테이블과 1:1 매핑되는 엔티티 (모듈 구분 없음, 플랫 구조)
│   ├── StAdminBase.java       # ST_ADMIN_BASE
│   ├── StMenuBase.java        # ST_MENU_BASE
│   ├── EtMbrBase.java         # ET_MBR_BASE
│   ├── PrCtgBase.java         # PR_CTG_BASE
│   └── ...                    # 클래스명 = DB 테이블명의 PascalCase
├── dto/                       # 요청/응답 DTO
│   ├── member/
│   ├── goods/
│   ├── cart/
│   ├── order/
│   ├── prom/
│   └── system/
└── config/                    # Spring 설정 (Security, Web 등)
```

## 레이어 역할

| 레이어 | 위치 | 역할 |
|--------|------|------|
| Controller | `controller/` | HTTP 요청 수신, DTO 변환, Service 호출 |
| Service | `service/` | 비즈니스 로직, 트랜잭션 처리 |
| Mapper | `mapper/` | MyBatis SQL 매퍼 인터페이스 |
| DTO | `dto/` | 요청/응답 데이터 구조 정의 |
| Entity | `entity/` | DB 테이블과 1:1 매핑되는 엔티티 클래스 (플랫 구조, 클래스명 = 테이블명 PascalCase) |

## 네이밍 컨벤션

| 구분 | 형식 | 예시 |
|------|------|------|
| Controller | `{Module}Controller` | `OrderController` |
| Service | `{Module}{Action}Service` | `GoodsCreateService`, `OrderCancelService` |
| Mapper | `{Module}Mapper` | `OrderMapper` |
| Request DTO | `{Action}{Module}Request` | `CreateOrderRequest` |
| Response DTO | `{Module}{Detail}Response` | `OrderDetailResponse` |
| Entity | `{TableName PascalCase}` | `OpOrdBase`, `PrCtgBase` |

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
