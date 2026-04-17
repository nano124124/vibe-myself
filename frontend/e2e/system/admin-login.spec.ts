import { test, expect } from '@playwright/test'

// middleware의 authGuard(jwtVerify)를 통과하는 유효한 JWT (role: ROLE_ADMIN, .env.local 의 JWT_SECRET으로 서명)
const FAKE_ADMIN_JWT = `eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjoiUk9MRV9BRE1JTiJ9.or1nnDYfW4mtyPBCBUdR4uamUe3UyfpMcjb0El17i14`

const LOGIN_API = '**/api/admin/system/login'

test.describe('관리자 로그인', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/admin')
  })

  // ── 필드 검증 (HTML required) ──────────────────────────────────────────────

  test('아이디를 입력하지 않으면 API 요청 없이 폼이 제출되지 않는다', async ({ page }) => {
    let requested = false
    await page.route(LOGIN_API, () => { requested = true })

    await page.getByLabel('비밀번호').fill('password123')
    await page.getByRole('button', { name: '로그인' }).click()

    expect(requested).toBe(false)
  })

  test('비밀번호를 입력하지 않으면 API 요청 없이 폼이 제출되지 않는다', async ({ page }) => {
    let requested = false
    await page.route(LOGIN_API, () => { requested = true })

    await page.getByLabel('아이디').fill('admin')
    await page.getByRole('button', { name: '로그인' }).click()

    expect(requested).toBe(false)
  })

  // ── 서버 에러 응답 처리 ────────────────────────────────────────────────────

  test('잘못된 계정 정보(401)로 로그인하면 에러 메시지가 표시된다', async ({ page }) => {
    await page.route(LOGIN_API, (route) =>
      route.fulfill({
        status: 401,
        contentType: 'application/json',
        body: JSON.stringify({ message: '아이디 또는 비밀번호가 올바르지 않습니다.' }),
      }),
    )

    await page.getByLabel('아이디').fill('wrong')
    await page.getByLabel('비밀번호').fill('wrong')
    await page.getByRole('button', { name: '로그인' }).click()

    await expect(page.getByText('아이디 또는 비밀번호가 올바르지 않습니다.')).toBeVisible()
  })

  test('서버 오류(500) 발생 시 오류 메시지가 표시된다', async ({ page }) => {
    await page.route(LOGIN_API, (route) =>
      route.fulfill({
        status: 500,
        contentType: 'application/json',
        body: '{}',
      }),
    )

    await page.getByLabel('아이디').fill('admin')
    await page.getByLabel('비밀번호').fill('password123')
    await page.getByRole('button', { name: '로그인' }).click()

    await expect(page.getByText('로그인 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.')).toBeVisible()
  })

  // ── 로그인 성공 ────────────────────────────────────────────────────────────

  test('올바른 계정 정보로 로그인하면 어드민 대시보드로 이동한다', async ({ page }) => {
    // 실제 백엔드처럼 Set-Cookie로 access_token 설정 → middleware(authGuard) 통과
    await page.route(LOGIN_API, (route) =>
      route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: '{}',
        headers: {
          'Set-Cookie': `access_token=${FAKE_ADMIN_JWT}; Path=/`,
        },
      }),
    )

    await page.getByLabel('아이디').fill('admin')
    await page.getByLabel('비밀번호').fill('password123')
    await page.getByRole('button', { name: '로그인' }).click()

    await expect(page).toHaveURL('/admin/dashboard')
  })
})
