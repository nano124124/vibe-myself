# Frontend 코딩 가이드

## 공통

- 모든 파일은 해당 모듈의 폴더 하위에 생성한다 (`frontend/structure.md` 참조)
- `any` 사용을 금지한다. 명시적 타입/인터페이스를 정의한다
- 파일 하나에 하나의 컴포넌트/훅/함수를 원칙으로 한다
- 컴포넌트는 한 가지 역할만 한다. 길어지면 하위 컴포넌트로 분리한다
- Props는 최소화한다. 필요한 값만 명시적으로 내려준다
- Props drilling이 2단계를 초과하면 훅 또는 Zustand로 상태를 끌어올린다
- 함수는 화살표 함수 패턴을 사용한다. `function` 선언식을 사용하지 않는다 (단, Page 컴포넌트는 Next.js 규약상 `export default function` 허용)

---

## Page

- 비즈니스 로직을 직접 작성하지 않는다. 훅으로 분리한다
- 데이터 패칭은 `hooks/{module}/` 훅을 통해서만 수행한다

---

## Component

- `components/{module}/` 하위에 위치한다
- Props 타입은 파일 상단에 `interface {Name}Props` 로 정의한다
- 서버 상태를 컴포넌트 내부에서 직접 fetch하지 않는다. 훅에서 받아 Props로 전달한다
- 조건부 렌더링이 복잡해지면 컴포넌트로 분리한다

---

## Hook (Tanstack Query)

- `hooks/{module}/` 하위에 위치한다
- 파일명: `use{Module}{Action}.ts` (예: `useOrderList.ts`, `useOrderCreate.ts`)
- `queryKey` 는 모듈명을 첫 번째 요소로 사용한다 (`['order', 'list']`)
- 서버 상태는 Tanstack Query로만 관리한다. Zustand에 저장하지 않는다

---

## API

- `api/{module}.api.ts` 에 위치한다
- axios 인스턴스를 사용한다. `fetch` 를 직접 사용하지 않는다
- 함수명은 동사로 시작한다 (`getOrderDetail`, `createOrder`, `updateOrderStatus`, `deleteOrder`)
- 응답 타입을 명시한다

---

## Store (Zustand)

- `store/{module}Store.ts` 에 위치한다
- UI 상태, 로컬 임시 데이터 등 순수 클라이언트 상태에만 사용한다

---

## Types

- `types/{module}.types.ts` 에 위치한다
- API 요청/응답 타입, 도메인 모델 타입을 정의한다
- `enum` 보다 `as const` 또는 유니언 타입을 선호한다
