# 카테고리 관리 화면 설계 (어드민)

## 개요

어드민 상품관리 하위 카테고리 관리 화면. URL: `/admin/goods/categories`
카테고리 트리 목록 조회, 등록, 수정 기능을 단일 페이지에서 제공한다.

## API

| 메서드 | 경로 | 설명 |
|--------|------|------|
| GET | `/api/admin/goods/categories` | 카테고리 트리 조회 |
| POST | `/api/admin/goods/categories` | 카테고리 등록 |
| PUT | `/api/admin/goods/categories/{ctgNo}` | 카테고리 수정 |

**등록 요청 body:** `{ upCtgNo?: number, ctgNm: string, sortOrd: number }`
**수정 요청 body:** `{ ctgNm: string, useYn: 'Y'|'N', sortOrd: number }`
**응답 구조 (CategoryResponse):** `{ ctgNo, upCtgNo, ctgLvl, ctgNm, sortOrd, useYn, children[] }`
카테고리는 최대 3단계 계층. 수정 시 상위카테고리 변경 불가.

## 페이지 레이아웃

좌측 트리 테이블 + 우측 인라인 패널 구조.

```
┌─────────────────────────────┬─────────────────────┐
│  카테고리 목록               │  등록/수정 패널       │
│  [+ 최상위 카테고리 등록]     │  (클릭 시 슬라이드)   │
│  ─────────────────────────  │                      │
│  카테고리명   레벨  정렬 사용 │  카테고리명: [______] │
│  ├ 의류       1    1    Y ✏+ │  정렬순서:  [______] │
│  │  ├ 상의    2    1    Y ✏+ │  사용여부:  [Y/N]    │
│  │  └ 하의    2    2    Y ✏  │                      │
│  └ 신발       1    2    Y ✏+ │  [취소] [저장]       │
└─────────────────────────────┴─────────────────────┘
```

- 패널이 열리면 좌측 목록이 좁아지고 우측에 고정 너비 패널이 나타남
- 3단계 카테고리(ctgLvl=3)는 하위 추가(+) 버튼 없음
- 저장/취소 후 패널 닫힘, 목록 자동 갱신

## 파일 구조

```
frontend/
├── app/admin/(main)/goods/categories/
│   └── page.tsx                          # 페이지 진입점
├── components/goods/
│   ├── CategoryTree.tsx                  # 트리 테이블 컴포넌트
│   ├── CategoryPanel.tsx                 # 등록/수정 인라인 패널
│   └── CategoryForm.tsx                  # 등록/수정 공통 폼
├── hooks/goods/
│   ├── useCategoryList.ts                # GET 쿼리 훅
│   ├── useCategoryCreate.ts              # POST 뮤테이션 훅
│   └── useCategoryUpdate.ts             # PUT 뮤테이션 훅
├── api/
│   └── goods.api.ts                     # API 함수
└── types/
    └── goods.types.ts                   # 타입 정의
```

## 컴포넌트 설계

### page.tsx
- 패널 open 상태(`isPanelOpen`), 현재 선택 카테고리(`selectedCategory`), 패널 모드(`'create'|'edit'`) 관리
- `CategoryTree`와 `CategoryPanel`을 조합

### CategoryTree
- Props: `categories`, `onCreateChild(upCtgNo)`, `onEdit(category)`
- 트리를 재귀적으로 렌더링, 들여쓰기로 계층 표현
- 컬럼: 카테고리명, 레벨, 정렬순서, 사용여부, 액션(수정/하위추가)

### CategoryPanel
- Props: `mode`, `parentCtgNo?`, `category?`, `onClose`, `onSuccess`
- 내부에 `CategoryForm` 포함
- 모드에 따라 등록/수정 폼 필드 차등 노출 (사용여부는 수정 시만)

### CategoryForm
- 등록: `ctgNm`, `sortOrd`
- 수정: `ctgNm`, `sortOrd`, `useYn` (Y/N 토글)
- 저장 성공 시 `onSuccess()` 호출

## 타입 (goods.types.ts)

```ts
export interface CategoryResponse {
  ctgNo: number
  upCtgNo: number | null
  ctgLvl: string
  ctgNm: string
  sortOrd: number
  useYn: 'Y' | 'N'
  children: CategoryResponse[]
}

export interface CreateCategoryRequest {
  upCtgNo?: number
  ctgNm: string
  sortOrd: number
}

export interface UpdateCategoryRequest {
  ctgNm: string
  useYn: 'Y' | 'N'
  sortOrd: number
}
```

## 훅 설계

- `useCategoryList`: `queryKey: ['goods', 'categories']`, `queryFn: getCategories`
- `useCategoryCreate`: `useMutation`, 성공 시 `['goods', 'categories']` invalidate
- `useCategoryUpdate`: `useMutation`, 성공 시 `['goods', 'categories']` invalidate
