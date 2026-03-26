# Admin Sidebar Menu Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 어드민 대시보드 좌측 사이드바에 DB 기반 대/소메뉴를 아코디언 방식으로 렌더링한다.

**Architecture:** TanStack Query로 `GET /api/admin/system/menus`에서 메뉴 트리를 가져와 `AdminSidebar` 클라이언트 컴포넌트에서 렌더링한다. 대메뉴 클릭 시 소메뉴 펼침/접힘(단일 열림), 소메뉴는 `<Link>`로 페이지 이동한다.

**Tech Stack:** Next.js 16 (App Router), React 19, TypeScript, TanStack Query v5, Axios, Tailwind CSS v4, `@fontsource/pretendard`, Vitest + React Testing Library

---

## File Map

| 파일 | 작업 | 책임 |
|------|------|------|
| `frontend/types/system.types.ts` | 수정 | `MenuResponse` 타입 추가 |
| `frontend/api/system.api.ts` | 수정 | `getMenus()` API 함수 추가 |
| `frontend/hooks/system/useAdminMenus.ts` | 생성 | TanStack Query 훅 |
| `frontend/hooks/system/useAdminMenus.test.ts` | 생성 | 훅 단위 테스트 |
| `frontend/components/system/AdminSidebar.tsx` | 생성 | 아코디언 사이드바 컴포넌트 |
| `frontend/components/system/AdminSidebar.test.tsx` | 생성 | 컴포넌트 단위 테스트 |
| `frontend/app/admin/layout.tsx` | 수정 | Pretendard 폰트 적용 |
| `frontend/app/admin/(main)/layout.tsx` | 수정 | hardcoded aside → AdminSidebar 교체 |

---

## Chunk 1: 데이터 레이어 (타입 + API + 훅)

### Task 1: MenuResponse 타입 추가

**Files:**
- Modify: `frontend/types/system.types.ts`

- [ ] **Step 1: `MenuResponse` 인터페이스 추가**

  `frontend/types/system.types.ts` 파일 끝에 추가:

  ```typescript
  export interface MenuResponse {
    menuNo: number // Java Long → JS number (현재 메뉴 수 기준 MAX_SAFE_INTEGER 범위 내 안전)
    menuNm: string
    menuUrl: string | null
    sortOrd: number
    children: MenuResponse[]
  }
  ```

- [ ] **Step 2: 타입스크립트 컴파일 확인**

  `frontend/` 디렉토리에서:
  ```bash
  pnpm tsc --noEmit
  ```
  Expected: 에러 없음

---

### Task 2: `getMenus()` API 함수

**Files:**
- Modify: `frontend/api/system.api.ts`
- Test: `frontend/api/system.api.test.ts` (새로 생성)

- [ ] **Step 1: 테스트 파일 생성 (failing)**

  `frontend/api/system.api.test.ts`:

  ```typescript
  import { describe, it, expect, vi, beforeEach } from 'vitest'
  import api from '@/lib/api'
  import { getMenus } from './system.api'
  import type { MenuResponse } from '@/types/system.types'

  vi.mock('@/lib/api', () => ({
    default: {
      get: vi.fn(),
      post: vi.fn(),
    },
  }))

  const mockMenus: MenuResponse[] = [
    {
      menuNo: 1,
      menuNm: '대시보드',
      menuUrl: '/admin/dashboard',
      sortOrd: 1,
      children: [],
    },
    {
      menuNo: 2,
      menuNm: '상품관리',
      menuUrl: null,
      sortOrd: 2,
      children: [
        {
          menuNo: 11,
          menuNm: '상품 목록',
          menuUrl: '/admin/products',
          sortOrd: 1,
          children: [],
        },
      ],
    },
  ]

  describe('getMenus', () => {
    beforeEach(() => {
      vi.clearAllMocks()
    })

    it('메뉴 트리를 반환한다', async () => {
      vi.mocked(api.get).mockResolvedValueOnce({
        data: { success: true, data: mockMenus, message: null },
      })

      const result = await getMenus()

      expect(api.get).toHaveBeenCalledWith('/api/admin/system/menus')
      expect(result).toEqual(mockMenus)
    })
  })
  ```

- [ ] **Step 2: 테스트 실행 — 실패 확인**

  ```bash
  cd frontend && pnpm vitest run api/system.api.test.ts
  ```
  Expected: FAIL — `getMenus is not a function` (아직 구현 안 됨)

- [ ] **Step 3: `getMenus()` 구현**

  `frontend/api/system.api.ts` 파일에 추가:

  ```typescript
  import type { MenuResponse } from '@/types/system.types'

  // 기존 import 줄 아래에 타입 추가
  // 기존 loginAdmin, logoutAdmin 함수 아래에 추가:

  export const getMenus = (): Promise<MenuResponse[]> =>
    api.get<{ success: boolean; data: MenuResponse[]; message: string | null }>(
      '/api/admin/system/menus'
    ).then((res) => res.data.data)
  ```

  최종 파일 전체:
  ```typescript
  import api from '@/lib/api'
  import type { LoginAdminRequest, MenuResponse } from '@/types/system.types'

  export const loginAdmin = (data: LoginAdminRequest): Promise<void> =>
    api.post('/api/admin/system/login', data).then(() => undefined)

  export const logoutAdmin = (): Promise<void> =>
    api.post('/api/admin/system/logout').then(() => undefined)

  export const getMenus = (): Promise<MenuResponse[]> =>
    api
      .get<{ success: boolean; data: MenuResponse[]; message: string | null }>(
        '/api/admin/system/menus'
      )
      .then((res) => res.data.data)
  ```

- [ ] **Step 4: 테스트 실행 — 통과 확인**

  ```bash
  cd frontend && pnpm vitest run api/system.api.test.ts
  ```
  Expected: PASS (1 test passed)

- [ ] **Step 5: 커밋**

  ```bash
  git add frontend/types/system.types.ts frontend/api/system.api.ts frontend/api/system.api.test.ts
  git commit -m "feat: add MenuResponse type and getMenus API function"
  ```

---

### Task 3: `useAdminMenus` 훅

**Files:**
- Create: `frontend/hooks/system/useAdminMenus.ts`
- Create: `frontend/hooks/system/useAdminMenus.test.ts`

- [ ] **Step 1: 테스트 파일 작성 (failing)**

  `frontend/hooks/system/useAdminMenus.test.ts`:

  ```typescript
  import { describe, it, expect, vi, beforeEach } from 'vitest'
  import { renderHook, waitFor } from '@testing-library/react'
  import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
  import React from 'react'
  import { useAdminMenus } from './useAdminMenus'
  import * as systemApi from '@/api/system.api'
  import type { MenuResponse } from '@/types/system.types'

  vi.mock('@/api/system.api')

  const createWrapper = () => {
    const queryClient = new QueryClient({
      defaultOptions: { queries: { retry: false } },
    })
    return ({ children }: { children: React.ReactNode }) =>
      React.createElement(QueryClientProvider, { client: queryClient }, children)
  }

  const mockMenus: MenuResponse[] = [
    { menuNo: 1, menuNm: '대시보드', menuUrl: '/admin/dashboard', sortOrd: 1, children: [] },
  ]

  describe('useAdminMenus', () => {
    beforeEach(() => {
      vi.clearAllMocks()
    })

    it('메뉴 목록을 반환한다', async () => {
      vi.mocked(systemApi.getMenus).mockResolvedValueOnce(mockMenus)

      const { result } = renderHook(() => useAdminMenus(), { wrapper: createWrapper() })

      await waitFor(() => expect(result.current.isSuccess).toBe(true))
      expect(result.current.data).toEqual(mockMenus)
    })

    it('API 실패 시 isError가 true가 된다', async () => {
      vi.mocked(systemApi.getMenus).mockRejectedValueOnce(new Error('Network Error'))

      const { result } = renderHook(() => useAdminMenus(), { wrapper: createWrapper() })

      await waitFor(() => expect(result.current.isError).toBe(true))
    })
  })
  ```

- [ ] **Step 2: 테스트 실행 — 실패 확인**

  ```bash
  cd frontend && pnpm vitest run hooks/system/useAdminMenus.test.ts
  ```
  Expected: FAIL — `useAdminMenus is not a function`

- [ ] **Step 3: 훅 구현**

  `frontend/hooks/system/useAdminMenus.ts`:

  ```typescript
  import { useQuery } from '@tanstack/react-query'
  import { getMenus } from '@/api/system.api'

  export const useAdminMenus = () =>
    useQuery({
      queryKey: ['system', 'menus'],
      queryFn: getMenus,
    })
  ```

- [ ] **Step 4: 테스트 실행 — 통과 확인**

  ```bash
  cd frontend && pnpm vitest run hooks/system/useAdminMenus.test.ts
  ```
  Expected: PASS (2 tests passed)

- [ ] **Step 5: 커밋**

  ```bash
  git add frontend/hooks/system/useAdminMenus.ts frontend/hooks/system/useAdminMenus.test.ts
  git commit -m "feat: add useAdminMenus TanStack Query hook"
  ```

---

## Chunk 2: AdminSidebar 컴포넌트

### Task 4: AdminSidebar 컴포넌트

**Files:**
- Create: `frontend/components/system/AdminSidebar.tsx`
- Create: `frontend/components/system/AdminSidebar.test.tsx`

- [ ] **Step 1: 테스트 파일 작성 (failing)**

  `frontend/components/system/AdminSidebar.test.tsx`:

  ```typescript
  import { describe, it, expect, vi, beforeEach } from 'vitest'
  import { render, screen } from '@testing-library/react'
  import userEvent from '@testing-library/user-event'
  import AdminSidebar from './AdminSidebar'
  import type { MenuResponse } from '@/types/system.types'

  // next/navigation 모킹
  vi.mock('next/navigation', () => ({
    usePathname: vi.fn(() => '/admin/dashboard'),
  }))

  import * as nextNavigation from 'next/navigation'

  // useAdminMenus 모킹
  vi.mock('@/hooks/system/useAdminMenus')
  import * as useAdminMenusModule from '@/hooks/system/useAdminMenus'

  const mockMenus: MenuResponse[] = [
    { menuNo: 1, menuNm: '대시보드', menuUrl: '/admin/dashboard', sortOrd: 1, children: [] },
    {
      menuNo: 2,
      menuNm: '상품관리',
      menuUrl: null,
      sortOrd: 2,
      children: [
        { menuNo: 11, menuNm: '상품 목록', menuUrl: '/admin/products', sortOrd: 1, children: [] },
        { menuNo: 12, menuNm: '상품 등록', menuUrl: '/admin/products/new', sortOrd: 2, children: [] },
      ],
    },
  ]

  describe('AdminSidebar', () => {
    beforeEach(() => {
      vi.clearAllMocks()
    })

    it('로딩 중에는 스켈레톤을 표시한다', () => {
      vi.mocked(useAdminMenusModule.useAdminMenus).mockReturnValue({
        data: undefined,
        isLoading: true,
        isError: false,
        isSuccess: false,
      } as ReturnType<typeof useAdminMenusModule.useAdminMenus>)

      render(<AdminSidebar />)
      expect(screen.getAllByTestId('menu-skeleton')).toHaveLength(5)
    })

    it('에러 시 에러 메시지를 표시한다', () => {
      vi.mocked(useAdminMenusModule.useAdminMenus).mockReturnValue({
        data: undefined,
        isLoading: false,
        isError: true,
        isSuccess: false,
      } as ReturnType<typeof useAdminMenusModule.useAdminMenus>)

      render(<AdminSidebar />)
      expect(screen.getByText('메뉴를 불러올 수 없습니다')).toBeInTheDocument()
    })

    it('대메뉴 목록을 렌더링한다', () => {
      vi.mocked(useAdminMenusModule.useAdminMenus).mockReturnValue({
        data: mockMenus,
        isLoading: false,
        isError: false,
        isSuccess: true,
      } as ReturnType<typeof useAdminMenusModule.useAdminMenus>)

      render(<AdminSidebar />)
      expect(screen.getByText('대시보드')).toBeInTheDocument()
      expect(screen.getByText('상품관리')).toBeInTheDocument()
    })

    it('소메뉴는 초기에 숨겨져 있다', () => {
      vi.mocked(useAdminMenusModule.useAdminMenus).mockReturnValue({
        data: mockMenus,
        isLoading: false,
        isError: false,
        isSuccess: true,
      } as ReturnType<typeof useAdminMenusModule.useAdminMenus>)

      render(<AdminSidebar />)
      expect(screen.queryByText('상품 목록')).not.toBeInTheDocument()
    })

    it('대메뉴 클릭 시 소메뉴가 펼쳐진다', async () => {
      vi.mocked(useAdminMenusModule.useAdminMenus).mockReturnValue({
        data: mockMenus,
        isLoading: false,
        isError: false,
        isSuccess: true,
      } as ReturnType<typeof useAdminMenusModule.useAdminMenus>)

      render(<AdminSidebar />)
      await userEvent.click(screen.getByText('상품관리'))
      expect(screen.getByText('상품 목록')).toBeInTheDocument()
      expect(screen.getByText('상품 등록')).toBeInTheDocument()
    })

    it('다른 대메뉴 클릭 시 기존 소메뉴가 닫힌다', async () => {
      const menus: MenuResponse[] = [
        { menuNo: 1, menuNm: '메뉴A', menuUrl: null, sortOrd: 1,
          children: [{ menuNo: 11, menuNm: '메뉴A-1', menuUrl: '/admin/a', sortOrd: 1, children: [] }] },
        { menuNo: 2, menuNm: '메뉴B', menuUrl: null, sortOrd: 2,
          children: [{ menuNo: 21, menuNm: '메뉴B-1', menuUrl: '/admin/b', sortOrd: 1, children: [] }] },
      ]
      vi.mocked(useAdminMenusModule.useAdminMenus).mockReturnValue({
        data: menus,
        isLoading: false,
        isError: false,
        isSuccess: true,
      } as ReturnType<typeof useAdminMenusModule.useAdminMenus>)

      render(<AdminSidebar />)
      await userEvent.click(screen.getByText('메뉴A'))
      expect(screen.getByText('메뉴A-1')).toBeInTheDocument()

      await userEvent.click(screen.getByText('메뉴B'))
      expect(screen.queryByText('메뉴A-1')).not.toBeInTheDocument()
      expect(screen.getByText('메뉴B-1')).toBeInTheDocument()
    })

    it('현재 경로에 해당하는 메뉴에 활성화 스타일이 적용된다', () => {
      vi.mocked(useAdminMenusModule.useAdminMenus).mockReturnValue({
        data: mockMenus,
        isLoading: false,
        isError: false,
        isSuccess: true,
      } as ReturnType<typeof useAdminMenusModule.useAdminMenus>)

      render(<AdminSidebar />)
      // /admin/dashboard가 현재 경로 — 대시보드 메뉴가 활성화
      const dashboardLink = screen.getByText('대시보드').closest('a')
      expect(dashboardLink).toHaveClass('text-blue-400')
    })

    it('menuUrl이 null인 항목은 클릭 불가 처리된다', () => {
      const menusWithNullUrl: MenuResponse[] = [
        { menuNo: 99, menuNm: '빈메뉴', menuUrl: null, sortOrd: 1, children: [] },
      ]
      vi.mocked(useAdminMenusModule.useAdminMenus).mockReturnValue({
        data: menusWithNullUrl,
        isLoading: false,
        isError: false,
        isSuccess: true,
      } as ReturnType<typeof useAdminMenusModule.useAdminMenus>)

      render(<AdminSidebar />)
      const item = screen.getByText('빈메뉴')
      expect(item.closest('span')).toHaveClass('pointer-events-none')
    })

    it('현재 경로의 소메뉴를 포함한 대메뉴가 자동으로 펼쳐진다', () => {
      // usePathname을 /admin/products로 오버라이드 (상품 목록 URL)
      vi.mocked(nextNavigation.usePathname).mockReturnValue('/admin/products')

      vi.mocked(useAdminMenusModule.useAdminMenus).mockReturnValue({
        data: mockMenus,
        isLoading: false,
        isError: false,
        isSuccess: true,
      } as ReturnType<typeof useAdminMenusModule.useAdminMenus>)

      render(<AdminSidebar />)
      // 상품관리 대메뉴가 자동으로 펼쳐져 소메뉴가 보여야 함
      expect(screen.getByText('상품 목록')).toBeInTheDocument()
    })
  })
  ```

- [ ] **Step 2: 테스트 실행 — 실패 확인**

  ```bash
  cd frontend && pnpm vitest run components/system/AdminSidebar.test.tsx
  ```
  Expected: FAIL — `AdminSidebar` 모듈 없음

- [ ] **Step 3: `@testing-library/user-event` 설치 확인**

  ```bash
  cd frontend && pnpm list @testing-library/user-event
  ```
  없으면 설치:
  ```bash
  cd frontend && pnpm add -D @testing-library/user-event
  ```

- [ ] **Step 4: AdminSidebar 구현**

  `frontend/components/system/AdminSidebar.tsx`:

  ```typescript
  'use client'

  import Link from 'next/link'
  import { usePathname } from 'next/navigation'
  import { useState, useEffect } from 'react'
  import { useAdminMenus } from '@/hooks/system/useAdminMenus'
  import type { MenuResponse } from '@/types/system.types'

  const AdminSidebar = () => {
    const pathname = usePathname()
    const { data: menus, isLoading, isError } = useAdminMenus()
    const [openMenuNo, setOpenMenuNo] = useState<number | null>(null)

    // 현재 경로에 해당하는 대메뉴 자동 펼침
    useEffect(() => {
      if (!menus) return
      const parentMenu = menus.find((menu) =>
        menu.children.some((child) => child.menuUrl && pathname.startsWith(child.menuUrl))
      )
      if (parentMenu) {
        setOpenMenuNo(parentMenu.menuNo)
      }
    }, [menus, pathname])

    if (isLoading) {
      return (
        <aside className="w-48 shrink-0 border-r border-slate-200 bg-slate-900 p-3">
          {Array.from({ length: 5 }).map((_, i) => (
            <div
              key={i}
              data-testid="menu-skeleton"
              className="mb-1 h-8 w-full animate-pulse rounded bg-slate-700"
            />
          ))}
        </aside>
      )
    }

    if (isError) {
      return (
        <aside className="w-48 shrink-0 border-r border-slate-200 bg-slate-900 p-3">
          <p className="text-xs text-red-400">메뉴를 불러올 수 없습니다</p>
        </aside>
      )
    }

    const isActive = (menuUrl: string) => pathname.startsWith(menuUrl)

    const renderLeaf = (menu: MenuResponse, isChild = false) => {
      if (!menu.menuUrl) {
        return (
          <span
            key={menu.menuNo}
            className={`pointer-events-none block cursor-default rounded px-3 py-1.5 text-xs text-slate-600 ${isChild ? 'pl-5' : ''}`}
          >
            {menu.menuNm}
          </span>
        )
      }
      return (
        <Link
          key={menu.menuNo}
          href={menu.menuUrl}
          className={`block rounded px-3 py-1.5 text-xs transition-colors hover:bg-slate-700 hover:text-slate-100 ${isChild ? 'pl-5' : ''} ${
            isActive(menu.menuUrl)
              ? 'bg-slate-800 text-blue-400'
              : 'text-slate-400'
          }`}
        >
          {menu.menuNm}
        </Link>
      )
    }

    return (
      <aside className="w-48 shrink-0 border-r border-slate-700 bg-slate-900 p-3">
        <div className="mb-3 px-3 text-[10px] font-bold uppercase tracking-widest text-slate-500">
          Admin
        </div>
        <nav className="space-y-0.5">
          {menus?.map((menu) => {
            if (menu.children.length === 0) {
              return renderLeaf(menu)
            }

            const isOpen = openMenuNo === menu.menuNo
            const hasActiveChild = menu.children.some(
              (child) => child.menuUrl && isActive(child.menuUrl)
            )

            return (
              <div key={menu.menuNo}>
                <button
                  onClick={() => setOpenMenuNo(isOpen ? null : menu.menuNo)}
                  className={`flex w-full items-center justify-between rounded px-3 py-1.5 text-xs transition-colors hover:bg-slate-700 hover:text-slate-100 ${
                    hasActiveChild ? 'text-blue-400' : 'text-slate-400'
                  }`}
                >
                  <span>{menu.menuNm}</span>
                  <span className="text-[8px]">{isOpen ? '▾' : '▸'}</span>
                </button>
                {isOpen && (
                  <div className="ml-2 mt-0.5 space-y-0.5 border-l border-slate-700 pl-2">
                    {menu.children.map((child) => renderLeaf(child, true))}
                  </div>
                )}
              </div>
            )
          })}
        </nav>
      </aside>
    )
  }

  export default AdminSidebar
  ```

- [ ] **Step 5: 테스트 실행 — 통과 확인**

  ```bash
  cd frontend && pnpm vitest run components/system/AdminSidebar.test.tsx
  ```
  Expected: PASS (8 tests passed)

- [ ] **Step 6: 커밋**

  ```bash
  git add frontend/components/system/AdminSidebar.tsx frontend/components/system/AdminSidebar.test.tsx
  git commit -m "feat: add AdminSidebar accordion component"
  ```

---

## Chunk 3: 레이아웃 통합 + 폰트

### Task 5: Pretendard 폰트 설치 및 적용

**Files:**
- Modify: `frontend/app/admin/layout.tsx`

- [ ] **Step 1: `@fontsource/pretendard` 설치**

  ```bash
  cd frontend && pnpm add @fontsource/pretendard
  ```
  Expected: `@fontsource/pretendard` 패키지가 `package.json` dependencies에 추가됨

- [ ] **Step 2: 어드민 루트 레이아웃에 폰트 임포트 적용**

  `frontend/app/admin/layout.tsx` 전체.
  > 인라인 스타일로 어드민 영역만 Pretendard 폰트를 적용한다. 쇼핑몰(shop) 영역에는 영향을 주지 않는다.

  ```typescript
  import type { ReactNode } from 'react'
  import '@fontsource/pretendard/400.css'
  import '@fontsource/pretendard/500.css'
  import '@fontsource/pretendard/600.css'
  import '@fontsource/pretendard/700.css'

  export default function AdminLayout({ children }: { children: ReactNode }) {
    return (
      <div style={{ fontFamily: "'Pretendard', sans-serif" }}>
        {children}
      </div>
    )
  }
  ```

- [ ] **Step 3: 타입스크립트 컴파일 확인**

  ```bash
  cd frontend && pnpm tsc --noEmit
  ```
  Expected: 에러 없음

---

### Task 6: 레이아웃에 AdminSidebar 통합

**Files:**
- Modify: `frontend/app/admin/(main)/layout.tsx`

- [ ] **Step 1: AdminSidebar로 교체**

  `frontend/app/admin/(main)/layout.tsx` 전체:

  ```typescript
  import AdminHeader from '@/components/system/AdminHeader'
  import AdminSidebar from '@/components/system/AdminSidebar'

  export default function AdminMainLayout({ children }: { children: React.ReactNode }) {
    return (
      <div className="flex min-h-screen flex-col">
        <AdminHeader />
        <div className="flex flex-1">
          <AdminSidebar />
          <main className="flex-1 p-6">{children}</main>
        </div>
      </div>
    )
  }
  ```

- [ ] **Step 2: 개발 서버 실행 후 동작 확인**

  ```bash
  cd frontend && pnpm dev
  ```

  브라우저에서 `http://localhost:3000/admin/dashboard` 접속 후 확인:
  - [ ] 좌측 사이드바에 메뉴 목록이 표시됨
  - [ ] 대메뉴 클릭 시 소메뉴 펼침/접힘 동작
  - [ ] 소메뉴 클릭 시 해당 페이지로 이동
  - [ ] 현재 페이지 메뉴 활성화 스타일 표시
  - [ ] Pretendard 폰트 적용 확인

- [ ] **Step 3: 전체 테스트 실행**

  ```bash
  cd frontend && pnpm vitest run
  ```
  Expected: 모든 테스트 통과

- [ ] **Step 4: 최종 커밋**

  ```bash
  git add frontend/app/admin/layout.tsx frontend/app/admin/\(main\)/layout.tsx frontend/package.json frontend/pnpm-lock.yaml
  git commit -m "feat: integrate AdminSidebar into admin layout with Pretendard font"
  ```
