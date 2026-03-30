import { useMutation, useQueryClient } from '@tanstack/react-query'
import { updateCategory } from '@/api/goods.api'
import type { UpdateCategoryRequest } from '@/types/goods.types'

export const useCategoryUpdate = () => {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ ctgNo, ...data }: { ctgNo: number } & UpdateCategoryRequest) =>
      updateCategory(ctgNo, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['goods', 'categories'] })
    },
  })
}
