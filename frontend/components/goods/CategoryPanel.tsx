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
