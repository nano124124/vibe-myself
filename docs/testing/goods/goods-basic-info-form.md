# GoodsBasicInfoForm 테스트

**대상 컴포넌트**: `components/goods/GoodsBasicInfoForm.tsx`
**관련 페이지**: `app/admin/(main)/goods/create/page.tsx`
**API**: `POST /api/admin/goods`

---

## Vitest (컴포넌트 단위)

파일: `components/goods/GoodsBasicInfoForm.test.tsx`

| # | 테스트명 | 검증 내용 |
|---|----------|-----------|
| 1 | 모든 필드 라벨이 화면에 존재한다 | 상품명/상품유형/판매상태/카테고리/브랜드/판매가/정상가/공급원가/배송정책/판매시작·종료일시/상세설명 라벨 존재 |
| 2 | 상품유형 select에 공통코드 옵션이 렌더링된다 | 일반상품·e쿠폰·사은품 option 존재 |
| 3 | 판매상태 select에 공통코드 옵션이 렌더링된다 | 판매중·판매중지 option 존재 |
| 4 | 브랜드 select에 "브랜드 없음" 기본 옵션과 브랜드 목록이 렌더링된다 | 브랜드 없음·나이키·아디다스 option 존재 |
| 5 | 배송정책 select에 "배송정책 선택" 기본 옵션과 정책 목록이 렌더링된다 | 배송정책 선택·무료배송·유료배송 option 존재 |
| 6 | goodsNm 에러가 있으면 에러 메시지가 표시된다 | FieldErrors 주입 → 에러 메시지 텍스트 노출 |
| 7 | salePrc 에러가 있으면 에러 메시지가 표시된다 | FieldErrors 주입 → 에러 메시지 텍스트 노출 |
| 8 | dlvPolicyNo 에러가 있으면 에러 메시지가 표시된다 | FieldErrors 주입 → 에러 메시지 텍스트 노출 |
| 9 | mrgnRate가 null이면 "-"가 표시된다 | `마진율 -` 텍스트 확인 |
| 10 | mrgnRate 값이 있으면 "37.5%"처럼 표시된다 | `마진율 37.5%` 텍스트 확인 |

**패턴 특이사항**: RHF `Controller` 를 사용하는 카테고리·브랜드 필드는 실제 `useForm`으로 감싼 Wrapper 컴포넌트를 통해 테스트. QueryClientProvider 불필요 (데이터는 props로 주입).

---

## Playwright (E2E, page.route() mock)

파일: `e2e/goods/goods-create.spec.ts`

| # | 테스트명 | mock 설정 | 검증 내용 |
|---|----------|-----------|-----------|
| 1 | 카테고리와 배송정책을 선택하지 않으면 상품등록 API가 호출되지 않는다 | 참조 데이터 API mock, 상품등록 API 없음 | POST 요청 횟수 = 0 |
| 2 | 인증 오류(401) 발생 시 에러 메시지가 표시된다 | `POST /api/admin/goods` → 401 | `.bg-red-50` 에러 블록 노출 |
| 3 | 서버 오류(500) 발생 시 에러 메시지가 표시된다 | `POST /api/admin/goods` → 500 | `.bg-red-50` 에러 블록 노출 |
| 4 | 필수 항목을 모두 입력하고 등록하면 상품 목록 페이지로 이동한다 | `POST /api/admin/goods` → 200 | URL = `/admin/goods` |

**주요 설정**:
- `access_token` 쿠키: `JWT_SECRET`(.env.local)으로 서명한 유효한 JWT 사용 (authGuard가 `jwtVerify`로 서명 검증)
- 참조 데이터 (categories, brands, dlv-policies, opt-groups, codes, menus) 전부 mock
- 상품등록 API 경로는 `**/api/admin/goods` glob이 하위 경로(categories 등)와 오매칭되므로 `/\/api\/admin\/goods$/` RegExp 사용

---

## 실행

```bash
# Vitest (단위)
cd frontend && pnpm test

# Playwright (E2E)
cd frontend && pnpm test:e2e
```
