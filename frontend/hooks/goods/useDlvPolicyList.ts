import { useQuery } from '@tanstack/react-query'
import { getDlvPolicies } from '@/api/goods.api'

export const useDlvPolicyList = () =>
  useQuery({
    queryKey: ['goods', 'dlv-policies'],
    queryFn: getDlvPolicies,
  })