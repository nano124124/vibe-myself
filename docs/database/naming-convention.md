# Database 네이밍 규칙

---

## 1. 테이블 명명 규칙

- 모두 대문자, 단어 구분은 언더스코어(`_`)
- 형식: `{주제영역PREFIX}_{도메인}_{엔티티}`

### 1.1 주제영역 정의

| 주제영역 | PREFIX | 설명 |
|---------|--------|------|
| 회원/협력사 | `ET` | 회원, 협력사, 파트너, 공급사, 고객사 관련 |
| 상품/전시 | `PR` | 상품, 카테고리, 브랜드, 전시, 진열, 재고 관련 |
| 고객접촉 | `CC` | 프로모션, 쿠폰, 이벤트, 마케팅, 알림, 캠페인, 배너 관련 |
| 주문/클레임 | `OP` | 주문, 결제, 배송, 클레임, 반품, 교환, 취소, 정산 관련 |
| 고객서비스 | `CS` | 상담, 문의, 리뷰, 평가, 보상, QA 관련 |
| 시스템 | `ST` | 사용자, 권한, 코드, 메뉴, 설정, 로그, 관리자, 조직 관련 |
| 인터페이스 | `IF` | 외부 연동, API, 배치, 스케줄 관련 |

### 1.2 주제영역 키워드 사전

주제영역을 명시하지 않은 경우, 테이블명의 키워드를 아래 사전과 매칭하여 가장 적합한 주제영역을 추천한다.

| 주제영역 | PREFIX | 매칭 키워드 |
|---------|--------|------------|
| 회원/협력사 | `ET` | 회원, 협력사, 파트너, 멤버, 공급사, 고객사 |
| 상품/전시 | `PR` | 상품, 제품, 카테고리, 브랜드, 전시, 진열, 재고 |
| 고객접촉 | `CC` | 프로모션, 쿠폰, 이벤트, 마케팅, 알림, 메시지, 캠페인, 혜택, 배너 |
| 주문/클레임 | `OP` | 주문, 결제, 배송, 클레임, 반품, 교환, 취소, 정산, 수단, 배분 |
| 고객서비스 | `CS` | 상담, 문의, 리뷰, 평가, 보상, QA, 답변 |
| 시스템 | `ST` | 사용자, 권한, 코드, 메뉴, 설정, 로그, 관리자, 조직, 부서 |
| 인터페이스 | `IF` | 연동, 인터페이스, API, 배치, 스케줄, 외부 |

**추천 알고리즘**
1. 테이블명·설명에 포함된 핵심 키워드를 위 사전과 매칭
2. 가장 많은 키워드가 매칭된 주제영역을 우선 선택
3. 동점인 경우 위 표의 순서(`ET → PR → CC → OP → CS → ST → IF`)를 따름

### 1.3 명명 예시

| 테이블 설명 | 매칭 키워드 | PREFIX | 테이블명 |
|------------|------------|--------|---------|
| 회원 기본정보 | 회원 → `ET` | `ET` | `ET_MBR_BASE` |
| 상품 카테고리 | 상품, 카테고리 → `PR` | `PR` | `PR_GOODS_CTG` |
| 쿠폰 발급 이력 | 쿠폰 → `CC` | `CC` | `CC_CPN_ISS_HIST` |
| 주문 상세 | 주문 → `OP` | `OP` | `OP_ORD_DTL` |
| 상담 문의 | 상담, 문의 → `CS` | `CS` | `CS_CNSL_INQ` |
| 권한 코드 | 권한, 코드 → `ST` | `ST` | `ST_AUTH_CD` |
| 외부 API 연동 로그 | 연동, API → `IF` | `IF` | `IF_API_LOG` |

❌ **잘못된 예시**
```
category           -- 소문자, PREFIX 없음
dispCategory       -- 카멜케이스
pr_disp_ctg_base   -- 소문자
ET_memberBase      -- 혼용
```

✅ **올바른 예시**
```
PR_GOODS_CTG_BASE
ET_MBR_BASE
OP_ORD_DTL
```

---

## 2. 컬럼 명명 규칙

- 모두 대문자, 단어 구분은 언더스코어(`_`)
- 의미를 명확히 전달하는 이름 사용, 과도한 약어 지양
- 동일한 의미의 컬럼은 프로젝트 전체에서 동일한 이름 사용

### 2.1 표준 컬럼명

자주 사용되는 컬럼은 아래 표준 명칭을 반드시 따른다.

| 의미 | 표준 컬럼명 | 타입 |
|------|------------|------|
| 사용여부 | `USE_YN` | `CHAR(1)` |
| 정렬순서 | `SORT_ORD` | `INTEGER` |
| 등록일시 | `REG_DTM` | `TIMESTAMP` |
| 수정일시 | `MOD_DTM` | `TIMESTAMP` |
| 등록자ID | `REG_ID` | `VARCHAR(50)` |
| 수정자ID | `MOD_ID` | `VARCHAR(50)` |
| 사이트번호 | `SITE_NO` | `BIGINT` |
| 삭제여부 | `DEL_YN` | `CHAR(1)` |
| 노출여부 | `DISP_YN` | `CHAR(1)` |

### 2.2 Postfix 사용 방법

컬럼명 끝에 붙는 Postfix는 해당 컬럼의 도메인을 나타낸다. 도메인에 따라 데이터 타입과 길이가 결정된다.

| Postfix | 도메인 | 데이터타입 | 예시 |
|---------|--------|-----------|------|
| `~_ID` | 아이디 | `VARCHAR(30)` | `USER_ID VARCHAR(30)` |
| `~_CD` | 코드 | `VARCHAR(10)` | `STAT_CD VARCHAR(10)` |
| `~_NM` | 명칭 | `VARCHAR(200)` | `USER_NM VARCHAR(200)` |
| `~_DESC` | 설명 | `VARCHAR(4000)` | `PROD_DESC VARCHAR(4000)` |
| `~_CONTS` | 내용 | `VARCHAR(4000)` | `NOTI_CONTS VARCHAR(4000)` |
| `~_NO` | 번호 | `VARCHAR(15)` | `ORD_NO VARCHAR(15)` |
| `~_AMT` | 금액 | `NUMERIC(15,2)` | `PAY_AMT NUMERIC(15,2)` |
| `~_QTY` | 수량 | `NUMERIC(3,0)` | `ORD_QTY NUMERIC(3,0)` |
| `~_PRC` | 단가 | `DECIMAL(15,2)` | `SALE_PRC DECIMAL(15,2)` |
| `~_DT` | 일자 | `VARCHAR(8)` | `ORD_DT VARCHAR(8)` |
| `~_DTM` | 일시 | `TIMESTAMP` | `REG_DTM TIMESTAMP` |
| `~_YN` | 여부 | `CHAR(1)` | `ACTV_YN CHAR(1)` |
| `~_ADDR` | 주소 | `VARCHAR(400)` | `DLV_ADDR VARCHAR(400)` |
| `~_URL` | URL | `VARCHAR(2000)` | `IMG_URL VARCHAR(2000)` |
| `~_MSG` | 메시지 | `VARCHAR(4000)` | `ERR_MSG VARCHAR(4000)` |
| `~_RATE` | 비율 | `DECIMAL(4,2)` | `DC_RATE DECIMAL(4,2)` |
| `~_SEQ` | 순번 | `VARCHAR(15)` | `DLVP_SEQ VARCHAR(15)` |
| `~_DTL` | 상세 | `VARCHAR(100)` | `GOODS_DTL VARCHAR(100)` |

---

## 3. PK

- 자연키로 유일성이 보장되면 자연키 또는 복합 PK를 우선 사용한다
- 자연키가 없거나 부적합할 때만 `BIGINT GENERATED ALWAYS AS IDENTITY`를 사용한다

| 경우 | PK 방식 | 예시 |
|------|---------|------|
| 단일 자연키 | 해당 컬럼을 PK | `ST_ADMIN_BASE.LOGIN_ID` |
| 복합 자연키 | 복합 PK | `ST_CODE_DTL.(CODE_GRP_CD, CODE_CD)` |
| 자연키 없음 | IDENTITY 시퀀스 | `OP_ORD_BASE.ORD_ID BIGINT GENERATED ALWAYS AS IDENTITY` |

---

## 4. 공통 컬럼

모든 테이블에 반드시 포함해야 하는 컬럼이다.

```sql
REG_DTM  TIMESTAMP    NOT NULL DEFAULT NOW(),  -- 등록일시
REG_ID   VARCHAR(50)  NOT NULL,                -- 등록자
MOD_DTM  TIMESTAMP    NOT NULL DEFAULT NOW(),  -- 수정일시
MOD_ID   VARCHAR(50)  NOT NULL                 -- 수정자
```

---

## 5. 제약 조건 네이밍

| 종류 | 형식 | 예시 |
|------|------|------|
| PK | `PK_{TABLE}` | `PK_OP_ORD_BASE` |
| FK | `FK_{TABLE}_{REF_TABLE}` | `FK_OP_ORD_DTL_OP_ORD_BASE` |
| Unique | `UK_{TABLE}_{COLUMN}` | `UK_ET_MBR_BASE_EMAIL` |
| Index | `IDX_{TABLE}_{COLUMN}` | `IDX_OP_ORD_BASE_MBR_ID` |
