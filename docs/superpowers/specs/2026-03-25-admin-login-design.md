# Admin Login FO 설계

**날짜:** 2026-03-25
**범위:** 관리자 로그인/로그아웃 프론트엔드 구현

---

## 1. 목표

관리자 로그인 화면을 구현하고, 인증 가드를 올바르게 수정하며, 로그아웃 기능을 어드민 레이아웃에 추가한다.

---

## 2. 라우팅 구조

| 경로 | 설명 | 인증 필요 |
|------|------|-----------|
| `/admin` | 로그인 페이지 | ❌ |
| `/admin/dashboard` | 대시보드 (기존 `/admin` page 이동) | ✅ |
| `/admin/members`, `/admin/orders` 등 | 기존 어드민 페이지들 | ✅ |

`proxy.ts`의 `matcher`는 이미 `/admin/:path*` 형태라 `/admin` 루트는 보호하지 않는다. 리다이렉트 목적지만 `/auth/login` → `/admin`으로 수정한다.

---

## 3. 파일 구조

```
frontend/
├── app/
│   └── admin/
│       ├── page.tsx                   # 수정: 로그인 페이지
│       ├── dashboard/
│       │   └── page.tsx              # 신규: 기존 대시보드 이동
│       └── layout.tsx                # 수정: 로그아웃 버튼 추가
├── components/
│   └── system/
│       └── AdminLoginForm.tsx         # 신규: 로그인 폼 컴포넌트
├── hooks/
│   └── system/
│       ├── useAdminLogin.ts           # 신규: 로그인 mutation
│       └── useAdminLogout.ts          # 신규: 로그아웃 mutation
├── api/
│   └── system.api.ts                  # 신규: login/logout API 함수
├── types/
│   └── system.types.ts               # 신규: 관리자 인증 타입
└── lib/
    └── authGuard.ts                   # 수정: ROLE_SUPER 허용, 리다이렉트 경로 수정
```

---

## 4. 컴포넌트 설계

### AdminLoginForm

- **위치:** `components/system/AdminLoginForm.tsx`
- **레이아웃:** 좌우 분할형
  - 왼쪽: 다크 그라디언트 브랜드 영역 (로고, 타이틀, 역할 배지)
  - 오른쪽: 아이디/비밀번호 폼
- **에러 처리:** 로그인 실패 시 폼 하단 인라인 에러 메시지 표시
- **Props:** 없음 (훅에서 상태 관리)

### AdminLayout (수정)

- 인증된 페이지(`/admin/dashboard` 이하)에서 헤더에 로그아웃 버튼 표시
- `/admin` 루트(로그인 페이지)에서는 로그아웃 버튼 미표시

---

## 5. 데이터 흐름

### 로그인

```
AdminLoginForm
  → useAdminLogin (useMutation)
    → POST /api/admin/system/login
      → 성공: router.push('/admin/dashboard')
      → 실패: 에러 메시지 인라인 표시
```

### 로그아웃

```
AdminLayout 로그아웃 버튼
  → useAdminLogout (useMutation)
    → POST /api/admin/system/logout
      → 완료: router.push('/admin')
```

---

## 6. API

**파일:** `api/system.api.ts`

```ts
loginAdmin(data: LoginAdminRequest): Promise<void>
// POST /api/admin/system/login

logoutAdmin(): Promise<void>
// POST /api/admin/system/logout
```

**타입:** `types/system.types.ts`

```ts
interface LoginAdminRequest {
  loginId: string
  password: string
}
```

---

## 7. authGuard 수정

**파일:** `lib/authGuard.ts`

```ts
// 수정 전 (버그: ROLE_SUPER 차단됨)
if (payload.role !== 'ROLE_ADMIN') return '/'

// 수정 후
const ADMIN_ROLES = ['ROLE_ADMIN', 'ROLE_SUPER']
if (!ADMIN_ROLES.includes(payload.role)) return '/admin'
```

**proxy.ts 수정:**
```ts
// 수정 전
if (redirectPath) return NextResponse.redirect(new URL(redirectPath, request.url))
// redirectPath가 '/' → 쇼핑몰 메인으로 보내던 버그

// 수정 후: authGuard가 '/admin'을 반환하므로 자연스럽게 로그인 페이지로 이동
```

---

## 8. 비주얼 디자인 방향

- 좌우 분할 레이아웃
- 왼쪽: `#0f172a` 다크 배경 + 인디고/퍼플 그라디언트 액센트
- 오른쪽: 흰 배경, 깔끔한 폼
- 버튼: 인디고-퍼플 그라디언트 (`#6366f1` → `#8b5cf6`)
- 에러: 빨간 인라인 박스 (`bg-red-50`, `border-red-200`)
- shadcn/ui 컴포넌트 활용 (Input, Button, Label)

---

## 9. 구현 범위 외

- 토큰 자동 갱신 (axios interceptor) — 별도 세션에서 구현
- 비밀번호 찾기 — 미구현
