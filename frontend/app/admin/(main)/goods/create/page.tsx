'use client'

import { useState, useEffect } from 'react'
import { useRouter } from 'next/navigation'
import { useCategoryList } from '@/hooks/goods/useCategoryList'
import { useBrandList } from '@/hooks/goods/useBrandList'
import { useDlvPolicyList } from '@/hooks/goods/useDlvPolicyList'
import { useOptGroupList } from '@/hooks/goods/useOptGroupList'
import { useGoodsCreate } from '@/hooks/goods/useGoodsCreate'
import GoodsBasicInfoForm from '@/components/goods/GoodsBasicInfoForm'
import GoodsImageInput from '@/components/goods/GoodsImageInput'
import GoodsOptionSelector from '@/components/goods/GoodsOptionSelector'
import GoodsUnitTable from '@/components/goods/GoodsUnitTable'
import type { CreateGoodsRequest, GoodsTypeCd, OptGrpResponse, SaleStatCd, UnitRequest } from '@/types/goods.types'

const buildUnits = (selectedCds: string[], optGroups: OptGrpResponse[]): UnitRequest[] => {
  const selectedGroups = optGroups.filter((g) => selectedCds.includes(g.optGrpCd))
  if (selectedGroups.length === 0) return []

  const cartesian = (groups: OptGrpResponse[]): UnitRequest[] => {
    const [first, ...rest] = groups
    const base: UnitRequest[] = first.items.map((item) => ({
      optItms: [{ optGrpCd: first.optGrpCd, optItmCd: item.optItmCd }],
      addPrc: 0,
      stockQty: 0,
    }))
    if (rest.length === 0) return base

    return base.flatMap((unit) =>
      rest[0].items.map((item) => ({
        optItms: [...unit.optItms, { optGrpCd: rest[0].optGrpCd, optItmCd: item.optItmCd }],
        addPrc: 0,
        stockQty: 0,
      }))
    )
  }

  return cartesian(selectedGroups)
}

export default function GoodsCreatePage() {
  const router = useRouter()
  const { data: categories = [] } = useCategoryList()
  const { data: brands = [] } = useBrandList()
  const { data: dlvPolicies = [] } = useDlvPolicyList()
  const { data: optGroups = [] } = useOptGroupList()
  const { mutate: createGoods, isPending, error } = useGoodsCreate()

  const [goodsNm, setGoodsNm] = useState('')
  const [goodsTpCd, setGoodsTpCd] = useState<GoodsTypeCd>('NORMAL')
  const [ctgNo, setCtgNo] = useState<number | ''>('')
  const [brandNo, setBrandNo] = useState<number | ''>('')
  const [salePrc, setSalePrc] = useState<number | ''>('')
  const [goodsDesc, setGoodsDesc] = useState('')
  const [saleStatCd, setSaleStatCd] = useState<SaleStatCd>('SALE')
  const [dlvPolicyNo, setDlvPolicyNo] = useState('')
  const [imgUrls, setImgUrls] = useState<string[]>([])
  const [selectedOptGrpCds, setSelectedOptGrpCds] = useState<string[]>([])
  const [units, setUnits] = useState<UnitRequest[]>([])

  useEffect(() => {
    setUnits(buildUnits(selectedOptGrpCds, optGroups))
  }, [selectedOptGrpCds, optGroups])

  const handleBasicInfoChange = (field: string, value: string | number | undefined) => {
    if (field === 'goodsNm') setGoodsNm(value as string)
    else if (field === 'goodsTpCd') setGoodsTpCd(value as GoodsTypeCd)
    else if (field === 'ctgNo') setCtgNo(value as number)
    else if (field === 'brandNo') setBrandNo(value !== undefined ? (value as number) : '')
    else if (field === 'salePrc') setSalePrc(value as number)
    else if (field === 'goodsDesc') setGoodsDesc(value as string)
    else if (field === 'saleStatCd') setSaleStatCd(value as SaleStatCd)
    else if (field === 'dlvPolicyNo') setDlvPolicyNo(value as string)
  }

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    if (!ctgNo || !salePrc || !dlvPolicyNo) return

    const request: CreateGoodsRequest = {
      goodsNm,
      goodsTpCd,
      ctgNo: ctgNo as number,
      brandNo: brandNo !== '' ? (brandNo as number) : undefined,
      salePrc: salePrc as number,
      goodsDesc: goodsDesc || undefined,
      saleStatCd,
      dlvPolicyNo,
      imgUrls: imgUrls.filter((url) => url.trim() !== ''),
      optGrpCds: selectedOptGrpCds,
      units,
    }

    createGoods(request, {
      onSuccess: () => router.push('/admin/products'),
    })
  }

  const errorMessage = error instanceof Error ? error.message : null

  return (
    <div className="mx-auto max-w-4xl">
      <div className="mb-6 flex items-center justify-between">
        <h1 className="text-lg font-semibold text-slate-800">상품 등록</h1>
        <button
          type="button"
          onClick={() => router.back()}
          className="text-sm text-slate-500 hover:text-slate-700"
        >
          ← 뒤로
        </button>
      </div>

      {errorMessage && (
        <div className="mb-4 rounded border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-600">
          {errorMessage}
        </div>
      )}

      <form onSubmit={handleSubmit} className="flex flex-col gap-6">
        <section className="rounded-lg border border-slate-200 bg-white p-6">
          <h2 className="mb-4 text-sm font-semibold text-slate-700">기본 정보</h2>
          <GoodsBasicInfoForm
            goodsNm={goodsNm}
            goodsTpCd={goodsTpCd}
            ctgNo={ctgNo}
            brandNo={brandNo}
            salePrc={salePrc}
            goodsDesc={goodsDesc}
            saleStatCd={saleStatCd}
            dlvPolicyNo={dlvPolicyNo}
            categories={categories}
            brands={brands}
            dlvPolicies={dlvPolicies}
            onChange={handleBasicInfoChange}
          />
        </section>

        <section className="rounded-lg border border-slate-200 bg-white p-6">
          <h2 className="mb-4 text-sm font-semibold text-slate-700">상품 이미지</h2>
          <GoodsImageInput imgUrls={imgUrls} onChange={setImgUrls} />
        </section>

        <section className="rounded-lg border border-slate-200 bg-white p-6">
          <h2 className="mb-4 text-sm font-semibold text-slate-700">옵션 설정</h2>
          <GoodsOptionSelector
            optGroups={optGroups}
            selectedCds={selectedOptGrpCds}
            onChange={setSelectedOptGrpCds}
          />
        </section>

        <section className="rounded-lg border border-slate-200 bg-white p-6">
          <h2 className="mb-4 text-sm font-semibold text-slate-700">단품 (재고 / 추가금액)</h2>
          <GoodsUnitTable units={units} onChange={setUnits} />
        </section>

        <div className="flex justify-end gap-3 pb-8">
          <button
            type="button"
            onClick={() => router.back()}
            className="rounded border border-slate-300 px-6 py-2.5 text-sm text-slate-600 hover:bg-slate-50"
          >
            취소
          </button>
          <button
            type="submit"
            disabled={isPending}
            className="rounded bg-blue-600 px-6 py-2.5 text-sm text-white hover:bg-blue-700 disabled:opacity-50"
          >
            {isPending ? '등록 중...' : '상품 등록'}
          </button>
        </div>
      </form>
    </div>
  )
}
