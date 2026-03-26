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
