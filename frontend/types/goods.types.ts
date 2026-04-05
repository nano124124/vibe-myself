export interface CategoryResponse {
  ctgNo: number
  upCtgNo: number | null
  ctgLvl: string
  ctgNm: string
  sortOrd: number
  useYn: 'Y' | 'N'
  children: CategoryResponse[]
}

export interface CreateCategoryRequest {
  upCtgNo?: number
  ctgNm: string
  sortOrd: number
}

export interface UpdateCategoryRequest {
  ctgNm: string
  useYn: 'Y' | 'N'
  sortOrd: number
}

export interface CreateCategoryResponse {
  ctgNo: number
}

// ── 브랜드 ──────────────────────────────────
export interface BrandResponse {
  brandNo: number
  brandNm: string
  brandImgUrl: string | null
}

// ── 배송정책 ─────────────────────────────────
export interface DlvPolicyResponse {
  dlvPolicyNo: string
  dlvPolicyNm: string
  dlvTpCd: string
  dlvAmt: number
}

// ── 옵션 ─────────────────────────────────────
export interface OptItmResponse {
  optGrpCd: string
  optItmCd: string
  optItmNm: string
  sortOrd: number
}

export interface OptGrpResponse {
  optGrpCd: string
  optGrpNm: string
  sortOrd: number
  items: OptItmResponse[]
}

// ── 상품 등록 ─────────────────────────────────
export type GoodsTypeCd = 'NORMAL' | 'EGIFT' | 'GIFT'
export type SaleStatCd = 'SALE' | 'STOP'

export interface UnitOptRequest {
  optGrpCd: string
  optItmCd: string
}

export interface UnitRequest {
  optItms: UnitOptRequest[]
  addPrc: number
  stockQty: number
}

export interface CreateGoodsRequest {
  goodsNm: string
  goodsTpCd: GoodsTypeCd
  ctgNo: number
  brandNo?: number
  salePrc: number
  goodsDesc?: string
  saleStatCd: SaleStatCd
  dlvPolicyNo: string
  imgUrls: string[]
  optGrpCds: string[]
  units: UnitRequest[]
}

export interface CreateGoodsResponse {
  goodsNo: string
}