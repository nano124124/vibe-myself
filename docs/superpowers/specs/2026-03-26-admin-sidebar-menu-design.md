# 어드민 사이드바 메뉴 설계

**날짜**: 2026-03-26
**브랜치**: feature/auth
**관련 API**: `GET /api/admin/system/menus` (기존 구현 완료)

---

## 개요

어드민 대시보드 좌측 사이드바에 DB 기반 메뉴 목록을 동적으로 렌더링한다.
대메뉴-소메뉴 2단계 계층 구조를 아코디언 방식으로 표시하며, 소메뉴 클릭 시 해당 페이지로 이동한다.

---

## 요구사항

- 대메뉴 목록 노출 (API에서 가져옴)
- 대메뉴 하위에 소메뉴 존재 (트리 구조)
- 소메뉴 클릭 시 해당 URL로 이동
- 현재 페이지에 해당하는 메뉴 활성화 표시
- 아코디언 방식: 대메뉴 클릭 시 소메뉴 펼침/접힘 (한 번에 하나만 열림)
- 현재 페이지의 상위 대메뉴는 자동으로 펼쳐짐

---

## 기술 결정

| 항목 | 결정 | 이유 |
|------|------|------|
| 폰트 | Pretendard | 한글 어드민 최적화, 가독성 우수 |
| 사이드바 스타일 | 아코디언 (단일 열림) | 가장 일반적인 어드민 패턴, 깔끔한 UX |
| 데이터 페칭 | TanStack Query (client-side) | 기존 프로젝트 패턴과 일치 |

---

## 데이터 흐름

```
(main)/layout.tsx  (Server Component)
  └── AdminSidebar  ('use client')
        └── useAdminMenus()        ← TanStack Query, queryKey: ['system', 'menus']
              └── getMenus()       ← Axios, GET /api/admin/system/menus
                    └── Promise<MenuResponse[]>  (ApiResponse 래퍼 언래핑 후)
```

**API 응답 구조** (`MenuResponse`):
```typescript
interface MenuResponse {
  menuNo: number      // Java Long → JS number. 현재 메뉴 수 기준 MAX_SAFE_INTEGER 범위 내 안전.
  menuNm: string
  menuUrl: string | null
  sortOrd: number
  children: MenuResponse[]
}
```

---

## API 함수 (`api/system.api.ts`)

`getMenus()`는 Axios 응답에서 `ApiResponse` 래퍼를 벗겨 `MenuResponse[]`를 반환한다.

```typescript
// 반환 타입: Promise<MenuResponse[]>
const getMenus = async (): Promise<MenuResponse[]> => {
  const res = await apiClient.get<ApiResponse<MenuResponse[]>>('/api/admin/system/menus')
  return res.data.data
}
```

---

## 컴포넌트 설계

### AdminSidebar

- 위치: `frontend/src/components/system/AdminSidebar.tsx`
- **`'use client'` 선언 필수** — `useState`, `usePathname` 사용
- 역할: 메뉴 트리 렌더링, 아코디언 상태 관리
- 사용 훅: `useAdminMenus`, `usePathname`
- 상태: `openMenuNo: number | null` — 현재 펼쳐진 대메뉴 번호

**렌더링 규칙**:
- `children`이 있는 대메뉴 → 클릭 시 `openMenuNo` 토글. URL 이동 없음.
- `children`이 없는 대메뉴 / 소메뉴 → `<Link href={menuUrl}>` 로 이동. `menuUrl`이 `null`이면 비활성 스타일로 렌더링하고 클릭 불가(pointer-events-none).
- 현재 `pathname`이 `menuUrl`로 시작(`startsWith`)하는 항목 → 활성화 스타일 적용 (예: `/admin/products/1`이면 `/admin/products` 메뉴가 활성화됨).
- 현재 페이지의 소메뉴를 포함하는 대메뉴도 함께 활성화 스타일 적용.
- 네비게이션은 `<Link>` 사용 (`router.push` 미사용 — App Router 표준 패턴, prefetch 활용).

**초기 열림 상태**:
- TanStack Query 데이터 로드 완료 후 현재 `pathname`을 포함하는 대메뉴를 찾아 자동 펼침.
- 데이터 로드 전에는 아코디언 모두 접힌 상태 유지(스켈레톤 표시).

**로딩/에러 처리**:
- 로딩 중: 사이드바에 메뉴 항목 형태의 스켈레톤 5개 표시 (각 높이 32px, 너비 100%, 간격 4px).
- 에러: "메뉴를 불러올 수 없습니다" 텍스트 표시.

---

## 생성/수정 파일 목록

| 파일 | 작업 | 내용 |
|------|------|------|
| `types/system.types.ts` | 수정 | `MenuResponse` 인터페이스 추가 |
| `api/system.api.ts` | 수정 | `getMenus(): Promise<MenuResponse[]>` 추가, `.data.data` 언래핑 |
| `hooks/system/useAdminMenus.ts` | 생성 | TanStack Query 훅 |
| `components/system/AdminSidebar.tsx` | 생성 | `'use client'`, 아코디언 사이드바 |
| `app/admin/(main)/layout.tsx` | 수정 | 하드코딩된 aside → AdminSidebar 교체 |

---

## 폰트 적용

Pretendard는 Google Fonts 미지원 → `@fontsource/pretendard` 패키지 사용.

```bash
pnpm add @fontsource/pretendard
```

`app/admin/layout.tsx` (루트 어드민 레이아웃)에서 임포트:

```typescript
import '@fontsource/pretendard/400.css'
import '@fontsource/pretendard/500.css'
import '@fontsource/pretendard/600.css'
import '@fontsource/pretendard/700.css'
```

Tailwind CSS에서 font-family 적용:
```css
/* globals.css 또는 tailwind config */
font-family: 'Pretendard', sans-serif;
```

---

## 범위 외

- 메뉴 3단계 이상 지원 (현재 DB 구조상 2단계)
- 사이드바 접힘/펼침 너비 축소 토글
- 메뉴 권한별 필터링
