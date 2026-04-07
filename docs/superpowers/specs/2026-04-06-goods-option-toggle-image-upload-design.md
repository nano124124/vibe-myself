# 설계: 옵션 사용 여부 토글 + 이미지 파일 업로드

## 개요

상품 등록 폼에 두 가지 기능을 추가한다.

1. **옵션 사용 여부 라디오버튼** — "옵션 없음 / 옵션 사용" 선택에 따라 옵션 섹션과 단품 섹션 표시 제어
2. **이미지 파일 업로드 (서버사이드)** — URL 직접 입력 방식에서 파일 업로드로 교체, 백엔드가 Supabase Storage에 업로드 후 URL 저장

---

## Feature 1: 옵션 사용 여부 라디오버튼

### 변경 범위
- `frontend/app/admin/(main)/goods/create/page.tsx` 만 수정 (백엔드 변경 없음)

### 동작
- `optEnabled` boolean state 추가 (기본값: `false` = 옵션 없음)
- "옵션 설정" 섹션 헤더에 라디오버튼 두 개 배치
  - `옵션 없음` (기본 선택)
  - `옵션 사용`
- `optEnabled=false` 시: `GoodsOptionSelector`, `GoodsUnitTable` 렌더링 제거 + `selectedOptGrpCds`, `selectedOptItms`, `units` 초기화
- submit 시 `optEnabled=false`면 `optGrpCds: []`, `units: []` 전송 (백엔드 기존 로직 그대로)

---

## Feature 2: 이미지 파일 업로드

### 방식: 단일 요청 (multipart/form-data)

이미지 업로드와 상품 등록을 한 번의 요청으로 처리. 등록 실패 시 업로드가 발생하지 않으므로 고아 파일 없음.

### Supabase Storage 설정
- 버킷: `goods-images` (public)
- 파일 경로: `{goodsNo}/{UUID}_{originalFilename}`
- Public URL: `https://ekmmmbmsxwiprqzkrsmd.supabase.co/storage/v1/object/public/goods-images/{path}`

### Frontend 변경

**`GoodsImageInput.tsx`** — 전면 교체
- 파일 선택 input (accept: image/*)
- 선택한 파일을 `File[]`로 관리
- 로컬 `URL.createObjectURL()`로 썸네일 미리보기
- 삭제 시 목록에서 제거 (storage 호출 없음 — 아직 업로드 전)
- 최대 5장

**`create/page.tsx`**
- `imgUrls: string[]` → `imgFiles: File[]` state로 교체
- submit 시 `FormData`에 `data`(JSON Blob) + `images`(File[]) 첨부

**`goods.api.ts`**
- `createGoods` 함수: `CreateGoodsRequest` + `File[]` 인자 받아 `multipart/form-data`로 전송
- `Content-Type` 헤더 제거 (axios가 boundary 자동 설정)

**`types/goods.types.ts`**
- `CreateGoodsRequest`에서 `imgUrls` 필드 제거

### Backend 변경

**`application-local.yml`** (gitignore)
```yaml
supabase:
  url: https://ekmmmbmsxwiprqzkrsmd.supabase.co
  service-role-key: <service_role_key>
  storage:
    goods-bucket: goods-images
```

**`SupabaseStorageService.java`** (신규, `common/storage/`)
- `RestTemplate`으로 Supabase Storage REST API 호출
  - `PUT /storage/v1/object/{bucket}/{path}`
  - Header: `Authorization: Bearer {service_role_key}`, `Content-Type: {file.contentType}`
- 업로드 성공 시 public URL 반환
- 업로드 실패 시 `AppException` throw

**`GoodsController.java`**
- `@RequestBody` → `@RequestPart`
  - `@RequestPart("data") CreateGoodsRequest data`
  - `@RequestPart(value = "images", required = false) List<MultipartFile> images`

**`GoodsCreateService.java`**
- `createGoods(CreateGoodsRequest, List<MultipartFile>)` 시그니처 변경
- Supabase 업로드 후 URL 목록 획득 → 기존 `insertImages()` 활용
- 파일 경로: `{goodsNo}/{UUID}_{originalFilename}`

**`CreateGoodsRequest.java`**
- `imgUrls` 필드 제거

---

## 변경 파일 목록

| 파일 | 변경 유형 |
|------|-----------|
| `frontend/app/admin/(main)/goods/create/page.tsx` | 수정 |
| `frontend/components/goods/GoodsImageInput.tsx` | 수정 |
| `frontend/api/goods.api.ts` | 수정 |
| `frontend/types/goods.types.ts` | 수정 |
| `backend/.../GoodsController.java` | 수정 |
| `backend/.../GoodsCreateService.java` | 수정 |
| `backend/.../dto/goods/CreateGoodsRequest.java` | 수정 |
| `backend/.../common/storage/SupabaseStorageService.java` | 신규 |
| `backend/src/main/resources/application-local.yml` | 수정 (gitignore) |
