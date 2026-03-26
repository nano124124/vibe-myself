import api from '@/lib/api'
import type { LoginAdminRequest, MenuResponse } from '@/types/system.types'

export const loginAdmin = (data: LoginAdminRequest): Promise<void> =>
  api.post('/api/admin/system/login', data).then(() => undefined)

export const logoutAdmin = (): Promise<void> =>
  api.post('/api/admin/system/logout').then(() => undefined)

export const getMenus = (): Promise<MenuResponse[]> =>
  api
    .get<{ success: boolean; data: MenuResponse[]; message: string | null }>(
      '/api/admin/system/menus'
    )
    .then((res) => res.data.data)
