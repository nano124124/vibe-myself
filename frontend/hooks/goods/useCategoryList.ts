import { useQuery } from '@tanstack/react-query'
import { getCategories } from '@/api/goods.api'

export const useCategoryList = () =>
  useQuery({
    queryKey: ['goods', 'categories'],
    queryFn: getCategories,
  })
