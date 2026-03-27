// frontend/lib/authGuard.ts
import { jwtVerify } from 'jose'

const ADMIN_ROLES = ['ROLE_ADMIN', 'ROLE_SUPER']

// Next.js 16: Proxy는 Node.js 런타임을 기본으로 사용
// atob()은 Web API 표준으로 Node.js/Edge 모두 동작
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
