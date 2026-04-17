import { test, expect, type Page } from '@playwright/test'

// middleware의 authGuard(jwtVerify)를 통과하는 유효한 JWT (role: ROLE_ADMIN, .env.local 의 JWT_SECRET으로 서명)
const FAKE_ADMIN_JWT = `eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjoiUk9MRV9BRE1JTiJ9.or1nnDYfW4mtyPBCBUdR4uamUe3UyfpMcjb0El17i14`

// 정확한 경로 매칭을 위해 RegExp 사용 (glob '**' 가 하위 경로와 오매칭되는 것 방지)
const GOODS_CREATE_RE   = /\/api\/admin\/goods$/
const CATEGORIES_URL    = '**/api/admin/goods/categories'
const BRANDS_URL        = '**/api/admin/goods/brands'
const DLV_POLICIES_URL  = '**/api/admin/goods/dlv-policies'
const OPT_GROUPS_URL    = '**/api/admin/goods/opt-groups'
const CODES_GOODS_TP    = '**/api/admin/system/codes/GOODS_TP'
const CODES_SALE_STAT   = '**/api/admin/system/codes/SALE_STAT'
const MENUS_URL         = '**/api/admin/system/menus'

const ok = (data: unknown) => ({
  status: 200,
  contentType: 'application/json',
  body: JSON.stringify({ success: true, data, message: null }),
})

// ── 참조 데이터 mock ──────────────────────────────────────────────────────────

async function mockRefApis(page: Page) {
  await page.route(CATEGORIES_URL, (route) =>
    route.fulfill(ok([
      {
        ctgNo: 1, upCtgNo: null, ctgLvl: '1', ctgNm: '의류', sortOrd: 1, useYn: 'Y',
        children: [{
          ctgNo: 2, upCtgNo: 1, ctgLvl: '2', ctgNm: '상의', sortOrd: 1, useYn: 'Y',
          children: [
            { ctgNo: 3, upCtgNo: 2, ctgLvl: '3', ctgNm: '티셔츠', sortOrd: 1, useYn: 'Y', children: [] },
          ],
        }],
      },
    ])),
  )
  await page.route(BRANDS_URL, (route) => route.fulfill(ok([])))
  await page.route(DLV_POLICIES_URL, (route) =>
    route.fulfill(ok([
      { dlvPolicyNo: 'DLV001', dlvPolicyNm: '무료배송', dlvTpCd: 'FREE', dlvAmt: 0 },
    ])),
  )
  await page.route(OPT_GROUPS_URL, (route) => route.fulfill(ok([])))
  await page.route(CODES_GOODS_TP, (route) =>
    route.fulfill(ok([{ codeCd: 'NORMAL', codeNm: '일반상품', sortOrd: 1 }])),
  )
  await page.route(CODES_SALE_STAT, (route) =>
    route.fulfill(ok([{ codeCd: 'SALE', codeNm: '판매중', sortOrd: 1 }])),
  )
  await page.route(MENUS_URL, (route) => route.fulfill(ok([])))
}

// ── 필수 입력 헬퍼 ────────────────────────────────────────────────────────────

async function fillRequiredFields(page: Page) {
  await page.getByPlaceholder('상품명을 입력하세요').fill('테스트 상품')

  // 판매가 (min=1 HTML 제약)
  await page.getByRole('spinbutton').first().fill('10000')

  // 카테고리: 대분류 → 중분류 → 소분류 순으로 선택
  await page.locator('select')
    .filter({ has: page.locator('option', { hasText: '대분류 선택' }) })
    .selectOption({ label: '의류' })

  await page.locator('select')
    .filter({ has: page.locator('option', { hasText: '중분류 선택' }) })
    .selectOption({ label: '상의' })

  await page.locator('select')
    .filter({ has: page.locator('option', { hasText: '소분류 선택' }) })
    .selectOption({ label: '티셔츠' })

  // 배송정책
  await page.locator('select')
    .filter({ has: page.locator('option', { hasText: '배송정책 선택' }) })
    .selectOption({ label: '무료배송' })
}

// ── 테스트 ────────────────────────────────────────────────────────────────────

test.describe('상품 등록', () => {
  test.beforeEach(async ({ page, context }) => {
    await context.addCookies([{
      name: 'access_token',
      value: FAKE_ADMIN_JWT,
      domain: 'localhost',
      path: '/',
    }])
    await mockRefApis(page)
    await page.goto('/admin/goods/create')
    // 폼이 렌더링될 때까지 대기
    await page.getByPlaceholder('상품명을 입력하세요').waitFor()
  })

  test('판매가와 공급원가 입력 시 마진율이 계산되어 표시된다', async ({ page }) => {
    // salePrc=10000, suplyPrc=6250 → (10000-6250)/10000*100 = 37.5%
    await page.getByRole('spinbutton').nth(0).fill('10000')
    await page.getByRole('spinbutton').nth(2).fill('6250')

    await expect(page.getByText('마진율 37.5%')).toBeVisible()
  })

  test('공급원가 미입력 시 마진율이 "-"로 표시된다', async ({ page }) => {
    await page.getByRole('spinbutton').nth(0).fill('10000')
    // suplyPrc 미입력 (기본값 0)

    await expect(page.getByText('마진율 -')).toBeVisible()
  })

  test('공급원가가 판매가를 초과하면 에러 메시지가 표시되고 API가 호출되지 않는다', async ({ page }) => {
    const postRequests: string[] = []
    page.on('request', (req) => {
      if (req.url().match(GOODS_CREATE_RE) && req.method() === 'POST') {
        postRequests.push(req.url())
      }
    })

    await fillRequiredFields(page)
    // suplyPrc(3번째 spinbutton) > salePrc(1번째 spinbutton=10000)
    await page.getByRole('spinbutton').nth(2).fill('20000')
    await page.getByRole('button', { name: '상품 등록' }).click()

    await expect(page.getByText('공급원가는 판매가를 초과할 수 없습니다.')).toBeVisible()
    await page.waitForTimeout(300)
    expect(postRequests).toHaveLength(0)
  })

  test('카테고리와 배송정책을 선택하지 않으면 상품등록 API가 호출되지 않는다', async ({ page }) => {
    // page.on('request')로 실제 POST 요청 감시 (route interception 없이)
    const postRequests: string[] = []
    page.on('request', (req) => {
      if (req.url().match(GOODS_CREATE_RE) && req.method() === 'POST') {
        postRequests.push(req.url())
      }
    })

    await page.getByPlaceholder('상품명을 입력하세요').fill('테스트 상품')
    await page.getByRole('button', { name: '상품 등록' }).click()

    // 짧은 대기 후 요청이 없음을 확인
    await page.waitForTimeout(300)
    expect(postRequests).toHaveLength(0)
  })

  test('인증 오류(401) 발생 시 에러 메시지가 표시된다', async ({ page }) => {
    await page.route(GOODS_CREATE_RE, (route) => {
      if (route.request().method() === 'POST') {
        route.fulfill({
          status: 401,
          contentType: 'application/json',
          body: JSON.stringify({ success: false, message: '인증이 필요합니다.' }),
        })
      } else {
        route.fallback()
      }
    })

    await fillRequiredFields(page)
    await page.getByRole('button', { name: '상품 등록' }).click()

    await expect(page.locator('.bg-red-50')).toBeVisible()
  })

  test('서버 오류(500) 발생 시 에러 메시지가 표시된다', async ({ page }) => {
    await page.route(GOODS_CREATE_RE, (route) => {
      if (route.request().method() === 'POST') {
        route.fulfill({
          status: 500,
          contentType: 'application/json',
          body: JSON.stringify({ success: false, message: '서버 오류' }),
        })
      } else {
        route.fallback()
      }
    })

    await fillRequiredFields(page)
    await page.getByRole('button', { name: '상품 등록' }).click()

    await expect(page.locator('.bg-red-50')).toBeVisible()
  })

  test('필수 항목을 모두 입력하고 등록하면 상품 목록 페이지로 이동한다', async ({ page }) => {
    await page.route(GOODS_CREATE_RE, (route) => {
      if (route.request().method() === 'POST') {
        route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({ success: true, data: { goodsNo: 'G0001' }, message: null }),
        })
      } else {
        route.fallback()
      }
    })

    await fillRequiredFields(page)
    await page.getByRole('button', { name: '상품 등록' }).click()

    await expect(page).toHaveURL('/admin/goods')
  })
})
