// frontend/lib/authGuard.ts
import { jwtVerify } from 'jose'

const ADMIN_ROLES = ['ROLE_ADMIN', 'ROLE_SUPER']

// atob()은 Edge Runtime에서도 동작하는 Web API 표준 함수
// Buffer.from(..., 'base64')는 Node.js 전용이라 Edge Runtime 미들웨어에서 사용 불가
const getSecret = () =>
  Uint8Array.from(atob(process.env.JWT_SECRET!), c => c.charCodeAt(0))

export const getRedirectPath = async (
  token: string | null,
  pathname: string,
): Promise<string | null> => {
  if (!token) return pathname === '/admin' ? null : '/admin'

  try {
    const { payload } = await jwtVerify(token, getSecret(), { algorithms: ['HS256'] })
    const role = payload.role

    if (typeof role !== 'string') return pathname === '/admin' ? null : '/admin'

    if (ADMIN_ROLES.includes(role) && pathname === '/admin') return '/admin/dashboard'
    if (!ADMIN_ROLES.includes(role)) return '/'

    return null
  } catch {
    return pathname === '/admin' ? null : '/admin'
  }
}
