import { useMutation, useQueryClient } from '@tanstack/react-query'
import { createGoods } from '@/api/goods.api'
import type { CreateGoodsRequest } from '@/types/goods.types'

export const useGoodsCreate = () => {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (data: CreateGoodsRequest) => createGoods(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['goods', 'list'] })
    },
  })
}