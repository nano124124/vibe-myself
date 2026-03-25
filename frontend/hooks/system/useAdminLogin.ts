import { useMutation } from '@tanstack/react-query'
import { useRouter } from 'next/navigation'
import { loginAdmin } from '@/api/system.api'
import type { LoginAdminRequest } from '@/types/system.types'

export const useAdminLogin = () => {
  const router = useRouter()

  return useMutation({
    mutationFn: (data: LoginAdminRequest) => loginAdmin(data),
    onSuccess: () => {
      router.push('/admin/dashboard')
    },
  })
}
