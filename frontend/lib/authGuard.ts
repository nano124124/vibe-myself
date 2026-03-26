const ADMIN_ROLES = ['ROLE_ADMIN', 'ROLE_SUPER']

export const getRedirectPath = (token: string | null, pathname: string): string | null => {
  if (!token) return pathname === '/admin' ? null : '/admin'

  try {
    const payload = JSON.parse(atob(token.split('.')[1]))

    if (ADMIN_ROLES.includes(payload.role) && pathname === '/admin') return '/admin/dashboard'
    if (!ADMIN_ROLES.includes(payload.role)) return '/admin'

    return null
  } catch {
    return pathname === '/admin' ? null : '/admin'
  }
}
