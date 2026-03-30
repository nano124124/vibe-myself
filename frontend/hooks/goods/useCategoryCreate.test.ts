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
