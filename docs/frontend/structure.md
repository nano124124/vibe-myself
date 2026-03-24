# Frontend 프로젝트 구조

Next.js App Router 기반 프론트엔드의 폴더 구조와 각 레이어의 역할을 정의한다.

## 폴더 구조

```
frontend/src/
├── app/
│   ├── (shop)/                # 쇼핑몰 화면 (URL 접두사 없음)
│   │   └── {module}/
│   │       ├── page.tsx
│   │       └── [id]/
│   │           └── page.tsx
│   └── admin/                 # 어드민 화면 (/admin/...)
│       └── {module}/
│           ├── page.tsx
│           └── [id]/
│               └── page.tsx
├── components/
│   ├── {module}/              # 모듈별 컴포넌트
│   └── common/                # 공용 컴포넌트
├── hooks/
│   └── {module}/              # 모듈별 Tanstack Query 훅
├── api/                       # Axios 기반 API 함수
├── store/                     # Zustand 스토어
└── types/                     # TypeScript 타입 정의
```

## 레이어 역할

| 레이어 | 위치 | 역할 |
|--------|------|------|
| Page | `app/(shop)/{module}/`, `app/admin/{module}/` | 라우트 진입점, 레이아웃 조합 |
| Component | `components/{module}/` | UI 단위 컴포넌트, 재사용 가능한 뷰 |
| Hook | `hooks/{module}/` | Tanstack Query 기반 서버 상태 관리 |
| API | `api/` | Axios 호출 함수, 엔드포인트 정의 |
| Store | `store/` | Zustand 클라이언트 상태 관리 |
| Types | `types/` | 모듈별 TypeScript 타입/인터페이스 |

## 네이밍 컨벤션

| 구분 | 형식 | 예시 |
|------|------|------|
| Page | `page.tsx` (Next.js 규약) | `app/(shop)/order/page.tsx` |
| Component | `{Module}{Name}.tsx` (PascalCase) | `OrderList.tsx`, `GoodsCard.tsx` |
| Hook (Query) | `use{Module}{Action}.ts` | `useOrderList.ts`, `useGoodsDetail.ts` |
| Hook (Mutation) | `use{Module}{Action}.ts` | `useOrderCreate.ts`, `useCartAdd.ts` |
| API 파일 | `{module}.api.ts` | `order.api.ts`, `goods.api.ts` |
| Store | `{module}Store.ts` | `cartStore.ts` |
| Types | `{module}.types.ts` | `order.types.ts` |

## 컴포넌트 분류

| 구분 | 위치 | 기준 |
|------|------|------|
| 모듈 컴포넌트 | `components/{module}/` | 특정 모듈에서만 사용 |
| 공용 컴포넌트 | `components/common/` | 2개 이상 모듈에서 사용 |
| shadcn/ui | `components/ui/` | shadcn 자동 생성, 직접 수정 금지 |

## 파일 생성 원칙

- API 함수를 추가할 때는 해당 모듈의 `api/{module}.api.ts` 파일에 추가한다.
- 훅을 추가할 때는 `hooks/{module}/` 하위에 파일을 생성한다.
- 페이지 컴포넌트에 비즈니스 로직을 직접 작성하지 않는다. 훅으로 분리한다.
