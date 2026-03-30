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
