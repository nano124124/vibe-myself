# Frontend Setup Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 기존 Pages Router 스캐폴드를 삭제하고, 스펙에 정의된 Next.js 15 App Router 구조로 프론트엔드를 재생성한다.

**Architecture:** 단일 Next.js 앱에서 `(shop)` Route Group(루트 URL)과 `admin` 세그먼트를 분리. Tailwind CSS + shadcn/ui로 UI, Tanstack Query + Zustand로 상태 관리, JWT httpOnly 쿠키 인증, middleware로 어드민 보호.

**Tech Stack:** Next.js 15 (App Router), React 19, TypeScript, Tailwind CSS v4, shadcn/ui, Tanstack Query v5, Zustand, Axios, Vitest, pnpm

**Spec:** `docs/superpowers/specs/2026-03-24-frontend-setup-design.md`

---

## Chunk 1: Scaffold, 의존성, shadcn, 폴더 구조

### Task 1: 기존 frontend 삭제 및 Next.js 15 앱 생성

**Files:**
- Delete: `frontend/` (기존 Pages Router 스캐폴드 전체)
- Create: `frontend/` (새 Next.js 15 App Router 앱)

- [ ] **Step 1: 기존 frontend 디렉토리 삭제**

프로젝트 루트(`/Users/nyj/Documents/git/vibe-myself`)에서 실행:

```bash
rm -rf frontend
```

- [ ] **Step 2: Next.js 15 앱 생성**

```bash
pnpm create next-app frontend
```

프롬프트 응답:
```
✔ Would you like to use TypeScript? › Yes
✔ Would you like to use ESLint? › Yes
✔ Would you like to use Tailwind CSS? › Yes
✔ Would you like your code inside a `src/` directory? › No
✔ Would you like to use App Router? › Yes
✔ Would you like to use Turbopack for next dev? › Yes
✔ What import alias would you like configured? › @/*
```

- [ ] **Step 3: 기동 확인**

```bash
cd frontend && pnpm dev
```

`http://localhost:3000` 접속해서 Next.js 기본 페이지가 보이면 성공. `Ctrl+C`로 종료.

- [ ] **Step 4: 커밋**

```bash
cd /Users/nyj/Documents/git/vibe-myself
git add frontend/
git commit -m "feat: initialize Next.js 15 App Router frontend"
```

---

### Task 2: 추가 의존성 설치

**Files:**
- Modify: `frontend/package.json`

- [ ] **Step 1: 런타임 의존성 설치**

```bash
cd /Users/nyj/Documents/git/vibe-myself/frontend
pnpm add @tanstack/react-query axios zustand
```

- [ ] **Step 2: 개발 의존성 설치 (테스트 + 개발 도구)**

```bash
pnpm add -D @tanstack/react-query-devtools vitest @vitejs/plugin-react jsdom \
  @testing-library/react @testing-library/jest-dom @testing-library/user-event
```

- [ ] **Step 3: package.json에 test 스크립트 추가**

`frontend/package.json`의 `"scripts"` 섹션에 추가:

```json
"test": "vitest run",
"test:watch": "vitest"
```

- [ ] **Step 4: vitest.config.ts 생성**

`frontend/vitest.config.ts` 생성:

```typescript
import { defineConfig } from 'vitest/config'
import react from '@vitejs/plugin-react'
import path from 'path'

export default defineConfig({
  plugins: [react()],
  test: {
    environment: 'jsdom',
    setupFiles: ['./vitest.setup.ts'],
    globals: true,
  },
  resolve: {
    alias: {
      '@': path.resolve(__dirname, '.'),
    },
  },
})
```

- [ ] **Step 5: vitest.setup.ts 생성**

`frontend/vitest.setup.ts` 생성:

```typescript
import '@testing-library/jest-dom'
```

- [ ] **Step 6: 설치 확인**

```bash
pnpm list @tanstack/react-query axios zustand vitest @vitejs/plugin-react jsdom
```

Expected: 6개 패키지 모두 버전 출력

- [ ] **Step 7: 커밋**

```bash
cd /Users/nyj/Documents/git/vibe-myself
git add frontend/
git commit -m "chore: add tanstack-query, axios, zustand, vitest dependencies"
```

---

### Task 3: shadcn/ui 초기화

**Files:**
- Create: `frontend/components/ui/` (shadcn 자동 생성)
- Create: `frontend/components.json`
- Modify: `frontend/app/globals.css` (CSS 변수 추가)

- [ ] **Step 1: shadcn 초기화**

```bash
cd /Users/nyj/Documents/git/vibe-myself/frontend
pnpm dlx shadcn@latest init
```

프롬프트 응답:
```
✔ Which style would you like to use? › Default
✔ Which color would you like to use as the base color? › Slate
✔ Would you like to use CSS variables for theming? › Yes
```

- [ ] **Step 2: 초기화 확인**

```bash
cat components.json
```

Expected: `"rsc": true`, `"tsx": true`, `"tailwind"` 설정 포함

- [ ] **Step 3: 커밋**

```bash
cd /Users/nyj/Documents/git/vibe-myself
git add frontend/
git commit -m "chore: initialize shadcn/ui"
```

---

### Task 4: App Router 폴더 구조 및 placeholder 페이지 생성

**Files:**
- Create: `frontend/app/(shop)/` — Route Group 및 하위 페이지
- Create: `frontend/app/admin/` — 어드민 라우트 및 하위 페이지
- Create: `frontend/components/shop/`, `components/admin/`, `components/common/`
- Create: `frontend/hooks/`, `frontend/store/`, `frontend/types/`

모든 placeholder `page.tsx`는 동일한 패턴으로 생성:
```typescript
export default function PageName() {
  return <div>PageName</div>
}
```

- [ ] **Step 1: (shop) 라우트 디렉토리 생성**

```bash
cd /Users/nyj/Documents/git/vibe-myself/frontend
mkdir -p "app/(shop)/products/[id]"
mkdir -p "app/(shop)/cart"
mkdir -p "app/(shop)/order/complete"
mkdir -p "app/(shop)/auth/login"
mkdir -p "app/(shop)/auth/register"
mkdir -p "app/(shop)/mypage/orders"
mkdir -p "app/(shop)/mypage/claims"
mkdir -p "app/(shop)/events/[id]"
mkdir -p "app/(shop)/promotions/[id]"
```

- [ ] **Step 2: admin 라우트 디렉토리 생성**

```bash
mkdir -p "app/admin/products/[id]"
mkdir -p app/admin/orders
mkdir -p app/admin/members
mkdir -p app/admin/claims
mkdir -p app/admin/events
mkdir -p app/admin/promotions
```

- [ ] **Step 3: 기타 디렉토리 생성**

작업 디렉토리: `frontend/` (`cd /Users/nyj/Documents/git/vibe-myself/frontend`)

```bash
mkdir -p components/shop components/admin components/common
mkdir -p hooks store types lib/__tests__
```

- [ ] **Step 4: (shop) placeholder 페이지 생성**

아래 파일들을 각각 생성. 내용은 파일명에 맞춰 `export default function XxxPage() { return <div>Xxx</div> }` 패턴:

| 파일 | 함수명 |
|------|--------|
| `app/(shop)/page.tsx` | `HomePage` |
| `app/(shop)/products/page.tsx` | `ProductsPage` |
| `app/(shop)/products/[id]/page.tsx` | `ProductDetailPage` |
| `app/(shop)/cart/page.tsx` | `CartPage` |
| `app/(shop)/order/page.tsx` | `OrderPage` |
| `app/(shop)/order/complete/page.tsx` | `OrderCompletePage` |
| `app/(shop)/auth/login/page.tsx` | `LoginPage` |
| `app/(shop)/auth/register/page.tsx` | `RegisterPage` |
| `app/(shop)/mypage/page.tsx` | `MypagePage` |
| `app/(shop)/mypage/orders/page.tsx` | `MyOrdersPage` |
| `app/(shop)/mypage/claims/page.tsx` | `MyClaimsPage` |
| `app/(shop)/events/page.tsx` | `EventsPage` |
| `app/(shop)/events/[id]/page.tsx` | `EventDetailPage` |
| `app/(shop)/promotions/page.tsx` | `PromotionsPage` |
| `app/(shop)/promotions/[id]/page.tsx` | `PromotionDetailPage` |

예시 (`app/(shop)/page.tsx`):
```typescript
export default function HomePage() {
  return <div>Home</div>
}
```

- [ ] **Step 5: admin placeholder 페이지 생성**

| 파일 | 함수명 |
|------|--------|
| `app/admin/page.tsx` | `AdminDashboardPage` |
| `app/admin/products/page.tsx` | `AdminProductsPage` |
| `app/admin/products/[id]/page.tsx` | `AdminProductDetailPage` |
| `app/admin/orders/page.tsx` | `AdminOrdersPage` |
| `app/admin/members/page.tsx` | `AdminMembersPage` |
| `app/admin/claims/page.tsx` | `AdminClaimsPage` |
| `app/admin/events/page.tsx` | `AdminEventsPage` |
| `app/admin/promotions/page.tsx` | `AdminPromotionsPage` |

- [ ] **Step 6: 빈 디렉토리 추적용 .gitkeep 생성**

```bash
touch components/shop/.gitkeep
touch components/admin/.gitkeep
touch components/common/.gitkeep
touch hooks/.gitkeep
touch types/.gitkeep
touch store/.gitkeep
```

- [ ] **Step 7: 빌드 확인**

```bash
pnpm build
```

Expected: `✓ Compiled successfully` (placeholder 페이지들 포함해서 빌드 성공)

- [ ] **Step 8: 커밋**

```bash
cd /Users/nyj/Documents/git/vibe-myself
git add frontend/
git commit -m "feat: scaffold App Router folder structure with placeholder pages"
```

---

## Chunk 2: lib, store, providers, middleware, config, env

### Task 5: lib/api.ts — Axios 인스턴스 (TDD)

**Files:**
- Create: `frontend/lib/api.ts`
- Create: `frontend/lib/__tests__/api.test.ts`

- [ ] **Step 1: 테스트 작성**

`frontend/lib/__tests__/api.test.ts`:

```typescript
import { describe, it, expect, beforeEach } from 'vitest'

describe('api instance', () => {
  beforeEach(() => {
    process.env.NEXT_PUBLIC_API_URL = 'http://localhost:8080'
  })

  it('exports an axios instance', async () => {
    const { default: api } = await import('../api')
    expect(api).toBeDefined()
    expect(typeof api.get).toBe('function')
    expect(typeof api.post).toBe('function')
  })

  it('sets withCredentials true for cookie forwarding', async () => {
    const { default: api } = await import('../api')
    expect(api.defaults.withCredentials).toBe(true)
  })

  it('sets Content-Type to application/json', async () => {
    const { default: api } = await import('../api')
    expect(api.defaults.headers.common['Content-Type']).toBe('application/json')
  })
})
```

- [ ] **Step 2: 테스트 실행 — 실패 확인**

```bash
cd /Users/nyj/Documents/git/vibe-myself/frontend
pnpm test lib/__tests__/api.test.ts
```

Expected: FAIL (lib/api.ts 아직 없음)

- [ ] **Step 3: lib/api.ts 구현**

`frontend/lib/api.ts`:

```typescript
import axios from 'axios'

const api = axios.create({
  baseURL: process.env.NEXT_PUBLIC_API_URL ?? 'http://localhost:8080',
  headers: {
    common: {
      'Content-Type': 'application/json',
    },
  },
  withCredentials: true,
})

export default api
```

- [ ] **Step 4: 테스트 실행 — 통과 확인**

```bash
pnpm test lib/__tests__/api.test.ts
```

Expected: PASS (3개 테스트 통과)

- [ ] **Step 5: 커밋**

```bash
cd /Users/nyj/Documents/git/vibe-myself
git add frontend/lib/
git commit -m "feat: add axios api instance"
```

---

### Task 6: lib/queryClient.ts — TanStack Query 설정

**Files:**
- Create: `frontend/lib/queryClient.ts`

이 파일은 QueryClient 인스턴스를 생성하는 단순 설정 파일이므로 별도 테스트 불필요.

- [ ] **Step 1: lib/queryClient.ts 생성**

`frontend/lib/queryClient.ts`:

```typescript
import { QueryClient } from '@tanstack/react-query'

export const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 1000 * 60,    // 1분
      retry: 1,
    },
  },
})
```

- [ ] **Step 2: 커밋**

```bash
cd /Users/nyj/Documents/git/vibe-myself
git add frontend/lib/queryClient.ts
git commit -m "feat: add TanStack Query client configuration"
```

---

### Task 7: lib/utils.ts — 공통 유틸

**Files:**
- Create: `frontend/lib/utils.ts`

shadcn/ui가 `cn()` 함수를 자동 생성해줄 수 있으나, 없을 경우 아래와 같이 생성.

- [ ] **Step 1: lib/utils.ts 존재 여부 확인**

```bash
cat /Users/nyj/Documents/git/vibe-myself/frontend/lib/utils.ts
```

shadcn init이 이미 생성했다면 Pass. 없다면 Step 2 진행.

- [ ] **Step 2: lib/utils.ts 생성 (없는 경우만)**

```bash
pnpm add clsx tailwind-merge
```

`frontend/lib/utils.ts`:

```typescript
import { clsx, type ClassValue } from 'clsx'
import { twMerge } from 'tailwind-merge'

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs))
}
```

- [ ] **Step 3: 커밋 (변경사항이 있는 경우만)**

```bash
cd /Users/nyj/Documents/git/vibe-myself
git add frontend/lib/utils.ts frontend/package.json frontend/pnpm-lock.yaml
git commit -m "feat: add cn utility from shadcn"
```

---

### Task 8: store/cart.ts — Zustand 장바구니 스토어 (TDD)

**Files:**
- Create: `frontend/store/cart.ts`
- Create: `frontend/store/__tests__/cart.test.ts`

- [ ] **Step 1: 테스트 작성**

`frontend/store/__tests__/cart.test.ts`:

```typescript
import { describe, it, expect, beforeEach } from 'vitest'
import { useCartStore } from '../cart'

describe('cart store', () => {
  beforeEach(() => {
    useCartStore.setState({ items: [] })
  })

  it('starts with empty items', () => {
    const { items } = useCartStore.getState()
    expect(items).toEqual([])
  })

  it('adds an item to cart', () => {
    const { addItem } = useCartStore.getState()
    addItem({ id: 1, name: '상품A', price: 10000, quantity: 1 })
    const { items } = useCartStore.getState()
    expect(items).toHaveLength(1)
    expect(items[0].name).toBe('상품A')
  })

  it('increments quantity if same item added again', () => {
    const { addItem } = useCartStore.getState()
    addItem({ id: 1, name: '상품A', price: 10000, quantity: 1 })
    addItem({ id: 1, name: '상품A', price: 10000, quantity: 1 })
    const { items } = useCartStore.getState()
    expect(items).toHaveLength(1)
    expect(items[0].quantity).toBe(2)
  })

  it('removes an item from cart', () => {
    const { addItem, removeItem } = useCartStore.getState()
    addItem({ id: 1, name: '상품A', price: 10000, quantity: 1 })
    removeItem(1)
    const { items } = useCartStore.getState()
    expect(items).toHaveLength(0)
  })

  it('clears all items', () => {
    const { addItem, clearCart } = useCartStore.getState()
    addItem({ id: 1, name: '상품A', price: 10000, quantity: 1 })
    addItem({ id: 2, name: '상품B', price: 20000, quantity: 2 })
    clearCart()
    const { items } = useCartStore.getState()
    expect(items).toHaveLength(0)
  })
})
```

- [ ] **Step 2: 테스트 실행 — 실패 확인**

```bash
cd /Users/nyj/Documents/git/vibe-myself/frontend
pnpm test store/__tests__/cart.test.ts
```

Expected: FAIL

- [ ] **Step 3: store/cart.ts 구현**

`frontend/store/cart.ts`:

```typescript
import { create } from 'zustand'
import { persist } from 'zustand/middleware'

interface CartItem {
  id: number
  name: string
  price: number
  quantity: number
}

interface CartStore {
  items: CartItem[]
  addItem: (item: CartItem) => void
  removeItem: (id: number) => void
  clearCart: () => void
}

export const useCartStore = create<CartStore>()(
  persist(
    (set, get) => ({
      items: [],
      addItem: (item) => {
        const existing = get().items.find((i) => i.id === item.id)
        if (existing) {
          set({
            items: get().items.map((i) =>
              i.id === item.id ? { ...i, quantity: i.quantity + item.quantity } : i
            ),
          })
        } else {
          set({ items: [...get().items, item] })
        }
      },
      removeItem: (id) => {
        set({ items: get().items.filter((i) => i.id !== id) })
      },
      clearCart: () => set({ items: [] }),
    }),
    { name: 'cart-storage' }
  )
)
```

- [ ] **Step 4: 테스트 실행 — 통과 확인**

```bash
pnpm test store/__tests__/cart.test.ts
```

Expected: PASS (5개 테스트 통과)

- [ ] **Step 5: 커밋**

```bash
cd /Users/nyj/Documents/git/vibe-myself
git add frontend/store/
git commit -m "feat: add cart zustand store with persist"
```

---

### Task 9: app/layout.tsx — Providers 등록

**Files:**
- Modify: `frontend/app/layout.tsx`
- Create: `frontend/components/common/Providers.tsx`

Provider는 `'use client'`가 필요하므로 별도 컴포넌트로 분리. `layout.tsx`는 Server Component로 유지.

- [ ] **Step 1: Providers 컴포넌트 생성**

`frontend/components/common/Providers.tsx`:

```typescript
'use client'

import { QueryClientProvider } from '@tanstack/react-query'
import { ReactQueryDevtools } from '@tanstack/react-query-devtools'
import { queryClient } from '@/lib/queryClient'

export default function Providers({ children }: { children: React.ReactNode }) {
  return (
    <QueryClientProvider client={queryClient}>
      {children}
      <ReactQueryDevtools initialIsOpen={false} />
    </QueryClientProvider>
  )
}
```

- [ ] **Step 2: app/layout.tsx에 Providers 등록**

기존 `frontend/app/layout.tsx`를 열어 `<body>` 안을 `<Providers>`로 감싸기:

```typescript
import type { Metadata } from 'next'
import { Geist, Geist_Mono } from 'next/font/google'
import Providers from '@/components/common/Providers'
import './globals.css'

const geistSans = Geist({ variable: '--font-geist-sans', subsets: ['latin'] })
const geistMono = Geist_Mono({ variable: '--font-geist-mono', subsets: ['latin'] })

export const metadata: Metadata = {
  title: 'Vibe Myself',
  description: 'Vibe Myself Shopping Mall',
}

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="ko">
      <body className={`${geistSans.variable} ${geistMono.variable} antialiased`}>
        <Providers>{children}</Providers>
      </body>
    </html>
  )
}
```

- [ ] **Step 3: 빌드 확인**

```bash
cd /Users/nyj/Documents/git/vibe-myself/frontend
pnpm build
```

Expected: 빌드 성공

- [ ] **Step 4: 커밋**

```bash
cd /Users/nyj/Documents/git/vibe-myself
git add frontend/app/layout.tsx frontend/components/common/Providers.tsx
git commit -m "feat: register QueryClientProvider in root layout"
```

---

### Task 10: (shop)/layout.tsx + admin/layout.tsx 생성

**Files:**
- Create: `frontend/app/(shop)/layout.tsx`
- Create: `frontend/app/admin/layout.tsx`

각 영역의 공통 레이아웃. 지금은 skeleton만 생성.

- [ ] **Step 1: (shop)/layout.tsx 생성**

`frontend/app/(shop)/layout.tsx`:

```typescript
export default function ShopLayout({ children }: { children: React.ReactNode }) {
  return (
    <div>
      <header>
        <nav>Vibe Myself</nav>
      </header>
      <main>{children}</main>
      <footer>Footer</footer>
    </div>
  )
}
```

- [ ] **Step 2: admin/layout.tsx 생성**

`frontend/app/admin/layout.tsx`:

```typescript
export default function AdminLayout({ children }: { children: React.ReactNode }) {
  return (
    <div style={{ display: 'flex' }}>
      <aside>
        <nav>Admin Menu</nav>
      </aside>
      <main>{children}</main>
    </div>
  )
}
```

- [ ] **Step 3: 커밋**

```bash
cd /Users/nyj/Documents/git/vibe-myself
git add "frontend/app/(shop)/layout.tsx" frontend/app/admin/layout.tsx
git commit -m "feat: add shop and admin skeleton layouts"
```

---

### Task 11: middleware.ts — 어드민 인증 보호

**Files:**
- Create: `frontend/middleware.ts`

Next.js middleware는 Edge Runtime에서 실행돼 일반 Vitest로 단위 테스트가 어렵다. 로직을 순수 함수로 분리해 테스트한다.

- [ ] **Step 1: 인증 체크 순수 함수 테스트 작성**

`frontend/lib/__tests__/authGuard.test.ts`:

```typescript
import { describe, it, expect } from 'vitest'
import { getRedirectPath } from '../authGuard'

describe('getRedirectPath', () => {
  it('returns /auth/login when no token', () => {
    expect(getRedirectPath(null)).toBe('/auth/login')
  })

  it('returns / when token exists but role is not ADMIN', () => {
    const payload = btoa(JSON.stringify({ role: 'ROLE_USER' }))
    const token = `header.${payload}.sig`
    expect(getRedirectPath(token)).toBe('/')
  })

  it('returns null (allow) when role is ADMIN', () => {
    const payload = btoa(JSON.stringify({ role: 'ROLE_ADMIN' }))
    const token = `header.${payload}.sig`
    expect(getRedirectPath(token)).toBeNull()
  })
})
```

- [ ] **Step 2: 테스트 실행 — 실패 확인**

```bash
cd /Users/nyj/Documents/git/vibe-myself/frontend
pnpm test lib/__tests__/authGuard.test.ts
```

Expected: FAIL

- [ ] **Step 3: lib/authGuard.ts 구현**

`frontend/lib/authGuard.ts`:

```typescript
/**
 * JWT payload에서 role을 읽어 어드민 접근 가능 여부를 판단.
 * @returns null이면 통과, string이면 해당 경로로 리다이렉트
 */
export function getRedirectPath(token: string | null): string | null {
  if (!token) return '/auth/login'

  try {
    const payloadBase64 = token.split('.')[1]
    const payload = JSON.parse(atob(payloadBase64))
    if (payload.role !== 'ROLE_ADMIN') return '/'
    return null
  } catch {
    return '/auth/login'
  }
}
```

- [ ] **Step 4: 테스트 실행 — 통과 확인**

```bash
pnpm test lib/__tests__/authGuard.test.ts
```

Expected: PASS (3개 테스트 통과)

- [ ] **Step 5: middleware.ts 생성**

`frontend/middleware.ts`:

```typescript
import { NextRequest, NextResponse } from 'next/server'
import { getRedirectPath } from '@/lib/authGuard'

export function middleware(request: NextRequest) {
  const token = request.cookies.get('access_token')?.value ?? null
  const redirectPath = getRedirectPath(token)

  if (redirectPath) {
    return NextResponse.redirect(new URL(redirectPath, request.url))
  }

  return NextResponse.next()
}

export const config = {
  matcher: ['/admin/:path*'],
}
```

- [ ] **Step 6: 전체 테스트 실행**

```bash
pnpm test
```

Expected: 모든 테스트 PASS

- [ ] **Step 7: 커밋**

```bash
cd /Users/nyj/Documents/git/vibe-myself
git add frontend/middleware.ts frontend/lib/authGuard.ts frontend/lib/__tests__/authGuard.test.ts
git commit -m "feat: add admin route middleware with role-based auth guard"
```

---

### Task 12: next.config.ts — 백엔드 프록시 설정

**Files:**
- Modify: `frontend/next.config.ts`

- [ ] **Step 1: next.config.ts 수정**

`frontend/next.config.ts`:

```typescript
import type { NextConfig } from 'next'

const nextConfig: NextConfig = {
  async rewrites() {
    return [
      {
        source: '/api/:path*',
        destination: `${process.env.NEXT_PUBLIC_API_URL ?? 'http://localhost:8080'}/:path*`,
      },
    ]
  },
}

export default nextConfig
```

- [ ] **Step 2: 커밋**

```bash
cd /Users/nyj/Documents/git/vibe-myself
git add frontend/next.config.ts
git commit -m "feat: add backend proxy rewrite in next.config.ts"
```

---

### Task 13: .env.local 및 .gitignore 설정

**Files:**
- Create: `frontend/.env.local`
- Modify: `frontend/.gitignore`

- [ ] **Step 1: .env.local 생성**

`frontend/.env.local`:

```
NEXT_PUBLIC_API_URL=http://localhost:8080
```

- [ ] **Step 2: frontend/.gitignore에 .env.local 추가**

`frontend/.gitignore`에 아래 항목이 없다면 추가:

```
.env.local
.env*.local
```

Next.js 기본 `.gitignore`는 이미 포함하고 있을 가능성이 높으므로 먼저 확인 후 없으면 추가.

- [ ] **Step 3: .env.local이 git에 추적되지 않는지 확인**

```bash
cd /Users/nyj/Documents/git/vibe-myself
git status frontend/.env.local
```

Expected: `.env.local`이 목록에 없어야 함 (ignored)

- [ ] **Step 4: 커밋**

```bash
git add frontend/.gitignore
git commit -m "chore: configure .env.local and verify gitignore"
```

---

### Task 14: 전체 검증

- [ ] **Step 1: 전체 테스트 통과 확인**

```bash
cd /Users/nyj/Documents/git/vibe-myself/frontend
pnpm test
```

Expected: 모든 테스트 PASS

- [ ] **Step 2: 프로덕션 빌드 확인**

```bash
pnpm build
```

Expected: `✓ Compiled successfully`

- [ ] **Step 3: 개발 서버 기동 및 라우트 확인**

```bash
pnpm dev
```

브라우저에서 아래 URL 접속 확인:
- `http://localhost:3000` → `<div>Home</div>` 출력
- `http://localhost:3000/products` → `<div>Products</div>` 출력
- `http://localhost:3000/admin` → `/auth/login`으로 리다이렉트 (토큰 없으므로)
- `http://localhost:3000/auth/login` → `<div>Login</div>` 출력

- [ ] **Step 4: 최종 커밋 및 push**

```bash
cd /Users/nyj/Documents/git/vibe-myself
git push origin master
```
