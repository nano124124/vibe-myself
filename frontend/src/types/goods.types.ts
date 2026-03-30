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
