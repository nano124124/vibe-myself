# Frontend Setup Design

**Date:** 2026-03-24
**Status:** Approved

## Overview

Next.js 15 (App Router) 기반 프론트엔드 초기 구조 정의. 고객용 쇼핑몰(shop)과 관리자(admin)를 하나의 Next.js 앱 안에서 분리.

> **주의:** 현재 `frontend/` 디렉토리는 Pages Router 기반 스캐폴드(`src/pages/` 등)로 되어 있음. 구현 시 해당 디렉토리를 삭제하고 App Router 구조로 재생성해야 함.

## 기술 스택

| 역할 | 라이브러리 |
|------|-----------|
| 프레임워크 | Next.js 15 (App Router) |
| UI | React 19 + TypeScript |
| 스타일링 | Tailwind CSS v4 + shadcn/ui |
| 서버 상태 | Tanstack Query v5 |
| 클라이언트 상태 | Zustand (장바구니, 사용자 세션) |
| HTTP 클라이언트 | Axios |
| 인증 | JWT — httpOnly 쿠키 저장 |
| 패키지 매니저 | pnpm |

## Route 구조 설명

- `(shop)/` — **Route Group** (URL 접두사 없음). `/`, `/products`, `/cart` 등이 루트에서 시작
- `admin/` — **일반 세그먼트** (URL에 포함). `/admin`, `/admin/products` 등으로 접근
- 두 영역은 각자 별도 `layout.tsx`를 가짐

## 폴더 구조

```
frontend/
├── app/
│   ├── layout.tsx                    # 전체 공통 (폰트, QueryClientProvider, ZustandProvider)
│   ├── (shop)/                       # Route Group — URL 접두사 없음
│   │   ├── layout.tsx                # 쇼핑몰 공통 레이아웃 (헤더, 푸터)
│   │   ├── page.tsx                  # 메인(전시) /
│   │   ├── products/
│   │   │   ├── page.tsx              # 상품 목록 /products
│   │   │   └── [id]/page.tsx         # 상품 상세 /products/:id
│   │   ├── cart/page.tsx             # 장바구니 /cart
│   │   ├── order/
│   │   │   ├── page.tsx              # 주문/결제 /order
│   │   │   └── complete/page.tsx     # 주문완료 /order/complete
│   │   ├── auth/
│   │   │   ├── login/page.tsx        # /auth/login
│   │   │   └── register/page.tsx     # /auth/register
│   │   ├── mypage/
│   │   │   ├── page.tsx              # 마이페이지 /mypage
│   │   │   ├── orders/page.tsx       # 주문 내역
│   │   │   └── claims/page.tsx       # 클레임
│   │   ├── events/
│   │   │   ├── page.tsx
│   │   │   └── [id]/page.tsx
│   │   └── promotions/
│   │       ├── page.tsx
│   │       └── [id]/page.tsx
│   ├── admin/                        # 일반 세그먼트 — URL: /admin/...
│   │   ├── layout.tsx                # 어드민 공통 레이아웃 (사이드바)
│   │   ├── page.tsx                  # 대시보드 /admin
│   │   ├── products/
│   │   │   ├── page.tsx
│   │   │   └── [id]/page.tsx
│   │   ├── orders/page.tsx
│   │   ├── members/page.tsx
│   │   ├── claims/page.tsx
│   │   ├── events/page.tsx
│   │   └── promotions/page.tsx
│   └── api/                          # (필요시 Next.js API Routes)
├── components/
│   ├── shop/                         # 쇼핑몰 전용 컴포넌트
│   ├── admin/                        # 어드민 전용 컴포넌트
│   ├── common/                       # shop/admin 공통 컴포넌트 (Pagination 등)
│   └── ui/                           # shadcn/ui 자동 생성 컴포넌트
├── lib/
│   ├── api.ts                        # Axios 인스턴스
│   ├── queryClient.ts                # TanStack Query QueryClient 설정
│   └── utils.ts                      # 공통 유틸 (shadcn cn() 포함)
├── hooks/                            # 커스텀 훅
├── store/
│   └── cart.ts                       # Zustand 장바구니 스토어 (예시)
├── types/                            # TypeScript 타입 정의
├── middleware.ts                     # 어드민 라우트 인증 보호
├── .env.local                        # 로컬 환경변수 (git 제외)
├── next.config.ts
├── tailwind.config.ts
└── package.json
```

## 인증 전략

- 로그인 → Spring Boot JWT 발급 → httpOnly 쿠키 저장 (XSS 방어)
- `middleware.ts`가 `/admin/*` 요청 시:
  - 쿠키 없음 → `/auth/login` 리다이렉트
  - 쿠키 있으나 `ROLE_ADMIN` 아님 → `/` 리다이렉트 (일반 유저 차단)
  - `ROLE_ADMIN` 확인 → 통과
- 역할 구분: `ROLE_USER` (shop), `ROLE_ADMIN` (`/admin/*`)
- `/auth/login` URL은 `(shop)` Route Group 안에 있어 URL에 그룹명이 포함되지 않음 → 정상 접근 가능

## 환경변수

`.env.local` (git 제외):
```
NEXT_PUBLIC_API_URL=http://localhost:8080
```

`.gitignore`에 `.env.local` 추가 필요.

## 백엔드 연동

- 개발 환경: `next.config.ts` rewrites로 `/api/*` → `localhost:8080` 프록시
- 환경변수: `NEXT_PUBLIC_API_URL`로 백엔드 URL 관리
- frontend `:3000` ↔ backend `:8080`

## Bootstrapping 순서

1. 기존 `frontend/` 디렉토리 삭제
2. `pnpm create next-app frontend` (TypeScript, App Router, Tailwind 선택)
3. 추가 의존성 설치: `@tanstack/react-query`, `axios`, `zustand`
4. shadcn/ui 초기화: `pnpm dlx shadcn@latest init`
5. `middleware.ts` 생성 및 어드민 보호 로직 작성
6. `lib/api.ts`, `lib/queryClient.ts` 생성
7. `app/layout.tsx`에 `QueryClientProvider` 등록
8. `.env.local` 생성 및 `.gitignore` 추가
9. 폴더 구조 생성 (route groups, components, store 등)
