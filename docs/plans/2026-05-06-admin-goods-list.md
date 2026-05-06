# Admin Goods List Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 어드민 상품 목록 조회 API(백엔드) + 화면(프론트엔드) 구현

**Architecture:** 백엔드에 `GET /api/admin/goods` 엔드포인트를 추가하고, 프론트엔드에서 Tanstack Query로 호출해 어드민 목록 페이지를 구성한다. 페이지네이션은 offset 기반이며 검색 조건(상품명, 판매상태, 카테고리)을 지원한다.

**Tech Stack:** Spring Boot 3 / MyBatis / PostgreSQL / Next.js App Router / Tanstack Query v5 / Tailwind CSS v4

---

## 파일 목록

### 백엔드 — 신규
| 파일 | 역할 |
|------|------|
| `dto/goods/GoodsListRequest.java` | GET 쿼리 파라미터 바인딩 DTO |
| `dto/goods/GoodsListItemResponse.java` | 목록 아이템 응답 DTO |
| `common/response/PageResponse.java` | 제네릭 페이지 응답 래퍼 |
| `service/goods/GoodsListService.java` | 목록 조회 비즈니스 로직 |
| `test/.../GoodsListServiceTest.java` | GoodsListService 단위 테스트 |

### 백엔드 — 수정
| 파일 | 변경 내용 |
|------|-----------|
| `mapper/goods/GoodsMapper.java` | `selectGoodsList`, `countGoodsList` 메서드 추가 |
| `resources/mapper/goods/GoodsMapper.xml` | 두 쿼리 SQL 추가 |
| `controller/goods/GoodsController.java` | `GET /api/admin/goods` 엔드포인트 추가 |

### 프론트엔드 — 신규
| 파일 | 역할 |
|------|------|
| `hooks/goods/useAdminGoodsList.ts` | Tanstack Query 목록 조회 훅 |
| `components/goods/GoodsListTable.tsx` | 상품 목록 테이블 컴포넌트 |

### 프론트엔드 — 수정
| 파일 | 변경 내용 |
|------|-----------|
| `types/goods.types.ts` | `GoodsListItemResponse`, `GoodsListSearchParams`, `PageResponse<T>` 추가 |
| `api/goods.api.ts` | `getAdminGoodsList` 함수 추가 |
| `app/admin/(main)/goods/page.tsx` | 플레이스홀더 → 실제 목록 페이지로 교체 |

---

## Task 1: 백엔드 — DTO & PageResponse

**Files:**
- Create: `backend/src/main/java/com/vibemyself/dto/goods/GoodsListRequest.java`
- Create: `backend/src/main/java/com/vibemyself/dto/goods/GoodsListItemResponse.java`
- Create: `backend/src/main/java/com/vibemyself/common/response/PageResponse.java`

- [ ] **Step 1: GoodsListRequest 생성**

```java
// backend/src/main/java/com/vibemyself/dto/goods/GoodsListRequest.java
package com.vibemyself.dto.goods;

public record GoodsListRequest(
        String goodsNm,
        String saleStatCd,
        Long ctgNo,
        int page,
        int size
) {
    public GoodsListRequest {
        if (page < 0) page = 0;
        if (size <= 0) size = 20;
        if (size > 100) size = 100;
    }

    public int offset() {
        return page * size;
    }
}
```

- [ ] **Step 2: GoodsListItemResponse 생성**

```java
// backend/src/main/java/com/vibemyself/dto/goods/GoodsListItemResponse.java
package com.vibemyself.dto.goods;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class GoodsListItemResponse {
    private String goodsNo;
    private String goodsNm;
    private String goodsTpCd;
    private String saleStatCd;
    private String ctgNm;
    private String brandNm;
    private BigDecimal salePrc;
    private String thumbImgUrl;
    private LocalDateTime regDtm;
}
```

- [ ] **Step 3: PageResponse 생성**

```java
// backend/src/main/java/com/vibemyself/common/response/PageResponse.java
package com.vibemyself.common.response;

import java.util.List;

public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    public static <T> PageResponse<T> of(List<T> content, int page, int size, long totalElements) {
        int totalPages = size == 0 ? 0 : (int) Math.ceil((double) totalElements / size);
        return new PageResponse<>(content, page, size, totalElements, totalPages);
    }
}
```

- [ ] **Step 4: 컴파일 확인**

```bash
cd backend && ./gradlew compileJava
```
Expected: BUILD SUCCESSFUL

- [ ] **Step 5: 커밋**

```bash
git add backend/src/main/java/com/vibemyself/dto/goods/GoodsListRequest.java \
        backend/src/main/java/com/vibemyself/dto/goods/GoodsListItemResponse.java \
        backend/src/main/java/com/vibemyself/common/response/PageResponse.java
git commit -m "feat: 상품 목록 조회 DTO 및 PageResponse 추가"
```

---

## Task 2: 백엔드 — Mapper

**Files:**
- Modify: `backend/src/main/java/com/vibemyself/mapper/goods/GoodsMapper.java`
- Modify: `backend/src/main/resources/mapper/goods/GoodsMapper.xml`

- [ ] **Step 1: GoodsMapper 인터페이스에 메서드 추가**

`GoodsMapper.java` 기존 import 블록 아래에 아래 두 메서드를 추가한다.

```java
import com.vibemyself.dto.goods.GoodsListItemResponse;
import com.vibemyself.dto.goods.GoodsListRequest;
// ... 기존 import 유지

// 인터페이스 본문 마지막에 추가:
List<GoodsListItemResponse> selectGoodsList(GoodsListRequest request);

long countGoodsList(GoodsListRequest request);
```

> 참고: GoodsListRequest는 record이며 `goodsNm()`, `saleStatCd()`, `ctgNo()`, `size()`, `offset()` accessor를 갖는다. MyBatis 3.5+는 record accessor를 프로퍼티로 인식한다.

- [ ] **Step 2: GoodsMapper.xml에 SQL 추가**

`GoodsMapper.xml` 닫는 `</mapper>` 태그 바로 앞에 아래 SQL을 추가한다.

```xml
<resultMap id="goodsListItemResultMap" type="com.vibemyself.dto.goods.GoodsListItemResponse">
    <result property="goodsNo"      column="GOODS_NO"/>
    <result property="goodsNm"      column="GOODS_NM"/>
    <result property="goodsTpCd"    column="GOODS_TP_CD"/>
    <result property="saleStatCd"   column="SALE_STAT_CD"/>
    <result property="ctgNm"        column="CTG_NM"/>
    <result property="brandNm"      column="BRAND_NM"/>
    <result property="salePrc"      column="SALE_PRC"/>
    <result property="thumbImgUrl"  column="THUMB_IMG_URL"/>
    <result property="regDtm"       column="REG_DTM"/>
</resultMap>

<select id="selectGoodsList" resultMap="goodsListItemResultMap">
    SELECT
        g.GOODS_NO,
        g.GOODS_NM,
        g.GOODS_TP_CD,
        g.SALE_STAT_CD,
        c.CTG_NM,
        b.BRAND_NM,
        p.SALE_PRC,
        (SELECT IMG_URL FROM PR_GOODS_IMG
         WHERE GOODS_NO = g.GOODS_NO AND SORT_ORD = 1 LIMIT 1) AS THUMB_IMG_URL,
        g.REG_DTM
    FROM PR_GOODS_BASE g
    JOIN PR_CTG_BASE c ON c.CTG_NO = g.CTG_NO
    LEFT JOIN PR_BRAND_BASE b ON b.BRAND_NO = g.BRAND_NO
    JOIN PR_GOODS_PRC p ON p.GOODS_NO = g.GOODS_NO AND p.APLY_TO_DT = '99991231'
    <where>
        <if test="goodsNm != null and goodsNm != ''">
            AND g.GOODS_NM LIKE CONCAT('%', #{goodsNm}, '%')
        </if>
        <if test="saleStatCd != null and saleStatCd != ''">
            AND g.SALE_STAT_CD = #{saleStatCd}
        </if>
        <if test="ctgNo != null">
            AND g.CTG_NO = #{ctgNo}
        </if>
    </where>
    ORDER BY g.REG_DTM DESC
    LIMIT #{size} OFFSET #{offset}
</select>

<select id="countGoodsList" resultType="long">
    SELECT COUNT(*)
    FROM PR_GOODS_BASE g
    <where>
        <if test="goodsNm != null and goodsNm != ''">
            AND g.GOODS_NM LIKE CONCAT('%', #{goodsNm}, '%')
        </if>
        <if test="saleStatCd != null and saleStatCd != ''">
            AND g.SALE_STAT_CD = #{saleStatCd}
        </if>
        <if test="ctgNo != null">
            AND g.CTG_NO = #{ctgNo}
        </if>
    </where>
</select>
```

- [ ] **Step 3: 컴파일 확인**

```bash
cd backend && ./gradlew compileJava
```
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: 커밋**

```bash
git add backend/src/main/java/com/vibemyself/mapper/goods/GoodsMapper.java \
        backend/src/main/resources/mapper/goods/GoodsMapper.xml
git commit -m "feat: 상품 목록 조회 Mapper 추가"
```

---

## Task 3: 백엔드 — Service + 테스트

**Files:**
- Create: `backend/src/main/java/com/vibemyself/service/goods/GoodsListService.java`
- Create: `backend/src/test/java/com/vibemyself/service/goods/GoodsListServiceTest.java`

- [ ] **Step 1: 실패하는 테스트 작성**

```java
// backend/src/test/java/com/vibemyself/service/goods/GoodsListServiceTest.java
package com.vibemyself.service.goods;

import com.vibemyself.common.response.PageResponse;
import com.vibemyself.dto.goods.GoodsListItemResponse;
import com.vibemyself.dto.goods.GoodsListRequest;
import com.vibemyself.mapper.goods.GoodsMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GoodsListServiceTest {

    @Mock GoodsMapper goodsMapper;
    @InjectMocks GoodsListService goodsListService;

    @Test
    void getGoodsList_결과없음_빈페이지반환() {
        GoodsListRequest request = new GoodsListRequest(null, null, null, 0, 20);
        when(goodsMapper.selectGoodsList(any())).thenReturn(List.of());
        when(goodsMapper.countGoodsList(any())).thenReturn(0L);

        PageResponse<GoodsListItemResponse> result = goodsListService.getGoodsList(request);

        assertThat(result.content()).isEmpty();
        assertThat(result.totalElements()).isZero();
        assertThat(result.totalPages()).isZero();
    }

    @Test
    void getGoodsList_2페이지_offset_계산() {
        GoodsListRequest request = new GoodsListRequest(null, null, null, 1, 20);
        when(goodsMapper.selectGoodsList(any())).thenReturn(List.of());
        when(goodsMapper.countGoodsList(any())).thenReturn(21L);

        PageResponse<GoodsListItemResponse> result = goodsListService.getGoodsList(request);

        assertThat(result.page()).isEqualTo(1);
        assertThat(result.totalPages()).isEqualTo(2);
    }

    @Test
    void getGoodsList_totalPages_올림계산() {
        GoodsListRequest request = new GoodsListRequest(null, null, null, 0, 20);
        when(goodsMapper.selectGoodsList(any())).thenReturn(List.of());
        when(goodsMapper.countGoodsList(any())).thenReturn(21L);

        PageResponse<GoodsListItemResponse> result = goodsListService.getGoodsList(request);

        assertThat(result.totalPages()).isEqualTo(2);
    }
}
```

- [ ] **Step 2: 테스트 실패 확인**

```bash
cd backend && ./gradlew test --tests "com.vibemyself.service.goods.GoodsListServiceTest"
```
Expected: FAILED — GoodsListService 클래스 없음

- [ ] **Step 3: GoodsListService 구현**

```java
// backend/src/main/java/com/vibemyself/service/goods/GoodsListService.java
package com.vibemyself.service.goods;

import com.vibemyself.common.response.PageResponse;
import com.vibemyself.dto.goods.GoodsListItemResponse;
import com.vibemyself.dto.goods.GoodsListRequest;
import com.vibemyself.mapper.goods.GoodsMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GoodsListService {

    private final GoodsMapper goodsMapper;

    @Transactional(readOnly = true)
    public PageResponse<GoodsListItemResponse> getGoodsList(GoodsListRequest request) {
        List<GoodsListItemResponse> content = goodsMapper.selectGoodsList(request);
        long total = goodsMapper.countGoodsList(request);
        return PageResponse.of(content, request.page(), request.size(), total);
    }
}
```

- [ ] **Step 4: 테스트 통과 확인**

```bash
cd backend && ./gradlew test --tests "com.vibemyself.service.goods.GoodsListServiceTest"
```
Expected: BUILD SUCCESSFUL, 3 tests passed

- [ ] **Step 5: 커밋**

```bash
git add backend/src/main/java/com/vibemyself/service/goods/GoodsListService.java \
        backend/src/test/java/com/vibemyself/service/goods/GoodsListServiceTest.java
git commit -m "feat: 상품 목록 조회 서비스 구현 및 테스트"
```

---

## Task 4: 백엔드 — Controller 엔드포인트 추가

**Files:**
- Modify: `backend/src/main/java/com/vibemyself/controller/goods/GoodsController.java`

- [ ] **Step 1: GET 엔드포인트 추가**

기존 `GoodsController.java`에 아래 내용을 추가한다.

import 블록에 추가:
```java
import com.vibemyself.common.response.PageResponse;
import com.vibemyself.dto.goods.GoodsListItemResponse;
import com.vibemyself.dto.goods.GoodsListRequest;
import com.vibemyself.service.goods.GoodsListService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
```

생성자 주입 필드 추가 (`GoodsCreateService` 아래에):
```java
private final GoodsListService goodsListService;
```

클래스 본문에 메서드 추가:
```java
@GetMapping
public ResponseEntity<ApiResponse<PageResponse<GoodsListItemResponse>>> getGoodsList(
        @RequestParam(defaultValue = "") String goodsNm,
        @RequestParam(defaultValue = "") String saleStatCd,
        @RequestParam(required = false) Long ctgNo,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size) {
    GoodsListRequest request = new GoodsListRequest(
            goodsNm.isBlank() ? null : goodsNm,
            saleStatCd.isBlank() ? null : saleStatCd,
            ctgNo, page, size);
    return ResponseEntity.ok(ApiResponse.ok(goodsListService.getGoodsList(request)));
}
```

> `@ModelAttribute`로 record를 바인딩하면 compact constructor의 validation이 동작하지 않을 수 있으므로 `@RequestParam`을 개별로 받아 직접 생성한다.

- [ ] **Step 2: 컴파일 + 전체 테스트 확인**

```bash
cd backend && ./gradlew test
```
Expected: BUILD SUCCESSFUL (기존 CategoryServiceTest 4개 실패는 기존 문제 — 무관)

- [ ] **Step 3: 커밋**

```bash
git add backend/src/main/java/com/vibemyself/controller/goods/GoodsController.java
git commit -m "feat: 어드민 상품 목록 조회 API 추가 (GET /api/admin/goods)"
```

---

## Task 5: 프론트엔드 — 타입 추가

**Files:**
- Modify: `frontend/types/goods.types.ts`

- [ ] **Step 1: 타입 추가**

`frontend/types/goods.types.ts` 파일 맨 아래에 추가:

```typescript
// ── 공통 페이지 응답 ───────────────────────────────
export interface PageResponse<T> {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

// ── 상품 목록 ─────────────────────────────────────
export type GoodsSaleStatCd = 'SELLING' | 'SOLD_OUT' | 'STOPPED'

export interface GoodsListItemResponse {
  goodsNo: string
  goodsNm: string
  goodsTpCd: GoodsTypeCd
  saleStatCd: GoodsSaleStatCd
  ctgNm: string
  brandNm: string | null
  salePrc: number
  thumbImgUrl: string | null
  regDtm: string
}

export interface GoodsListSearchParams {
  goodsNm?: string
  saleStatCd?: GoodsSaleStatCd | ''
  ctgNo?: number
  page?: number
  size?: number
}
```

- [ ] **Step 2: 타입 체크**

```bash
cd frontend && pnpm tsc --noEmit
```
Expected: 오류 없음

- [ ] **Step 3: 커밋**

```bash
git add frontend/types/goods.types.ts
git commit -m "feat: 상품 목록 조회 타입 추가"
```

---

## Task 6: 프론트엔드 — API 함수 + 훅

**Files:**
- Modify: `frontend/api/goods.api.ts`
- Create: `frontend/hooks/goods/useAdminGoodsList.ts`

- [ ] **Step 1: API 함수 추가**

`frontend/api/goods.api.ts` import 블록에 타입 추가:
```typescript
import type {
  // ... 기존 타입들 유지
  GoodsListItemResponse,
  GoodsListSearchParams,
  PageResponse,
} from '@/types/goods.types'
```

파일 맨 아래에 함수 추가:
```typescript
export const getAdminGoodsList = (params: GoodsListSearchParams): Promise<PageResponse<GoodsListItemResponse>> =>
  api
    .get<ApiResponse<PageResponse<GoodsListItemResponse>>>('/api/admin/goods', { params })
    .then((res) => res.data.data)
```

- [ ] **Step 2: 훅 생성**

```typescript
// frontend/hooks/goods/useAdminGoodsList.ts
import { useQuery } from '@tanstack/react-query'
import { getAdminGoodsList } from '@/api/goods.api'
import type { GoodsListSearchParams } from '@/types/goods.types'

export const useAdminGoodsList = (params: GoodsListSearchParams) => {
  return useQuery({
    queryKey: ['goods', 'list', params],
    queryFn: () => getAdminGoodsList(params),
  })
}
```

- [ ] **Step 3: 타입 체크**

```bash
cd frontend && pnpm tsc --noEmit
```
Expected: 오류 없음

- [ ] **Step 4: 커밋**

```bash
git add frontend/api/goods.api.ts frontend/hooks/goods/useAdminGoodsList.ts
git commit -m "feat: 상품 목록 API 함수 및 훅 추가"
```

---

## Task 7: 프론트엔드 — GoodsListTable 컴포넌트

**Files:**
- Create: `frontend/components/goods/GoodsListTable.tsx`

- [ ] **Step 1: GoodsListTable 컴포넌트 생성**

```typescript
// frontend/components/goods/GoodsListTable.tsx
'use client'

import Image from 'next/image'
import Link from 'next/link'
import type { GoodsListItemResponse, GoodsSaleStatCd } from '@/types/goods.types'

interface GoodsListTableProps {
  items: GoodsListItemResponse[]
}

const SALE_STAT_LABEL: Record<GoodsSaleStatCd, string> = {
  SELLING: '판매중',
  SOLD_OUT: '품절',
  STOPPED: '판매중지',
}

const SALE_STAT_CLASS: Record<GoodsSaleStatCd, string> = {
  SELLING: 'text-green-600 bg-green-50',
  SOLD_OUT: 'text-amber-600 bg-amber-50',
  STOPPED: 'text-slate-500 bg-slate-100',
}

const formatPrice = (price: number) => price.toLocaleString('ko-KR') + '원'

const formatDtm = (dtm: string) => dtm.replace('T', ' ').slice(0, 16)

const GoodsListTable = ({ items }: GoodsListTableProps) => {
  if (items.length === 0) {
    return (
      <div className="rounded-lg border border-slate-200 bg-white py-16 text-center text-sm text-slate-400">
        등록된 상품이 없습니다.
      </div>
    )
  }

  return (
    <div className="overflow-x-auto rounded-lg border border-slate-200 bg-white">
      <table className="min-w-full divide-y divide-slate-200 text-sm">
        <thead className="bg-slate-50">
          <tr>
            {['상품번호', '이미지', '상품명', '카테고리', '브랜드', '판매가', '판매상태', '등록일'].map((h) => (
              <th key={h} className="px-4 py-3 text-left text-xs font-semibold text-slate-500">
                {h}
              </th>
            ))}
          </tr>
        </thead>
        <tbody className="divide-y divide-slate-100">
          {items.map((item) => (
            <tr key={item.goodsNo} className="hover:bg-slate-50">
              <td className="px-4 py-3 font-mono text-xs text-slate-500">{item.goodsNo}</td>
              <td className="px-4 py-3">
                {item.thumbImgUrl ? (
                  <Image
                    src={item.thumbImgUrl}
                    alt={item.goodsNm}
                    width={48}
                    height={48}
                    className="rounded object-cover"
                  />
                ) : (
                  <div className="h-12 w-12 rounded bg-slate-100" />
                )}
              </td>
              <td className="px-4 py-3">
                <Link
                  href={`/admin/goods/${item.goodsNo}`}
                  className="font-medium text-slate-800 hover:text-blue-600"
                >
                  {item.goodsNm}
                </Link>
              </td>
              <td className="px-4 py-3 text-slate-600">{item.ctgNm}</td>
              <td className="px-4 py-3 text-slate-500">{item.brandNm ?? '-'}</td>
              <td className="px-4 py-3 text-right font-medium text-slate-800">
                {formatPrice(item.salePrc)}
              </td>
              <td className="px-4 py-3">
                <span
                  className={`inline-block rounded-full px-2 py-0.5 text-xs font-medium ${SALE_STAT_CLASS[item.saleStatCd]}`}
                >
                  {SALE_STAT_LABEL[item.saleStatCd]}
                </span>
              </td>
              <td className="px-4 py-3 text-xs text-slate-400">{formatDtm(item.regDtm)}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}

export default GoodsListTable
```

- [ ] **Step 2: 타입 체크**

```bash
cd frontend && pnpm tsc --noEmit
```
Expected: 오류 없음

- [ ] **Step 3: 커밋**

```bash
git add frontend/components/goods/GoodsListTable.tsx
git commit -m "feat: 상품 목록 테이블 컴포넌트 추가"
```

---

## Task 8: 프론트엔드 — 어드민 상품 목록 페이지

**Files:**
- Modify: `frontend/app/admin/(main)/goods/page.tsx`

- [ ] **Step 1: 페이지 구현**

```typescript
// frontend/app/admin/(main)/goods/page.tsx
'use client'

import { useState } from 'react'
import Link from 'next/link'
import { useAdminGoodsList } from '@/hooks/goods/useAdminGoodsList'
import GoodsListTable from '@/components/goods/GoodsListTable'
import type { GoodsListSearchParams, GoodsSaleStatCd } from '@/types/goods.types'

const PAGE_SIZE = 20

export default function AdminGoodsPage() {
  const [search, setSearch] = useState('')
  const [saleStatCd, setSaleStatCd] = useState<GoodsSaleStatCd | ''>('')
  const [page, setPage] = useState(0)

  const params: GoodsListSearchParams = {
    goodsNm: search || undefined,
    saleStatCd: saleStatCd || undefined,
    page,
    size: PAGE_SIZE,
  }

  const { data, isLoading, isError } = useAdminGoodsList(params)

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault()
    setPage(0)
  }

  return (
    <div className="flex flex-col gap-6">
      <div className="flex items-center justify-between">
        <h1 className="text-lg font-semibold text-slate-800">상품 목록</h1>
        <Link
          href="/admin/goods/create"
          className="rounded bg-blue-600 px-4 py-2 text-sm text-white hover:bg-blue-700"
        >
          상품 등록
        </Link>
      </div>

      <form onSubmit={handleSearch} className="flex gap-2">
        <input
          type="text"
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          placeholder="상품명 검색"
          className="rounded border border-slate-300 px-3 py-2 text-sm focus:border-blue-400 focus:outline-none"
        />
        <select
          value={saleStatCd}
          onChange={(e) => {
            setSaleStatCd(e.target.value as GoodsSaleStatCd | '')
            setPage(0)
          }}
          className="rounded border border-slate-300 px-3 py-2 text-sm focus:border-blue-400 focus:outline-none"
        >
          <option value="">전체 상태</option>
          <option value="SELLING">판매중</option>
          <option value="SOLD_OUT">품절</option>
          <option value="STOPPED">판매중지</option>
        </select>
        <button
          type="submit"
          className="rounded bg-slate-700 px-4 py-2 text-sm text-white hover:bg-slate-800"
        >
          검색
        </button>
      </form>

      {isLoading && (
        <div className="py-16 text-center text-sm text-slate-400">불러오는 중...</div>
      )}

      {isError && (
        <div className="rounded border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-600">
          목록을 불러오지 못했습니다.
        </div>
      )}

      {data && (
        <>
          <div className="text-sm text-slate-500">
            총 <span className="font-medium text-slate-700">{data.totalElements.toLocaleString()}</span>건
          </div>

          <GoodsListTable items={data.content} />

          {data.totalPages > 1 && (
            <div className="flex justify-center gap-1">
              <button
                onClick={() => setPage((p) => Math.max(0, p - 1))}
                disabled={page === 0}
                className="rounded border border-slate-300 px-3 py-1.5 text-sm disabled:opacity-40 hover:bg-slate-50"
              >
                이전
              </button>
              {Array.from({ length: data.totalPages }, (_, i) => (
                <button
                  key={i}
                  onClick={() => setPage(i)}
                  className={`rounded border px-3 py-1.5 text-sm ${
                    page === i
                      ? 'border-blue-600 bg-blue-600 text-white'
                      : 'border-slate-300 hover:bg-slate-50'
                  }`}
                >
                  {i + 1}
                </button>
              ))}
              <button
                onClick={() => setPage((p) => Math.min(data.totalPages - 1, p + 1))}
                disabled={page === data.totalPages - 1}
                className="rounded border border-slate-300 px-3 py-1.5 text-sm disabled:opacity-40 hover:bg-slate-50"
              >
                다음
              </button>
            </div>
          )}
        </>
      )}
    </div>
  )
}
```

- [ ] **Step 2: 타입 체크**

```bash
cd frontend && pnpm tsc --noEmit
```
Expected: 오류 없음

- [ ] **Step 3: 개발 서버 + 백엔드 동시 실행 후 브라우저 확인**

```bash
# terminal 1
cd backend && ./gradlew bootRun

# terminal 2
cd frontend && pnpm dev
```

브라우저에서 `http://localhost:3000/admin/goods` 접속 → 상품 목록 또는 빈 상태 메시지 표시 확인

- [ ] **Step 4: 커밋**

```bash
git add frontend/app/admin/\(main\)/goods/page.tsx
git commit -m "feat: 어드민 상품 목록 페이지 구현"
```