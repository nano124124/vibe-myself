// frontend/lib/__tests__/authGuard.test.ts
import { describe, it, expect, vi, beforeEach, beforeAll } from 'vitest'
import { jwtVerify } from 'jose'
import { getRedirectPath } from '../authGuard'

vi.mock('jose', () => ({
  jwtVerify: vi.fn(),
}))

const mockJwtVerify = vi.mocked(jwtVerify)

beforeAll(() => {
  process.env.JWT_SECRET = 'dGVzdHNlY3JldGtleWZvcnRlc3Rpbmc='
})

beforeEach(() => {
  vi.clearAllMocks()
})

describe('getRedirectPath', () => {
  describe('토큰 없음', () => {
    it('/admin 접근 시 null 반환 (로그인 페이지 허용)', async () => {
      const result = await getRedirectPath(null, '/admin')
      expect(result).toBeNull()
    })

    it('/admin/dashboard 접근 시 /admin 리다이렉트', async () => {
      const result = await getRedirectPath(null, '/admin/dashboard')
      expect(result).toBe('/admin')
    })
  })

  describe('유효한 토큰 + ROLE_ADMIN', () => {
    beforeEach(() => {
      mockJwtVerify.mockResolvedValue({ payload: { role: 'ROLE_ADMIN' } } as never)
    })

    it('/admin 접근 시 /admin/dashboard 리다이렉트 (이미 로그인됨)', async () => {
      const result = await getRedirectPath('valid.token.here', '/admin')
      expect(result).toBe('/admin/dashboard')
    })

    it('/admin/dashboard 접근 시 null 반환 (통과)', async () => {
      const result = await getRedirectPath('valid.token.here', '/admin/dashboard')
      expect(result).toBeNull()
    })

    it('/admin/goods 접근 시 null 반환 (통과)', async () => {
      const result = await getRedirectPath('valid.token.here', '/admin/goods')
      expect(result).toBeNull()
    })
  })

  describe('유효한 토큰 + ROLE_SUPER', () => {
    beforeEach(() => {
      mockJwtVerify.mockResolvedValue({ payload: { role: 'ROLE_SUPER' } } as never)
    })

    it('/admin 접근 시 /admin/dashboard 리다이렉트 (이미 로그인됨)', async () => {
      const result = await getRedirectPath('valid.token.here', '/admin')
      expect(result).toBe('/admin/dashboard')
    })

    it('/admin/dashboard 접근 시 null 반환 (통과)', async () => {
      const result = await getRedirectPath('valid.token.here', '/admin/dashboard')
      expect(result).toBeNull()
    })
  })

  describe('유효한 토큰 + 어드민 아닌 role', () => {
    beforeEach(() => {
      mockJwtVerify.mockResolvedValue({ payload: { role: 'ROLE_USER' } } as never)
    })

    it('/admin/dashboard 접근 시 / 리다이렉트', async () => {
      const result = await getRedirectPath('valid.token.here', '/admin/dashboard')
      expect(result).toBe('/')
    })

    it('/admin 접근 시 / 리다이렉트', async () => {
      const result = await getRedirectPath('valid.token.here', '/admin')
      expect(result).toBe('/')
    })
  })

  describe('유효하지 않은 토큰 (서명 검증 실패)', () => {
    beforeEach(() => {
      mockJwtVerify.mockRejectedValue(new Error('JWSSignatureVerificationFailed'))
    })

    it('/admin 접근 시 null 반환 (로그인 페이지 허용)', async () => {
      const result = await getRedirectPath('invalid.token', '/admin')
      expect(result).toBeNull()
    })

    it('/admin/dashboard 접근 시 /admin 리다이렉트', async () => {
      const result = await getRedirectPath('invalid.token', '/admin/dashboard')
      expect(result).toBe('/admin')
    })
  })

  describe('토큰 payload에 role 필드 없음', () => {
    beforeEach(() => {
      mockJwtVerify.mockResolvedValue({ payload: {} } as never)
    })

    it('/admin 접근 시 null 반환 (로그인 페이지 허용)', async () => {
      const result = await getRedirectPath('valid.token.here', '/admin')
      expect(result).toBeNull()
    })

    it('/admin/dashboard 접근 시 /admin 리다이렉트', async () => {
      const result = await getRedirectPath('valid.token.here', '/admin/dashboard')
      expect(result).toBe('/admin')
    })
  })
})
