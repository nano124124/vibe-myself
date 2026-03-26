# Admin Login FO 설계

**날짜:** 2026-03-25
**범위:** 관리자 로그인/로그아웃 프론트엔드 구현

---

## 1. 목표

관리자 로그인 화면을 구현하고, 인증 가드를 올바르게 수정하며, 로그아웃 기능을 어드민 레이아웃에 추가한다.

---

## 2. 모듈 정의

프론트엔드에서 어드민 인증 관련 파일은 `system` 모듈로 분류한다. 백엔드 `system/` 패키지(AdminAuthController 등)와 대응된다.

---

## 3. 라우팅 구조

| 경로 | 설명 | 인증 필요 |
|------|------|-----------|
| `/admin` | 로그인 페이지 | ❌ |
| `/admin/dashboard` | 대시보드 (기존 `/admin` page 이동) | ✅ |
| `/admin/members`, `/admin/orders` 등 | 기존 어드민 페이지들 | ✅ |

`proxy.ts`의 `matcher`는 이미 `/admin/:path*` 형태라 `/admin` 루트는 보호 대상이 아니다.

**이미 로그인된 사용자가 `/admin`을 방문할 경우:** `authGuard`에서 유효한 토큰 + 어드민 역할이 확인되면 `/admin/dashboard`로 리다이렉트한다.

---

## 4. 파일 구조

로그인 페이지는 어드민 쉘(헤더/사이드바) 없이 렌더링되어야 하므로, 인증이 필요한 페이지를 `(main)` route group으로 분리한다.

```
frontend/
├── app/
│   └── admin/
│       ├── page.tsx                   # 수정: 로그인 페이지 (쉘 없음)
│       ├── layout.tsx                 # 수정: 최소화 (쉘 제거)
│       └── (main)/                    # 신규: 인증 필요 페이지 그룹 (URL에 영향 없음)
│           ├── layout.tsx             # 신규: AdminHeader 포함하는 어드민 쉘
│           ├── dashboard/
│           │   └── page.tsx          # 신규: 기존 AdminDashboardPage 이동
│           ├── members/page.tsx       # 이동: app/admin/members → app/admin/(main)/members
│           ├── orders/page.tsx        # 이동
│           ├── products/[id]/page.tsx # 이동
│           ├── claims/page.tsx        # 이동
│           ├── events/page.tsx        # 이동
│           └── promotions/page.tsx    # 이동
├── components/
│   └── system/
│       ├── AdminLoginForm.tsx         # 신규: 로그인 폼 컴포넌트
│       └── AdminHeader.tsx            # 신규: 'use client', 로그아웃 버튼
├── hooks/
│   └── system/
│       ├── useAdminLogin.ts           # 신규: 로그인 mutation
│       └── useAdminLogout.ts          # 신규: 로그아웃 mutation
├── api/                               # 신규 디렉토리
│   └── system.api.ts                  # 신규: login/logout API 함수
├── types/
│   └── system.types.ts               # 신규: 관리자 인증 타입
└── lib/
    └── authGuard.ts                   # 수정: ROLE_SUPER 허용, 인증 사용자 리다이렉트 추가
```

> `lib/` 폴더는 프로젝트에 이미 존재한다 (`frontend/lib/`).
> `(main)` route group은 URL 접두사 없이 `/admin/dashboard`, `/admin/members` 등으로 접근된다.

---

## 5. 컴포넌트 설계

### AdminLoginForm

- **위치:** `components/system/AdminLoginForm.tsx`
- **레이아웃:** 좌우 분할형
  - 왼쪽: 다크 그라디언트 브랜드 영역 (로고, 타이틀, SUPER/ADMIN/OPS 역할 배지)
  - 오른쪽: 아이디/비밀번호 폼
- **상태:**
  - `isPending`: 로그인 버튼 disabled + 로딩 스피너 표시
  - 에러: 폼 하단 인라인 에러 박스 표시
  - 성공: `/admin/dashboard`로 이동
- **에러 메시지:**
  - 401: "아이디 또는 비밀번호가 올바르지 않습니다."
  - 그 외: "로그인 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요."
- **Props:** 없음 (훅에서 상태 관리)

### AdminHeader (신규 Client Component)

- **위치:** `components/system/AdminHeader.tsx` (`'use client'`)
- 로그아웃 버튼 + `useAdminLogout` 훅 사용
- `app/admin/(main)/layout.tsx`에서만 import → 로그인 페이지(`/admin`)에는 표시되지 않음

---

## 6. 데이터 흐름

### 로그인

```
AdminLoginForm (아이디/비밀번호 입력)
  → useAdminLogin.mutate()
    → system.api.ts: POST /api/admin/system/login
      → 성공: router.push('/admin/dashboard')
      → 실패 (401): "아이디 또는 비밀번호..." 인라인 에러
      → 실패 (기타): "오류가 발생했습니다." 인라인 에러
    → isPending: 버튼 disabled
```

### 로그아웃

```
AdminLayout 로그아웃 버튼
  → useAdminLogout.mutate()
    → system.api.ts: POST /api/admin/system/logout
      → onSettled (성공/실패 무관): queryClient.clear() → router.push('/admin')
```

> `useMutation`의 `onSettled` 콜백을 사용해 성공/실패 무관하게 캐시 초기화 후 로그인 페이지로 이동한다. 서버 쿠키 삭제는 백엔드가 처리한다.

---

## 7. API

**파일:** `api/system.api.ts`

```ts
loginAdmin(data: LoginAdminRequest): Promise<void>
// POST /api/admin/system/login
// 백엔드가 Set-Cookie로 access_token, refresh_token 세팅

logoutAdmin(): Promise<void>
// POST /api/admin/system/logout
// 백엔드가 쿠키를 만료시킴
```

**타입:** `types/system.types.ts`

```ts
interface LoginAdminRequest {
  loginId: string
  password: string
}
```

> 쿠키(access_token, refresh_token)는 백엔드가 `Set-Cookie`로 설정/만료 처리한다. 프론트엔드는 쿠키를 직접 조작하지 않는다.

---

## 8. authGuard 수정

**파일:** `lib/authGuard.ts`

```ts
// 수정 전 (버그 1: ROLE_SUPER 차단, 버그 2: 잘못된 리다이렉트 경로)
if (payload.role !== 'ROLE_ADMIN') return '/'

// 수정 후
const ADMIN_ROLES = ['ROLE_ADMIN', 'ROLE_SUPER']

export const getRedirectPath = (token: string | null, pathname: string): string | null => {
  // 토큰 없음 → 로그인 페이지
  if (!token) return '/admin'

  try {
    const payload = JSON.parse(atob(token.split('.')[1]))

    // 이미 로그인된 상태에서 로그인 페이지 접근 → 대시보드로
    if (ADMIN_ROLES.includes(payload.role) && pathname === '/admin') return '/admin/dashboard'

    // 어드민 역할 아님 → 로그인 페이지
    if (!ADMIN_ROLES.includes(payload.role)) return '/admin'

    return null // 통과
  } catch {
    return '/admin' // 토큰 파싱 실패 → 로그인 페이지 (기존: '/auth/login' → 수정)
  }
}
```

> `ROLE_SUPER`는 백엔드 `ROLE_CD` 공통코드에 정의된 역할로, `RoleCode.SUPER`에 해당한다. JWT payload의 `role` claim에 `ROLE_SUPER`로 발급된다.

**proxy.ts 수정:**

`getRedirectPath`에 `pathname` 파라미터를 추가하고, `matcher`에 `/admin` 루트를 추가한다.

```ts
// proxy.ts 수정 후
export function proxy(request: NextRequest) {
  const token = request.cookies.get('access_token')?.value ?? null
  const redirectPath = getRedirectPath(token, request.nextUrl.pathname)

  if (redirectPath) {
    return NextResponse.redirect(new URL(redirectPath, request.url))
  }
  return NextResponse.next()
}

export const config = {
  matcher: ['/admin', '/admin/:path*'], // '/admin' 루트 추가 (이미 로그인된 사용자 리다이렉트용)
}
```

**authGuard 테스트 업데이트:**
기존 `lib/__tests__/authGuard.test.ts`는 `getRedirectPath(token)` 단일 파라미터 + `/auth/login` 반환값을 테스트하고 있어 수정이 필요하다.
- 파라미터 `pathname` 추가에 따른 호출 방식 변경
- 반환값 `/auth/login` → `/admin`으로 업데이트
- 이미 인증된 사용자가 `/admin` 접근 시 `/admin/dashboard` 반환하는 케이스 추가

---

## 9. 비주얼 디자인 방향

- 좌우 분할 레이아웃
- 왼쪽: `#0f172a` 다크 배경 + 인디고/퍼플 그라디언트 액센트
- 오른쪽: 흰 배경, 깔끔한 폼
- 버튼: 인디고-퍼플 그라디언트 (`#6366f1` → `#8b5cf6`), pending 시 disabled
- 에러: 빨간 인라인 박스 (`bg-red-50`, `border-red-200`)
- shadcn/ui 컴포넌트 활용 (Input, Button, Label)

---

## 10. 구현 범위 외

- 토큰 자동 갱신 (axios interceptor) — 별도 세션에서 구현
- 비밀번호 찾기 — 미구현
- 세션 만료 중 동시 요청 처리 — 토큰 갱신 구현 시 함께 처리
