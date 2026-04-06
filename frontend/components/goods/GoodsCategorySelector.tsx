'use client'

import { useState } from 'react'
import type { CategoryResponse } from '@/types/goods.types'

interface GoodsCategorySelectorProps {
  categories: CategoryResponse[]
  value: number | ''
  onChange: (ctgNo: number | '') => void
}

const isLeaf = (c: CategoryResponse) => c.children.length === 0

const selectCls =
  'w-full rounded border border-slate-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none disabled:bg-slate-50 disabled:text-slate-400'

const GoodsCategorySelector = ({ categories, value, onChange }: GoodsCategorySelectorProps) => {
  const [l1No, setL1No] = useState<number | ''>('')
  const [l2No, setL2No] = useState<number | ''>('')

  const l1List = categories
  const l2List = l1No !== '' ? (l1List.find((c) => c.ctgNo === l1No)?.children ?? []) : []
  const l3List = l2No !== '' ? (l2List.find((c) => c.ctgNo === l2No)?.children ?? []) : []

  const handleL1Change = (ctgNo: number | '') => {
    setL1No(ctgNo)
    setL2No('')
    if (ctgNo === '') { onChange(''); return }
    const c = l1List.find((x) => x.ctgNo === ctgNo)
    onChange(c && isLeaf(c) ? ctgNo : '')
  }

  const handleL2Change = (ctgNo: number | '') => {
    setL2No(ctgNo)
    if (ctgNo === '') { onChange(''); return }
    const c = l2List.find((x) => x.ctgNo === ctgNo)
    onChange(c && isLeaf(c) ? ctgNo : '')
  }

  const handleL3Change = (ctgNo: number | '') => {
    onChange(ctgNo)
  }

  return (
    <div className="flex flex-col gap-2">
      <select
        value={l1No}
        onChange={(e) => handleL1Change(e.target.value ? Number(e.target.value) : '')}
        className={selectCls}
      >
        <option value="">대분류 선택</option>
        {l1List.map((c) => (
          <option key={c.ctgNo} value={c.ctgNo}>{c.ctgNm}</option>
        ))}
      </select>

      {l1No !== '' && l2List.length > 0 && (
        <select
          value={l2No}
          onChange={(e) => handleL2Change(e.target.value ? Number(e.target.value) : '')}
          className={selectCls}
        >
          <option value="">중분류 선택</option>
          {l2List.map((c) => (
            <option key={c.ctgNo} value={c.ctgNo}>{c.ctgNm}</option>
          ))}
        </select>
      )}

      {l2No !== '' && l3List.length > 0 && (
        <select
          value={value}
          onChange={(e) => handleL3Change(e.target.value ? Number(e.target.value) : '')}
          className={selectCls}
        >
          <option value="">소분류 선택</option>
          {l3List.map((c) => (
            <option key={c.ctgNo} value={c.ctgNo}>{c.ctgNm}</option>
          ))}
        </select>
      )}

      {value !== '' && (
        <p className="text-xs text-blue-600">
          ✓ 선택됨: {[
            l1List.find((c) => c.ctgNo === l1No)?.ctgNm,
            l2List.find((c) => c.ctgNo === l2No)?.ctgNm,
            l3List.find((c) => c.ctgNo === value)?.ctgNm,
          ].filter(Boolean).join(' > ')}
        </p>
      )}
    </div>
  )
}

export default GoodsCategorySelector
