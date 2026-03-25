# Admin Login FO Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 관리자 로그인/로그아웃 프론트엔드 구현 — `/admin` 로그인 페이지, `(main)` route group으로 인증 레이아웃 분리, authGuard 버그 수정.

**Architecture:** `lib/authGuard.ts`를 수정해 `ROLE_SUPER` 허용 및 이미 인증된 사용자 리다이렉트를 추가한다. 인증이 필요한 어드민 페이지를 `app/admin/(main)/` route group으로 이동해 로그인 페이지가 어드민 쉘 없이 렌더링되도록 분리한다. 로그인/로그아웃은 Tanstack Query `useMutation` + Axios로 처리한다.

**Tech Stack:** Next.js 16 App Router, Tanstack Query v5, Axios, shadcn/ui, Tailwind CSS v4, Vitest

---

## Chunk 1: Foundation — authGuard, proxy, types, API, hooks

### Task 1: authGuard 수정

**Files:**
- Modify: `frontend/lib/authGuard.ts`
- Modify: `frontend/lib/__tests__/authGuard.test.ts`

- [ ] **Step 1: 기존 테스트를 새 시그니처에 맞게 업데이트**

`frontend/lib/__tests__/authGuard.test.ts` 전체를 아래로 교체한다:

```ts
import { describe, it, expect } from 'vitest'
import { getRedirectPath } from '../authGuard'

describe('getRedirectPath', () => {
  it('토큰 없음 → /admin 반환', () => {
    expect(getRedirectPath(null, '/admin/dashboard')).toBe('/admin')
  })

  it('토큰 파싱 실패 → /admin 반환', () => {
    expect(getRedirectPath('invalid.token.here', '/admin/dashboard')).toBe('/admin')
  })

  it('ROLE_USER → /admin 반환', () => {
    const payload = btoa(JSON.stringify({ role: 'ROLE_USER' }))
    const token = `h.${payload}.s`
    expect(getRedirectPath(token, '/admin/dashboard')).toBe('/admin')
  })

  it('ROLE_ADMIN + /admin 이외 경로 → null (통과)', () => {
    const payload = btoa(JSON.stringify({ role: 'ROLE_ADMIN' }))
    const token = `h.${payload}.s`
    expect(getRedirectPath(token, '/admin/dashboard')).toBeNull()
  })

  it('ROLE_SUPER + /admin 이외 경로 → null (통과)', () => {
    const payload = btoa(JSON.stringify({ role: 'ROLE_SUPER' }))
    const token = `h.${payload}.s`
    expect(getRedirectPath(token, '/admin/orders')).toBeNull()
  })

  it('ROLE_ADMIN + /admin 접근 → /admin/dashboard 반환 (이미 로그인)', () => {
    const payload = btoa(JSON.stringify({ role: 'ROLE_ADMIN' }))
    const token = `h.${payload}.s`
    expect(getRedirectPath(token, '/admin')).toBe('/admin/dashboard')
  })

  it('ROLE_SUPER + /admin 접근 → /admin/dashboard 반환 (이미 로그인)', () => {
    const payload = btoa(JSON.stringify({ role: 'ROLE_SUPER' }))
    const token = `h.${payload}.s`
    expect(getRedirectPath(token, '/admin')).toBe('/admin/dashboard')
  })
})
```

- [ ] **Step 2: 테스트 실행 → FAIL 확인**

```bash
cd frontend && pnpm vitest run lib/__tests__/authGuard.test.ts
```
Expected: 대부분 FAIL (시그니처 불일치)

- [ ] **Step 3: authGuard.ts 구현 교체**

`frontend/lib/authGuard.ts` 전체를 아래로 교체한다:

```ts
const ADMIN_ROLES = ['ROLE_ADMIN', 'ROLE_SUPER']

export const getRedirectPath = (token: string | null, pathname: string): string | null => {
  if (!token) return '/admin'

  try {
    const payload = JSON.parse(atob(token.split('.')[1]))

    if (ADMIN_ROLES.includes(payload.role) && pathname === '/admin') return '/admin/dashboard'
    if (!ADMIN_ROLES.includes(payload.role)) return '/admin'

    return null
  } catch {
    return '/admin'
  }
}
```

- [ ] **Step 4: 테스트 실행 → PASS 확인**

```bash
cd frontend && pnpm vitest run lib/__tests__/authGuard.test.ts
```
Expected: 7 tests passed

- [ ] **Step 5: 커밋**

```bash
git add frontend/lib/authGuard.ts frontend/lib/__tests__/authGuard.test.ts
git commit -m "fix: update authGuard to allow ROLE_SUPER and redirect authenticated users"
```

---

### Task 2: proxy.ts 수정

> Task 1에서 `getRedirectPath` 시그니처가 `(token, pathname)`으로 변경됐으므로 반드시 Task 1 이후에 실행한다.

**Files:**
- Modify: `frontend/proxy.ts`

- [ ] **Step 1: matcher에 `/admin` 루트 추가 및 pathname 전달**

`frontend/proxy.ts` 전체를 아래로 교체한다:

```ts
import { NextRequest, NextResponse } from 'next/server'
import { getRedirectPath } from '@/lib/authGuard'

export const proxy = (request: NextRequest) => {
  const token = request.cookies.get('access_token')?.value ?? null
  const redirectPath = getRedirectPath(token, request.nextUrl.pathname)

  if (redirectPath) {
    return NextResponse.redirect(new URL(redirectPath, request.url))
  }

  return NextResponse.next()
}

export const config = {
  matcher: ['/admin', '/admin/:path*'],
}
```

- [ ] **Step 2: 변경사항 확인**

두 가지가 모두 반영되었는지 확인한다:
- `matcher`에 `'/admin'` 추가됨
- `getRedirectPath(token, request.nextUrl.pathname)` — pathname 인자 전달됨

- [ ] **Step 3: 커밋**

```bash
git add frontend/proxy.ts
git commit -m "fix: update proxy matcher to include /admin root and pass pathname to authGuard"
```

---

### Task 3: 타입 정의

**Files:**
- Create: `frontend/types/system.types.ts`

- [ ] **Step 1: 파일 생성**

```ts
export interface LoginAdminRequest {
  loginId: string
  password: string
}
```

- [ ] **Step 2: 커밋**

```bash
git add frontend/types/system.types.ts
git commit -m "feat: add system types for admin auth"
```

---

### Task 4: API 함수

**Files:**
- Create: `frontend/api/system.api.ts`

> `frontend/api/` 디렉토리가 아직 없으므로 이 태스크에서 새로 생성된다.

- [ ] **Step 1: api 디렉토리 생성 및 파일 생성**

```ts
import api from '@/lib/api'
import type { LoginAdminRequest } from '@/types/system.types'

export const loginAdmin = (data: LoginAdminRequest): Promise<void> =>
  api.post('/api/admin/system/login', data).then(() => undefined)

export const logoutAdmin = (): Promise<void> =>
  api.post('/api/admin/system/logout').then(() => undefined)
```

- [ ] **Step 2: 커밋**

```bash
git add frontend/api/system.api.ts
git commit -m "feat: add system API functions for admin login/logout"
```

---

### Task 5: useAdminLogin 훅

**Files:**
- Create: `frontend/hooks/system/useAdminLogin.ts`

> `frontend/hooks/system/` 디렉토리가 아직 없으므로 이 태스크에서 새로 생성된다.

- [ ] **Step 1: 디렉토리 생성 및 파일 생성**

```ts
import { useMutation } from '@tanstack/react-query'
import { useRouter } from 'next/navigation'
import { isAxiosError } from 'axios'
import { loginAdmin } from '@/api/system.api'
import type { LoginAdminRequest } from '@/types/system.types'

export const useAdminLogin = () => {
  const router = useRouter()

  return useMutation({
    mutationFn: (data: LoginAdminRequest) => loginAdmin(data),
    onSuccess: () => {
      router.push('/admin/dashboard')
    },
  })
}
```

> 에러 메시지는 컴포넌트에서 `mutation.error`를 통해 처리한다. 401과 그 외 에러 구분은 `isAxiosError(error) && error.response?.status === 401`로 판단한다.

- [ ] **Step 2: 커밋**

```bash
git add frontend/hooks/system/useAdminLogin.ts
git commit -m "feat: add useAdminLogin hook"
```

---

### Task 6: useAdminLogout 훅

**Files:**
- Create: `frontend/hooks/system/useAdminLogout.ts`

> `onSettled`를 사용하므로 API 성공/실패 무관하게 항상 캐시 초기화 및 `/admin` 이동이 실행된다.

- [ ] **Step 1: 파일 생성**

```ts
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { useRouter } from 'next/navigation'
import { logoutAdmin } from '@/api/system.api'

export const useAdminLogout = () => {
  const router = useRouter()
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: logoutAdmin,
    onSettled: () => {
      queryClient.clear()
      router.push('/admin')
    },
  })
}
```

- [ ] **Step 2: 커밋**

```bash
git add frontend/hooks/system/useAdminLogout.ts
git commit -m "feat: add useAdminLogout hook with cache clear on settled"
```

---

## Chunk 2: UI & Routes

### Task 7: shadcn Input, Label 설치

**Files:**
- Auto-generated: `frontend/components/ui/input.tsx`
- Auto-generated: `frontend/components/ui/label.tsx`

현재 `components/ui/`에는 `button.tsx`만 있다. Input과 Label을 추가한다.

- [ ] **Step 1: shadcn 컴포넌트 설치**

```bash
cd frontend && pnpm dlx shadcn@latest add input label
```
Expected: `components/ui/input.tsx`, `components/ui/label.tsx` 생성됨

- [ ] **Step 2: 커밋**

```bash
git add frontend/components/ui/input.tsx frontend/components/ui/label.tsx
git commit -m "chore: add shadcn Input and Label components"
```

---

### Task 8: AdminLoginForm 컴포넌트

**Files:**
- Create: `frontend/components/system/AdminLoginForm.tsx`

> `frontend/components/system/` 디렉토리는 이 태스크에서 새로 생성된다.

- [ ] **Step 1: 파일 생성**

```tsx
'use client'

import { useState } from 'react'
import { isAxiosError } from 'axios'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { useAdminLogin } from '@/hooks/system/useAdminLogin'

const AdminLoginForm = () => {
  const [loginId, setLoginId] = useState('')
  const [password, setPassword] = useState('')
  const mutation = useAdminLogin()

  const errorMessage = (() => {
    if (!mutation.error) return null
    if (isAxiosError(mutation.error) && mutation.error.response?.status === 401) {
      return '아이디 또는 비밀번호가 올바르지 않습니다.'
    }
    return '로그인 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.'
  })()

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    mutation.mutate({ loginId, password })
  }

  return (
    <div className="flex min-h-screen">
      {/* 왼쪽: 브랜드 */}
      <div className="flex flex-1 flex-col items-center justify-center gap-3 bg-[#0f172a] px-10 relative overflow-hidden">
        <div className="absolute inset-0 bg-[radial-gradient(circle_at_20%_20%,rgba(99,102,241,0.15),transparent_50%)]" />
        <div className="relative z-10 flex h-14 w-14 items-center justify-center rounded-2xl bg-gradient-to-br from-indigo-500 to-violet-500 text-2xl shadow-[0_8px_24px_rgba(99,102,241,0.4)]">
          🛡
        </div>
        <p className="relative z-10 text-xl font-bold tracking-tight text-white">Vibe Admin</p>
        <div className="relative z-10 h-0.5 w-8 rounded-full bg-gradient-to-r from-indigo-500 to-violet-500" />
        <p className="relative z-10 text-center text-sm leading-relaxed text-slate-500">
          쇼핑몰 관리자 콘솔<br />인가된 운영팀만 접근 가능합니다
        </p>
        <div className="relative z-10 mt-6 flex gap-2">
          {['SUPER', 'ADMIN', 'OPS'].map((role) => (
            <span
              key={role}
              className="rounded-full border border-indigo-500/25 bg-indigo-500/15 px-2.5 py-1 text-[10px] font-semibold tracking-wide text-indigo-400"
            >
              {role}
            </span>
          ))}
        </div>
      </div>

      {/* 오른쪽: 폼 */}
      <div className="flex flex-[1.1] flex-col justify-center px-12 py-14">
        <p className="mb-2 text-[11px] font-bold uppercase tracking-widest text-indigo-500">Admin Console</p>
        <h1 className="mb-1.5 text-2xl font-extrabold tracking-tight text-slate-900">로그인</h1>
        <p className="mb-9 text-sm text-slate-400">계속하려면 관리자 계정으로 로그인하세요.</p>

        <form onSubmit={handleSubmit} className="flex flex-col gap-4">
          <div className="flex flex-col gap-1.5">
            <Label htmlFor="loginId" className="text-xs font-semibold text-slate-700">아이디</Label>
            <Input
              id="loginId"
              value={loginId}
              onChange={(e) => setLoginId(e.target.value)}
              placeholder="관리자 아이디 입력"
              autoComplete="username"
              required
            />
          </div>

          <div className="flex flex-col gap-1.5">
            <Label htmlFor="password" className="text-xs font-semibold text-slate-700">비밀번호</Label>
            <Input
              id="password"
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="비밀번호 입력"
              autoComplete="current-password"
              required
            />
          </div>

          {errorMessage && (
            <div className="flex items-center gap-2 rounded-lg border border-red-200 bg-red-50 px-3 py-2.5 text-sm text-red-500">
              ⚠️ {errorMessage}
            </div>
          )}

          <Button
            type="submit"
            disabled={mutation.isPending}
            className="mt-1 h-12 w-full rounded-xl bg-gradient-to-br from-indigo-500 to-violet-500 text-sm font-bold shadow-[0_4px_16px_rgba(99,102,241,0.35)] hover:opacity-90"
          >
            {mutation.isPending ? '로그인 중...' : '로그인'}
          </Button>
        </form>
      </div>
    </div>
  )
}

export default AdminLoginForm
```

- [ ] **Step 2: 커밋**

```bash
git add frontend/components/system/AdminLoginForm.tsx
git commit -m "feat: add AdminLoginForm component with split layout"
```

---

### Task 9: AdminHeader 컴포넌트

**Files:**
- Create: `frontend/components/system/AdminHeader.tsx`

- [ ] **Step 1: 파일 생성**

```tsx
'use client'

import { Button } from '@/components/ui/button'
import { useAdminLogout } from '@/hooks/system/useAdminLogout'

const AdminHeader = () => {
  const { mutate: logout, isPending } = useAdminLogout()

  return (
    <header className="flex h-14 items-center justify-between border-b border-slate-200 bg-white px-6">
      <span className="text-sm font-semibold text-slate-700">Vibe Admin</span>
      <Button
        variant="outline"
        size="sm"
        onClick={() => logout()}
        disabled={isPending}
      >
        {isPending ? '로그아웃 중...' : '로그아웃'}
      </Button>
    </header>
  )
}

export default AdminHeader
```

- [ ] **Step 2: 커밋**

```bash
git add frontend/components/system/AdminHeader.tsx
git commit -m "feat: add AdminHeader component with logout button"
```

---

### Task 10: 라우트 구조 재편

기존 `app/admin/` 하위 페이지들을 `app/admin/(main)/`으로 이동한다.

**Files:**
- Move: `frontend/app/admin/members/page.tsx` → `frontend/app/admin/(main)/members/page.tsx`
- Move: `frontend/app/admin/orders/page.tsx` → `frontend/app/admin/(main)/orders/page.tsx`
- Move: `frontend/app/admin/products/page.tsx` → `frontend/app/admin/(main)/products/page.tsx`
- Move: `frontend/app/admin/products/[id]/page.tsx` → `frontend/app/admin/(main)/products/[id]/page.tsx`
- Move: `frontend/app/admin/claims/page.tsx` → `frontend/app/admin/(main)/claims/page.tsx`
- Move: `frontend/app/admin/events/page.tsx` → `frontend/app/admin/(main)/events/page.tsx`
- Move: `frontend/app/admin/promotions/page.tsx` → `frontend/app/admin/(main)/promotions/page.tsx`
- Create: `frontend/app/admin/(main)/dashboard/page.tsx`
- Create: `frontend/app/admin/(main)/layout.tsx`

- [ ] **Step 1: (main) 디렉토리 생성 및 페이지 이동**

```bash
cd frontend
mkdir -p app/admin/\(main\)/members
mkdir -p app/admin/\(main\)/orders
mkdir -p app/admin/\(main\)/products/\[id\]
mkdir -p app/admin/\(main\)/claims
mkdir -p app/admin/\(main\)/events
mkdir -p app/admin/\(main\)/promotions
mkdir -p app/admin/\(main\)/dashboard

mv app/admin/members/page.tsx app/admin/\(main\)/members/page.tsx
mv app/admin/orders/page.tsx app/admin/\(main\)/orders/page.tsx
mv app/admin/products/page.tsx app/admin/\(main\)/products/page.tsx
mv app/admin/products/\[id\]/page.tsx app/admin/\(main\)/products/\[id\]/page.tsx
mv app/admin/claims/page.tsx app/admin/\(main\)/claims/page.tsx
mv app/admin/events/page.tsx app/admin/\(main\)/events/page.tsx
mv app/admin/promotions/page.tsx app/admin/\(main\)/promotions/page.tsx
```

- [ ] **Step 2: 기존 대시보드 페이지를 (main)/dashboard로 이동**

`frontend/app/admin/(main)/dashboard/page.tsx` 생성:

```tsx
export default function AdminDashboardPage() {
  return <div>AdminDashboardPage</div>
}
```

- [ ] **Step 3: (main)/layout.tsx 생성 (어드민 쉘)**

`frontend/app/admin/(main)/layout.tsx` 생성:

```tsx
import AdminHeader from '@/components/system/AdminHeader'

export default function AdminMainLayout({ children }: { children: React.ReactNode }) {
  return (
    <div className="flex min-h-screen flex-col">
      <AdminHeader />
      <div className="flex flex-1">
        <aside className="w-48 border-r border-slate-200 bg-white p-4">
          <nav className="text-sm text-slate-600">Admin Menu</nav>
        </aside>
        <main className="flex-1 p-6">{children}</main>
      </div>
    </div>
  )
}
```

- [ ] **Step 4: 커밋**

```bash
git add frontend/app/admin/
git commit -m "refactor: move admin pages into (main) route group for layout separation"
```

---

### Task 11: 로그인 페이지 및 최상위 레이아웃 수정

**Files:**
- Modify: `frontend/app/admin/page.tsx`
- Modify: `frontend/app/admin/layout.tsx`

- [ ] **Step 1: admin/layout.tsx 최소화 (쉘 제거)**

`frontend/app/admin/layout.tsx` 전체를 아래로 교체한다:

```tsx
export default function AdminLayout({ children }: { children: React.ReactNode }) {
  return <>{children}</>
}
```

- [ ] **Step 2: admin/page.tsx 로그인 페이지로 교체**

`frontend/app/admin/page.tsx` 전체를 아래로 교체한다:

```tsx
import AdminLoginForm from '@/components/system/AdminLoginForm'

export default function AdminLoginPage() {
  return <AdminLoginForm />
}
```

- [ ] **Step 3: 커밋**

```bash
git add frontend/app/admin/layout.tsx frontend/app/admin/page.tsx
git commit -m "feat: implement admin login page at /admin root"
```

---

### Task 12: 동작 확인

- [ ] **Step 1: dev 서버 실행**

```bash
cd frontend && pnpm dev
```

- [ ] **Step 2: 시나리오 확인**

| 시나리오 | 기대 동작 |
|----------|-----------|
| `http://localhost:3000/admin` 접근 (비인증) | 로그인 폼 표시 |
| 잘못된 아이디/비밀번호로 로그인 | 인라인 에러 메시지 표시 |
| 올바른 계정으로 로그인 | `/admin/dashboard`로 이동, 어드민 쉘(헤더+사이드바) 표시 |
| 로그아웃 버튼 클릭 | `/admin`으로 이동, 로그인 폼 표시 |
| 로그인 상태에서 `/admin` 접근 | `/admin/dashboard`로 자동 리다이렉트 |
| `/admin/members` 등 비인증 접근 | `/admin`으로 리다이렉트 |

- [ ] **Step 3: 전체 테스트 실행**

```bash
cd frontend && pnpm vitest run
```
Expected: All tests pass

- [ ] **Step 4: feature 브랜치 push**

```bash
git push origin feature/auth
```
