'use client'

import type { OptGrpResponse } from '@/types/goods.types'

interface GoodsOptionSelectorProps {
  optGroups: OptGrpResponse[]
  selectedCds: string[]
  selectedItms: Record<string, string[]>
  onChange: (selectedCds: string[]) => void
  onItemChange: (grpCd: string, itmCds: string[]) => void
}

const GoodsOptionSelector = ({
  optGroups, selectedCds, selectedItms, onChange, onItemChange,
}: GoodsOptionSelectorProps) => {
  const toggleGrp = (cd: string) => {
    if (selectedCds.includes(cd)) {
      onChange(selectedCds.filter((c) => c !== cd))
    } else {
      onChange([...selectedCds, cd])
    }
  }

  const toggleItm = (grpCd: string, itmCd: string) => {
    const current = selectedItms[grpCd] ?? []
    const next = current.includes(itmCd)
      ? current.filter((c) => c !== itmCd)
      : [...current, itmCd]
    onItemChange(grpCd, next)
  }

  return (
    <div className="flex flex-col gap-3">
      <p className="text-sm text-slate-500">
        옵션 그룹을 선택한 뒤 사용할 항목을 지정하면 조합에 따른 단품이 자동 생성됩니다.
      </p>
      <div className="flex flex-wrap gap-2">
        {optGroups.map((grp) => (
          <button
            key={grp.optGrpCd}
            type="button"
            onClick={() => toggleGrp(grp.optGrpCd)}
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
        <div className="flex flex-col gap-3 rounded bg-slate-50 p-3">
          {optGroups
            .filter((g) => selectedCds.includes(g.optGrpCd))
            .map((grp) => (
              <div key={grp.optGrpCd}>
                <p className="mb-1.5 text-xs font-medium text-slate-600">{grp.optGrpNm}</p>
                <div className="flex flex-wrap gap-2">
                  {grp.items.map((itm) => {
                    const checked = (selectedItms[grp.optGrpCd] ?? []).includes(itm.optItmCd)
                    return (
                      <label
                        key={itm.optItmCd}
                        className={
                          'flex cursor-pointer items-center gap-1.5 rounded border px-2.5 py-1 text-sm transition-colors ' +
                          (checked
                            ? 'border-blue-400 bg-blue-50 text-blue-700'
                            : 'border-slate-300 text-slate-600 hover:border-slate-400')
                        }
                      >
                        <input
                          type="checkbox"
                          checked={checked}
                          onChange={() => toggleItm(grp.optGrpCd, itm.optItmCd)}
                          className="hidden"
                        />
                        {itm.optItmNm}
                      </label>
                    )
                  })}
                </div>
              </div>
            ))}
        </div>
      )}
    </div>
  )
}

export default GoodsOptionSelector
