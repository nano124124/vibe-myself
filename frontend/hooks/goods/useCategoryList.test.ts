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
