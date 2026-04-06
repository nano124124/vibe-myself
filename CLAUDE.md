# Vibe Myself

## 프로젝트 개요

쇼핑몰 서비스 — 고객용 쇼핑 화면과 관리자 어드민을 포함한 풀스택 이커머스 플랫폼이다.

### AI 협업 원칙

**사용자는 7년차 이커머스 개발자다.** 기초 설명은 생략하고, 아래 수준에 맞춰 응답한다.

- 이커머스 도메인 지식 (주문 흐름, 재고 관리, 결제, 정산, 배송, 프로모션, CS 처리 등) 을 전제로 대화한다.
- 설계·구현 제안 시 이커머스 실무 맥락(트래픽 패턴, 동시성 이슈, 데이터 정합성 등)을 반영한다.
- 코드 리뷰·개선안은 "왜 이게 이커머스에서 문제인가" 관점에서 핵심만 짚는다.
- 불필요한 개념 설명·초보용 주석은 달지 않는다.
- AI 활용 측면에서는 Claude Code, Prompt Engineering, MCP 등 최신 AI 도구 활용법을 적극 제안한다.

## 상세 문서

@docs/modules.md
@docs/backend/structure.md
@docs/backend/coding-guide.md
@docs/frontend/structure.md
@docs/frontend/coding-guide.md
@docs/database/naming-convention.md

## 최근 작업 기록
<!-- session-wrap-up 스킬이 아래 줄을 자동 업데이트한다 -->
@docs/progress/2026-04-06.md



## 프로젝트 구조

```
vibe-myself/
├── backend/    # Spring Boot API 서버
└── frontend/   # Next.js 쇼핑몰 + 어드민
```

## 기술 스택

### Backend (`backend/`)

| 항목 | 내용 |
|------|------|
| 언어 | Java 21 |
| 프레임워크 | Spring Boot 3.4.3 |
| 빌드 | Gradle Kotlin DSL |
| DB | PostgreSQL |
| ORM | MyBatis |
| 보안 | Spring Security (JWT) |
| 포트 | 8080 |

패키지 루트: `com.vibemyself`

로컬 실행: `backend/` 디렉토리에서 `./gradlew bootRun`
로컬 DB 설정: `src/main/resources/application-local.yml` (git 제외)

### Frontend (`frontend/`)

| 항목 | 내용 |
|------|------|
| 언어 | TypeScript |
| 프레임워크 | Next.js 16 (App Router) |
| UI | React 19 + Tailwind CSS v4 + shadcn/ui |
| 서버 상태 | Tanstack Query v5 |
| 클라이언트 상태 | Zustand |
| HTTP | Axios |
| 테스트 | Vitest |
| 패키지 매니저 | pnpm |
| 포트 | 3000 |

로컬 실행: `frontend/` 디렉토리에서 `pnpm dev`
환경변수: `frontend/.env.local` (git 제외)

```
NEXT_PUBLIC_API_URL=http://localhost:8080
```

## 라우트 구조 (Frontend)

- `(shop)` Route Group → URL 접두사 없음 (`/`, `/products`, `/cart` 등)
- `admin` → `/admin/...`
- 어드민 인증: `frontend/proxy.ts` — JWT `access_token` 쿠키 검사, `ROLE_ADMIN` 미보유 시 차단

## 협업 규칙

> **이 규칙은 반드시 지켜야 한다.**

- **커밋/푸시는 사용자가 명시적으로 요청할 때만 진행한다.** 임의로 커밋하거나 push하지 않는다.
- **지시가 모호하거나 판단이 애매한 경우, 먼저 다시 물어본 후 진행한다.** 추측으로 진행하지 않는다.
- **브랜치 머지는 항상 `--no-ff` 옵션을 사용한다.** fast-forward 머지 금지. 머지 커밋을 생성하여 브랜치 이력을 보존한다.
- **API 개발 시 작업 전 반드시 `@docs/backend/coding-guide.md` 를 읽고 규칙을 준수한다.**
- **프론트엔드 개발 시 작업 전 반드시 `@docs/frontend/coding-guide.md` 를 읽고 규칙을 준수한다.**
- **작업 범위가 크거나 설계 판단이 필요한 경우, 구현 전 `docs/plans` 에 계획을 먼저 작성하고 확인받는다.**
- **작업 시 참고한 파일을 반드시 마지막에 명시한다.**
- **`frontend/components/` 하위 컴포넌트를 Write/Edit한 직후, `form-validation-tests` 스킬을 반드시 호출한다. 훅 메시지(`[MANDATORY]`)가 출력되면 즉시 실행한다. 스킬 실행 없이 "완료"를 선언하지 않는다.**

## 백엔드 연동

개발 환경에서 `/api/*` 요청은 `next.config.ts`의 rewrites를 통해 `:8080`으로 프록시된다.
