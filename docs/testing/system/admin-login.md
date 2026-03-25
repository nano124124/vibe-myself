# 관리자 로그인 테스트

**대상 컴포넌트**: `components/system/AdminLoginForm.tsx`
**API**: `POST /api/admin/system/login`

---

## Vitest (컴포넌트 단위)

파일: `components/system/AdminLoginForm.test.tsx`

| # | 테스트명 | 검증 내용 |
|---|----------|-----------|
| 1 | 아이디와 비밀번호 입력 필드가 렌더링된다 | 두 입력 필드가 DOM에 존재 |
| 2 | 아이디 입력 필드에 required 속성이 있다 | `required` 속성 존재 |
| 3 | 비밀번호 입력 필드에 required 속성이 있다 | `required` 속성 존재 |
| 4 | 아이디와 비밀번호 입력 후 제출하면 login mutation이 호출된다 | `mutate({ loginId, password })` 호출 확인 |
| 5 | 로그인 중일 때 버튼이 비활성화된다 | `isPending=true` → 버튼 `disabled`, 텍스트 "로그인 중..." |
| 6 | 401 에러 시 아이디 또는 비밀번호가 올바르지 않습니다 메시지가 표시된다 | AxiosError 401 → 에러 메시지 표시 |
| 7 | 기타 에러 시 잠시 후 다시 시도해주세요 메시지가 표시된다 | 일반 Error → 오류 메시지 표시 |

---

## Playwright (E2E, `page.route()` mock)

파일: `e2e/system/admin-login.spec.ts`

| # | 테스트명 | mock 설정 | 검증 내용 |
|---|----------|-----------|-----------|
| 1 | 아이디를 입력하지 않으면 API 요청 없이 폼이 제출되지 않는다 | route 감시 | API 요청 횟수 = 0 |
| 2 | 비밀번호를 입력하지 않으면 API 요청 없이 폼이 제출되지 않는다 | route 감시 | API 요청 횟수 = 0 |
| 3 | 잘못된 계정 정보(401)로 로그인하면 에러 메시지가 표시된다 | 401 응답 | "아이디 또는 비밀번호가 올바르지 않습니다." 표시 |
| 4 | 서버 오류(500) 발생 시 오류 메시지가 표시된다 | 500 응답 | "로그인 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요." 표시 |
| 5 | 올바른 계정 정보로 로그인하면 어드민 대시보드로 이동한다 | 200 + Set-Cookie | URL `/admin/dashboard` 이동 |

> **note** 성공 케이스의 mock 응답은 `Set-Cookie: access_token=<jwt>` 포함 — middleware(authGuard)가 ROLE_ADMIN 쿠키를 검사하기 때문

---

## 실행

```bash
# 단위 테스트
pnpm test

# E2E (dev 서버 자동 실행)
pnpm test:e2e

# E2E UI 모드
pnpm test:e2e:ui
```
