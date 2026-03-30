'use client'

import { useState } from 'react'
import type { CategoryResponse, CreateCategoryRequest, UpdateCategoryRequest } from '@/types/goods.types'

interface CreateFormProps {
  mode: 'create'
  onSubmit: (data: CreateCategoryRequest) => void
  isPending: boolean
  onCancel: () => void
}

interface EditFormProps {
  mode: 'edit'
  category: CategoryResponse
  onSubmit: (data: UpdateCategoryRequest) => void
  isPending: boolean
  onCancel: () => void
}

type CategoryFormProps = CreateFormProps | EditFormProps

const CategoryForm = (props: CategoryFormProps) => {
  const initialNm = props.mode === 'edit' ? props.category.ctgNm : ''
  const initialOrd = props.mode === 'edit' ? String(props.category.sortOrd) : '0'
  const initialUseYn = props.mode === 'edit' ? props.category.useYn : 'Y'

  const [ctgNm, setCtgNm] = useState(initialNm)
  const [sortOrd, setSortOrd] = useState(initialOrd)
  const [useYn, setUseYn] = useState<'Y' | 'N'>(initialUseYn)

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    if (props.mode === 'create') {
      props.onSubmit({ ctgNm, sortOrd: Number(sortOrd) })
    } else {
      props.onSubmit({ ctgNm, sortOrd: Number(sortOrd), useYn })
    }
  }

  return (
    <form onSubmit={handleSubmit} className="flex flex-col gap-4">
      <div className="flex flex-col gap-1">
        <label htmlFor="ctgNm" className="text-sm font-medium text-slate-700">
          카테고리명
        </label>
        <input
          id="ctgNm"
          type="text"
          value={ctgNm}
          onChange={(e) => setCtgNm(e.target.value)}
          required
          className="rounded border border-slate-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none"
        />
      </div>

      <div className="flex flex-col gap-1">
        <label htmlFor="sortOrd" className="text-sm font-medium text-slate-700">
          정렬순서
        </label>
        <input
          id="sortOrd"
          type="number"
          value={sortOrd}
          onChange={(e) => setSortOrd(e.target.value)}
          min={0}
          required
          className="rounded border border-slate-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none"
        />
      </div>

      {props.mode === 'edit' && (
        <div className="flex flex-col gap-1">
          <label htmlFor="useYn" className="text-sm font-medium text-slate-700">
            사용여부
          </label>
          <select
            id="useYn"
            value={useYn}
            onChange={(e) => setUseYn(e.target.value as 'Y' | 'N')}
            className="rounded border border-slate-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none"
          >
            <option value="Y">사용</option>
            <option value="N">미사용</option>
          </select>
        </div>
      )}

      <div className="mt-2 flex gap-2">
        <button
          type="button"
          onClick={props.onCancel}
          className="flex-1 rounded border border-slate-300 py-2 text-sm text-slate-600 hover:bg-slate-50"
        >
          취소
        </button>
        <button
          type="submit"
          disabled={props.isPending}
          className="flex-1 rounded bg-blue-600 py-2 text-sm text-white hover:bg-blue-700 disabled:opacity-50"
        >
          {props.isPending ? '저장 중...' : '저장'}
        </button>
      </div>
    </form>
  )
}

export default CategoryForm
