'use client'

import { Controller } from 'react-hook-form'
import type { Control, FieldErrors, UseFormRegister } from 'react-hook-form'
import type { BrandResponse, CategoryResponse, DlvPolicyResponse, GoodsCreateFormValues } from '@/types/goods.types'
import type { CodeResponse } from '@/types/system.types'
import GoodsCategorySelector from '@/components/goods/GoodsCategorySelector'

interface GoodsBasicInfoFormProps {
  control: Control<GoodsCreateFormValues>
  register: UseFormRegister<GoodsCreateFormValues>
  errors: FieldErrors<GoodsCreateFormValues>
  mrgnRate: number | null
  categories: CategoryResponse[]
  brands: BrandResponse[]
  dlvPolicies: DlvPolicyResponse[]
  goodsTypeCodes: CodeResponse[]
  saleStatCodes: CodeResponse[]
}

const inputCls = 'w-full rounded border border-slate-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none'
const inputErrCls = 'w-full rounded border border-red-400 px-3 py-2 text-sm focus:border-red-500 focus:outline-none'
const labelCls = 'text-sm font-medium text-slate-700'
const errCls = 'text-xs text-red-500'

const GoodsBasicInfoForm = ({
  control, register, errors, mrgnRate,
  categories, brands, dlvPolicies, goodsTypeCodes, saleStatCodes,
}: GoodsBasicInfoFormProps) => {
  return (
    <div className="grid grid-cols-2 gap-4">

      <div className="col-span-2 flex flex-col gap-1">
        <label className={labelCls}>상품명 *</label>
        <input
          {...register('goodsNm')}
          placeholder="상품명을 입력하세요"
          className={errors.goodsNm ? inputErrCls : inputCls}
        />
        {errors.goodsNm && <span className={errCls}>{errors.goodsNm.message}</span>}
      </div>

      <div className="flex flex-col gap-1">
        <label className={labelCls}>상품유형 *</label>
        <select {...register('goodsTpCd')} className={inputCls}>
          {goodsTypeCodes.map((c) => (
            <option key={c.codeCd} value={c.codeCd}>{c.codeNm}</option>
          ))}
        </select>
      </div>

      <div className="flex flex-col gap-1">
        <label className={labelCls}>판매상태 *</label>
        <select {...register('saleStatCd')} className={inputCls}>
          {saleStatCodes.map((c) => (
            <option key={c.codeCd} value={c.codeCd}>{c.codeNm}</option>
          ))}
        </select>
      </div>

      <div className="flex flex-col gap-1">
        <label className={labelCls}>카테고리 *</label>
        <Controller
          name="ctgNo"
          control={control}
          render={({ field }) => (
            <GoodsCategorySelector
              categories={categories}
              value={field.value ?? ''}
              onChange={(val) => field.onChange(val !== '' ? val : null)}
            />
          )}
        />
        {errors.ctgNo && <span className={errCls}>{errors.ctgNo.message}</span>}
      </div>

      <div className="flex flex-col gap-1">
        <label className={labelCls}>브랜드</label>
        <Controller
          name="brandNo"
          control={control}
          render={({ field }) => (
            <select
              value={field.value ?? ''}
              onChange={(e) => field.onChange(e.target.value ? Number(e.target.value) : null)}
              className={inputCls}
            >
              <option value="">브랜드 없음</option>
              {brands.map((b) => (
                <option key={b.brandNo} value={b.brandNo}>{b.brandNm}</option>
              ))}
            </select>
          )}
        />
      </div>

      <div className="flex flex-col gap-1">
        <label className={labelCls}>판매가 *</label>
        <input
          {...register('salePrc', { valueAsNumber: true })}
          type="number"
          min={1}
          placeholder="0"
          className={errors.salePrc ? inputErrCls : inputCls}
        />
        {errors.salePrc && <span className={errCls}>{errors.salePrc.message}</span>}
      </div>

      <div className="flex flex-col gap-1">
        <label className={labelCls}>정상가</label>
        <input
          {...register('normPrc', { valueAsNumber: true })}
          type="number"
          min={0}
          placeholder="0"
          className={inputCls}
        />
      </div>

      <div className="flex flex-col gap-1">
        <label className={labelCls}>공급원가</label>
        <div className="flex items-center gap-2">
          <input
            {...register('suplyPrc', { valueAsNumber: true })}
            type="number"
            min={0}
            placeholder="0"
            className={inputCls}
          />
          <span className="shrink-0 text-sm text-slate-500">
            마진율 {mrgnRate !== null ? `${mrgnRate}%` : '-'}
          </span>
        </div>
      </div>

      <div className="flex flex-col gap-1">
        <label className={labelCls}>배송정책 *</label>
        <select {...register('dlvPolicyNo')} className={errors.dlvPolicyNo ? inputErrCls : inputCls}>
          <option value="">배송정책 선택</option>
          {dlvPolicies.map((d) => (
            <option key={d.dlvPolicyNo} value={d.dlvPolicyNo}>{d.dlvPolicyNm}</option>
          ))}
        </select>
        {errors.dlvPolicyNo && <span className={errCls}>{errors.dlvPolicyNo.message}</span>}
      </div>

      <div className="flex flex-col gap-1">
        <label className={labelCls}>판매시작일시</label>
        <input
          {...register('saleStartDtm')}
          type="datetime-local"
          className={inputCls}
        />
      </div>

      <div className="flex flex-col gap-1">
        <label className={labelCls}>판매종료일시</label>
        <input
          {...register('saleEndDtm')}
          type="datetime-local"
          className={inputCls}
        />
      </div>

      <div className="col-span-2 flex flex-col gap-1">
        <label className={labelCls}>상품 상세설명</label>
        <textarea
          {...register('goodsDesc')}
          rows={5}
          placeholder="상품 상세설명을 입력하세요"
          className={inputCls + ' resize-none'}
        />
      </div>
    </div>
  )
}

export default GoodsBasicInfoForm
