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
