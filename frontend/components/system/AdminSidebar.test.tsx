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
      {
        menuNo: 1, menuNm: '메뉴A', menuUrl: null, sortOrd: 1,
        children: [{ menuNo: 11, menuNm: '메뉴A-1', menuUrl: '/admin/a', sortOrd: 1, children: [] }],
      },
      {
        menuNo: 2, menuNm: '메뉴B', menuUrl: null, sortOrd: 2,
        children: [{ menuNo: 21, menuNm: '메뉴B-1', menuUrl: '/admin/b', sortOrd: 1, children: [] }],
      },
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
    vi.mocked(nextNavigation.usePathname).mockReturnValue('/admin/products')

    vi.mocked(useAdminMenusModule.useAdminMenus).mockReturnValue({
      data: mockMenus,
      isLoading: false,
      isError: false,
      isSuccess: true,
    } as ReturnType<typeof useAdminMenusModule.useAdminMenus>)

    render(<AdminSidebar />)
    expect(screen.getByText('상품 목록')).toBeInTheDocument()
  })
})
