import api from '@/lib/api'
import type {
  BrandResponse,
  CategoryResponse,
  CreateCategoryRequest,
  CreateCategoryResponse,
  CreateGoodsRequest,
  CreateGoodsResponse,
  DlvPolicyResponse,
  OptGrpResponse,
  UpdateCategoryRequest,
} from '@/types/goods.types'

type ApiResponse<T> = { success: boolean; data: T; message: string | null }

export const getCategories = (): Promise<CategoryResponse[]> =>
  api
    .get<ApiResponse<CategoryResponse[]>>('/api/admin/goods/categories')
    .then((res) => res.data.data)

export const createCategory = (data: CreateCategoryRequest): Promise<CreateCategoryResponse> =>
  api
    .post<ApiResponse<CreateCategoryResponse>>('/api/admin/goods/categories', data)
    .then((res) => res.data.data)

export const updateCategory = (ctgNo: number, data: UpdateCategoryRequest): Promise<void> =>
  api
    .put<ApiResponse<null>>(`/api/admin/goods/categories/${ctgNo}`, data)
    .then(() => undefined)

export const getBrands = (): Promise<BrandResponse[]> =>
  api
    .get<ApiResponse<BrandResponse[]>>('/api/admin/goods/brands')
    .then((res) => res.data.data)

export const getDlvPolicies = (): Promise<DlvPolicyResponse[]> =>
  api
    .get<ApiResponse<DlvPolicyResponse[]>>('/api/admin/goods/dlv-policies')
    .then((res) => res.data.data)

export const getOptGroups = (): Promise<OptGrpResponse[]> =>
  api
    .get<ApiResponse<OptGrpResponse[]>>('/api/admin/goods/opt-groups')
    .then((res) => res.data.data)

export const createGoods = (data: CreateGoodsRequest, images: File[]): Promise<CreateGoodsResponse> => {
  const formData = new FormData()
  formData.append('data', new Blob([JSON.stringify(data)], { type: 'application/json' }))
  images.forEach((file) => formData.append('images', file))
  return api
    .post<ApiResponse<CreateGoodsResponse>>('/api/admin/goods', formData, {
      headers: { 'Content-Type': undefined },
    })
    .then((res) => res.data.data)
}