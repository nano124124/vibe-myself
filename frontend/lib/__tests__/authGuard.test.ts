import { describe, it, expect } from 'vitest'
import { getRedirectPath } from '../authGuard'

describe('getRedirectPath', () => {
  it('returns /auth/login when no token', () => {
    expect(getRedirectPath(null)).toBe('/auth/login')
  })

  it('returns / when token exists but role is not ADMIN', () => {
    const payload = btoa(JSON.stringify({ role: 'ROLE_USER' }))
    const token = `header.${payload}.sig`
    expect(getRedirectPath(token)).toBe('/')
  })

  it('returns null (allow) when role is ADMIN', () => {
    const payload = btoa(JSON.stringify({ role: 'ROLE_ADMIN' }))
    const token = `header.${payload}.sig`
    expect(getRedirectPath(token)).toBeNull()
  })
})
