'use client'

import { useState, useEffect, useMemo } from 'react'
import { useRouter } from 'next/navigation'
import { useForm } from 'react-hook-form'
import { useCategoryList } from '@/hooks/goods/useCategoryList'
import { useBrandList } from '@/hooks/goods/useBrandList'
import { useDlvPolicyList } from '@/hooks/goods/useDlvPolicyList'
import { useOptGroupList } from '@/hooks/goods/useOptGroupList'
import { useGoodsCreate } from '@/hooks/goods/useGoodsCreate'
import { useCodeList } from '@/hooks/system/useCodeList'
import GoodsBasicInfoForm from '@/components/goods/GoodsBasicInfoForm'
import GoodsImageInput from '@/components/goods/GoodsImageInput'
import GoodsOptionSelector from '@/components/goods/GoodsOptionSelector'
import GoodsUnitTable from '@/components/goods/GoodsUnitTable'
import { CODE_GROUP } from '@/types/system.types'
import type { CreateGoodsRequest, GoodsCreateFormValues, OptGrpResponse, UnitRequest } from '@/types/goods.types'

const SALE_END_DTM_DEFAULT = '2999-12-31T00:00'
const EMPTY_OPT_GROUPS: OptGrpResponse[] = []

const toDatetimeLocal = (d: Date) => {
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`
}

const buildUnits = (
  selectedCds: string[],
  optGroups: OptGrpResponse[],
  selectedItms: Record<string, string[]>,
): UnitRequest[] => {
  const selectedGroups = optGroups
    .filter((g) => selectedCds.includes(g.optGrpCd))
    .map((g) => ({
      ...g,
      items: g.items.filter((i) => (selectedItms[g.optGrpCd] ?? []).includes(i.optItmCd)),
    }))
    .filter((g) => g.items.length > 0)

  if (selectedGroups.length === 0) return []

  const cartesian = (groups: typeof selectedGroups): UnitRequest[] => {
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

const calcMrgnRate = (salePrc: number, suplyPrc: number): number | null => {
  if (!suplyPrc || !salePrc || salePrc <= 0) return null
  return Math.round(((salePrc - suplyPrc) / salePrc) * 10000) / 100
}

export default function GoodsCreatePage() {
  const router = useRouter()

  // 참조 데이터
  const { data: categories = [] } = useCategoryList()
  const { data: brands = [] } = useBrandList()
  const { data: dlvPolicies = [] } = useDlvPolicyList()
  const { data: optGroups = EMPTY_OPT_GROUPS } = useOptGroupList()
  const { data: goodsTypeCodes = [] } = useCodeList(CODE_GROUP.GOODS_TP)
  const { data: saleStatCodes = [] } = useCodeList(CODE_GROUP.SALE_STAT)

  const { mutate: createGoods, isPending, error } = useGoodsCreate()

  // RHF
  const { register, handleSubmit, control, watch, formState: { errors } } = useForm<GoodsCreateFormValues>({
    defaultValues: {
      goodsNm: '',
      goodsTpCd: 'NORMAL',
      ctgNo: null,
      brandNo: null,
      salePrc: 0,
      normPrc: 0,
      suplyPrc: 0,
      goodsDesc: '',
      saleStartDtm: toDatetimeLocal(new Date()),
      saleEndDtm: SALE_END_DTM_DEFAULT,
      saleStatCd: 'SALE',
      dlvPolicyNo: '',
    },
  })

  const salePrc = watch('salePrc')
  const suplyPrc = watch('suplyPrc')
  const mrgnRate = useMemo(() => calcMrgnRate(salePrc, suplyPrc), [salePrc, suplyPrc])

  // 옵션/단품 상태 (RHF 외부 관리)
  const [imgUrls, setImgUrls] = useState<string[]>([])
  const [selectedOptGrpCds, setSelectedOptGrpCds] = useState<string[]>([])
  const [selectedOptItms, setSelectedOptItms] = useState<Record<string, string[]>>({})
  const [units, setUnits] = useState<UnitRequest[]>([])

  useEffect(() => {
    setUnits(buildUnits(selectedOptGrpCds, optGroups, selectedOptItms))
  }, [selectedOptGrpCds, optGroups, selectedOptItms])

  const handleGrpChange = (cds: string[]) => {
    const added = cds.find((cd) => !selectedOptGrpCds.includes(cd))
    const removed = selectedOptGrpCds.find((cd) => !cds.includes(cd))

    if (added) {
      const grp = optGroups.find((g) => g.optGrpCd === added)
      setSelectedOptItms((prev) => ({
        ...prev,
        [added]: grp ? grp.items.map((i) => i.optItmCd) : [],
      }))
    }
    if (removed) {
      setSelectedOptItms((prev) => {
        const next = { ...prev }
        delete next[removed]
        return next
      })
    }
    setSelectedOptGrpCds(cds)
  }

  const handleItmChange = (grpCd: string, itmCds: string[]) => {
    setSelectedOptItms((prev) => ({ ...prev, [grpCd]: itmCds }))
  }

  const onSubmit = (formValues: GoodsCreateFormValues) => {
    if (!formValues.ctgNo || !formValues.dlvPolicyNo) return

    const request: CreateGoodsRequest = {
      goodsNm: formValues.goodsNm,
      goodsTpCd: formValues.goodsTpCd,
      ctgNo: formValues.ctgNo,
      brandNo: formValues.brandNo ?? undefined,
      salePrc: formValues.salePrc,
      normPrc: formValues.normPrc || undefined,
      suplyPrc: formValues.suplyPrc || undefined,
      goodsDesc: formValues.goodsDesc || undefined,
      saleStartDtm: formValues.saleStartDtm || undefined,
      saleEndDtm: formValues.saleEndDtm || undefined,
      saleStatCd: formValues.saleStatCd,
      dlvPolicyNo: formValues.dlvPolicyNo,
      imgUrls: imgUrls.filter((url) => url.trim() !== ''),
      optGrpCds: selectedOptGrpCds,
      units,
    }

    createGoods(request, {
      onSuccess: () => router.push('/admin/goods'),
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

      <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-6">
        <section className="rounded-lg border border-slate-200 bg-white p-6">
          <h2 className="mb-4 text-sm font-semibold text-slate-700">기본 정보</h2>
          <GoodsBasicInfoForm
            control={control}
            register={register}
            errors={errors}
            mrgnRate={mrgnRate}
            categories={categories}
            brands={brands}
            dlvPolicies={dlvPolicies}
            goodsTypeCodes={goodsTypeCodes}
            saleStatCodes={saleStatCodes}
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
            selectedItms={selectedOptItms}
            onChange={handleGrpChange}
            onItemChange={handleItmChange}
          />
        </section>

        <section className="rounded-lg border border-slate-200 bg-white p-6">
          <h2 className="mb-4 text-sm font-semibold text-slate-700">단품 (재고 / 추가금액)</h2>
          <GoodsUnitTable units={units} optGroups={optGroups} onChange={setUnits} />
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
