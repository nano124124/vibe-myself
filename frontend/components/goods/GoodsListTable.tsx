'use client'

import Image from 'next/image'
import Link from 'next/link'
import type { GoodsListItemResponse, GoodsSaleStatCd } from '@/types/goods.types'

interface GoodsListTableProps {
  items: GoodsListItemResponse[]
}

const SALE_STAT_LABEL: Record<GoodsSaleStatCd, string> = {
  SELLING: '판매중',
  SOLD_OUT: '품절',
  STOPPED: '판매중지',
}

const SALE_STAT_CLASS: Record<GoodsSaleStatCd, string> = {
  SELLING: 'text-green-600 bg-green-50',
  SOLD_OUT: 'text-amber-600 bg-amber-50',
  STOPPED: 'text-slate-500 bg-slate-100',
}

const formatPrice = (price: number) => price.toLocaleString('ko-KR') + '원'

const formatDtm = (dtm: string) => dtm.replace('T', ' ').slice(0, 16)

const GoodsListTable = ({ items }: GoodsListTableProps) => {
  if (items.length === 0) {
    return (
      <div className="rounded-lg border border-slate-200 bg-white py-16 text-center text-sm text-slate-400">
        등록된 상품이 없습니다.
      </div>
    )
  }

  return (
    <div className="overflow-x-auto rounded-lg border border-slate-200 bg-white">
      <table className="min-w-full divide-y divide-slate-200 text-sm">
        <thead className="bg-slate-50">
          <tr>
            {['상품번호', '이미지', '상품명', '카테고리', '브랜드', '판매가', '판매상태', '등록일'].map((h) => (
              <th key={h} className="px-4 py-3 text-left text-xs font-semibold text-slate-500">
                {h}
              </th>
            ))}
          </tr>
        </thead>
        <tbody className="divide-y divide-slate-100">
          {items.map((item) => (
            <tr key={item.goodsNo} className="hover:bg-slate-50">
              <td className="px-4 py-3 font-mono text-xs text-slate-500">{item.goodsNo}</td>
              <td className="px-4 py-3">
                {item.thumbImgUrl ? (
                  <Image
                    src={item.thumbImgUrl}
                    alt={item.goodsNm}
                    width={48}
                    height={48}
                    className="rounded object-cover"
                  />
                ) : (
                  <div className="h-12 w-12 rounded bg-slate-100" />
                )}
              </td>
              <td className="px-4 py-3">
                <Link
                  href={`/admin/goods/${item.goodsNo}`}
                  className="font-medium text-slate-800 hover:text-blue-600"
                >
                  {item.goodsNm}
                </Link>
              </td>
              <td className="px-4 py-3 text-slate-600">{item.ctgNm}</td>
              <td className="px-4 py-3 text-slate-500">{item.brandNm ?? '-'}</td>
              <td className="px-4 py-3 text-right font-medium text-slate-800">
                {formatPrice(item.salePrc)}
              </td>
              <td className="px-4 py-3">
                <span
                  className={`inline-block rounded-full px-2 py-0.5 text-xs font-medium ${SALE_STAT_CLASS[item.saleStatCd]}`}
                >
                  {SALE_STAT_LABEL[item.saleStatCd]}
                </span>
              </td>
              <td className="px-4 py-3 text-xs text-slate-400">{formatDtm(item.regDtm)}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}

export default GoodsListTable