'use client'

import type { OptGrpResponse } from '@/types/goods.types'

interface GoodsOptionSelectorProps {
  optGroups: OptGrpResponse[]
  selectedCds: string[]
  onChange: (selectedCds: string[]) => void
}

const GoodsOptionSelector = ({ optGroups, selectedCds, onChange }: GoodsOptionSelectorProps) => {
  const toggle = (cd: string) => {
    if (selectedCds.includes(cd)) {
      onChange(selectedCds.filter((c) => c !== cd))
    } else {
      onChange([...selectedCds, cd])
    }
  }

  return (
    <div className="flex flex-col gap-3">
      <p className="text-sm text-slate-500">
        옵션 그룹을 선택하면 조합에 따른 단품이 자동 생성됩니다.
      </p>
      <div className="flex flex-wrap gap-2">
        {optGroups.map((grp) => (
          <button
            key={grp.optGrpCd}
            type="button"
            onClick={() => toggle(grp.optGrpCd)}
            className={
              'rounded border px-3 py-1.5 text-sm transition-colors ' +
              (selectedCds.includes(grp.optGrpCd)
                ? 'border-blue-500 bg-blue-50 text-blue-700'
                : 'border-slate-300 text-slate-600 hover:border-slate-400')
            }
          >
            {grp.optGrpNm}
            <span className="ml-1 text-xs text-slate-400">({grp.items.length})</span>
          </button>
        ))}
      </div>

      {selectedCds.length > 0 && (
        <div className="rounded bg-slate-50 p-3">
          <p className="mb-2 text-xs font-medium text-slate-500">선택된 옵션 항목</p>
          <div className="flex flex-col gap-2">
            {optGroups
              .filter((g) => selectedCds.includes(g.optGrpCd))
              .map((grp) => (
                <div key={grp.optGrpCd} className="flex items-center gap-2 text-sm">
                  <span className="w-20 shrink-0 font-medium text-slate-700">{grp.optGrpNm}</span>
                  <span className="text-slate-500">{grp.items.map((i) => i.optItmNm).join(', ')}</span>
                </div>
              ))}
          </div>
        </div>
      )}
    </div>
  )
}

export default GoodsOptionSelector