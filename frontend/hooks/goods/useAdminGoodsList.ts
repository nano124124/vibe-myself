import { useQuery } from '@tanstack/react-query'
import { getAdminGoodsList } from '@/api/goods.api'
import type { GoodsListSearchParams } from '@/types/goods.types'

export const useAdminGoodsList = (params: GoodsListSearchParams) => {
  return useQuery({
    queryKey: ['goods', 'list', params],
    queryFn: () => getAdminGoodsList(params),
  })
}