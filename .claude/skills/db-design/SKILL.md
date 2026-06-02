---
name: db-design
description: "vibe-myself DB 테이블 DDL을 설계하는 스킬. '테이블 설계해줘', '테이블 만들어줘', 'DDL 작성', 'DB 스키마 설계', '새 테이블 추가'처럼 데이터베이스 설계를 요청할 때 반드시 이 스킬을 사용한다. module-scaffold 오케스트레이터에서 db-designer 에이전트가 이 가이드를 따른다."
---

# DB Design Skill

vibe-myself 명명 컨벤션을 준수한 PostgreSQL DDL을 생성한다.

## 사전 확인

1. `docs/database/naming-convention.md` Read — 컨벤션 전체 숙지
2. MCP postgres로 기존 테이블 목록 조회 (`SELECT table_name FROM information_schema.tables WHERE table_schema = 'public'`)
3. 연관 기존 entity 파일 Read (`backend/src/main/java/com/vibemyself/entity/`)

## DDL 생성 체크리스트

### 테이블명
- [ ] PREFIX 선택: `ET`(회원) / `PR`(상품) / `CC`(프로모션) / `OP`(주문) / `CS`(고객서비스) / `ST`(시스템) / `IF`(인터페이스)
- [ ] 형식: `{PREFIX}_{도메인약어}_{엔티티약어}` 대문자

### PK
- [ ] 자연키로 유일성 보장 → 해당 컬럼을 PK
- [ ] 복합 자연키 → 복합 PK
- [ ] 자연키 없음 → `BIGINT GENERATED ALWAYS AS IDENTITY`

### 컬럼
- [ ] Postfix 규칙 준수 (`_ID`, `_CD`, `_NM`, `_AMT`, `_QTY`, `_PRC`, `_YN`, `_DT`, `_DTM` 등)
- [ ] 공통 컬럼 4개 포함: `REG_DTM TIMESTAMP NOT NULL DEFAULT NOW()`, `REG_ID VARCHAR(50) NOT NULL`, `MOD_DTM TIMESTAMP NOT NULL DEFAULT NOW()`, `MOD_ID VARCHAR(50) NOT NULL`
- [ ] 상태코드: `VARCHAR(10)` (ST_CODE_DTL 참조값)
- [ ] 금액: `NUMERIC(15,2)`, 단가: `DECIMAL(15,2)`, 수량: `NUMERIC(3,0)`

### 제약조건
- [ ] PK: `CONSTRAINT PK_{TABLE} PRIMARY KEY (...)`
- [ ] FK: `CONSTRAINT FK_{TABLE}_{REF_TABLE} FOREIGN KEY (...)`
- [ ] Unique: `CONSTRAINT UK_{TABLE}_{COLUMN} UNIQUE (...)`
- [ ] Index: `CREATE INDEX IDX_{TABLE}_{COLUMN} ON {TABLE}({COLUMN})`

## Entity 클래스명 제안

테이블명을 PascalCase로 변환:
- `PR_GOODS_BASE` → `PrGoodsBase`
- `OP_ORD_DTL` → `OpOrdDtl`

## 출력 형식

```sql
-- {테이블 설명}
CREATE TABLE {TABLE_NAME} (
    -- PK
    {PK_COLUMN} ...,

    -- 업무 컬럼
    {COLUMN_NAME} {TYPE} {NULL|NOT NULL},
    ...

    -- 공통 컬럼
    REG_DTM  TIMESTAMP   NOT NULL DEFAULT NOW(),
    REG_ID   VARCHAR(50) NOT NULL,
    MOD_DTM  TIMESTAMP   NOT NULL DEFAULT NOW(),
    MOD_ID   VARCHAR(50) NOT NULL,

    CONSTRAINT PK_{TABLE_NAME} PRIMARY KEY ({PK})
);

-- 인덱스
CREATE INDEX IDX_{TABLE}_{COL} ON {TABLE}({COL});
```

설계 결정 근거를 DDL 아래에 별도 섹션으로 작성한다.