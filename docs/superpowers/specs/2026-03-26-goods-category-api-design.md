# 카테고리 API 설계 (어드민)

**날짜:** 2026-03-26
**모듈:** goods
**대상 테이블:** `PR_CTG_BASE`

---

## 요구사항

1. 상위 카테고리(레벨 1)를 등록할 수 있다.
2. 상위 카테고리에 대한 하위 카테고리를 등록할 수 있다. 최대 3단계.
3. 카테고리 수정은 카테고리명, 사용여부, 정렬순서만 가능하다. 상위 카테고리 변경 불가.
4. 카테고리 목록은 트리 구조로 조회한다.

---

## 테이블

`PR_CTG_BASE`

| 컬럼 | 타입 | 설명 |
|------|------|------|
| `CTG_NO` | `BIGINT` IDENTITY | PK |
| `UP_CTG_NO` | `BIGINT` NULL | 상위 카테고리 FK (NULL = 최상위) |
| `CTG_LVL` | `CHAR(1)` | 계층 레벨 ('1'=대, '2'=중, '3'=소) |
| `CTG_NM` | `VARCHAR(200)` | 카테고리명 |
| `SORT_ORD` | `INTEGER` | 정렬순서 |
| `USE_YN` | `CHAR(1)` | 사용여부 |
| 공통 컬럼 | | `REG_DTM`, `REG_ID`, `MOD_DTM`, `MOD_ID` |

---

## API

| Method | URL | 설명 |
|--------|-----|------|
| `GET` | `/api/admin/goods/categories` | 전체 카테고리 트리 조회 |
| `POST` | `/api/admin/goods/categories` | 카테고리 등록 |
| `PUT` | `/api/admin/goods/categories/{ctgNo}` | 카테고리 수정 |

### GET /api/admin/goods/categories

트리 구조로 응답한다. `SORT_ORD` 기준 오름차순 정렬.

**Response:** `200 OK`
```json
{
  "success": true,
  "data": [
    {
      "ctgNo": 1,
      "upCtgNo": null,
      "ctgLvl": "1",
      "ctgNm": "의류",
      "sortOrd": 1,
      "useYn": "Y",
      "children": [
        {
          "ctgNo": 11,
          "upCtgNo": 1,
          "ctgLvl": "2",
          "ctgNm": "상의",
          "sortOrd": 1,
          "useYn": "Y",
          "children": []
        }
      ]
    }
  ]
}
```

### POST /api/admin/goods/categories

**Request Body:**
```json
{
  "upCtgNo": null,
  "ctgNm": "의류",
  "sortOrd": 1
}
```

- `upCtgNo`: null이면 최상위(레벨 1), 값이 있으면 하위 카테고리 등록
- `ctgNm`: 필수, 공백만 있는 값 불가 (`@NotBlank`)
- `sortOrd`: 선택 (기본값 0, 음수 허용)
- `USE_YN`: 등록 시 항상 `'Y'`로 고정 (요청 필드 없음)

**Response:** `201 Created`
```json
{ "success": true, "data": { "ctgNo": 1 } }
```

**검증:**
- `upCtgNo`가 존재할 경우 해당 카테고리가 DB에 있는지 확인 (없으면 `400`)
- 부모의 `CTG_LVL`이 `'3'`이면 등록 불가 (`400`, 4단계 불허)
- `CTG_LVL` = 부모 레벨 + 1 (최상위는 `'1'`)
- 비활성화(`USE_YN='N'`)된 상위 카테고리에도 하위 등록 허용

### PUT /api/admin/goods/categories/{ctgNo}

**Request Body:**
```json
{
  "ctgNm": "상의류",
  "useYn": "Y",
  "sortOrd": 2
}
```

- `upCtgNo` 변경 불가 (요청 필드에 포함하지 않음)
- `ctgNm`: 필수, 공백만 있는 값 불가 (`@NotBlank`)
- `useYn`: 필수, `'Y'` 또는 `'N'`만 허용 (`@Pattern(regexp = "[YN]")`, 그 외 `400`)
- `sortOrd`: 필수, 정수 (음수 허용)

**Response:** `200 OK`

**검증:**
- `ctgNo`가 DB에 존재하는지 확인 (없으면 `404`)

---

## 구현 파일

### Backend

| 레이어 | 파일 |
|--------|------|
| Controller | `controller/goods/CategoryController.java` |
| Service | `service/goods/CategoryService.java` |
| Mapper | `mapper/goods/CategoryMapper.java` |
| Mapper XML | `resources/mapper/goods/CategoryMapper.xml` |
| Domain | `model/goods/Category.java` (`ctgLvl` → `String`) |
| DTO | `dto/goods/CategoryResponse.java` (GET 트리 노드용, `children` 포함) |
| DTO | `dto/goods/CreateCategoryRequest.java` |
| DTO | `dto/goods/CreateCategoryResponse.java` (`ctgNo`만 반환, POST용) |
| DTO | `dto/goods/UpdateCategoryRequest.java` |
| Exception | `global/exception/CategoryNotFoundException.java` |

### 트리 조립 방식

전체 카테고리를 flat하게 한 번 조회 후 Java에서 재귀로 트리 조립.

```java
private List<CategoryResponse> buildTree(List<Category> all, Long upCtgNo) {
    return all.stream()
        .filter(c -> Objects.equals(c.getUpCtgNo(), upCtgNo))
        .sorted(Comparator.comparingInt(Category::getSortOrd))
        .map(c -> CategoryResponse.of(c, buildTree(all, c.getCtgNo())))
        .toList();
}
// 루트 호출: buildTree(all, null)
```

---

## 비즈니스 규칙 요약

| 규칙 | 내용 |
|------|------|
| 최대 depth | 3단계 (`CTG_LVL` = '1', '2', '3') |
| 최상위 등록 | `upCtgNo` = null, `CTG_LVL` = '1' |
| 하위 등록 | 부모 레벨 + 1, 부모가 '3'이면 등록 불가 (`400`) |
| 수정 가능 필드 | `CTG_NM`, `USE_YN`, `SORT_ORD` |
| 상위 변경 | 불가 |
| 삭제 | 물리 삭제 없음. 비활성화는 `USE_YN='N'` 수정으로 처리 |
| 응답 공통 컬럼 | `regDtm`, `regId`, `modDtm`, `modId`는 응답에 포함하지 않음 |
