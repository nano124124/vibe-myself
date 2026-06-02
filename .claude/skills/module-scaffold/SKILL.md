---
name: module-scaffold
description: "vibe-myself 풀스택 모듈 스캐폴딩 오케스트레이터. 'order 목록 API 만들어줘', 'cart 모듈 추가해줘', '상품 상세 페이지 만들어줘', '주문 등록 구현해줘'처럼 새 기능/모듈 구현을 요청할 때 반드시 이 스킬을 사용한다. 후속 작업: 이전 결과 수정, 부분 재실행, BE/FE만 다시, 업데이트, 보완 요청 시에도 반드시 이 스킬을 사용한다."
---

# Module Scaffold Orchestrator

DB 설계 → DB 마이그레이션 → BE 레이어 → FE 레이어 → QA 검증의 풀스택 파이프라인을 자동화한다.

## 실행 모드: 서브 에이전트 (순차 파이프라인)

| Phase | 에이전트 | subagent_type | model | 입력 | 출력 |
|-------|---------|--------------|-------|------|------|
| Phase 2 | db-designer | `general-purpose` | opus | `_workspace/00_input.md` | `_workspace/02_db_design.md` |
| Phase 2.5 | db-migrator | `general-purpose` | opus | `_workspace/02_db_design.md` | `_workspace/02_migrate_result.md` |
| Phase 3 | backend-scaffolder | `general-purpose` | opus | `_workspace/02_db_design.md` | `_workspace/03_backend_spec.md` + 소스 |
| Phase 4 | frontend-scaffolder | `general-purpose` | opus | `_workspace/03_backend_spec.md` | `_workspace/03_frontend_spec.md` + 소스 |
| Phase 5 | qa-reviewer | `general-purpose` | opus | `_workspace/03_*` + 소스 | `_workspace/04_qa_report.md` |

## 워크플로우

### Phase 0: 프로젝트 루트 확인 + 컨텍스트 확인

```bash
# 프로젝트 루트 동적 확인 — 환경에 무관하게 동작
PROJECT_ROOT=$(pwd)
WORKSPACE="$PROJECT_ROOT/_workspace"
```

`$WORKSPACE` 존재 여부 확인:
- **미존재** → 초기 실행. Phase 1로 진행
- **존재 + 부분 수정 요청** ("BE만 다시", "FE만 수정") → 해당 Phase만 재호출, 기존 파일 보존
- **존재 + 새 기능 요청** → 기존 폴더를 `_workspace_{YYYYMMDD_HHMMSS}/`로 이동 후 Phase 1 진행

이후 모든 Phase에서 `$PROJECT_ROOT`, `$WORKSPACE`를 Agent 프롬프트에 전달한다.

### Phase 1: 입력 파싱

`$WORKSPACE/00_input.md` 작성:

```markdown
## 요청 요약
{사용자 요청 원문}

## 모듈
{module}: goods | order | cart | member | prom | system

## 구현 대상
- 기능: {목록 조회 | 상세 조회 | 등록 | 수정 | 삭제}
- 레이어: {ALL | DB만 | BE만 | FE만}
- 화면 유형: {어드민 | 쇼핑몰}

## 엔티티 설명
{필요한 테이블/엔티티 설명}

## 추가 요구사항
{검색 조건, 페이지네이션, 특이사항 등}
```

`레이어` 값에 따라 실행할 Phase를 결정한다:
- `ALL` → Phase 2, 2.5, 3, 4, 5 모두
- `DB만` → Phase 2, 2.5
- `BE만` → Phase 3만 (`_workspace/02_db_design.md` 또는 기존 Entity 파일로 대체)
- `FE만` → Phase 4만 (`_workspace/03_backend_spec.md` 또는 기존 API 파일로 대체)

### Phase 2: DB 설계

```
Agent(
  subagent_type: "general-purpose",
  model: "opus",
  prompt: "
    {PROJECT_ROOT}/.claude/agents/db-designer.md 를 Read하여 역할과 원칙을 숙지한다.
    {PROJECT_ROOT}/.claude/skills/db-design/SKILL.md 를 Read하여 절차를 따른다.
    {WORKSPACE}/00_input.md 를 읽고 DDL을 설계한다.
    산출물: {WORKSPACE}/02_db_design.md
  "
)
```

### Phase 2.5: DB 마이그레이션

Phase 2 완료 후 실행:

```
Agent(
  subagent_type: "general-purpose",
  model: "opus",
  prompt: "
    {PROJECT_ROOT}/.claude/agents/db-migrator.md 를 Read하여 역할과 원칙을 숙지한다.
    {PROJECT_ROOT}/.claude/skills/db-migrate/SKILL.md 를 Read하여 절차를 따른다.
    {WORKSPACE}/02_db_design.md 의 DDL을 MCP postgres에 실행한다.
    산출물: {WORKSPACE}/02_migrate_result.md
  "
)
```

`$WORKSPACE/02_migrate_result.md`에 FAILED 항목이 있으면 사용자에게 알리고 계속 진행 여부를 확인한다.

### Phase 3: BE 레이어 생성

Phase 2.5 완료 후 실행:

```
Agent(
  subagent_type: "general-purpose",
  model: "opus",
  prompt: "
    {PROJECT_ROOT}/.claude/agents/backend-scaffolder.md 를 Read하여 역할과 원칙을 숙지한다.
    {PROJECT_ROOT}/.claude/skills/backend-layer/SKILL.md 를 Read하여 절차를 따른다.
    {WORKSPACE}/00_input.md, {WORKSPACE}/02_db_design.md 를 읽고 BE 레이어를 생성한다.
    프로젝트 루트: {PROJECT_ROOT}
    산출물: {WORKSPACE}/03_backend_spec.md + 실제 소스 파일
  "
)
```

### Phase 4: FE 레이어 생성

Phase 3 완료 후 실행:

```
Agent(
  subagent_type: "general-purpose",
  model: "opus",
  prompt: "
    {PROJECT_ROOT}/.claude/agents/frontend-scaffolder.md 를 Read하여 역할과 원칙을 숙지한다.
    {PROJECT_ROOT}/.claude/skills/frontend-layer/SKILL.md 를 Read하여 절차를 따른다.
    {WORKSPACE}/00_input.md, {WORKSPACE}/03_backend_spec.md 를 읽고 FE 레이어를 생성한다.
    프로젝트 루트: {PROJECT_ROOT}
    산출물: {WORKSPACE}/03_frontend_spec.md + 실제 소스 파일
    components/ 하위 파일 생성 후 form-validation-tests 스킬을 반드시 호출한다.
  "
)
```

### Phase 5: QA 검증

Phase 3, 4 완료 후 실행:

```
Agent(
  subagent_type: "general-purpose",
  model: "opus",
  prompt: "
    {PROJECT_ROOT}/.claude/agents/qa-reviewer.md 를 Read하여 역할과 원칙을 숙지한다.
    {WORKSPACE}/03_backend_spec.md, {WORKSPACE}/03_frontend_spec.md 와 실제 소스 파일을 비교하여
    인터페이스 정합성을 검증한다.
    프로젝트 루트: {PROJECT_ROOT}
    산출물: {WORKSPACE}/04_qa_report.md
  "
)
```

### Phase 6: 결과 요약

1. `$WORKSPACE/04_qa_report.md` 내용 요약
2. 생성 파일 목록 출력
3. CRITICAL 이슈 있으면 명시 및 수정 여부 확인
4. 사용자에게 다음 작업 제안 (예: 브랜치 생성, 테스트 실행, 커밋)

## 에러 핸들링

| 상황 | 전략 |
|------|------|
| Phase 2 실패 | `docs/database/naming-convention.md` 확인 후 재시도. 재실패 시 사용자에게 테이블 요구사항 확인 요청 |
| Phase 2.5 실패 (SQL 에러) | 사용자에게 DDL 수정 요청 후 중단 |
| Phase 2.5 실패 (MCP 연결) | 마이그레이션 스킵, `_workspace/02_migrate_result.md`에 "수동 실행 필요" 명시 후 Phase 3 진행 |
| Phase 3 실패 | `_workspace/02_db_design.md` 내용 확인 후 재시도 |
| Phase 4 실패 | `_workspace/03_backend_spec.md` 내용 확인 후 재시도 |
| Phase 5 실패 | QA 없이 진행, 최종 보고에 "QA 미수행" 명시 |

## 데이터 흐름

```
사용자 요청
    → $WORKSPACE/00_input.md
    → db-designer:        $WORKSPACE/02_db_design.md
    → db-migrator:        $WORKSPACE/02_migrate_result.md (postgres 적용)
    → backend-scaffolder: $WORKSPACE/03_backend_spec.md + BE 소스
    → frontend-scaffolder: $WORKSPACE/03_frontend_spec.md + FE 소스
    → qa-reviewer:        $WORKSPACE/04_qa_report.md
    → 최종 요약 보고
```

## 테스트 시나리오

### 정상 흐름

1. 사용자: "order 주문 목록 API와 어드민 페이지 만들어줘"
2. Phase 0: `pwd` → PROJECT_ROOT 확인
3. Phase 1: `_workspace/00_input.md` 생성 (module: order, 기능: 목록 조회, 레이어: ALL)
4. Phase 2: `OP_ORD_BASE` DDL 설계 → `_workspace/02_db_design.md`
5. Phase 2.5: MCP postgres로 테이블 생성 → `_workspace/02_migrate_result.md`
6. Phase 3: `OpOrdBase` Entity, `OrderListService`, `OrderController` 등 BE 레이어 생성
7. Phase 4: `order.types.ts`, `order.api.ts`, `useAdminOrderList.ts`, `OrderListTable.tsx`, `page.tsx` 생성
8. Phase 5: DTO shape vs 타입 비교 → `_workspace/04_qa_report.md`
9. Phase 6: 결과 요약, 생성 파일 목록, QA 이슈 보고

### 에러 흐름

1. Phase 3에서 기존 `GoodsMapper`와 충돌 발생
2. backend-scaffolder가 기존 파일 Read 후 신규 메서드만 추가
3. `_workspace/03_backend_spec.md`에 "기존 파일 수정" 명시
4. 나머지 Phase 정상 진행