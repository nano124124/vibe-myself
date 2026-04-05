import { useQuery } from '@tanstack/react-query'
import { getBrands } from '@/api/goods.api'

export const useBrandList = () =>
  useQuery({
    queryKey: ['goods', 'brands'],
    queryFn: getBrands,
  })