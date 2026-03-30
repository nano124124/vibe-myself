import api from '@/lib/api'
import type {
  CategoryResponse,
  CreateCategoryRequest,
  CreateCategoryResponse,
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
