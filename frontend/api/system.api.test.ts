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
