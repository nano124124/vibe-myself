---
name: frontend-layer
description: "vibe-myself Next.js 프론트엔드 레이어(types/api/hook/component/page)를 생성하는 스킬. '프론트엔드 만들어줘', '페이지 만들어줘', '컴포넌트 추가', 'hook 추가', '화면 구현', '어드민 목록 페이지', '상세 페이지', '등록 폼', '수정 폼' 요청 시 반드시 이 스킬을 사용한다."
---

# Frontend Layer Skill

vibe-myself coding-guide 기준으로 Next.js 프론트엔드 레이어를 생성한다.

## 사전 확인

1. `docs/frontend/coding-guide.md` Read
2. `docs/frontend/structure.md` Read
3. 대상 모듈의 기존 파일 탐색 (충돌 방지)
4. `_workspace/03_backend_spec.md` Read (API 스펙 파악) — 없으면 기존 API 파일에서 직접 파악

## 레이어별 생성 가이드

### Types (`types/{module}.types.ts`)

```typescript
// API 응답 타입 (BE Response DTO와 1:1 대응)
export interface {Module}{Detail}Response {
  fieldName: string;    // BE: FIELD_NM → FE: fieldName (camelCase 변환)
  amount: number;       // BE: AMT_FIELD → FE: amount
  statusCode: string;   // BE: STAT_CD → FE: statusCode
}

// 검색 파라미터
export interface {Module}ListParams {
  page?: number;
  size?: number;
  keyword?: string;
}

// 공통 페이지 응답 (이미 정의된 경우 재사용)
// PageResponse<T>는 types/goods.types.ts 참고
```

BE 필드명 → FE 필드명 변환 규칙:
- MyBatis는 `camelCase: true` 설정으로 자동 변환 확인 필요
- `GOODS_NM` → `goodsNm`, `SALE_PRC` → `salePrc` (BE에서 camelCase 매핑 설정 시)

### API (`api/{module}.api.ts`)

```typescript
import axiosInstance from '@/lib/axios';
import { {Module}ListParams, {Module}DetailResponse, PageResponse } from '@/types/{module}.types';

export const getAdmin{Module}List = async (params: {Module}ListParams): Promise<PageResponse<{Module}ListItemResponse>> => {
  const { data } = await axiosInstance.get('/api/admin/{module}', { params });
  return data.data;
};

export const get{Module}Detail = async (id: number): Promise<{Module}DetailResponse> => {
  const { data } = await axiosInstance.get(`/api/admin/{module}/${id}`);
  return data.data;
};
```

### Hook (`hooks/{module}/use{Module}{Action}.ts`)

```typescript
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { getAdmin{Module}List } from '@/api/{module}.api';

export const useAdmin{Module}List = (params: {Module}ListParams) => {
  return useQuery({
    queryKey: ['{module}', 'list', params],
    queryFn: () => getAdmin{Module}List(params),
  });
};

// Mutation 예시
export const use{Module}Create = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: create{Module},
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['{module}'] });
    },
  });
};
```

### Component (`components/{module}/{Module}{Name}.tsx`)

```typescript
interface {Module}{Name}Props {
  data: {Module}ListItemResponse[];
  onSelect?: (id: number) => void;
}

const {Module}{Name} = ({ data, onSelect }: {Module}{Name}Props) => {
  return (...);
};

export default {Module}{Name};
```

컴포넌트 Write/Edit 완료 직후 → **반드시 `form-validation-tests` 스킬 호출** (CLAUDE.md 필수 규칙)

### Page (`app/admin/{module}/page.tsx`)

```typescript
export default function {Module}Page() {
  const [params, setParams] = useState<{Module}ListParams>({ page: 0, size: 20 });
  const { data, isLoading } = useAdmin{Module}List(params);

  return (...);
}
```

- 비즈니스 로직은 훅으로 분리, Page는 레이아웃/조합만
- 어드민 경로: `app/admin/{module}/page.tsx`
- 쇼핑몰 경로: `app/(shop)/{module}/page.tsx`

## 출력 산출물

생성한 모든 파일 경로를 `_workspace/03_frontend_spec.md`에 기록:
- 생성 파일 목록 (절대 경로)
- `components/` 하위 파일 목록 (form-validation-tests 대상)