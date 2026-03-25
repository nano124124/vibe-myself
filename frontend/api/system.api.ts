import api from '@/lib/api'
import type { LoginAdminRequest } from '@/types/system.types'

export const loginAdmin = (data: LoginAdminRequest): Promise<void> =>
  api.post('/api/admin/system/login', data).then(() => undefined)

export const logoutAdmin = (): Promise<void> =>
  api.post('/api/admin/system/logout').then(() => undefined)
