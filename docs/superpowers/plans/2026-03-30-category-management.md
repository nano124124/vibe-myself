# 카테고리 관리 화면 구현 계획

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 어드민 `/admin/goods/categories` 페이지에서 카테고리 트리 조회, 등록, 수정 기능을 인라인 패널 방식으로 제공한다.

**Architecture:** 좌측 카테고리 트리 테이블 + 우측 인라인 패널 구조. 데이터 레이어(types → api → hooks)를 먼저 TDD로 구현한 뒤 UI 컴포넌트를 조립한다. Tanstack Query로 서버 상태를 관리하고, 패널 open/mode/선택 카테고리 상태는 page.tsx에서 useState로 관리한다.

**Tech Stack:** Next.js 16 (App Router), React 19, Tailwind CSS v4, Tanstack Query v5, Vitest, @testing-library/react

---

## Chunk 1: 데이터 레이어 (Types → API → Hooks)

### Task 1: 타입 정의

**Files:**
- Create: `frontend/types/goods.types.ts`

- [ ] **Step 1: 파일 생성**

```ts
// frontend/types/goods.types.ts
export interface CategoryResponse {
  ctgNo: number
  upCtgNo: number | null
  ctgLvl: string
  ctgNm: string
  sortOrd: number
  useYn: 'Y' | 'N'
  children: CategoryResponse[]
}

export interface CreateCategoryRequest {
  upCtgNo?: number
  ctgNm: string
  sortOrd: number
}

export interface UpdateCategoryRequest {
  ctgNm: string
  useYn: 'Y' | 'N'
  sortOrd: number
}

export interface CreateCategoryResponse {
  ctgNo: number
}
```

- [ ] **Step 2: 커밋**

```bash
git add frontend/types/goods.types.ts
git commit -m "feat: goods 타입 정의 추가"
```

---

### Task 2: API 함수 (TDD)

**Files:**
- Create: `frontend/api/goods.api.ts`
- Create: `frontend/api/goods.api.test.ts`

- [ ] **Step 1: 테스트 먼저 작성**

```ts
// frontend/api/goods.api.test.ts
import { describe, it, expect, vi, beforeEach } from 'vitest'
import api from '@/lib/api'
import { getCategories, createCategory, updateCategory } from './goods.api'
import type { CategoryResponse, CreateCategoryRequest, UpdateCategoryRequest } from '@/types/goods.types'

vi.mock('@/lib/api', () => ({
  default: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
  },
}))

const mockCategories: CategoryResponse[] = [
  {
    ctgNo: 1,
    upCtgNo: null,
    ctgLvl: '1',
    ctgNm: '의류',
    sortOrd: 1,
    useYn: 'Y',
    children: [
      {
        ctgNo: 11,
        upCtgNo: 1,
        ctgLvl: '2',
        ctgNm: '상의',
        sortOrd: 1,
        useYn: 'Y',
        children: [],
      },
    ],
  },
]

describe('getCategories', () => {
  beforeEach(() => vi.clearAllMocks())

  it('카테고리 트리를 반환한다', async () => {
    vi.mocked(api.get).mockResolvedValueOnce({
      data: { success: true, data: mockCategories, message: null },
    })

    const result = await getCategories()

    expect(api.get).toHaveBeenCalledWith('/api/admin/goods/categories')
    expect(result).toEqual(mockCategories)
  })
})

describe('createCategory', () => {
  beforeEach(() => vi.clearAllMocks())

  it('카테고리를 등록하고 생성된 ctgNo를 반환한다', async () => {
    const req: CreateCategoryRequest = { ctgNm: '신발', sortOrd: 2 }
    vi.mocked(api.post).mockResolvedValueOnce({
      data: { success: true, data: { ctgNo: 100 }, message: null },
    })

    const result = await createCategory(req)

    expect(api.post).toHaveBeenCalledWith('/api/admin/goods/categories', req)
    expect(result).toEqual({ ctgNo: 100 })
  })
})

describe('updateCategory', () => {
  beforeEach(() => vi.clearAllMocks())

  it('카테고리를 수정한다', async () => {
    const req: UpdateCategoryRequest = { ctgNm: '수정명', useYn: 'N', sortOrd: 3 }
    vi.mocked(api.put).mockResolvedValueOnce({
      data: { success: true, data: null, message: null },
    })

    await updateCategory(1, req)

    expect(api.put).toHaveBeenCalledWith('/api/admin/goods/categories/1', req)
  })
})
```

- [ ] **Step 2: 테스트 실행 (실패 확인)**

```bash
cd frontend && pnpm test goods.api
```
Expected: FAIL — "Cannot find module './goods.api'"

- [ ] **Step 3: API 함수 구현**

```ts
// frontend/api/goods.api.ts
import api from '@/lib/api'
import type {
  CategoryResponse,
  CreateCategoryRequest,
  CreateCategoryResponse,
  UpdateCategoryRequest,
} from '@/types/goods.types'

type ApiResponse<T> = { success: boolean; data: T; message: string | null }

export const getCategories = (): Promise<CategoryResponse[]> =>
  api
    .get<ApiResponse<CategoryResponse[]>>('/api/admin/goods/categories')
    .then((res) => res.data.data)

export const createCategory = (data: CreateCategoryRequest): Promise<CreateCategoryResponse> =>
  api
    .post<ApiResponse<CreateCategoryResponse>>('/api/admin/goods/categories', data)
    .then((res) => res.data.data)

export const updateCategory = (ctgNo: number, data: UpdateCategoryRequest): Promise<void> =>
  api
    .put<ApiResponse<null>>(`/api/admin/goods/categories/${ctgNo}`, data)
    .then(() => undefined)
```

- [ ] **Step 4: 테스트 실행 (통과 확인)**

```bash
cd frontend && pnpm test goods.api
```
Expected: PASS (3 tests)

- [ ] **Step 5: 커밋**

```bash
git add frontend/api/goods.api.ts frontend/api/goods.api.test.ts
git commit -m "feat: goods API 함수 및 테스트 추가"
```

---

### Task 3: useCategoryList 훅 (TDD)

**Files:**
- Create: `frontend/hooks/goods/useCategoryList.ts`
- Create: `frontend/hooks/goods/useCategoryList.test.ts`

- [ ] **Step 1: 테스트 작성**

```ts
// frontend/hooks/goods/useCategoryList.test.ts
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { renderHook, waitFor } from '@testing-library/react'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import React from 'react'
import { useCategoryList } from './useCategoryList'
import * as goodsApi from '@/api/goods.api'
import type { CategoryResponse } from '@/types/goods.types'

vi.mock('@/api/goods.api')

const createWrapper = () => {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false } },
  })
  return ({ children }: { children: React.ReactNode }) =>
    React.createElement(QueryClientProvider, { client: queryClient }, children)
}

const mockCategories: CategoryResponse[] = [
  { ctgNo: 1, upCtgNo: null, ctgLvl: '1', ctgNm: '의류', sortOrd: 1, useYn: 'Y', children: [] },
]

describe('useCategoryList', () => {
  beforeEach(() => vi.clearAllMocks())

  it('카테고리 목록을 반환한다', async () => {
    vi.mocked(goodsApi.getCategories).mockResolvedValueOnce(mockCategories)

    const { result } = renderHook(() => useCategoryList(), { wrapper: createWrapper() })

    await waitFor(() => expect(result.current.isSuccess).toBe(true))
    expect(result.current.data).toEqual(mockCategories)
  })

  it('API 실패 시 isError가 true가 된다', async () => {
    vi.mocked(goodsApi.getCategories).mockRejectedValueOnce(new Error('Network Error'))

    const { result } = renderHook(() => useCategoryList(), { wrapper: createWrapper() })

    await waitFor(() => expect(result.current.isError).toBe(true))
  })
})
```

- [ ] **Step 2: 테스트 실행 (실패 확인)**

```bash
cd frontend && pnpm test useCategoryList
```
Expected: FAIL

- [ ] **Step 3: 훅 구현**

```ts
// frontend/hooks/goods/useCategoryList.ts
import { useQuery } from '@tanstack/react-query'
import { getCategories } from '@/api/goods.api'

export const useCategoryList = () =>
  useQuery({
    queryKey: ['goods', 'categories'],
    queryFn: getCategories,
  })
```

- [ ] **Step 4: 테스트 통과 확인**

```bash
cd frontend && pnpm test useCategoryList
```
Expected: PASS (2 tests)

- [ ] **Step 5: 커밋**

```bash
git add frontend/hooks/goods/useCategoryList.ts frontend/hooks/goods/useCategoryList.test.ts
git commit -m "feat: useCategoryList 훅 추가"
```

---

### Task 4: useCategoryCreate / useCategoryUpdate 훅 (TDD)

**Files:**
- Create: `frontend/hooks/goods/useCategoryCreate.ts`
- Create: `frontend/hooks/goods/useCategoryCreate.test.ts`
- Create: `frontend/hooks/goods/useCategoryUpdate.ts`
- Create: `frontend/hooks/goods/useCategoryUpdate.test.ts`

- [ ] **Step 1: useCategoryCreate 테스트 작성**

```ts
// frontend/hooks/goods/useCategoryCreate.test.ts
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { renderHook, waitFor, act } from '@testing-library/react'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import React from 'react'
import { useCategoryCreate } from './useCategoryCreate'
import * as goodsApi from '@/api/goods.api'

vi.mock('@/api/goods.api')

const createWrapper = () => {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false } },
  })
  return ({ children }: { children: React.ReactNode }) =>
    React.createElement(QueryClientProvider, { client: queryClient }, children)
}

describe('useCategoryCreate', () => {
  beforeEach(() => vi.clearAllMocks())

  it('카테고리를 등록하고 성공 시 isSuccess가 true가 된다', async () => {
    vi.mocked(goodsApi.createCategory).mockResolvedValueOnce({ ctgNo: 100 })

    const { result } = renderHook(() => useCategoryCreate(), { wrapper: createWrapper() })

    act(() => {
      result.current.mutate({ ctgNm: '신발', sortOrd: 1 })
    })

    await waitFor(() => expect(result.current.isSuccess).toBe(true))
    expect(goodsApi.createCategory).toHaveBeenCalledWith({ ctgNm: '신발', sortOrd: 1 })
  })
})
```

- [ ] **Step 2: useCategoryUpdate 테스트 작성**

```ts
// frontend/hooks/goods/useCategoryUpdate.test.ts
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { renderHook, waitFor, act } from '@testing-library/react'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import React from 'react'
import { useCategoryUpdate } from './useCategoryUpdate'
import * as goodsApi from '@/api/goods.api'

vi.mock('@/api/goods.api')

const createWrapper = () => {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false } },
  })
  return ({ children }: { children: React.ReactNode }) =>
    React.createElement(QueryClientProvider, { client: queryClient }, children)
}

describe('useCategoryUpdate', () => {
  beforeEach(() => vi.clearAllMocks())

  it('카테고리를 수정하고 성공 시 isSuccess가 true가 된다', async () => {
    vi.mocked(goodsApi.updateCategory).mockResolvedValueOnce(undefined)

    const { result } = renderHook(() => useCategoryUpdate(), { wrapper: createWrapper() })

    act(() => {
      result.current.mutate({ ctgNo: 1, ctgNm: '수정명', useYn: 'N', sortOrd: 2 })
    })

    await waitFor(() => expect(result.current.isSuccess).toBe(true))
    expect(goodsApi.updateCategory).toHaveBeenCalledWith(1, { ctgNm: '수정명', useYn: 'N', sortOrd: 2 })
  })
})
```

- [ ] **Step 3: 테스트 실행 (실패 확인)**

```bash
cd frontend && pnpm test useCategoryCreate useCategoryUpdate
```
Expected: FAIL

- [ ] **Step 4: useCategoryCreate 구현**

```ts
// frontend/hooks/goods/useCategoryCreate.ts
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { createCategory } from '@/api/goods.api'
import type { CreateCategoryRequest } from '@/types/goods.types'

export const useCategoryCreate = () => {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (data: CreateCategoryRequest) => createCategory(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['goods', 'categories'] })
    },
  })
}
```

- [ ] **Step 5: useCategoryUpdate 구현**

```ts
// frontend/hooks/goods/useCategoryUpdate.ts
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { updateCategory } from '@/api/goods.api'
import type { UpdateCategoryRequest } from '@/types/goods.types'

export const useCategoryUpdate = () => {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ ctgNo, ...data }: { ctgNo: number } & UpdateCategoryRequest) =>
      updateCategory(ctgNo, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['goods', 'categories'] })
    },
  })
}
```

- [ ] **Step 6: 테스트 통과 확인**

```bash
cd frontend && pnpm test useCategoryCreate useCategoryUpdate
```
Expected: PASS (2 tests)

- [ ] **Step 7: 커밋**

```bash
git add frontend/hooks/goods/
git commit -m "feat: useCategoryCreate, useCategoryUpdate 훅 추가"
```

---

## Chunk 2: UI 컴포넌트

### Task 5: CategoryTree 컴포넌트 (TDD)

**Files:**
- Create: `frontend/components/goods/CategoryTree.tsx`
- Create: `frontend/components/goods/CategoryTree.test.tsx`

- [ ] **Step 1: 테스트 작성**

```tsx
// frontend/components/goods/CategoryTree.test.tsx
import { describe, it, expect, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import CategoryTree from './CategoryTree'
import type { CategoryResponse } from '@/types/goods.types'

const mockCategories: CategoryResponse[] = [
  {
    ctgNo: 1,
    upCtgNo: null,
    ctgLvl: '1',
    ctgNm: '의류',
    sortOrd: 1,
    useYn: 'Y',
    children: [
      {
        ctgNo: 11,
        upCtgNo: 1,
        ctgLvl: '2',
        ctgNm: '상의',
        sortOrd: 1,
        useYn: 'Y',
        children: [
          {
            ctgNo: 111,
            upCtgNo: 11,
            ctgLvl: '3',
            ctgNm: '반팔',
            sortOrd: 1,
            useYn: 'N',
            children: [],
          },
        ],
      },
    ],
  },
]

describe('CategoryTree', () => {
  const onEdit = vi.fn()
  const onCreateChild = vi.fn()

  it('카테고리 트리를 렌더링한다', () => {
    render(<CategoryTree categories={mockCategories} onEdit={onEdit} onCreateChild={onCreateChild} />)

    expect(screen.getByText('의류')).toBeInTheDocument()
    expect(screen.getByText('상의')).toBeInTheDocument()
    expect(screen.getByText('반팔')).toBeInTheDocument()
  })

  it('사용여부를 표시한다', () => {
    render(<CategoryTree categories={mockCategories} onEdit={onEdit} onCreateChild={onCreateChild} />)

    // 반팔(미사용)
    expect(screen.getByTestId('use-yn-111')).toHaveTextContent('N')
  })

  it('수정 버튼 클릭 시 onEdit을 호출한다', async () => {
    render(<CategoryTree categories={mockCategories} onEdit={onEdit} onCreateChild={onCreateChild} />)

    await userEvent.click(screen.getByTestId('edit-btn-1'))
    expect(onEdit).toHaveBeenCalledWith(mockCategories[0])
  })

  it('하위 추가 버튼 클릭 시 onCreateChild를 호출한다', async () => {
    render(<CategoryTree categories={mockCategories} onEdit={onEdit} onCreateChild={onCreateChild} />)

    await userEvent.click(screen.getByTestId('add-child-btn-1'))
    expect(onCreateChild).toHaveBeenCalledWith(1)
  })

  it('3단계 카테고리(ctgLvl=3)에는 하위 추가 버튼이 없다', () => {
    render(<CategoryTree categories={mockCategories} onEdit={onEdit} onCreateChild={onCreateChild} />)

    expect(screen.queryByTestId('add-child-btn-111')).not.toBeInTheDocument()
  })
})
```

- [ ] **Step 2: 테스트 실행 (실패 확인)**

```bash
cd frontend && pnpm test CategoryTree
```
Expected: FAIL

- [ ] **Step 3: CategoryTree 구현**

```tsx
// frontend/components/goods/CategoryTree.tsx
'use client'

import type { CategoryResponse } from '@/types/goods.types'

interface CategoryTreeProps {
  categories: CategoryResponse[]
  onEdit: (category: CategoryResponse) => void
  onCreateChild: (upCtgNo: number) => void
}

interface RowProps {
  category: CategoryResponse
  depth: number
  onEdit: (category: CategoryResponse) => void
  onCreateChild: (upCtgNo: number) => void
}

const CategoryRow = ({ category, depth, onEdit, onCreateChild }: RowProps) => {
  const indent = depth * 16

  return (
    <>
      <tr className="border-b border-slate-100 hover:bg-slate-50">
        <td className="py-2 pr-4 text-sm" style={{ paddingLeft: `${12 + indent}px` }}>
          {depth > 0 && <span className="mr-1 text-slate-400">└</span>}
          {category.ctgNm}
        </td>
        <td className="px-4 py-2 text-center text-sm text-slate-500">{category.ctgLvl}</td>
        <td className="px-4 py-2 text-center text-sm text-slate-500">{category.sortOrd}</td>
        <td
          className="px-4 py-2 text-center text-sm"
          data-testid={`use-yn-${category.ctgNo}`}
        >
          <span
            className={`inline-block rounded px-1.5 py-0.5 text-xs font-medium ${
              category.useYn === 'Y'
                ? 'bg-green-100 text-green-700'
                : 'bg-slate-100 text-slate-500'
            }`}
          >
            {category.useYn}
          </span>
        </td>
        <td className="px-4 py-2 text-center">
          <div className="flex items-center justify-center gap-1">
            <button
              data-testid={`edit-btn-${category.ctgNo}`}
              onClick={() => onEdit(category)}
              className="rounded px-2 py-1 text-xs text-slate-600 hover:bg-slate-200"
            >
              수정
            </button>
            {category.ctgLvl !== '3' && (
              <button
                data-testid={`add-child-btn-${category.ctgNo}`}
                onClick={() => onCreateChild(category.ctgNo)}
                className="rounded px-2 py-1 text-xs text-blue-600 hover:bg-blue-50"
              >
                + 하위
              </button>
            )}
          </div>
        </td>
      </tr>
      {category.children.map((child) => (
        <CategoryRow
          key={child.ctgNo}
          category={child}
          depth={depth + 1}
          onEdit={onEdit}
          onCreateChild={onCreateChild}
        />
      ))}
    </>
  )
}

const CategoryTree = ({ categories, onEdit, onCreateChild }: CategoryTreeProps) => {
  return (
    <table className="w-full border-collapse text-left">
      <thead>
        <tr className="border-b-2 border-slate-200 text-xs font-semibold uppercase text-slate-500">
          <th className="py-2 pl-3">카테고리명</th>
          <th className="px-4 py-2 text-center">레벨</th>
          <th className="px-4 py-2 text-center">정렬</th>
          <th className="px-4 py-2 text-center">사용</th>
          <th className="px-4 py-2 text-center">액션</th>
        </tr>
      </thead>
      <tbody>
        {categories.map((category) => (
          <CategoryRow
            key={category.ctgNo}
            category={category}
            depth={0}
            onEdit={onEdit}
            onCreateChild={onCreateChild}
          />
        ))}
      </tbody>
    </table>
  )
}

export default CategoryTree
```

- [ ] **Step 4: 테스트 통과 확인**

```bash
cd frontend && pnpm test CategoryTree
```
Expected: PASS (5 tests)

- [ ] **Step 5: 커밋**

```bash
git add frontend/components/goods/CategoryTree.tsx frontend/components/goods/CategoryTree.test.tsx
git commit -m "feat: CategoryTree 컴포넌트 추가"
```

---

### Task 6: CategoryPanel + CategoryForm 컴포넌트 (TDD)

**Files:**
- Create: `frontend/components/goods/CategoryForm.tsx`
- Create: `frontend/components/goods/CategoryPanel.tsx`
- Create: `frontend/components/goods/CategoryPanel.test.tsx`

- [ ] **Step 1: 테스트 작성**

```tsx
// frontend/components/goods/CategoryPanel.test.tsx
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import CategoryPanel from './CategoryPanel'
import type { CategoryResponse } from '@/types/goods.types'

vi.mock('@/hooks/goods/useCategoryCreate', () => ({
  useCategoryCreate: () => ({
    mutate: vi.fn(),
    isPending: false,
  }),
}))

vi.mock('@/hooks/goods/useCategoryUpdate', () => ({
  useCategoryUpdate: () => ({
    mutate: vi.fn(),
    isPending: false,
  }),
}))

const mockCategory: CategoryResponse = {
  ctgNo: 1,
  upCtgNo: null,
  ctgLvl: '1',
  ctgNm: '의류',
  sortOrd: 1,
  useYn: 'Y',
  children: [],
}

describe('CategoryPanel', () => {
  const onClose = vi.fn()
  const onSuccess = vi.fn()

  beforeEach(() => vi.clearAllMocks())

  it('create 모드에서 등록 폼을 표시한다', () => {
    render(
      <CategoryPanel mode="create" onClose={onClose} onSuccess={onSuccess} />
    )

    expect(screen.getByText('카테고리 등록')).toBeInTheDocument()
    expect(screen.getByLabelText('카테고리명')).toBeInTheDocument()
    expect(screen.getByLabelText('정렬순서')).toBeInTheDocument()
    expect(screen.queryByLabelText('사용여부')).not.toBeInTheDocument()
  })

  it('edit 모드에서 수정 폼을 표시하고 기존 값을 채운다', () => {
    render(
      <CategoryPanel mode="edit" category={mockCategory} onClose={onClose} onSuccess={onSuccess} />
    )

    expect(screen.getByText('카테고리 수정')).toBeInTheDocument()
    expect(screen.getByLabelText('카테고리명')).toHaveValue('의류')
    expect(screen.getByLabelText('사용여부')).toBeInTheDocument()
  })

  it('취소 버튼 클릭 시 onClose를 호출한다', async () => {
    render(
      <CategoryPanel mode="create" onClose={onClose} onSuccess={onSuccess} />
    )

    await userEvent.click(screen.getByText('취소'))
    expect(onClose).toHaveBeenCalledOnce()
  })
})
```

- [ ] **Step 2: 테스트 실행 (실패 확인)**

```bash
cd frontend && pnpm test CategoryPanel
```
Expected: FAIL

- [ ] **Step 3: CategoryForm 구현**

```tsx
// frontend/components/goods/CategoryForm.tsx
'use client'

import { useState } from 'react'
import type { CategoryResponse, CreateCategoryRequest, UpdateCategoryRequest } from '@/types/goods.types'

interface CreateFormProps {
  mode: 'create'
  onSubmit: (data: CreateCategoryRequest) => void
  isPending: boolean
  onCancel: () => void
}

interface EditFormProps {
  mode: 'edit'
  category: CategoryResponse
  onSubmit: (data: UpdateCategoryRequest) => void
  isPending: boolean
  onCancel: () => void
}

type CategoryFormProps = CreateFormProps | EditFormProps

const CategoryForm = (props: CategoryFormProps) => {
  const initialNm = props.mode === 'edit' ? props.category.ctgNm : ''
  const initialOrd = props.mode === 'edit' ? String(props.category.sortOrd) : '0'
  const initialUseYn = props.mode === 'edit' ? props.category.useYn : 'Y'

  const [ctgNm, setCtgNm] = useState(initialNm)
  const [sortOrd, setSortOrd] = useState(initialOrd)
  const [useYn, setUseYn] = useState<'Y' | 'N'>(initialUseYn)

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    if (props.mode === 'create') {
      props.onSubmit({ ctgNm, sortOrd: Number(sortOrd) })
    } else {
      props.onSubmit({ ctgNm, sortOrd: Number(sortOrd), useYn })
    }
  }

  return (
    <form onSubmit={handleSubmit} className="flex flex-col gap-4">
      <div className="flex flex-col gap-1">
        <label htmlFor="ctgNm" className="text-sm font-medium text-slate-700">
          카테고리명
        </label>
        <input
          id="ctgNm"
          type="text"
          value={ctgNm}
          onChange={(e) => setCtgNm(e.target.value)}
          required
          className="rounded border border-slate-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none"
        />
      </div>

      <div className="flex flex-col gap-1">
        <label htmlFor="sortOrd" className="text-sm font-medium text-slate-700">
          정렬순서
        </label>
        <input
          id="sortOrd"
          type="number"
          value={sortOrd}
          onChange={(e) => setSortOrd(e.target.value)}
          min={0}
          required
          className="rounded border border-slate-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none"
        />
      </div>

      {props.mode === 'edit' && (
        <div className="flex flex-col gap-1">
          <label htmlFor="useYn" className="text-sm font-medium text-slate-700">
            사용여부
          </label>
          <select
            id="useYn"
            value={useYn}
            onChange={(e) => setUseYn(e.target.value as 'Y' | 'N')}
            className="rounded border border-slate-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none"
          >
            <option value="Y">사용</option>
            <option value="N">미사용</option>
          </select>
        </div>
      )}

      <div className="mt-2 flex gap-2">
        <button
          type="button"
          onClick={props.onCancel}
          className="flex-1 rounded border border-slate-300 py-2 text-sm text-slate-600 hover:bg-slate-50"
        >
          취소
        </button>
        <button
          type="submit"
          disabled={props.isPending}
          className="flex-1 rounded bg-blue-600 py-2 text-sm text-white hover:bg-blue-700 disabled:opacity-50"
        >
          {props.isPending ? '저장 중...' : '저장'}
        </button>
      </div>
    </form>
  )
}

export default CategoryForm
```

- [ ] **Step 4: CategoryPanel 구현**

```tsx
// frontend/components/goods/CategoryPanel.tsx
'use client'

import CategoryForm from './CategoryForm'
import { useCategoryCreate } from '@/hooks/goods/useCategoryCreate'
import { useCategoryUpdate } from '@/hooks/goods/useCategoryUpdate'
import type { CategoryResponse, CreateCategoryRequest, UpdateCategoryRequest } from '@/types/goods.types'

interface CreatePanelProps {
  mode: 'create'
  upCtgNo?: number
  onClose: () => void
  onSuccess: () => void
}

interface EditPanelProps {
  mode: 'edit'
  category: CategoryResponse
  onClose: () => void
  onSuccess: () => void
}

type CategoryPanelProps = CreatePanelProps | EditPanelProps

const CategoryPanel = (props: CategoryPanelProps) => {
  const createMutation = useCategoryCreate()
  const updateMutation = useCategoryUpdate()

  const handleCreate = (data: CreateCategoryRequest) => {
    const payload = props.mode === 'create' && props.upCtgNo
      ? { ...data, upCtgNo: props.upCtgNo }
      : data
    createMutation.mutate(payload, { onSuccess: props.onSuccess })
  }

  const handleUpdate = (data: UpdateCategoryRequest) => {
    if (props.mode !== 'edit') return
    updateMutation.mutate(
      { ctgNo: props.category.ctgNo, ...data },
      { onSuccess: props.onSuccess }
    )
  }

  const title = props.mode === 'create' ? '카테고리 등록' : '카테고리 수정'

  return (
    <div className="w-72 shrink-0 border-l border-slate-200 bg-white p-5">
      <h2 className="mb-4 text-base font-semibold text-slate-800">{title}</h2>
      {props.mode === 'create' ? (
        <CategoryForm
          mode="create"
          onSubmit={handleCreate}
          isPending={createMutation.isPending}
          onCancel={props.onClose}
        />
      ) : (
        <CategoryForm
          mode="edit"
          category={props.category}
          onSubmit={handleUpdate}
          isPending={updateMutation.isPending}
          onCancel={props.onClose}
        />
      )}
    </div>
  )
}

export default CategoryPanel
```

- [ ] **Step 5: 테스트 통과 확인**

```bash
cd frontend && pnpm test CategoryPanel
```
Expected: PASS (3 tests)

- [ ] **Step 6: 커밋**

```bash
git add frontend/components/goods/CategoryForm.tsx frontend/components/goods/CategoryPanel.tsx frontend/components/goods/CategoryPanel.test.tsx
git commit -m "feat: CategoryPanel, CategoryForm 컴포넌트 추가"
```

---

## Chunk 3: 페이지 조립

### Task 7: 카테고리 관리 페이지

**Files:**
- Create: `frontend/app/admin/(main)/goods/categories/page.tsx`

- [ ] **Step 1: 페이지 구현**

```tsx
// frontend/app/admin/(main)/goods/categories/page.tsx
'use client'

import { useState } from 'react'
import { useCategoryList } from '@/hooks/goods/useCategoryList'
import CategoryTree from '@/components/goods/CategoryTree'
import CategoryPanel from '@/components/goods/CategoryPanel'
import type { CategoryResponse } from '@/types/goods.types'

type PanelMode = 'create' | 'edit'

export default function AdminCategoriesPage() {
  const { data: categories, isLoading, isError } = useCategoryList()

  const [isPanelOpen, setIsPanelOpen] = useState(false)
  const [panelMode, setPanelMode] = useState<PanelMode>('create')
  const [upCtgNo, setUpCtgNo] = useState<number | undefined>(undefined)
  const [selectedCategory, setSelectedCategory] = useState<CategoryResponse | undefined>(undefined)

  const openCreatePanel = (parentCtgNo?: number) => {
    setUpCtgNo(parentCtgNo)
    setSelectedCategory(undefined)
    setPanelMode('create')
    setIsPanelOpen(true)
  }

  const openEditPanel = (category: CategoryResponse) => {
    setSelectedCategory(category)
    setUpCtgNo(undefined)
    setPanelMode('edit')
    setIsPanelOpen(true)
  }

  const closePanel = () => {
    setIsPanelOpen(false)
  }

  return (
    <div>
      <div className="mb-4 flex items-center justify-between">
        <h1 className="text-lg font-semibold text-slate-800">카테고리 관리</h1>
        <button
          onClick={() => openCreatePanel()}
          className="rounded bg-blue-600 px-3 py-1.5 text-sm text-white hover:bg-blue-700"
        >
          + 최상위 카테고리 등록
        </button>
      </div>

      <div className="flex gap-0 rounded-lg border border-slate-200 bg-white">
        <div className="flex-1 overflow-x-auto p-4">
          {isLoading && (
            <div className="space-y-2">
              {Array.from({ length: 5 }).map((_, i) => (
                <div key={i} className="h-8 animate-pulse rounded bg-slate-100" />
              ))}
            </div>
          )}
          {isError && (
            <p className="text-sm text-red-500">카테고리를 불러올 수 없습니다.</p>
          )}
          {categories && (
            <CategoryTree
              categories={categories}
              onEdit={openEditPanel}
              onCreateChild={openCreatePanel}
            />
          )}
        </div>

        {isPanelOpen && (
          panelMode === 'create' ? (
            <CategoryPanel
              mode="create"
              upCtgNo={upCtgNo}
              onClose={closePanel}
              onSuccess={closePanel}
            />
          ) : selectedCategory ? (
            <CategoryPanel
              mode="edit"
              category={selectedCategory}
              onClose={closePanel}
              onSuccess={closePanel}
            />
          ) : null
        )}
      </div>
    </div>
  )
}
```

- [ ] **Step 2: 전체 테스트 실행**

```bash
cd frontend && pnpm test
```
Expected: 모든 테스트 PASS

- [ ] **Step 3: 커밋**

```bash
git add frontend/app/admin/\(main\)/goods/categories/page.tsx
git commit -m "feat: 카테고리 관리 페이지 구현"
```

---

### Task 8: 동작 확인

- [ ] **Step 1: 개발 서버 실행 및 확인**

```bash
cd frontend && pnpm dev
```

브라우저에서 `http://localhost:3000/admin/goods/categories` 접속 후 확인:
- 카테고리 트리 목록이 렌더링된다
- "최상위 카테고리 등록" 버튼 클릭 → 우측 등록 패널 열림
- 수정 버튼 클릭 → 해당 카테고리 데이터 채워진 수정 패널 열림
- "+ 하위" 버튼 클릭 → 해당 카테고리를 상위로 하는 등록 패널 열림
- 3단계 카테고리에는 "+ 하위" 버튼 없음
- 저장 후 목록 자동 갱신

- [ ] **Step 2: 최종 커밋**

```bash
git add "frontend/app/admin/(main)/goods/categories/"
git commit -m "feat: 카테고리 관리 화면 구현 완료"
```
