---
name: db-migrate
description: "vibe-myself DDL을 PostgreSQL에 실제 적용하는 스킬. 'DDL 실행해줘', '테이블 적용해줘', 'DB 마이그레이션', '테이블 생성 실행', 'postgres에 반영해줘' 요청 시 반드시 이 스킬을 사용한다. module-scaffold 오케스트레이터에서 DB 설계 후 자동 실행된다."
---

# DB Migrate Skill

DDL 파일을 읽어 MCP postgres로 실제 테이블을 생성하고 결과를 검증한다.

## 사전 확인

1. `_workspace/02_db_design.md` 존재 여부 확인
   - 없으면: 사용자에게 DDL 파일 경로 또는 내용 요청
2. MCP postgres 연결 확인:
   ```sql
   SELECT current_database(), current_user;
   ```

## 실행 절차

### Step 1: 기존 테이블 존재 확인

```sql
SELECT table_name
FROM information_schema.tables
WHERE table_schema = 'public'
  AND table_name IN ({DDL에서 추출한 테이블명 목록});
```

- 이미 존재하는 테이블: SKIP 목록에 추가, DDL에서 해당 테이블 제외
- 존재하지 않는 테이블: 생성 대상으로 분류

### Step 2: DDL 실행

실행 순서:
1. 독립 테이블 (FK 참조 없는 테이블) 먼저 실행
2. 참조 테이블 (FK 있는 테이블) 이후 실행
3. 인덱스 마지막 실행

각 SQL 문장을 MCP postgres로 순서대로 실행한다.

### Step 3: 검증

생성된 각 테이블에 대해:

```sql
-- 컬럼 목록 확인
SELECT column_name, data_type, is_nullable, column_default
FROM information_schema.columns
WHERE table_name = '{table_name}'
  AND table_schema = 'public'
ORDER BY ordinal_position;

-- PK 확인
SELECT constraint_name
FROM information_schema.table_constraints
WHERE table_name = '{table_name}'
  AND constraint_type = 'PRIMARY KEY';
```

DDL에 명시된 컬럼 수와 실제 컬럼 수 대조.

### Step 4: 결과 기록

`_workspace/02_migrate_result.md` 작성:

```markdown
## 마이그레이션 결과

### 실행 완료
| 테이블명 | 컬럼 수 | PK | 상태 |
|---------|--------|----|----|
| {TABLE} | {N}개 | {PK_COL} | SUCCESS |

### 스킵 (이미 존재)
| 테이블명 | 사유 |
|---------|------|
| {TABLE} | 이미 존재 |

### 실패
| 테이블명 | 에러 메시지 |
|---------|-----------|
| {TABLE} | {error} |

## 수동 실행이 필요한 경우
DDL 파일 위치: /Users/nyj/Documents/git/vibe-myself/_workspace/02_db_design.md
```

## 에러 처리

| 에러 | 처리 |
|------|------|
| 테이블 이미 존재 | SKIP, 결과 파일에 명시 |
| FK 참조 오류 | 참조 대상 테이블 먼저 생성 후 재시도 |
| SQL 문법 오류 | 즉시 중단, 에러 메시지와 함께 수정 요청 |
| MCP 연결 실패 | DDL 파일 경로 안내 후 수동 실행 요청 |