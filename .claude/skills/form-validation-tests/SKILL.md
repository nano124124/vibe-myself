---
name: form-validation-tests
description: >
  폼 컴포넌트(.tsx)를 새로 만들거나 수정할 때 자동으로 실행한다.
  컴포넌트를 분석해서 Vitest 단위 테스트와 Playwright E2E 테스트를 생성하고 실행한다.
  "폼 테스트 만들어줘", "테스트 추가해줘", 새 폼 컴포넌트 파일을 Write/Edit한 직후에도 반드시 실행한다.
  테스트 파일 생성 + 실행 + docs/testing/{모듈}/파일명.md 문서화까지 한 번에 처리한다.
---

# Form Validation Tests

폼 컴포넌트가 생성되거나 수정되면, 검증 테스트를 자동으로 만들고 실행한다.
이 스킬의 목적은 "잘못된 데이터가 실제로 막히는가"를 Vitest(단위)와 Playwright(E2E) 두 레이어에서 확인하는 것이다.

## Step 1 — 컴포넌트 분석

대상 파일을 읽고 아래를 파악한다:

- **입력 필드**: id, label 텍스트, `required` 여부, type (text/password/email 등)
- **사용하는 훅**: `useXxx()` → 해당 훅 파일도 읽어서 `mutationFn`, `onSuccess` 동작 파악
- **에러 처리**: `isAxiosError` 분기, HTTP 상태코드별 메시지, 일반 에러 메시지
- **성공 처리**: `router.push()` 경로, 상태 초기화 등
- **모듈 추론**: 파일 경로(`components/{module}/`)에서 모듈명 추출

## Step 2 — Vitest 단위 테스트 생성

파일 위치: 컴포넌트와 같은 디렉토리, `{ComponentName}.test.tsx`

**반드시 포함할 케이스:**

| 케이스 | 검증 내용 |
|--------|-----------|
| 렌더링 | 모든 입력 필드가 DOM에 존재 |
| required 속성 | `required` 필드마다 `toBeRequired()` |
| 정상 제출 | 값 입력 후 submit → mutation이 올바른 데이터로 호출됨 |
| 로딩 상태 | `isPending=true` → 버튼 disabled, 텍스트 변경 |
| 401 에러 | AxiosError(status=401) → 해당 에러 메시지 표시 |
| 기타 에러 | 일반 Error → fallback 메시지 표시 |

**패턴:**
```tsx
vi.mock('@/hooks/{module}/use{ComponentName}')

function setup(overrides = {}) {
  mockHook.mockReturnValue({
    mutate: mockMutate,
    isPending: overrides.isPending ?? false,
    error: overrides.error ?? null,
  } as ReturnType<typeof useHook>)
  return render(<ComponentName />)
}
```

- 훅 전체를 `vi.mock`으로 교체 → `QueryClientProvider` 불필요
- AxiosError 생성 시 `AxiosHeaders` 사용, `response.status` 명시
- 테스트명: 한글로 상황과 기대값 포함 (`401 에러 시 ... 메시지가 표시된다`)

## Step 3 — Playwright E2E 테스트 생성

파일 위치: `e2e/{module}/{component-kebab-case}.spec.ts`

**반드시 포함할 케이스:**

| 케이스 | mock 방식 | 검증 내용 |
|--------|-----------|-----------|
| 필수 필드 빈 값 | route 감시 (fulfilled 없음) | API 요청 횟수 = 0 |
| 인증 실패 (401) | `route.fulfill({ status: 401 })` | 에러 메시지 표시 |
| 서버 오류 (500) | `route.fulfill({ status: 500 })` | fallback 메시지 표시 |
| 로그인 성공 | `route.fulfill({ status: 200, Set-Cookie })` | 목적지 URL 이동 |

**패턴:**
```ts
const API_ROUTE = '**/api/{path}'  // ** glob 필수 — 절대경로로 쓰면 매칭 안 됨

test.beforeEach(async ({ page }) => {
  await page.goto('/{route}')
})
```

**주의사항:**
- `page.route()` 경로는 반드시 `**/` prefix glob 패턴 사용
- 성공 케이스에서 middleware가 쿠키를 검사하면 mock 응답에 `Set-Cookie` 헤더 포함
  - `frontend/proxy.ts`와 `lib/authGuard.ts`를 확인해서 쿠키 구조 파악
  - fake JWT payload: `base64({"role":"ROLE_ADMIN"})` = `eyJyb2xlIjoiUk9MRV9BRE1JTiJ9`

## Step 4 — 테스트 실행

```bash
# Vitest
cd frontend && pnpm test

# Playwright
cd frontend && pnpm test:e2e
```

실패 시 원인을 분석하고 수정한다. 반복하되 동일한 수정을 두 번 시도하지 않는다.

## Step 5 — 문서 생성

파일 위치: `docs/testing/{module}/{component-kebab-case}.md`

```markdown
# {컴포넌트명} 테스트

**대상 컴포넌트**: `components/{module}/{ComponentName}.tsx`
**API**: `{METHOD} {path}`

---

## Vitest (컴포넌트 단위)
파일: `components/{module}/{ComponentName}.test.tsx`
| # | 테스트명 | 검증 내용 |

---

## Playwright (E2E, page.route() mock)
파일: `e2e/{module}/{component-kebab-case}.spec.ts`
| # | 테스트명 | mock 설정 | 검증 내용 |

---

## 실행
\`\`\`bash
pnpm test
pnpm test:e2e
\`\`\`
```

## 완료 기준

- [ ] Vitest 테스트: 렌더링 / required / 정상제출 / 로딩 / 에러(401) / 에러(기타) 모두 포함
- [ ] Playwright 테스트: 필드 빈 값 / 401 / 500 / 성공 모두 포함
- [ ] `pnpm test` 전체 통과
- [ ] `pnpm test:e2e` 전체 통과
- [ ] `docs/testing/{module}/` 문서 생성
