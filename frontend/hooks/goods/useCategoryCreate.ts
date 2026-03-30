import { useMutation, useQueryClient } from '@tanstack/react-query'
import { createCategory } from '@/api/goods.api'
import type { CreateCategoryRequest } from '@/types/goods.types'

export const useCategoryCreate = () => {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (data: CreateCategoryRequest) => createCategory(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['goods', 'categories'] })
    },
  })
}
