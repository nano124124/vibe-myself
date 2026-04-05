'use client'

import type { BrandResponse, CategoryResponse, DlvPolicyResponse, GoodsTypeCd, SaleStatCd } from '@/types/goods.types'

interface GoodsBasicInfoFormProps {
  goodsNm: string
  goodsTpCd: GoodsTypeCd
  ctgNo: number | ''
  brandNo: number | ''
  salePrc: number | ''
  goodsDesc: string
  saleStatCd: SaleStatCd
  dlvPolicyNo: string
  categories: CategoryResponse[]
  brands: BrandResponse[]
  dlvPolicies: DlvPolicyResponse[]
  onChange: (field: string, value: string | number | undefined) => void
}

const GOODS_TYPE_OPTIONS: { value: GoodsTypeCd; label: string }[] = [
  { value: 'NORMAL', label: '일반상품' },
  { value: 'EGIFT', label: 'e쿠폰' },
  { value: 'GIFT', label: '사은품' },
]

const SALE_STAT_OPTIONS: { value: SaleStatCd; label: string }[] = [
  { value: 'SALE', label: '판매중' },
  { value: 'STOP', label: '판매중지' },
]

const flattenCategories = (categories: CategoryResponse[], depth = 0): { ctgNo: number; label: string }[] =>
  categories.flatMap((c) => [
    { ctgNo: c.ctgNo, label: '\u00A0'.repeat(depth * 4) + c.ctgNm },
    ...flattenCategories(c.children, depth + 1),
  ])

const inputCls = 'w-full rounded border border-slate-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none'
const labelCls = 'text-sm font-medium text-slate-700'

const GoodsBasicInfoForm = ({
  goodsNm, goodsTpCd, ctgNo, brandNo, salePrc, goodsDesc,
  saleStatCd, dlvPolicyNo, categories, brands, dlvPolicies, onChange,
}: GoodsBasicInfoFormProps) => {
  const flatCategories = flattenCategories(categories)

  return (
    <div className="grid grid-cols-2 gap-4">
      <div className="col-span-2 flex flex-col gap-1">
        <label className={labelCls}>상품명 *</label>
        <input
          type="text"
          value={goodsNm}
          onChange={(e) => onChange('goodsNm', e.target.value)}
          placeholder="상품명을 입력하세요"
          className={inputCls}
        />
      </div>

      <div className="flex flex-col gap-1">
        <label className={labelCls}>상품유형 *</label>
        <select value={goodsTpCd} onChange={(e) => onChange('goodsTpCd', e.target.value)} className={inputCls}>
          {GOODS_TYPE_OPTIONS.map((o) => (
            <option key={o.value} value={o.value}>{o.label}</option>
          ))}
        </select>
      </div>

      <div className="flex flex-col gap-1">
        <label className={labelCls}>판매상태 *</label>
        <select value={saleStatCd} onChange={(e) => onChange('saleStatCd', e.target.value)} className={inputCls}>
          {SALE_STAT_OPTIONS.map((o) => (
            <option key={o.value} value={o.value}>{o.label}</option>
          ))}
        </select>
      </div>

      <div className="flex flex-col gap-1">
        <label className={labelCls}>카테고리 *</label>
        <select value={ctgNo} onChange={(e) => onChange('ctgNo', Number(e.target.value))} className={inputCls}>
          <option value="">카테고리 선택</option>
          {flatCategories.map((c) => (
            <option key={c.ctgNo} value={c.ctgNo}>{c.label}</option>
          ))}
        </select>
      </div>

      <div className="flex flex-col gap-1">
        <label className={labelCls}>브랜드</label>
        <select value={brandNo} onChange={(e) => onChange('brandNo', e.target.value ? Number(e.target.value) : undefined)} className={inputCls}>
          <option value="">브랜드 없음</option>
          {brands.map((b) => (
            <option key={b.brandNo} value={b.brandNo}>{b.brandNm}</option>
          ))}
        </select>
      </div>

      <div className="flex flex-col gap-1">
        <label className={labelCls}>판매가 *</label>
        <input
          type="number"
          value={salePrc}
          onChange={(e) => onChange('salePrc', Number(e.target.value))}
          min={1}
          placeholder="0"
          className={inputCls}
        />
      </div>

      <div className="flex flex-col gap-1">
        <label className={labelCls}>배송정책 *</label>
        <select value={dlvPolicyNo} onChange={(e) => onChange('dlvPolicyNo', e.target.value)} className={inputCls}>
          <option value="">배송정책 선택</option>
          {dlvPolicies.map((d) => (
            <option key={d.dlvPolicyNo} value={d.dlvPolicyNo}>{d.dlvPolicyNm}</option>
          ))}
        </select>
      </div>

      <div className="col-span-2 flex flex-col gap-1">
        <label className={labelCls}>상품 상세설명</label>
        <textarea
          value={goodsDesc}
          onChange={(e) => onChange('goodsDesc', e.target.value)}
          rows={5}
          placeholder="상품 상세설명을 입력하세요"
          className={inputCls + ' resize-none'}
        />
      </div>
    </div>
  )
}

export default GoodsBasicInfoForm