# Vibe Myself

## 상세 문서

@docs/modules.md
@docs/backend/structure.md
@docs/backend/coding-guide.md
@docs/frontend/structure.md
@docs/frontend/coding-guide.md
@docs/database/naming-convention.md

## 최근 작업 기록
<!-- session-wrap-up 스킬이 아래 줄을 자동 업데이트한다 -->
@docs/progress/2026-03-26.md



쇼핑몰 서비스. 고객용 쇼핑 화면과 관리자 어드민을 포함한다.

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

## 백엔드 연동

개발 환경에서 `/api/*` 요청은 `next.config.ts`의 rewrites를 통해 `:8080`으로 프록시된다.
