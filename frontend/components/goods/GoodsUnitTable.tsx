'use client'

import type { OptGrpResponse, UnitRequest } from '@/types/goods.types'

interface GoodsUnitTableProps {
  units: UnitRequest[]
  optGroups: OptGrpResponse[]
  onChange: (units: UnitRequest[]) => void
}

const GoodsUnitTable = ({ units, optGroups, onChange }: GoodsUnitTableProps) => {
  const itmNmMap: Record<string, Record<string, string>> = {}
  for (const grp of optGroups) {
    itmNmMap[grp.optGrpCd] = {}
    for (const itm of grp.items) {
      itmNmMap[grp.optGrpCd][itm.optItmCd] = itm.optItmNm
    }
  }

  const updateUnit = (index: number, field: 'addPrc' | 'stockQty', value: number) => {
    const next = units.map((u, i) => (i === index ? { ...u, [field]: value } : u))
    onChange(next)
  }

  if (units.length === 0) {
    return (
      <p className="text-sm text-slate-400">옵션 그룹을 선택하면 단품이 자동 생성됩니다.</p>
    )
  }

  return (
    <table className="w-full text-sm">
      <thead>
        <tr className="border-b border-slate-200 text-left text-xs text-slate-500">
          <th className="pb-2 font-medium">옵션 조합</th>
          <th className="pb-2 font-medium">추가금액 (원)</th>
          <th className="pb-2 font-medium">재고 수량</th>
        </tr>
      </thead>
      <tbody>
        {units.map((unit, i) => (
          <tr key={i} className="border-b border-slate-100">
            <td className="py-2 pr-4 text-slate-700">
              {unit.optItms.map((o) => itmNmMap[o.optGrpCd]?.[o.optItmCd] ?? o.optItmCd).join(' / ')}
            </td>
            <td className="py-2 pr-4">
              <input
                type="number"
                value={unit.addPrc}
                onChange={(e) => updateUnit(i, 'addPrc', Number(e.target.value))}
                min={0}
                className="w-28 rounded border border-slate-300 px-2 py-1 text-sm focus:border-blue-500 focus:outline-none"
              />
            </td>
            <td className="py-2">
              <input
                type="number"
                value={unit.stockQty}
                onChange={(e) => updateUnit(i, 'stockQty', Number(e.target.value))}
                min={0}
                className="w-24 rounded border border-slate-300 px-2 py-1 text-sm focus:border-blue-500 focus:outline-none"
              />
            </td>
          </tr>
        ))}
      </tbody>
    </table>
  )
}

export default GoodsUnitTable