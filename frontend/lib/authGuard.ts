/**
 * JWT payload에서 role을 읽어 어드민 접근 가능 여부를 판단.
 * @returns null이면 통과, string이면 해당 경로로 리다이렉트
 */
export function getRedirectPath(token: string | null): string | null {
  if (!token) return '/auth/login'

  try {
    const payloadBase64 = token.split('.')[1]
    const payload = JSON.parse(atob(payloadBase64))
    if (payload.role !== 'ROLE_ADMIN') return '/'
    return null
  } catch {
    return '/auth/login'
  }
}
