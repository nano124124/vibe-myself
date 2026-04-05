import { useQuery } from '@tanstack/react-query'
import { getOptGroups } from '@/api/goods.api'

export const useOptGroupList = () =>
  useQuery({
    queryKey: ['goods', 'opt-groups'],
    queryFn: getOptGroups,
  })