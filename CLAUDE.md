# Vibe Myself

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

## 백엔드 연동

개발 환경에서 `/api/*` 요청은 `next.config.ts`의 rewrites를 통해 `:8080`으로 프록시된다.
