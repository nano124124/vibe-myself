# 테이블 설계

---

## CC — 프로모션/이벤트

### CC_PROM_BASE (행사 마스터)

> 쿠폰행사/프로모션행사/사은품행사를 `PROM_TP_CD`로 구분. 유형별 미사용 컬럼은 NULL.

| 컬럼명 | 타입 | NOT NULL | 기본값 | 유형별 사용 | 설명 |
|--------|------|----------|--------|-------------|------|
| `PROM_NO` | `VARCHAR(15)` | Y | | 공통 | PK, 행사번호 |
| `PROM_NM` | `VARCHAR(200)` | Y | | 공통 | 행사명 |
| `PROM_TP_CD` | `VARCHAR(10)` | Y | | 공통 | 행사유형 코드 (쿠폰/프로모션/사은품) |
| `START_DT` | `VARCHAR(8)` | Y | | 공통 | 시작일자 |
| `END_DT` | `VARCHAR(8)` | Y | | 공통 | 종료일자 |
| `USE_YN` | `CHAR(1)` | Y | `'Y'` | 공통 | 사용여부 |
| `DC_TP_CD` | `VARCHAR(10)` | N | | 쿠폰/프로모션 | 할인유형 코드 (정률/정액) |
| `DC_VAL` | `NUMERIC(15,2)` | N | | 쿠폰/프로모션 | 할인값 (정률: %, 정액: 원) |
| `MAX_DC_AMT` | `NUMERIC(15,2)` | N | | 쿠폰/프로모션 | 최대할인금액 (정률 시) |
| `MIN_ORD_AMT` | `NUMERIC(15,2)` | N | | 쿠폰 | 최소주문금액 |
| `ISS_TP_CD` | `VARCHAR(10)` | N | | 쿠폰 | 발급유형 코드 (수동/자동) |
| `AUTO_ISS_COND_CD` | `VARCHAR(10)` | N | | 쿠폰 | 자동발급 조건 코드 |
| `VALID_DD` | `INTEGER` | N | | 쿠폰 | 발급 후 유효기간(일수) |
| `FIX_EXP_DT` | `VARCHAR(8)` | N | | 쿠폰 | 고정 만료일자 |
| `COND_AMT` | `NUMERIC(15,2)` | N | | 사은품 | 증정 조건금액 |
| `GIFT_GOODS_NO` | `VARCHAR(15)` | N | | 사은품 | 사은품 상품번호 |
| `GIFT_QTY` | `NUMERIC(3,0)` | N | | 사은품 | 증정수량 |
| `REG_DTM` | `TIMESTAMP` | Y | `now()` | 공통 | 등록일시 |
| `REG_ID` | `VARCHAR(50)` | Y | | 공통 | 등록자 |
| `MOD_DTM` | `TIMESTAMP` | Y | `now()` | 공통 | 수정일시 |
| `MOD_ID` | `VARCHAR(50)` | Y | | 공통 | 수정자 |

> 쿠폰: `VALID_DD` 또는 `FIX_EXP_DT` 중 하나만 사용
- FK: `GIFT_GOODS_NO` → `PR_GOODS_BASE.GOODS_NO`

---

### CC_PROM_GOODS (행사 대상 상품)

> 프로모션/사은품행사에서 사용. 쿠폰행사는 없을 수 있음.

| 컬럼명 | 타입 | NOT NULL | 기본값 | 설명 |
|--------|------|----------|--------|------|
| `PROM_NO` | `VARCHAR(15)` | Y | | PK(1), FK → CC_PROM_BASE |
| `GOODS_NO` | `VARCHAR(15)` | Y | | PK(2), 대상 상품번호 |
| `REG_DTM` | `TIMESTAMP` | Y | `now()` | 등록일시 |
| `REG_ID` | `VARCHAR(50)` | Y | | 등록자 |

- FK: `PROM_NO` → `CC_PROM_BASE.PROM_NO`

---

### CC_CPN_ISS (쿠폰 발급)

> 쿠폰행사(`PROM_TP_CD = 쿠폰`) 유형만 사용.

| 컬럼명 | 타입 | NOT NULL | 기본값 | 설명 |
|--------|------|----------|--------|------|
| `CPN_ISS_NO` | `VARCHAR(15)` | Y | | PK, 쿠폰발급번호 |
| `PROM_NO` | `VARCHAR(15)` | Y | | FK → CC_PROM_BASE |
| `MBR_NO` | `VARCHAR(15)` | Y | | 회원번호 |
| `EXP_DT` | `VARCHAR(8)` | Y | | 만료일자 (발급 시 계산) |
| `USE_YN` | `CHAR(1)` | Y | `'N'` | 사용여부 |
| `USE_DTM` | `TIMESTAMP` | N | | 사용일시 |
| `ORD_NO` | `VARCHAR(15)` | N | | 사용 주문번호 |
| `REG_DTM` | `TIMESTAMP` | Y | `now()` | 등록일시 |
| `REG_ID` | `VARCHAR(50)` | Y | | 등록자 |
| `MOD_DTM` | `TIMESTAMP` | Y | `now()` | 수정일시 |
| `MOD_ID` | `VARCHAR(50)` | Y | | 수정자 |

- FK: `PROM_NO` → `CC_PROM_BASE.PROM_NO`

---

### CC_EVT_BASE (이벤트/기획전)

| 컬럼명 | 타입 | NOT NULL | 기본값 | 설명 |
|--------|------|----------|--------|------|
| `EVT_NO` | `VARCHAR(15)` | Y | | PK, 이벤트번호 |
| `EVT_NM` | `VARCHAR(200)` | Y | | 이벤트명 |
| `EVT_TP_CD` | `VARCHAR(10)` | Y | | 이벤트유형 코드 (기획전/배너 등) |
| `CONTS` | `TEXT` | N | | 내용 |
| `START_DT` | `VARCHAR(8)` | Y | | 시작일자 |
| `END_DT` | `VARCHAR(8)` | Y | | 종료일자 |
| `DISP_YN` | `CHAR(1)` | Y | `'N'` | 노출여부 |
| `REG_DTM` | `TIMESTAMP` | Y | `now()` | 등록일시 |
| `REG_ID` | `VARCHAR(50)` | Y | | 등록자 |
| `MOD_DTM` | `TIMESTAMP` | Y | `now()` | 수정일시 |
| `MOD_ID` | `VARCHAR(50)` | Y | | 수정자 |

---

### CC_EVT_GOODS (이벤트 적용 상품)

| 컬럼명 | 타입 | NOT NULL | 기본값 | 설명 |
|--------|------|----------|--------|------|
| `EVT_NO` | `VARCHAR(15)` | Y | | PK(1), FK → CC_EVT_BASE |
| `GOODS_NO` | `VARCHAR(15)` | Y | | PK(2), 적용 상품번호 |
| `SORT_ORD` | `INTEGER` | Y | `0` | 정렬순서 |
| `REG_DTM` | `TIMESTAMP` | Y | `now()` | 등록일시 |
| `REG_ID` | `VARCHAR(50)` | Y | | 등록자 |

- FK: `EVT_NO` → `CC_EVT_BASE.EVT_NO`

---

## ET — 회원

### ET_MBR_GRD (회원 등급)

| 컬럼명 | 타입 | NOT NULL | 기본값 | 설명 |
|--------|------|----------|--------|------|
| `GRD_CD` | `VARCHAR(10)` | Y | | PK, 등급코드 |
| `GRD_NM` | `VARCHAR(200)` | Y | | 등급명 |
| `POINT_RATE` | `DECIMAL(4,2)` | Y | `0` | 포인트 적립률 |
| `MILE_RATE` | `DECIMAL(4,2)` | Y | `0` | 마일리지 적립률 |
| `SORT_ORD` | `INTEGER` | Y | `0` | 정렬순서 |
| `USE_YN` | `CHAR(1)` | Y | `'Y'` | 사용여부 |
| `REG_DTM` | `TIMESTAMP` | Y | `now()` | 등록일시 |
| `REG_ID` | `VARCHAR(50)` | Y | | 등록자 |
| `MOD_DTM` | `TIMESTAMP` | Y | `now()` | 수정일시 |
| `MOD_ID` | `VARCHAR(50)` | Y | | 수정자 |

---

### ET_MBR_BASE (회원 기본정보)

| 컬럼명 | 타입 | NOT NULL | 기본값 | 설명 |
|--------|------|----------|--------|------|
| `MBR_NO` | `VARCHAR(15)` | Y | | PK, 회원번호 |
| `LOGIN_ID` | `VARCHAR(30)` | N | | 로그인 아이디 (소셜 전용 회원은 NULL) |
| `LOGIN_PWD` | `VARCHAR(200)` | N | | 비밀번호 BCrypt (소셜 전용 회원은 NULL) |
| `MBR_NM` | `VARCHAR(200)` | Y | | 회원명 |
| `EMAIL` | `VARCHAR(200)` | Y | | 이메일 |
| `TEL_NO` | `VARCHAR(15)` | N | | 연락처 |
| `GRD_CD` | `VARCHAR(10)` | Y | | FK → ET_MBR_GRD, 등급코드 |
| `MBR_STAT_CD` | `VARCHAR(10)` | Y | | 회원상태 코드 (정상/탈퇴/정지) |
| `REG_DTM` | `TIMESTAMP` | Y | `now()` | 등록일시 |
| `REG_ID` | `VARCHAR(50)` | Y | | 등록자 |
| `MOD_DTM` | `TIMESTAMP` | Y | `now()` | 수정일시 |
| `MOD_ID` | `VARCHAR(50)` | Y | | 수정자 |

- FK: `GRD_CD` → `ET_MBR_GRD.GRD_CD`
- UK: `LOGIN_ID`
- UK: `EMAIL`

---

### ET_MBR_SNS (소셜 로그인 연결)

| 컬럼명 | 타입 | NOT NULL | 기본값 | 설명 |
|--------|------|----------|--------|------|
| `MBR_NO` | `VARCHAR(15)` | Y | | PK(1), FK → ET_MBR_BASE |
| `SNS_TP_CD` | `VARCHAR(10)` | Y | | PK(2), SNS 유형 코드 (카카오/네이버/구글) |
| `SNS_ID` | `VARCHAR(100)` | Y | | SNS 고유 ID |
| `REG_DTM` | `TIMESTAMP` | Y | `now()` | 등록일시 |
| `REG_ID` | `VARCHAR(50)` | Y | | 등록자 |
| `MOD_DTM` | `TIMESTAMP` | Y | `now()` | 수정일시 |
| `MOD_ID` | `VARCHAR(50)` | Y | | 수정자 |

- FK: `MBR_NO` → `ET_MBR_BASE.MBR_NO`

---

### ET_MBR_ADDR (회원 배송지)

| 컬럼명 | 타입 | NOT NULL | 기본값 | 설명 |
|--------|------|----------|--------|------|
| `MBR_NO` | `VARCHAR(15)` | Y | | PK(1), FK → ET_MBR_BASE |
| `ADDR_SEQ` | `INTEGER` | Y | | PK(2), 배송지 순번 |
| `ADDR_NM` | `VARCHAR(200)` | Y | | 배송지명 |
| `RCVR_NM` | `VARCHAR(200)` | Y | | 수령자명 |
| `RCVR_TEL` | `VARCHAR(15)` | Y | | 수령자 연락처 |
| `ZIP_CD` | `VARCHAR(10)` | Y | | 우편번호 |
| `ADDR` | `VARCHAR(400)` | Y | | 주소 |
| `ADDR_DTL` | `VARCHAR(400)` | N | | 상세주소 |
| `DEFAULT_YN` | `CHAR(1)` | Y | `'N'` | 기본배송지 여부 |
| `REG_DTM` | `TIMESTAMP` | Y | `now()` | 등록일시 |
| `REG_ID` | `VARCHAR(50)` | Y | | 등록자 |
| `MOD_DTM` | `TIMESTAMP` | Y | `now()` | 수정일시 |
| `MOD_ID` | `VARCHAR(50)` | Y | | 수정자 |

- FK: `MBR_NO` → `ET_MBR_BASE.MBR_NO`

---

### ET_MBR_AST (회원 자산 잔액)

| 컬럼명 | 타입 | NOT NULL | 기본값 | 설명 |
|--------|------|----------|--------|------|
| `MBR_NO` | `VARCHAR(15)` | Y | | PK, FK → ET_MBR_BASE |
| `POINT_AMT` | `NUMERIC(15,2)` | Y | `0` | 포인트 잔액 |
| `MILE_AMT` | `NUMERIC(15,2)` | Y | `0` | 마일리지 잔액 |
| `REG_DTM` | `TIMESTAMP` | Y | `now()` | 등록일시 |
| `REG_ID` | `VARCHAR(50)` | Y | | 등록자 |
| `MOD_DTM` | `TIMESTAMP` | Y | `now()` | 수정일시 |
| `MOD_ID` | `VARCHAR(50)` | Y | | 수정자 |

- FK: `MBR_NO` → `ET_MBR_BASE.MBR_NO`

---

### ET_MBR_AST_HIST (회원 자산 이력)

| 컬럼명 | 타입 | NOT NULL | 기본값 | 설명 |
|--------|------|----------|--------|------|
| `MBR_NO` | `VARCHAR(15)` | Y | | PK(1), FK → ET_MBR_BASE |
| `AST_TP_CD` | `VARCHAR(10)` | Y | | PK(2), 자산유형 코드 (포인트/마일리지) |
| `HIST_SEQ` | `INTEGER` | Y | | PK(3), 이력 순번 |
| `TRNS_TP_CD` | `VARCHAR(10)` | Y | | 거래유형 코드 (적립/사용/소멸/취소) |
| `TRNS_AMT` | `NUMERIC(15,2)` | Y | | 거래금액 |
| `BAL_AMT` | `NUMERIC(15,2)` | Y | | 거래 후 잔액 |
| `ORD_NO` | `VARCHAR(15)` | N | | 관련 주문번호 |
| `EXP_DT` | `VARCHAR(8)` | N | | 만료일자 |
| `REG_DTM` | `TIMESTAMP` | Y | `now()` | 등록일시 |
| `REG_ID` | `VARCHAR(50)` | Y | | 등록자 |

- FK: `MBR_NO` → `ET_MBR_BASE.MBR_NO`

---

## ST — 시스템

### ST_ADMIN_BASE (관리자 기본정보)

| 컬럼명 | 타입 | NOT NULL | 기본값 | 설명 |
|--------|------|----------|--------|------|
| `LOGIN_ID` | `VARCHAR(30)` | Y | | PK, 로그인 아이디 |
| `LOGIN_PWD` | `VARCHAR(200)` | Y | | 비밀번호 (BCrypt) |
| `ADMIN_NM` | `VARCHAR(200)` | Y | | 관리자명 |
| `ROLE_CD` | `VARCHAR(10)` | Y | | 역할코드 (SUPER, ADMIN) |
| `USE_YN` | `CHAR(1)` | Y | `'Y'` | 사용여부 |
| `REG_DTM` | `TIMESTAMP` | Y | `now()` | 등록일시 |
| `REG_ID` | `VARCHAR(50)` | Y | | 등록자 |
| `MOD_DTM` | `TIMESTAMP` | Y | `now()` | 수정일시 |
| `MOD_ID` | `VARCHAR(50)` | Y | | 수정자 |

---

### ST_CODE_GRP (공통코드 그룹)

| 컬럼명 | 타입 | NOT NULL | 기본값 | 설명 |
|--------|------|----------|--------|------|
| `CODE_GRP_CD` | `VARCHAR(10)` | Y | | PK, 그룹코드 |
| `CODE_GRP_NM` | `VARCHAR(200)` | Y | | 그룹명 |
| `USE_YN` | `CHAR(1)` | Y | `'Y'` | 사용여부 |
| `REG_DTM` | `TIMESTAMP` | Y | `now()` | 등록일시 |
| `REG_ID` | `VARCHAR(50)` | Y | | 등록자 |
| `MOD_DTM` | `TIMESTAMP` | Y | `now()` | 수정일시 |
| `MOD_ID` | `VARCHAR(50)` | Y | | 수정자 |

---

### ST_CODE_DTL (공통코드 상세)

| 컬럼명 | 타입 | NOT NULL | 기본값 | 설명 |
|--------|------|----------|--------|------|
| `CODE_GRP_CD` | `VARCHAR(10)` | Y | | PK(1), 그룹코드 (FK → ST_CODE_GRP) |
| `CODE_CD` | `VARCHAR(10)` | Y | | PK(2), 코드값 |
| `CODE_NM` | `VARCHAR(200)` | Y | | 코드명 |
| `SORT_ORD` | `INTEGER` | Y | `0` | 정렬순서 |
| `USE_YN` | `CHAR(1)` | Y | `'Y'` | 사용여부 |
| `REG_DTM` | `TIMESTAMP` | Y | `now()` | 등록일시 |
| `REG_ID` | `VARCHAR(50)` | Y | | 등록자 |
| `MOD_DTM` | `TIMESTAMP` | Y | `now()` | 수정일시 |
| `MOD_ID` | `VARCHAR(50)` | Y | | 수정자 |

- FK: `CODE_GRP_CD` → `ST_CODE_GRP.CODE_GRP_CD`

---

## PR — 상품/전시

### PR_CTG_BASE (전시 카테고리)

| 컬럼명 | 타입 | NOT NULL | 기본값 | 설명 |
|--------|------|----------|--------|------|
| `CTG_NO` | `BIGINT` | Y | IDENTITY | PK |
| `UP_CTG_NO` | `BIGINT` | N | `NULL` | 상위 카테고리 (NULL이면 최상위) |
| `CTG_LVL` | `CHAR(1)` | Y | | 계층 레벨 (1:대, 2:중, 3:소) |
| `CTG_NM` | `VARCHAR(200)` | Y | | 카테고리명 |
| `SORT_ORD` | `INTEGER` | Y | `0` | 정렬순서 |
| `USE_YN` | `CHAR(1)` | Y | `'Y'` | 사용여부 |
| `REG_DTM` | `TIMESTAMP` | Y | `now()` | 등록일시 |
| `REG_ID` | `VARCHAR(50)` | Y | | 등록자 |
| `MOD_DTM` | `TIMESTAMP` | Y | `now()` | 수정일시 |
| `MOD_ID` | `VARCHAR(50)` | Y | | 수정자 |

- FK: `UP_CTG_NO` → `PR_CTG_BASE.CTG_NO`

---

### PR_BRAND_BASE (브랜드)

| 컬럼명 | 타입 | NOT NULL | 기본값 | 설명 |
|--------|------|----------|--------|------|
| `BRAND_NO` | `BIGINT` | Y | IDENTITY | PK |
| `BRAND_NM` | `VARCHAR(200)` | Y | | 브랜드명 |
| `BRAND_IMG_URL` | `VARCHAR(2000)` | N | | 브랜드 이미지 URL |
| `USE_YN` | `CHAR(1)` | Y | `'Y'` | 사용여부 |
| `REG_DTM` | `TIMESTAMP` | Y | `now()` | 등록일시 |
| `REG_ID` | `VARCHAR(50)` | Y | | 등록자 |
| `MOD_DTM` | `TIMESTAMP` | Y | `now()` | 수정일시 |
| `MOD_ID` | `VARCHAR(50)` | Y | | 수정자 |

---

### PR_DLV_POLICY (배송정책)

| 컬럼명 | 타입 | NOT NULL | 기본값 | 설명 |
|--------|------|----------|--------|------|
| `DLV_POLICY_NO` | `VARCHAR(10)` | Y | | PK, 배송정책 코드 |
| `DLV_POLICY_NM` | `VARCHAR(200)` | Y | | 배송정책명 |
| `DLV_TP_CD` | `VARCHAR(10)` | Y | | 배송유형 코드 (무료/유료/조건부) |
| `DLV_AMT` | `NUMERIC(15,2)` | Y | `0` | 배송비 |
| `FREE_COND_AMT` | `NUMERIC(15,2)` | N | | 무료배송 조건금액 (조건부일 때) |
| `RTN_DLV_AMT` | `NUMERIC(15,2)` | Y | `0` | 반품배송비 |
| `USE_YN` | `CHAR(1)` | Y | `'Y'` | 사용여부 |
| `REG_DTM` | `TIMESTAMP` | Y | `now()` | 등록일시 |
| `REG_ID` | `VARCHAR(50)` | Y | | 등록자 |
| `MOD_DTM` | `TIMESTAMP` | Y | `now()` | 수정일시 |
| `MOD_ID` | `VARCHAR(50)` | Y | | 수정자 |

---

### PR_OPT_GRP (옵션 그룹)

| 컬럼명 | 타입 | NOT NULL | 기본값 | 설명 |
|--------|------|----------|--------|------|
| `OPT_GRP_CD` | `VARCHAR(10)` | Y | | PK, 옵션 그룹 코드 |
| `OPT_GRP_NM` | `VARCHAR(200)` | Y | | 옵션 그룹명 (예: 색상, 사이즈) |
| `SORT_ORD` | `INTEGER` | Y | `0` | 정렬순서 |
| `USE_YN` | `CHAR(1)` | Y | `'Y'` | 사용여부 |
| `REG_DTM` | `TIMESTAMP` | Y | `now()` | 등록일시 |
| `REG_ID` | `VARCHAR(50)` | Y | | 등록자 |
| `MOD_DTM` | `TIMESTAMP` | Y | `now()` | 수정일시 |
| `MOD_ID` | `VARCHAR(50)` | Y | | 수정자 |

---

### PR_OPT_ITM (옵션 항목)

| 컬럼명 | 타입 | NOT NULL | 기본값 | 설명 |
|--------|------|----------|--------|------|
| `OPT_GRP_CD` | `VARCHAR(10)` | Y | | PK(1), FK → PR_OPT_GRP |
| `OPT_ITM_CD` | `VARCHAR(10)` | Y | | PK(2), 옵션 항목 코드 |
| `OPT_ITM_NM` | `VARCHAR(200)` | Y | | 옵션 항목명 (예: 빨강, S) |
| `SORT_ORD` | `INTEGER` | Y | `0` | 정렬순서 |
| `USE_YN` | `CHAR(1)` | Y | `'Y'` | 사용여부 |
| `REG_DTM` | `TIMESTAMP` | Y | `now()` | 등록일시 |
| `REG_ID` | `VARCHAR(50)` | Y | | 등록자 |
| `MOD_DTM` | `TIMESTAMP` | Y | `now()` | 수정일시 |
| `MOD_ID` | `VARCHAR(50)` | Y | | 수정자 |

- FK: `OPT_GRP_CD` → `PR_OPT_GRP.OPT_GRP_CD`

---

### PR_GOODS_BASE (상품 기본정보)

| 컬럼명 | 타입 | NOT NULL | 기본값 | 설명 |
|--------|------|----------|--------|------|
| `GOODS_NO` | `VARCHAR(15)` | Y | | PK, 상품번호 |
| `GOODS_NM` | `VARCHAR(200)` | Y | | 상품명 |
| `GOODS_TP_CD` | `VARCHAR(10)` | Y | | 상품유형코드 (일반상품/e쿠폰/사은품 등) |
| `CTG_NO` | `BIGINT` | Y | | FK → PR_CTG_BASE |
| `BRAND_NO` | `BIGINT` | N | | FK → PR_BRAND_BASE |
| `SALE_STAT_CD` | `VARCHAR(10)` | Y | | 판매상태 코드 (판매중/판매중지) |
| `DLV_POLICY_NO` | `VARCHAR(10)` | Y | | FK → PR_DLV_POLICY, 배송정책 |
| `GOODS_DESC` | `TEXT` | N | | 상품 상세 설명 |
| `SALE_START_DTM` | `TIMESTAMP` | N | | 판매시작일시 |
| `SALE_END_DTM` | `TIMESTAMP` | N | | 판매종료일시 |
| `REG_DTM` | `TIMESTAMP` | Y | `now()` | 등록일시 |
| `REG_ID` | `VARCHAR(50)` | Y | | 등록자 |
| `MOD_DTM` | `TIMESTAMP` | Y | `now()` | 수정일시 |
| `MOD_ID` | `VARCHAR(50)` | Y | | 수정자 |

- FK: `CTG_NO` → `PR_CTG_BASE.CTG_NO`
- FK: `BRAND_NO` → `PR_BRAND_BASE.BRAND_NO`
- FK: `DLV_POLICY_NO` → `PR_DLV_POLICY.DLV_POLICY_NO`

---

### PR_GOODS_PRC (상품 가격 선분)

| 컬럼명 | 타입 | NOT NULL | 기본값 | 설명 |
|--------|------|----------|--------|------|
| `GOODS_NO` | `VARCHAR(15)` | Y | | PK(1), FK → PR_GOODS_BASE |
| `APLY_FROM_DT` | `VARCHAR(8)` | Y | | PK(2), 적용시작일자 (yyyyMMdd) |
| `APLY_TO_DT` | `VARCHAR(8)` | Y | | 적용종료일자 (yyyyMMdd, 현재가격: `99991231`) |
| `SALE_PRC` | `NUMERIC(15,2)` | Y | | 판매가 |
| `NORM_PRC` | `NUMERIC(15,2)` | N | | 정상가 |
| `SUPLY_PRC` | `NUMERIC(15,2)` | N | | 공급원가 |
| `MRGN_RATE` | `DECIMAL(4,2)` | N | | 마진율 (%) |
| `REG_DTM` | `TIMESTAMP` | Y | `now()` | 등록일시 |
| `REG_ID` | `VARCHAR(50)` | Y | | 등록자 |
| `MOD_DTM` | `TIMESTAMP` | Y | `now()` | 수정일시 |
| `MOD_ID` | `VARCHAR(50)` | Y | | 수정자 |

- FK: `GOODS_NO` → `PR_GOODS_BASE.GOODS_NO`
- INDEX: `IDX_PR_GOODS_PRC_DT` (`GOODS_NO`, `APLY_FROM_DT`, `APLY_TO_DT`)
- 현재 유효 가격 조건: `APLY_FROM_DT <= TODAY AND APLY_TO_DT >= TODAY`

---

### PR_GOODS_IMG (상품 이미지)

| 컬럼명 | 타입 | NOT NULL | 기본값 | 설명 |
|--------|------|----------|--------|------|
| `GOODS_NO` | `VARCHAR(15)` | Y | | PK(1), FK → PR_GOODS_BASE |
| `IMG_SEQ` | `INTEGER` | Y | | PK(2), 이미지 순번 |
| `IMG_URL` | `VARCHAR(2000)` | Y | | 이미지 URL |
| `SORT_ORD` | `INTEGER` | Y | `0` | 정렬순서 |
| `REG_DTM` | `TIMESTAMP` | Y | `now()` | 등록일시 |
| `REG_ID` | `VARCHAR(50)` | Y | | 등록자 |
| `MOD_DTM` | `TIMESTAMP` | Y | `now()` | 수정일시 |
| `MOD_ID` | `VARCHAR(50)` | Y | | 수정자 |

- FK: `GOODS_NO` → `PR_GOODS_BASE.GOODS_NO`

---

### PR_GOODS_OPT (상품-옵션 그룹 연결)

| 컬럼명 | 타입 | NOT NULL | 기본값 | 설명 |
|--------|------|----------|--------|------|
| `GOODS_NO` | `VARCHAR(15)` | Y | | PK(1), FK → PR_GOODS_BASE |
| `OPT_GRP_CD` | `VARCHAR(10)` | Y | | PK(2), FK → PR_OPT_GRP |
| `SORT_ORD` | `INTEGER` | Y | `0` | 정렬순서 |
| `REG_DTM` | `TIMESTAMP` | Y | `now()` | 등록일시 |
| `REG_ID` | `VARCHAR(50)` | Y | | 등록자 |
| `MOD_DTM` | `TIMESTAMP` | Y | `now()` | 수정일시 |
| `MOD_ID` | `VARCHAR(50)` | Y | | 수정자 |

- FK: `GOODS_NO` → `PR_GOODS_BASE.GOODS_NO`
- FK: `OPT_GRP_CD` → `PR_OPT_GRP.OPT_GRP_CD`

---

### PR_UNIT_BASE (단품)

| 컬럼명 | 타입 | NOT NULL | 기본값 | 설명 |
|--------|------|----------|--------|------|
| `GOODS_NO` | `VARCHAR(15)` | Y | | PK(1), FK → PR_GOODS_BASE |
| `UNIT_SEQ` | `INTEGER` | Y | | PK(2), 단품 순번 |
| `ADD_PRC` | `NUMERIC(15,2)` | Y | `0` | 추가 가격 |
| `STOCK_QTY` | `INTEGER` | Y | `0` | 재고 수량 |
| `USE_YN` | `CHAR(1)` | Y | `'Y'` | 사용여부 |
| `REG_DTM` | `TIMESTAMP` | Y | `now()` | 등록일시 |
| `REG_ID` | `VARCHAR(50)` | Y | | 등록자 |
| `MOD_DTM` | `TIMESTAMP` | Y | `now()` | 수정일시 |
| `MOD_ID` | `VARCHAR(50)` | Y | | 수정자 |

- FK: `GOODS_NO` → `PR_GOODS_BASE.GOODS_NO`

---

### PR_UNIT_OPT (단품-옵션 항목 매핑)

| 컬럼명 | 타입 | NOT NULL | 기본값 | 설명 |
|--------|------|----------|--------|------|
| `GOODS_NO` | `VARCHAR(15)` | Y | | PK(1), FK → PR_UNIT_BASE |
| `UNIT_SEQ` | `INTEGER` | Y | | PK(2), FK → PR_UNIT_BASE |
| `OPT_GRP_CD` | `VARCHAR(10)` | Y | | PK(3), FK → PR_OPT_ITM |
| `OPT_ITM_CD` | `VARCHAR(10)` | Y | | 옵션 항목 코드, FK → PR_OPT_ITM |
| `REG_DTM` | `TIMESTAMP` | Y | `now()` | 등록일시 |
| `REG_ID` | `VARCHAR(50)` | Y | | 등록자 |

- FK: `(GOODS_NO, UNIT_SEQ)` → `PR_UNIT_BASE.(GOODS_NO, UNIT_SEQ)`
- FK: `(OPT_GRP_CD, OPT_ITM_CD)` → `PR_OPT_ITM.(OPT_GRP_CD, OPT_ITM_CD)`

---

### PR_GOODS_TAG (상품 태그)

| 컬럼명 | 타입 | NOT NULL | 기본값 | 설명 |
|--------|------|----------|--------|------|
| `GOODS_NO` | `VARCHAR(15)` | Y | | PK(1), FK → PR_GOODS_BASE |
| `TAG_SEQ` | `INTEGER` | Y | | PK(2), 태그 순번 |
| `TAG_NM` | `VARCHAR(100)` | Y | | 태그명 |
| `REG_DTM` | `TIMESTAMP` | Y | `now()` | 등록일시 |
| `REG_ID` | `VARCHAR(50)` | Y | | 등록자 |
| `MOD_DTM` | `TIMESTAMP` | Y | `now()` | 수정일시 |
| `MOD_ID` | `VARCHAR(50)` | Y | | 수정자 |

- FK: `GOODS_NO` → `PR_GOODS_BASE.GOODS_NO`

---

### PR_DISP_BASE (전시 설정)

| 컬럼명 | 타입 | NOT NULL | 기본값 | 설명 |
|--------|------|----------|--------|------|
| `GOODS_NO` | `VARCHAR(15)` | Y | | PK, FK → PR_GOODS_BASE |
| `DISP_YN` | `CHAR(1)` | Y | `'N'` | 노출여부 |
| `SORT_ORD` | `INTEGER` | Y | `0` | 전시 순서 |
| `REG_DTM` | `TIMESTAMP` | Y | `now()` | 등록일시 |
| `REG_ID` | `VARCHAR(50)` | Y | | 등록자 |
| `MOD_DTM` | `TIMESTAMP` | Y | `now()` | 수정일시 |
| `MOD_ID` | `VARCHAR(50)` | Y | | 수정자 |

- FK: `GOODS_NO` → `PR_GOODS_BASE.GOODS_NO`

---

## OP — 주문/클레임/장바구니

### OP_CART (장바구니)

| 컬럼명 | 타입 | NOT NULL | 기본값 | 설명 |
|--------|------|----------|--------|------|
| `CART_NO` | `VARCHAR(15)` | Y | | PK, 장바구니 번호 |
| `MBR_NO` | `VARCHAR(15)` | Y | | 회원번호 |
| `GOODS_NO` | `VARCHAR(15)` | Y | | 상품번호 |
| `UNIT_SEQ` | `INTEGER` | Y | | 단품 순번 |
| `CART_QTY` | `NUMERIC(3,0)` | Y | | 수량 |
| `REG_DTM` | `TIMESTAMP` | Y | `now()` | 등록일시 |
| `REG_ID` | `VARCHAR(50)` | Y | | 등록자 |
| `MOD_DTM` | `TIMESTAMP` | Y | `now()` | 수정일시 |
| `MOD_ID` | `VARCHAR(50)` | Y | | 수정자 |

---

> 배송지는 ET 모듈(ET_MBR_ADDR)에서 관리한다.

### OP_ORD_BASE (주문 기본)

> 주문 식별 정보만 관리한다. 상태/가격은 OP_ORD_GOODS에서, 배송정보는 OP_DLV_INFO에서 관리한다.

| 컬럼명 | 타입 | NOT NULL | 기본값 | 설명 |
|--------|------|----------|--------|------|
| `ORD_NO` | `VARCHAR(15)` | Y | | PK, 주문번호 |
| `MBR_NO` | `VARCHAR(15)` | Y | | 회원번호 |
| `REG_DTM` | `TIMESTAMP` | Y | `now()` | 등록일시 |
| `REG_ID` | `VARCHAR(50)` | Y | | 등록자 |
| `MOD_DTM` | `TIMESTAMP` | Y | `now()` | 수정일시 |
| `MOD_ID` | `VARCHAR(50)` | Y | | 수정자 |

---

### OP_DLV_INFO (배송정보)

| 컬럼명 | 타입 | NOT NULL | 기본값 | 설명 |
|--------|------|----------|--------|------|
| `ORD_NO` | `VARCHAR(15)` | Y | | PK, FK → OP_ORD_BASE |
| `RCVR_NM` | `VARCHAR(200)` | Y | | 수령자명 (스냅샷) |
| `RCVR_TEL` | `VARCHAR(15)` | Y | | 수령자 연락처 (스냅샷) |
| `ZIP_CD` | `VARCHAR(10)` | Y | | 우편번호 (스냅샷) |
| `ADDR` | `VARCHAR(400)` | Y | | 주소 (스냅샷) |
| `ADDR_DTL` | `VARCHAR(400)` | N | | 상세주소 (스냅샷) |
| `DLV_MSG` | `VARCHAR(200)` | N | | 배송 메모 |
| `INVOICE_NO` | `VARCHAR(50)` | N | | 송장번호 |
| `REG_DTM` | `TIMESTAMP` | Y | `now()` | 등록일시 |
| `REG_ID` | `VARCHAR(50)` | Y | | 등록자 |
| `MOD_DTM` | `TIMESTAMP` | Y | `now()` | 수정일시 |
| `MOD_ID` | `VARCHAR(50)` | Y | | 수정자 |

- FK: `ORD_NO` → `OP_ORD_BASE.ORD_NO`

---

### OP_ORD_GOODS (주문상품)

> 주문/클레임/철회를 처리순번(PROC_SEQ) row로 함께 관리한다.
> PROC_SEQ=1이 최초 주문, 이후 클레임/철회가 추가 row로 쌓인다.

| 컬럼명 | 타입 | NOT NULL | 기본값 | 설명 |
|--------|------|----------|--------|------|
| `ORD_NO` | `VARCHAR(15)` | Y | | PK(1), FK → OP_ORD_BASE |
| `ORD_GOODS_SEQ` | `INTEGER` | Y | | PK(2), 주문상품 순번 |
| `PROC_SEQ` | `INTEGER` | Y | | PK(3), 처리순번 (1=주문, 2+=클레임/철회) |
| `GOODS_NO` | `VARCHAR(15)` | Y | | 상품번호 (스냅샷) |
| `UNIT_SEQ` | `INTEGER` | Y | | 단품 순번 (스냅샷) |
| `GOODS_NM` | `VARCHAR(200)` | Y | | 상품명 (스냅샷) |
| `UNIT_PRC` | `NUMERIC(15,2)` | Y | | 단품 가격 (스냅샷) |
| `DLV_POLICY_NO` | `VARCHAR(10)` | Y | | 배송정책 코드 (스냅샷, FK 없음) |
| `ORD_QTY` | `NUMERIC(3,0)` | Y | `0` | 주문수량 |
| `CNCL_QTY` | `NUMERIC(3,0)` | Y | `0` | 취소수량 |
| `EXCH_QTY` | `NUMERIC(3,0)` | Y | `0` | 교환수량 |
| `RTN_QTY` | `NUMERIC(3,0)` | Y | `0` | 반품수량 |
| `ORD_GOODS_STAT_CD` | `VARCHAR(10)` | Y | | 주문상품 상태 코드 (주문/취소/교환/반품) |
| `DLV_STAT_CD` | `VARCHAR(10)` | N | | 배송 상태 코드 (주문완료→구매확정, 정상 주문만 사용) |
| `CLAIM_RSN` | `VARCHAR(400)` | N | | 클레임 사유 |
| `REG_DTM` | `TIMESTAMP` | Y | `now()` | 등록일시 |
| `REG_ID` | `VARCHAR(50)` | Y | | 등록자 |
| `MOD_DTM` | `TIMESTAMP` | Y | `now()` | 수정일시 |
| `MOD_ID` | `VARCHAR(50)` | Y | | 수정자 |

- FK: `ORD_NO` → `OP_ORD_BASE.ORD_NO`

---

### OP_DLV_FEE (배송비)

> 주문배송비/클레임배송비를 row로 함께 관리한다. 배송정책별 배송비가 여러 건 존재할 수 있다.

| 컬럼명 | 타입 | NOT NULL | 기본값 | 설명 |
|--------|------|----------|--------|------|
| `ORD_NO` | `VARCHAR(15)` | Y | | PK(1), FK → OP_ORD_BASE |
| `PROC_SEQ` | `INTEGER` | Y | | PK(2), 처리순번 |
| `FEE_TP_CD` | `VARCHAR(10)` | Y | | PK(3), 배송비 유형 코드 (초도배송비/교환배송비/반품배송비) |
| `DLV_POLICY_NO` | `VARCHAR(10)` | Y | | FK → PR_DLV_POLICY |
| `ORD_GOODS_SEQ` | `INTEGER` | N | | 주문상품 순번 (클레임 배송비 시 참조) |
| `DLV_AMT` | `NUMERIC(15,2)` | Y | | 배송비 |
| `REG_DTM` | `TIMESTAMP` | Y | `now()` | 등록일시 |
| `REG_ID` | `VARCHAR(50)` | Y | | 등록자 |
| `MOD_DTM` | `TIMESTAMP` | Y | `now()` | 수정일시 |
| `MOD_ID` | `VARCHAR(50)` | Y | | 수정자 |

- FK: `ORD_NO` → `OP_ORD_BASE.ORD_NO`
- FK: `DLV_POLICY_NO` → `PR_DLV_POLICY.DLV_POLICY_NO`

---

### OP_PROM_APPL (프로모션/쿠폰 적용)

> 주문/클레임 프로모션·쿠폰 적용 내역을 row로 함께 관리한다.

| 컬럼명 | 타입 | NOT NULL | 기본값 | 설명 |
|--------|------|----------|--------|------|
| `ORD_NO` | `VARCHAR(15)` | Y | | PK(1), FK → OP_ORD_BASE |
| `PROM_APPL_SEQ` | `INTEGER` | Y | | PK(2), 적용 순번 |
| `APPL_TP_CD` | `VARCHAR(10)` | Y | | 적용구분 코드 (주문/클레임) |
| `PROM_KIND_CD` | `VARCHAR(10)` | Y | | 종류 코드 (프로모션/쿠폰) |
| `PROM_NO` | `VARCHAR(15)` | N | | 프로모션번호 |
| `COUPON_NO` | `VARCHAR(15)` | N | | 쿠폰번호 |
| `ORD_GOODS_SEQ` | `INTEGER` | N | | 주문상품 순번 (상품별 적용 시) |
| `PROC_SEQ` | `INTEGER` | N | | 처리순번 (클레임 시) |
| `DC_AMT` | `NUMERIC(15,2)` | Y | | 할인금액 |
| `REG_DTM` | `TIMESTAMP` | Y | `now()` | 등록일시 |
| `REG_ID` | `VARCHAR(50)` | Y | | 등록자 |

- FK: `ORD_NO` → `OP_ORD_BASE.ORD_NO`

---

### OP_PAY_BASE (결제)

| 컬럼명 | 타입 | NOT NULL | 기본값 | 설명 |
|--------|------|----------|--------|------|
| `ORD_NO` | `VARCHAR(15)` | Y | | PK, FK → OP_ORD_BASE |
| `PAY_MTHD_CD` | `VARCHAR(10)` | Y | | 결제수단 코드 (카드/무통장) |
| `PAY_STAT_CD` | `VARCHAR(10)` | Y | | 결제상태 코드 |
| `PAY_AMT` | `NUMERIC(15,2)` | Y | | 결제금액 |
| `PAY_DTM` | `TIMESTAMP` | N | | 결제일시 |
| `REG_DTM` | `TIMESTAMP` | Y | `now()` | 등록일시 |
| `REG_ID` | `VARCHAR(50)` | Y | | 등록자 |
| `MOD_DTM` | `TIMESTAMP` | Y | `now()` | 수정일시 |
| `MOD_ID` | `VARCHAR(50)` | Y | | 수정자 |

- FK: `ORD_NO` → `OP_ORD_BASE.ORD_NO`

---

### OP_PAY_HIST (결제 이력)

| 컬럼명 | 타입 | NOT NULL | 기본값 | 설명 |
|--------|------|----------|--------|------|
| `ORD_NO` | `VARCHAR(15)` | Y | | PK(1), FK → OP_PAY_BASE |
| `PAY_SEQ` | `INTEGER` | Y | | PK(2), 결제 이력 순번 |
| `PAY_TP_CD` | `VARCHAR(10)` | Y | | 처리유형 코드 (결제/취소) |
| `REQ_AMT` | `NUMERIC(15,2)` | Y | | 요청금액 |
| `PG_TXN_NO` | `VARCHAR(50)` | N | | PG 거래번호 |
| `PG_APPV_NO` | `VARCHAR(50)` | N | | PG 승인번호 |
| `RES_CD` | `VARCHAR(10)` | N | | PG 응답코드 |
| `RES_MSG` | `VARCHAR(200)` | N | | PG 응답메시지 |
| `PG_REQ_DTM` | `TIMESTAMP` | N | | PG 요청일시 |
| `PG_RES_DTM` | `TIMESTAMP` | N | | PG 응답일시 |
| `REG_DTM` | `TIMESTAMP` | Y | `now()` | 등록일시 |
| `REG_ID` | `VARCHAR(50)` | Y | | 등록자 |

- FK: `ORD_NO` → `OP_PAY_BASE.ORD_NO`

---

### OP_PAY_DIST (결제배분)

> 결제수단별 결제금액을 주문상품 단위로 배분한다. 배분 공식은 추후 정의한다.

| 컬럼명 | 타입 | NOT NULL | 기본값 | 설명 |
|--------|------|----------|--------|------|
| `ORD_NO` | `VARCHAR(15)` | Y | | PK(1), FK → OP_ORD_BASE |
| `ORD_GOODS_SEQ` | `INTEGER` | Y | | PK(2), 주문상품 순번 |
| `PROC_SEQ` | `INTEGER` | Y | | PK(3), 처리순번 |
| `PAY_MTHD_CD` | `VARCHAR(10)` | Y | | PK(4), 결제수단 코드 |
| `DIST_AMT` | `NUMERIC(15,2)` | Y | | 배분금액 |
| `REG_DTM` | `TIMESTAMP` | Y | `now()` | 등록일시 |
| `REG_ID` | `VARCHAR(50)` | Y | | 등록자 |

- FK: `ORD_NO` → `OP_ORD_BASE.ORD_NO`
