import { useMutation, useQueryClient } from '@tanstack/react-query'
import { useRouter } from 'next/navigation'
import { logoutAdmin } from '@/api/system.api'

export const useAdminLogout = () => {
  const router = useRouter()
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: logoutAdmin,
    onSettled: () => {
      queryClient.clear()
      router.push('/admin')
    },
  })
}
