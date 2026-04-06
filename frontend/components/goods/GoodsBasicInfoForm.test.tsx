import { describe, it, expect } from 'vitest'
import { render, screen } from '@testing-library/react'
import { useForm } from 'react-hook-form'
import type { FieldErrors } from 'react-hook-form'
import GoodsBasicInfoForm from './GoodsBasicInfoForm'
import type { BrandResponse, CategoryResponse, DlvPolicyResponse, GoodsCreateFormValues } from '@/types/goods.types'
import type { CodeResponse } from '@/types/system.types'

// ── mock 데이터 ───────────────────────────────────────────────────────────────

const mockCategories: CategoryResponse[] = [
  {
    ctgNo: 1, upCtgNo: null, ctgLvl: '1', ctgNm: '의류', sortOrd: 1, useYn: 'Y',
    children: [
      {
        ctgNo: 2, upCtgNo: 1, ctgLvl: '2', ctgNm: '상의', sortOrd: 1, useYn: 'Y',
        children: [
          { ctgNo: 3, upCtgNo: 2, ctgLvl: '3', ctgNm: '티셔츠', sortOrd: 1, useYn: 'Y', children: [] },
        ],
      },
    ],
  },
]

const mockBrands: BrandResponse[] = [
  { brandNo: 1, brandNm: '나이키', brandImgUrl: null },
  { brandNo: 2, brandNm: '아디다스', brandImgUrl: null },
]

const mockDlvPolicies: DlvPolicyResponse[] = [
  { dlvPolicyNo: 'DLV001', dlvPolicyNm: '무료배송', dlvTpCd: 'FREE', dlvAmt: 0 },
  { dlvPolicyNo: 'DLV002', dlvPolicyNm: '유료배송', dlvTpCd: 'PAID', dlvAmt: 3000 },
]

const mockGoodsTypeCodes: CodeResponse[] = [
  { codeCd: 'NORMAL', codeNm: '일반상품', sortOrd: 1 },
  { codeCd: 'EGIFT', codeNm: 'e쿠폰', sortOrd: 2 },
  { codeCd: 'GIFT', codeNm: '사은품', sortOrd: 3 },
]

const mockSaleStatCodes: CodeResponse[] = [
  { codeCd: 'SALE', codeNm: '판매중', sortOrd: 1 },
  { codeCd: 'STOP', codeNm: '판매중지', sortOrd: 2 },
]

// ── 테스트 Wrapper ─────────────────────────────────────────────────────────────

interface WrapperProps {
  errors?: FieldErrors<GoodsCreateFormValues>
  mrgnRate?: number | null
}

const Wrapper = ({ errors = {}, mrgnRate = null }: WrapperProps) => {
  const { register, control } = useForm<GoodsCreateFormValues>({
    defaultValues: {
      goodsNm: '', goodsTpCd: 'NORMAL', saleStatCd: 'SALE',
      ctgNo: null, brandNo: null, salePrc: 0, normPrc: 0, suplyPrc: 0,
      goodsDesc: '', saleStartDtm: '', saleEndDtm: '', dlvPolicyNo: '',
    },
  })
  return (
    <GoodsBasicInfoForm
      register={register}
      control={control}
      errors={errors}
      mrgnRate={mrgnRate}
      categories={mockCategories}
      brands={mockBrands}
      dlvPolicies={mockDlvPolicies}
      goodsTypeCodes={mockGoodsTypeCodes}
      saleStatCodes={mockSaleStatCodes}
    />
  )
}

// ── 테스트 ────────────────────────────────────────────────────────────────────

describe('GoodsBasicInfoForm', () => {

  describe('렌더링', () => {
    it('모든 필드 라벨이 화면에 존재한다', () => {
      render(<Wrapper />)

      expect(screen.getByText('상품명 *')).toBeInTheDocument()
      expect(screen.getByText('상품유형 *')).toBeInTheDocument()
      expect(screen.getByText('판매상태 *')).toBeInTheDocument()
      expect(screen.getByText('카테고리 *')).toBeInTheDocument()
      expect(screen.getByText('브랜드')).toBeInTheDocument()
      expect(screen.getByText('판매가 *')).toBeInTheDocument()
      expect(screen.getByText('정상가')).toBeInTheDocument()
      expect(screen.getByText('공급원가')).toBeInTheDocument()
      expect(screen.getByText('배송정책 *')).toBeInTheDocument()
      expect(screen.getByText('판매시작일시')).toBeInTheDocument()
      expect(screen.getByText('판매종료일시')).toBeInTheDocument()
      expect(screen.getByText('상품 상세설명')).toBeInTheDocument()
    })

    it('상품유형 select에 공통코드 옵션이 렌더링된다', () => {
      render(<Wrapper />)

      expect(screen.getByRole('option', { name: '일반상품' })).toBeInTheDocument()
      expect(screen.getByRole('option', { name: 'e쿠폰' })).toBeInTheDocument()
      expect(screen.getByRole('option', { name: '사은품' })).toBeInTheDocument()
    })

    it('판매상태 select에 공통코드 옵션이 렌더링된다', () => {
      render(<Wrapper />)

      expect(screen.getByRole('option', { name: '판매중' })).toBeInTheDocument()
      expect(screen.getByRole('option', { name: '판매중지' })).toBeInTheDocument()
    })

    it('브랜드 select에 "브랜드 없음" 기본 옵션과 브랜드 목록이 렌더링된다', () => {
      render(<Wrapper />)

      expect(screen.getByRole('option', { name: '브랜드 없음' })).toBeInTheDocument()
      expect(screen.getByRole('option', { name: '나이키' })).toBeInTheDocument()
      expect(screen.getByRole('option', { name: '아디다스' })).toBeInTheDocument()
    })

    it('배송정책 select에 "배송정책 선택" 기본 옵션과 정책 목록이 렌더링된다', () => {
      render(<Wrapper />)

      expect(screen.getByRole('option', { name: '배송정책 선택' })).toBeInTheDocument()
      expect(screen.getByRole('option', { name: '무료배송' })).toBeInTheDocument()
      expect(screen.getByRole('option', { name: '유료배송' })).toBeInTheDocument()
    })
  })

  describe('에러 표시', () => {
    it('goodsNm 에러가 있으면 에러 메시지가 표시된다', () => {
      const errors = {
        goodsNm: { message: '상품명은 필수입니다.', type: 'required' },
      } as FieldErrors<GoodsCreateFormValues>

      render(<Wrapper errors={errors} />)

      expect(screen.getByText('상품명은 필수입니다.')).toBeInTheDocument()
    })

    it('salePrc 에러가 있으면 에러 메시지가 표시된다', () => {
      const errors = {
        salePrc: { message: '판매가는 0보다 커야 합니다.', type: 'min' },
      } as FieldErrors<GoodsCreateFormValues>

      render(<Wrapper errors={errors} />)

      expect(screen.getByText('판매가는 0보다 커야 합니다.')).toBeInTheDocument()
    })

    it('dlvPolicyNo 에러가 있으면 에러 메시지가 표시된다', () => {
      const errors = {
        dlvPolicyNo: { message: '배송정책을 선택해주세요.', type: 'required' },
      } as FieldErrors<GoodsCreateFormValues>

      render(<Wrapper errors={errors} />)

      expect(screen.getByText('배송정책을 선택해주세요.')).toBeInTheDocument()
    })
  })

  describe('마진율 표시', () => {
    it('mrgnRate가 null이면 "-"가 표시된다', () => {
      render(<Wrapper mrgnRate={null} />)

      expect(screen.getByText('마진율 -')).toBeInTheDocument()
    })

    it('mrgnRate 값이 있으면 "37.5%"처럼 표시된다', () => {
      render(<Wrapper mrgnRate={37.5} />)

      expect(screen.getByText('마진율 37.5%')).toBeInTheDocument()
    })
  })
})
