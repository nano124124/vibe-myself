import { useMutation, useQueryClient } from '@tanstack/react-query'
import { isAxiosError } from 'axios'
import { toast } from 'sonner'
import { createGoods } from '@/api/goods.api'
import type { CreateGoodsRequest } from '@/types/goods.types'

export const useGoodsCreate = () => {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ data, images }: { data: CreateGoodsRequest; images: File[] }) =>
      createGoods(data, images),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['goods', 'list'] })
    },
    onError: (error) => {
      const message = isAxiosError(error)
        ? (error.response?.data?.message ?? '요청 처리 중 오류가 발생했습니다.')
        : '알 수 없는 오류가 발생했습니다.'
      toast.error(message)
    },
  })
}