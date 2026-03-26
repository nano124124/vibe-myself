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
          isActive(menu.menuUrl) ? 'bg-slate-800 text-blue-400' : 'text-slate-400'
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
