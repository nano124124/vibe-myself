# Auth Middleware Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** `proxy.ts`를 Next.js 미들웨어로 활성화하고, `authGuard.ts`의 JWT 검증을 `atob()` 디코딩에서 `jose` 서명 검증으로 교체한다.

**Architecture:** `lib/authGuard.ts`의 `getRedirectPath`를 async로 변경하여 `jose`의 `jwtVerify`로 서명 검증. `proxy.ts`를 `middleware.ts`로 리네임하고 export 함수명을 `middleware`로 변경하여 Next.js 미들웨어 활성화.

**Tech Stack:** Next.js 16 (Edge Runtime), jose, Vitest

---

## Chunk 1: jose 설치 + JWT_SECRET 환경변수 추가

### Task 1: jose 패키지 설치

**Files:**
- Modify: `frontend/package.json`

- [ ] **Step 1: jose 설치**

```bash
cd frontend && pnpm add jose
```

Expected output: `dependencies: + jose X.X.X`

- [ ] **Step 2: 설치 확인**

```bash
cat package.json | grep jose
```

Expected: `"jose": "^X.X.X"` in dependencies

---

### Task 2: JWT_SECRET 환경변수 추가

**Files:**
- Modify: `frontend/.env.local`

- [ ] **Step 1: .env.local에 JWT_SECRET 추가**

`frontend/.env.local`에 아래 줄 추가. 값은 백엔드 `application-local.yml`의 `jwt.secret`과 동일한 Base64 인코딩 문자열을 사용:

```
JWT_SECRET=<backend jwt.secret 값과 동일한 값>
```

> ⚠️ `NEXT_PUBLIC_` 접두사를 붙이지 않는다. 서버사이드 전용 환경변수다.

---

## Chunk 2: authGuard.ts 재작성 (TDD)

### Task 3: 테스트 파일 작성

**Files:**
- Create: `frontend/lib/__tests__/authGuard.test.ts`

- [ ] **Step 1: `__tests__` 디렉토리 생성**

```bash
mkdir -p frontend/lib/__tests__
```

- [ ] **Step 2: 테스트 파일 생성**

```typescript
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

    it('/admin/dashboard 접근 시 /admin 리다이렉트', async () => {
      const result = await getRedirectPath('valid.token.here', '/admin/dashboard')
      expect(result).toBe('/admin')
    })

    it('/admin 접근 시 /admin 리다이렉트', async () => {
      const result = await getRedirectPath('valid.token.here', '/admin')
      expect(result).toBe('/admin')
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
})
```

- [ ] **Step 3: 테스트 실행해서 실패 확인**

```bash
cd frontend && pnpm test lib/__tests__/authGuard.test.ts
```

Expected: FAIL — `authGuard.ts`가 아직 `atob()` 기반이라 `jwtVerify` mock이 호출되지 않음

---

### Task 4: authGuard.ts 재작성

**Files:**
- Modify: `frontend/lib/authGuard.ts`

- [ ] **Step 1: authGuard.ts를 jose 기반으로 교체**

```typescript
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
    const role = payload.role as string

    if (ADMIN_ROLES.includes(role) && pathname === '/admin') return '/admin/dashboard'
    if (!ADMIN_ROLES.includes(role)) return '/admin'

    return null
  } catch {
    return pathname === '/admin' ? null : '/admin'
  }
}
```

- [ ] **Step 2: 테스트 실행해서 통과 확인**

```bash
cd frontend && pnpm test lib/__tests__/authGuard.test.ts
```

Expected: PASS — 9개 테스트 전부 통과

- [ ] **Step 3: 커밋**

```bash
cd frontend && git add lib/authGuard.ts lib/__tests__/authGuard.test.ts package.json pnpm-lock.yaml
git commit -m "feat: authGuard jose 서명 검증으로 교체, middleware 테스트 추가"
```

---

## Chunk 3: proxy.ts → middleware.ts 활성화

### Task 5: proxy.ts를 middleware.ts로 교체

**Files:**
- Delete: `frontend/proxy.ts`
- Create: `frontend/middleware.ts`

- [ ] **Step 1: proxy.ts 삭제 후 middleware.ts 생성**

```typescript
// frontend/middleware.ts
import { NextRequest, NextResponse } from 'next/server'
import { getRedirectPath } from '@/lib/authGuard'

export async function middleware(request: NextRequest) {
  const token = request.cookies.get('access_token')?.value ?? null
  const redirectPath = await getRedirectPath(token, request.nextUrl.pathname)

  if (redirectPath) {
    return NextResponse.redirect(new URL(redirectPath, request.url))
  }

  return NextResponse.next()
}

export const config = {
  matcher: ['/admin', '/admin/:path*'],
}
```

그리고 `proxy.ts` 삭제:

```bash
cd frontend && rm proxy.ts
```

- [ ] **Step 2: 빌드 에러 없는지 확인**

```bash
cd frontend && pnpm build
```

Expected: Build 성공 (에러 없음)

- [ ] **Step 3: 전체 테스트 통과 확인**

```bash
cd frontend && pnpm test
```

Expected: 기존 테스트 + authGuard 테스트 전부 통과

- [ ] **Step 4: 커밋**

```bash
git add middleware.ts && git rm proxy.ts
git commit -m "feat: proxy.ts를 middleware.ts로 교체, Next.js 미들웨어 활성화"
```

---

## 수동 검증

- [ ] `pnpm dev` 실행 후 로그인 없이 `/admin/dashboard` 직접 접근 → `/admin` 리다이렉트 확인
- [ ] 어드민 로그인 후 `/admin` 접근 → `/admin/dashboard` 자동 이동 확인
- [ ] 잘못된 토큰이 담긴 쿠키로 접근 시 `/admin` 리다이렉트 확인
