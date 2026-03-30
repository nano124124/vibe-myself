'use client'

import type { CategoryResponse } from '@/types/goods.types'

interface CategoryTreeProps {
  categories: CategoryResponse[]
  onEdit: (category: CategoryResponse) => void
  onCreateChild: (upCtgNo: number) => void
}

interface RowProps {
  category: CategoryResponse
  depth: number
  onEdit: (category: CategoryResponse) => void
  onCreateChild: (upCtgNo: number) => void
}

const CategoryRow = ({ category, depth, onEdit, onCreateChild }: RowProps) => {
  const indent = depth * 16

  return (
    <>
      <tr className="border-b border-slate-100 hover:bg-slate-50">
        <td className="py-2 pr-4 text-sm" style={{ paddingLeft: `${12 + indent}px` }}>
          {depth > 0 && <span className="mr-1 text-slate-400">└</span>}
          {category.ctgNm}
        </td>
        <td className="px-4 py-2 text-center text-sm text-slate-500">{category.ctgLvl}</td>
        <td className="px-4 py-2 text-center text-sm text-slate-500">{category.sortOrd}</td>
        <td
          className="px-4 py-2 text-center text-sm"
          data-testid={`use-yn-${category.ctgNo}`}
        >
          <span
            className={`inline-block rounded px-1.5 py-0.5 text-xs font-medium ${
              category.useYn === 'Y'
                ? 'bg-green-100 text-green-700'
                : 'bg-slate-100 text-slate-500'
            }`}
          >
            {category.useYn}
          </span>
        </td>
        <td className="px-4 py-2 text-center">
          <div className="flex items-center justify-center gap-1">
            <button
              data-testid={`edit-btn-${category.ctgNo}`}
              onClick={() => onEdit(category)}
              className="rounded px-2 py-1 text-xs text-slate-600 hover:bg-slate-200"
            >
              수정
            </button>
            {category.ctgLvl !== '3' && (
              <button
                data-testid={`add-child-btn-${category.ctgNo}`}
                onClick={() => onCreateChild(category.ctgNo)}
                className="rounded px-2 py-1 text-xs text-blue-600 hover:bg-blue-50"
              >
                + 하위
              </button>
            )}
          </div>
        </td>
      </tr>
      {category.children.map((child) => (
        <CategoryRow
          key={child.ctgNo}
          category={child}
          depth={depth + 1}
          onEdit={onEdit}
          onCreateChild={onCreateChild}
        />
      ))}
    </>
  )
}

const CategoryTree = ({ categories, onEdit, onCreateChild }: CategoryTreeProps) => {
  return (
    <table className="w-full border-collapse text-left">
      <thead>
        <tr className="border-b-2 border-slate-200 text-xs font-semibold uppercase text-slate-500">
          <th className="py-2 pl-3">카테고리명</th>
          <th className="px-4 py-2 text-center">레벨</th>
          <th className="px-4 py-2 text-center">정렬</th>
          <th className="px-4 py-2 text-center">사용</th>
          <th className="px-4 py-2 text-center">액션</th>
        </tr>
      </thead>
      <tbody>
        {categories.map((category) => (
          <CategoryRow
            key={category.ctgNo}
            category={category}
            depth={0}
            onEdit={onEdit}
            onCreateChild={onCreateChild}
          />
        ))}
      </tbody>
    </table>
  )
}

export default CategoryTree
