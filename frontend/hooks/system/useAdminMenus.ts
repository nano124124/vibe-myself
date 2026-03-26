import { useQuery } from '@tanstack/react-query'
import { getMenus } from '@/api/system.api'

export const useAdminMenus = () =>
  useQuery({
    queryKey: ['system', 'menus'],
    queryFn: getMenus,
  })
