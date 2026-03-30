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
