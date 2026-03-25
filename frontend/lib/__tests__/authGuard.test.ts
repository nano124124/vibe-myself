import { describe, it, expect } from 'vitest'
import { getRedirectPath } from '../authGuard'

describe('getRedirectPath', () => {
  it('토큰 없음 → /admin 반환', () => {
    expect(getRedirectPath(null, '/admin/dashboard')).toBe('/admin')
  })

  it('토큰 파싱 실패 → /admin 반환', () => {
    expect(getRedirectPath('invalid.token.here', '/admin/dashboard')).toBe('/admin')
  })

  it('ROLE_USER → /admin 반환', () => {
    const payload = btoa(JSON.stringify({ role: 'ROLE_USER' }))
    const token = `h.${payload}.s`
    expect(getRedirectPath(token, '/admin/dashboard')).toBe('/admin')
  })

  it('ROLE_ADMIN + /admin 이외 경로 → null (통과)', () => {
    const payload = btoa(JSON.stringify({ role: 'ROLE_ADMIN' }))
    const token = `h.${payload}.s`
    expect(getRedirectPath(token, '/admin/dashboard')).toBeNull()
  })

  it('ROLE_SUPER + /admin 이외 경로 → null (통과)', () => {
    const payload = btoa(JSON.stringify({ role: 'ROLE_SUPER' }))
    const token = `h.${payload}.s`
    expect(getRedirectPath(token, '/admin/orders')).toBeNull()
  })

  it('ROLE_ADMIN + /admin 접근 → /admin/dashboard 반환 (이미 로그인)', () => {
    const payload = btoa(JSON.stringify({ role: 'ROLE_ADMIN' }))
    const token = `h.${payload}.s`
    expect(getRedirectPath(token, '/admin')).toBe('/admin/dashboard')
  })

  it('ROLE_SUPER + /admin 접근 → /admin/dashboard 반환 (이미 로그인)', () => {
    const payload = btoa(JSON.stringify({ role: 'ROLE_SUPER' }))
    const token = `h.${payload}.s`
    expect(getRedirectPath(token, '/admin')).toBe('/admin/dashboard')
  })
})
