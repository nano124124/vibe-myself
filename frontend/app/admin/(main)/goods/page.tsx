'use client'

import { useState } from 'react'
import Link from 'next/link'
import { useAdminGoodsList } from '@/hooks/goods/useAdminGoodsList'
import GoodsListTable from '@/components/goods/GoodsListTable'
import type { GoodsListSearchParams, GoodsSaleStatCd } from '@/types/goods.types'

const PAGE_SIZE = 20

export default function AdminGoodsPage() {
  const [search, setSearch] = useState('')
  const [saleStatCd, setSaleStatCd] = useState<GoodsSaleStatCd | ''>('')
  const [page, setPage] = useState(0)

  const params: GoodsListSearchParams = {
    goodsNm: search || undefined,
    saleStatCd: saleStatCd || undefined,
    page,
    size: PAGE_SIZE,
  }

  const { data, isLoading, isError } = useAdminGoodsList(params)

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault()
    setPage(0)
  }

  return (
    <div className="flex flex-col gap-6">
      <div className="flex items-center justify-between">
        <h1 className="text-lg font-semibold text-slate-800">상품 목록</h1>
        <Link
          href="/admin/goods/create"
          className="rounded bg-blue-600 px-4 py-2 text-sm text-white hover:bg-blue-700"
        >
          상품 등록
        </Link>
      </div>

      <form onSubmit={handleSearch} className="flex gap-2">
        <input
          type="text"
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          placeholder="상품명 검색"
          className="rounded border border-slate-300 px-3 py-2 text-sm focus:border-blue-400 focus:outline-none"
        />
        <select
          value={saleStatCd}
          onChange={(e) => {
            setSaleStatCd(e.target.value as GoodsSaleStatCd | '')
            setPage(0)
          }}
          className="rounded border border-slate-300 px-3 py-2 text-sm focus:border-blue-400 focus:outline-none"
        >
          <option value="">전체 상태</option>
          <option value="SELLING">판매중</option>
          <option value="SOLD_OUT">품절</option>
          <option value="STOPPED">판매중지</option>
        </select>
        <button
          type="submit"
          className="rounded bg-slate-700 px-4 py-2 text-sm text-white hover:bg-slate-800"
        >
          검색
        </button>
      </form>

      {isLoading && (
        <div className="py-16 text-center text-sm text-slate-400">불러오는 중...</div>
      )}

      {isError && (
        <div className="rounded border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-600">
          목록을 불러오지 못했습니다.
        </div>
      )}

      {data && (
        <>
          <div className="text-sm text-slate-500">
            총 <span className="font-medium text-slate-700">{data.totalElements.toLocaleString()}</span>건
          </div>

          <GoodsListTable items={data.content} />

          {data.totalPages > 1 && (
            <div className="flex justify-center gap-1">
              <button
                onClick={() => setPage((p) => Math.max(0, p - 1))}
                disabled={page === 0}
                className="rounded border border-slate-300 px-3 py-1.5 text-sm disabled:opacity-40 hover:bg-slate-50"
              >
                이전
              </button>
              {Array.from({ length: data.totalPages }, (_, i) => (
                <button
                  key={i}
                  onClick={() => setPage(i)}
                  className={`rounded border px-3 py-1.5 text-sm ${
                    page === i
                      ? 'border-blue-600 bg-blue-600 text-white'
                      : 'border-slate-300 hover:bg-slate-50'
                  }`}
                >
                  {i + 1}
                </button>
              ))}
              <button
                onClick={() => setPage((p) => Math.min(data.totalPages - 1, p + 1))}
                disabled={page === data.totalPages - 1}
                className="rounded border border-slate-300 px-3 py-1.5 text-sm disabled:opacity-40 hover:bg-slate-50"
              >
                다음
              </button>
            </div>
          )}
        </>
      )}
    </div>
  )
}