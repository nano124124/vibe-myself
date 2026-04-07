# 상품 등록 - 옵션 토글 + 이미지 파일 업로드 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 상품 등록 폼에 "옵션 없음/옵션 사용" 라디오버튼 추가, 이미지 입력을 URL 직접 입력에서 파일 업로드(Supabase Storage)로 교체

**Architecture:** 단일 multipart/form-data 요청으로 이미지 파일과 상품 데이터를 동시에 전송. 백엔드가 Supabase Storage REST API에 직접 업로드 후 URL을 DB에 저장. 옵션 사용 여부는 프론트엔드 상태로만 제어하며 백엔드 변경 없음 (빈 배열 전송).

**Tech Stack:** Spring Boot 3 (RestTemplate, @RequestPart), Next.js App Router, Supabase Storage REST API

---

## File Map

### Backend
| 파일 | 변경 |
|------|------|
| `backend/src/main/resources/application-local.yml` | 수정 - supabase 설정 추가 |
| `backend/src/main/java/com/vibemyself/config/AppConfig.java` | 신규 - RestTemplate 빈 |
| `backend/src/main/java/com/vibemyself/common/storage/SupabaseStorageService.java` | 신규 - Storage 업로드 |
| `backend/src/main/java/com/vibemyself/dto/goods/CreateGoodsRequest.java` | 수정 - imgUrls 제거, @NotEmpty 제거 |
| `backend/src/main/java/com/vibemyself/controller/goods/GoodsController.java` | 수정 - multipart @RequestPart |
| `backend/src/main/java/com/vibemyself/service/goods/GoodsCreateService.java` | 수정 - 이미지 업로드 연동 |
| `backend/src/test/java/com/vibemyself/common/storage/SupabaseStorageServiceTest.java` | 신규 |

### Frontend
| 파일 | 변경 |
|------|------|
| `frontend/types/goods.types.ts` | 수정 - imgUrls 제거 |
| `frontend/components/goods/GoodsImageInput.tsx` | 수정 - 파일 업로드 UI |
| `frontend/api/goods.api.ts` | 수정 - multipart 전송 |
| `frontend/app/admin/(main)/goods/create/page.tsx` | 수정 - optEnabled + imgFiles state |

---

## Task 1: Backend - application-local.yml에 Supabase 설정 추가

**Files:**
- Modify: `backend/src/main/resources/application-local.yml`

- [ ] **Step 1: `application-local.yml`에 supabase 섹션 추가**

기존 파일 하단에 추가:
```yaml
supabase:
  url: https://ekmmmbmsxwiprqzkrsmd.supabase.co
  service-role-key: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImVrbW1tYm1zeHdpcHJxemtyc21kIiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImlhdCI6MTc3NDMxNDUwNSwiZXhwIjoyMDg5ODkwNTA1fQ.YQTrNkXM9x0w1OASoFUCWq3ZH9I24QEgB-7hxAIvW-c
  storage:
    goods-bucket: goods-images

spring:
  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 60MB
```

- [ ] **Step 2: Supabase 대시보드에서 `goods-images` 버킷 생성 (수동)**

Storage → New bucket → Name: `goods-images` → Public bucket: ON → Create

---

## Task 2: Backend - RestTemplate 빈 + SupabaseStorageService 테스트 작성

**Files:**
- Create: `backend/src/main/java/com/vibemyself/config/AppConfig.java`
- Create: `backend/src/test/java/com/vibemyself/common/storage/SupabaseStorageServiceTest.java`

- [ ] **Step 1: AppConfig.java 생성 (RestTemplate 빈)**

```java
package com.vibemyself.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
```

- [ ] **Step 2: SupabaseStorageServiceTest 작성**

```java
package com.vibemyself.common.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class SupabaseStorageServiceTest {

    @Mock RestTemplate restTemplate;
    SupabaseStorageService service;

    @BeforeEach
    void setUp() {
        service = new SupabaseStorageService(
            restTemplate,
            "https://test.supabase.co",
            "test-service-key",
            "goods-images"
        );
    }

    @Test
    void upload_성공시_publicUrl_반환() {
        MockMultipartFile file = new MockMultipartFile(
            "images", "photo.jpg", "image/jpeg", "data".getBytes()
        );
        given(restTemplate.exchange(any(), eq(HttpMethod.PUT), any(), eq(String.class)))
            .willReturn(ResponseEntity.ok("{\"Key\":\"goods-images/G001/uuid_photo.jpg\"}"));

        String url = service.upload(file, "G001");

        assertThat(url).startsWith("https://test.supabase.co/storage/v1/object/public/goods-images/G001/");
        assertThat(url).endsWith("_photo.jpg");
    }

    @Test
    void upload_실패시_예외발생() {
        MockMultipartFile file = new MockMultipartFile(
            "images", "photo.jpg", "image/jpeg", "data".getBytes()
        );
        given(restTemplate.exchange(any(), eq(HttpMethod.PUT), any(), eq(String.class)))
            .willReturn(ResponseEntity.status(HttpStatus.FORBIDDEN).body("error"));

        assertThatThrownBy(() -> service.upload(file, "G001"))
            .isInstanceOf(RuntimeException.class);
    }
}
```

- [ ] **Step 3: 테스트 실행 (실패 확인)**

```bash
cd backend && ./gradlew test --tests "com.vibemyself.common.storage.SupabaseStorageServiceTest" 2>&1 | tail -20
```

Expected: FAIL (SupabaseStorageService 미존재)

---

## Task 3: Backend - SupabaseStorageService 구현

**Files:**
- Create: `backend/src/main/java/com/vibemyself/common/storage/SupabaseStorageService.java`

- [ ] **Step 1: SupabaseStorageService 구현**

```java
package com.vibemyself.common.storage;

import com.vibemyself.global.exception.AppException;
import com.vibemyself.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SupabaseStorageService {

    private final RestTemplate restTemplate;

    @Value("${supabase.url}")
    private final String supabaseUrl;

    @Value("${supabase.service-role-key}")
    private final String serviceRoleKey;

    @Value("${supabase.storage.goods-bucket}")
    private final String goodsBucket;

    public String upload(MultipartFile file, String goodsNo) {
        String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
        String path = goodsNo + "/" + filename;
        String uploadUrl = supabaseUrl + "/storage/v1/object/" + goodsBucket + "/" + path;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + serviceRoleKey);
        headers.setContentType(MediaType.parseMediaType(
            file.getContentType() != null ? file.getContentType() : "application/octet-stream"
        ));
        headers.set("x-upsert", "true");

        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            throw new AppException(ErrorCode.IMAGE_UPLOAD_FAILED);
        }

        ResponseEntity<String> response = restTemplate.exchange(
            uploadUrl, HttpMethod.PUT, new HttpEntity<>(bytes, headers), String.class
        );

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new AppException(ErrorCode.IMAGE_UPLOAD_FAILED);
        }

        return supabaseUrl + "/storage/v1/object/public/" + goodsBucket + "/" + path;
    }
}
```

- [ ] **Step 2: `ErrorCode`에 `IMAGE_UPLOAD_FAILED` 추가**

`ErrorCode` 파일을 찾아 기존 패턴에 맞게 추가:
```bash
find backend/src -name "ErrorCode.java" | head -1
```
파일 열어 기존 항목 마지막에 추가:
```java
IMAGE_UPLOAD_FAILED("이미지 업로드에 실패했습니다.")
```

- [ ] **Step 3: 테스트 실행 (통과 확인)**

```bash
cd backend && ./gradlew test --tests "com.vibemyself.common.storage.SupabaseStorageServiceTest" 2>&1 | tail -20
```

Expected: PASS 2개

- [ ] **Step 4: 커밋**

```bash
git add backend/src/main/java/com/vibemyself/config/AppConfig.java \
        backend/src/main/java/com/vibemyself/common/storage/SupabaseStorageService.java \
        backend/src/test/java/com/vibemyself/common/storage/SupabaseStorageServiceTest.java \
        backend/src/main/java/com/vibemyself/global/exception/ErrorCode.java \
        backend/src/main/resources/application-local.yml
git commit -m "feat: SupabaseStorageService 이미지 업로드 구현"
```

---

## Task 4: Backend - CreateGoodsRequest DTO 수정

**Files:**
- Modify: `backend/src/main/java/com/vibemyself/dto/goods/CreateGoodsRequest.java`

- [ ] **Step 1: `imgUrls` 필드 제거, `optGrpCds`·`units` `@NotEmpty` 제거**

`@NotEmpty` 제거 이유: "옵션 없음" 선택 시 빈 리스트 전송이 유효한 상태.

```java
package com.vibemyself.dto.goods;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record CreateGoodsRequest(

        @NotBlank(message = "상품명은 필수입니다.")
        String goodsNm,

        @NotBlank(message = "상품유형 코드는 필수입니다.")
        String goodsTpCd,

        @NotNull(message = "카테고리는 필수입니다.")
        Long ctgNo,

        Long brandNo,

        @NotNull(message = "판매가는 필수입니다.")
        @Positive(message = "판매가는 0보다 커야 합니다.")
        BigDecimal salePrc,

        BigDecimal normPrc,

        BigDecimal suplyPrc,

        String goodsDesc,

        LocalDateTime saleStartDtm,

        LocalDateTime saleEndDtm,

        @NotBlank(message = "판매상태 코드는 필수입니다.")
        String saleStatCd,

        @NotBlank(message = "배송정책은 필수입니다.")
        String dlvPolicyNo,

        List<String> optGrpCds,

        @Valid
        List<UnitRequest> units
) {}
```

- [ ] **Step 2: 백엔드 빌드 확인**

```bash
cd backend && ./gradlew compileJava 2>&1 | tail -20
```

Expected: BUILD SUCCESSFUL

---

## Task 5: Backend - GoodsController multipart 변경 + GoodsCreateService 이미지 연동

**Files:**
- Modify: `backend/src/main/java/com/vibemyself/controller/goods/GoodsController.java`
- Modify: `backend/src/main/java/com/vibemyself/service/goods/GoodsCreateService.java`

- [ ] **Step 1: GoodsController를 `@RequestPart` 방식으로 변경**

```java
package com.vibemyself.controller.goods;

import com.vibemyself.common.response.ApiResponse;
import com.vibemyself.dto.goods.CreateGoodsRequest;
import com.vibemyself.dto.goods.CreateGoodsResponse;
import com.vibemyself.service.goods.GoodsCreateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/admin/goods")
@RequiredArgsConstructor
public class GoodsController {

    private final GoodsCreateService goodsCreateService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<CreateGoodsResponse>> createGoods(
            @RequestPart("data") @Valid CreateGoodsRequest data,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        List<MultipartFile> imageList = images != null ? images : Collections.emptyList();
        String goodsNo = goodsCreateService.createGoods(data, imageList);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(new CreateGoodsResponse(goodsNo)));
    }
}
```

- [ ] **Step 2: GoodsCreateService에 이미지 업로드 연동**

`createGoods` 메서드 시그니처 변경 및 이미지 업로드 로직 추가:

```java
// 클래스 상단 필드에 추가
private final SupabaseStorageService supabaseStorageService;
```

`createGoods` 메서드:
```java
@Transactional
public String createGoods(CreateGoodsRequest request, List<MultipartFile> images) {
    validate(request);

    String userId = SecurityUtils.currentUserId();

    PrGoodsBase goods = PrGoodsBase.builder()
            .goodsNm(request.goodsNm())
            .goodsTpCd(GoodsType.fromCd(request.goodsTpCd()).getCd())
            .ctgNo(request.ctgNo())
            .brandNo(request.brandNo())
            .saleStatCd(GoodsSaleStatus.fromCd(request.saleStatCd()).getCd())
            .dlvPolicyNo(request.dlvPolicyNo())
            .goodsDesc(request.goodsDesc())
            .saleStartDtm(request.saleStartDtm())
            .saleEndDtm(request.saleEndDtm() != null ? request.saleEndDtm() : SALE_END_DTM_DEFAULT)
            .regId(userId)
            .modId(userId)
            .build();
    goodsMapper.insertGoods(goods);

    goodsMapper.insertGoodsPrc(PrGoodsPrc.builder()
            .goodsNo(goods.getGoodsNo())
            .aplyFromDt(LocalDate.now().format(DT_FORMATTER))
            .aplyToDt(PRC_APLY_TO_DT_INFINITY)
            .salePrc(request.salePrc())
            .normPrc(request.normPrc())
            .suplyPrc(request.suplyPrc())
            .mrgnRate(calcMrgnRate(request.salePrc(), request.suplyPrc()))
            .regId(userId)
            .modId(userId)
            .build());

    List<String> imgUrls = images.stream()
            .map(file -> supabaseStorageService.upload(file, goods.getGoodsNo()))
            .toList();

    insertImages(goods.getGoodsNo(), imgUrls, userId);

    List<String> optGrpCds = request.optGrpCds() != null ? request.optGrpCds() : Collections.emptyList();
    List<UnitRequest> units = request.units() != null ? request.units() : Collections.emptyList();

    insertGoodsOpts(goods.getGoodsNo(), optGrpCds, userId);
    insertUnits(goods.getGoodsNo(), units, userId);

    return goods.getGoodsNo();
}
```

`validate` 메서드에서 `imgUrls` 관련 검증 및 `optGrpCds`/`units` null 방어:
```java
private void validate(CreateGoodsRequest request) {
    if (!GoodsType.isValid(request.goodsTpCd())) {
        throw new AppException(ErrorCode.INVALID_GOODS_TYPE_CD);
    }
    if (!GoodsSaleStatus.isValid(request.saleStatCd())) {
        throw new AppException(ErrorCode.INVALID_SALE_STAT_CD);
    }
    if (categoryMapper.selectByCtgNo(request.ctgNo()) == null) {
        throw new AppException(ErrorCode.CATEGORY_NOT_FOUND);
    }
    if (request.brandNo() != null && goodsMapper.selectBrandByNo(request.brandNo()) == null) {
        throw new AppException(ErrorCode.BRAND_NOT_FOUND);
    }
    if (goodsMapper.selectDlvPolicyByNo(request.dlvPolicyNo()) == null) {
        throw new AppException(ErrorCode.DLV_POLICY_NOT_FOUND);
    }

    List<String> optGrpCds = request.optGrpCds() != null ? request.optGrpCds() : Collections.emptyList();
    for (String optGrpCd : optGrpCds) {
        if (goodsMapper.selectOptGrpByCd(optGrpCd) == null) {
            throw new AppException(ErrorCode.OPT_GRP_NOT_FOUND);
        }
    }
    List<UnitRequest> units = request.units() != null ? request.units() : Collections.emptyList();
    for (UnitRequest unit : units) {
        validateUnitOptItems(unit);
    }
}
```

import 추가 필요:
```java
import com.vibemyself.common.storage.SupabaseStorageService;
import java.util.Collections;
```

- [ ] **Step 3: 백엔드 빌드 확인**

```bash
cd backend && ./gradlew compileJava 2>&1 | tail -20
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 4: 커밋**

```bash
git add backend/src/main/java/com/vibemyself/controller/goods/GoodsController.java \
        backend/src/main/java/com/vibemyself/service/goods/GoodsCreateService.java \
        backend/src/main/java/com/vibemyself/dto/goods/CreateGoodsRequest.java
git commit -m "feat: 상품 등록 API multipart 전환 및 옵션 없음 지원"
```

---

## Task 6: Frontend - types 수정 + goods.api.ts multipart 전환

**Files:**
- Modify: `frontend/types/goods.types.ts`
- Modify: `frontend/api/goods.api.ts`

- [ ] **Step 1: `CreateGoodsRequest`에서 `imgUrls` 제거**

`frontend/types/goods.types.ts`의 `CreateGoodsRequest` 인터페이스:
```typescript
export interface CreateGoodsRequest {
  goodsNm: string
  goodsTpCd: GoodsTypeCd
  ctgNo: number
  brandNo?: number
  salePrc: number
  normPrc?: number
  suplyPrc?: number
  goodsDesc?: string
  saleStartDtm?: string
  saleEndDtm?: string
  saleStatCd: SaleStatCd
  dlvPolicyNo: string
  optGrpCds: string[]
  units: UnitRequest[]
}
```

- [ ] **Step 2: `goods.api.ts`의 `createGoods` 함수를 multipart로 변경**

`frontend/api/goods.api.ts`의 `createGoods` 함수:
```typescript
export const createGoods = (data: CreateGoodsRequest, images: File[]): Promise<CreateGoodsResponse> => {
  const formData = new FormData()
  formData.append('data', new Blob([JSON.stringify(data)], { type: 'application/json' }))
  images.forEach((file) => formData.append('images', file))
  return axiosInstance
    .post<ApiResponse<CreateGoodsResponse>>('/api/admin/goods', formData)
    .then((res) => res.data.data)
}
```

- [ ] **Step 3: `useGoodsCreate.ts` 시그니처 변경**

`frontend/hooks/goods/useGoodsCreate.ts`:
```typescript
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { isAxiosError } from 'axios'
import { toast } from 'sonner'
import { createGoods } from '@/api/goods.api'
import type { CreateGoodsRequest } from '@/types/goods.types'

export const useGoodsCreate = () => {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ data, images }: { data: CreateGoodsRequest; images: File[] }) =>
      createGoods(data, images),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['goods', 'list'] })
    },
    onError: (error) => {
      const message = isAxiosError(error)
        ? (error.response?.data?.message ?? '요청 처리 중 오류가 발생했습니다.')
        : '알 수 없는 오류가 발생했습니다.'
      toast.error(message)
    },
  })
}
```

- [ ] **Step 4: 타입 체크**

```bash
cd frontend && pnpm tsc --noEmit 2>&1 | head -30
```

Expected: 에러 없음 (또는 이후 task에서 해결될 에러만 남음)

---

## Task 7: Frontend - GoodsImageInput.tsx 파일 업로드 UI로 교체

**Files:**
- Modify: `frontend/components/goods/GoodsImageInput.tsx`

- [ ] **Step 1: GoodsImageInput을 파일 업로드 방식으로 교체**

```typescript
'use client'

import { useRef } from 'react'

interface GoodsImageInputProps {
  files: File[]
  onChange: (files: File[]) => void
}

const MAX_IMAGES = 5

const GoodsImageInput = ({ files, onChange }: GoodsImageInputProps) => {
  const inputRef = useRef<HTMLInputElement>(null)

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const selected = Array.from(e.target.files ?? [])
    const merged = [...files, ...selected].slice(0, MAX_IMAGES)
    onChange(merged)
    if (inputRef.current) inputRef.current.value = ''
  }

  const removeFile = (index: number) => {
    onChange(files.filter((_, i) => i !== index))
  }

  return (
    <div className="flex flex-col gap-3">
      <div className="flex items-center justify-between">
        <p className="text-sm text-slate-500">
          첫 번째 이미지가 대표 이미지로 설정됩니다. (최대 {MAX_IMAGES}장)
        </p>
        <button
          type="button"
          onClick={() => inputRef.current?.click()}
          disabled={files.length >= MAX_IMAGES}
          className="rounded border border-blue-500 px-3 py-1 text-sm text-blue-600 hover:bg-blue-50 disabled:opacity-40"
        >
          + 이미지 추가
        </button>
        <input
          ref={inputRef}
          type="file"
          accept="image/*"
          multiple
          className="hidden"
          onChange={handleFileChange}
        />
      </div>

      {files.length === 0 && (
        <p className="text-sm text-slate-400">등록된 이미지가 없습니다.</p>
      )}

      <div className="flex flex-wrap gap-3">
        {files.map((file, i) => (
          <div key={i} className="relative">
            {i === 0 && (
              <span className="absolute left-1 top-1 rounded bg-blue-600 px-1 py-0.5 text-xs text-white">
                대표
              </span>
            )}
            <img
              src={URL.createObjectURL(file)}
              alt={file.name}
              className="h-24 w-24 rounded border border-slate-200 object-cover"
            />
            <button
              type="button"
              onClick={() => removeFile(i)}
              className="absolute right-1 top-1 rounded-full bg-white px-1 text-xs text-slate-500 shadow hover:text-red-500"
            >
              ✕
            </button>
          </div>
        ))}
      </div>
    </div>
  )
}

export default GoodsImageInput
```

---

## Task 8: Frontend - create/page.tsx 수정 (옵션 토글 + imgFiles state)

**Files:**
- Modify: `frontend/app/admin/(main)/goods/create/page.tsx`

- [ ] **Step 1: `imgUrls` → `imgFiles`, `optEnabled` state 추가 및 UI 반영**

변경 포인트:
1. `imgUrls: string[]` → `imgFiles: File[]`
2. `optEnabled: boolean` state 추가 (기본 `false`)
3. `optEnabled=false` 시 `selectedOptGrpCds`, `selectedOptItms`, `units` 초기화
4. submit 시 `createGoods({ data, images: imgFiles })`
5. "옵션 설정" 섹션에 라디오버튼 추가
6. `GoodsImageInput` props 변경 (`imgUrls`→`files`, `onChange` 유지)

```typescript
'use client'

import { useState, useEffect, useMemo } from 'react'
import { useRouter } from 'next/navigation'
import { useForm } from 'react-hook-form'
import { useCategoryList } from '@/hooks/goods/useCategoryList'
import { useBrandList } from '@/hooks/goods/useBrandList'
import { useDlvPolicyList } from '@/hooks/goods/useDlvPolicyList'
import { useOptGroupList } from '@/hooks/goods/useOptGroupList'
import { useGoodsCreate } from '@/hooks/goods/useGoodsCreate'
import { useCodeList } from '@/hooks/system/useCodeList'
import GoodsBasicInfoForm from '@/components/goods/GoodsBasicInfoForm'
import GoodsImageInput from '@/components/goods/GoodsImageInput'
import GoodsOptionSelector from '@/components/goods/GoodsOptionSelector'
import GoodsUnitTable from '@/components/goods/GoodsUnitTable'
import { CODE_GROUP } from '@/types/system.types'
import type { CreateGoodsRequest, GoodsCreateFormValues, OptGrpResponse, UnitRequest } from '@/types/goods.types'

const SALE_END_DTM_DEFAULT = '2999-12-31T00:00'
const EMPTY_OPT_GROUPS: OptGrpResponse[] = []

const toDatetimeLocal = (d: Date) => {
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`
}

const buildUnits = (
  selectedCds: string[],
  optGroups: OptGrpResponse[],
  selectedItms: Record<string, string[]>,
): UnitRequest[] => {
  const selectedGroups = optGroups
    .filter((g) => selectedCds.includes(g.optGrpCd))
    .map((g) => ({
      ...g,
      items: g.items.filter((i) => (selectedItms[g.optGrpCd] ?? []).includes(i.optItmCd)),
    }))
    .filter((g) => g.items.length > 0)

  if (selectedGroups.length === 0) return []

  const cartesian = (groups: typeof selectedGroups): UnitRequest[] => {
    const [first, ...rest] = groups
    const base: UnitRequest[] = first.items.map((item) => ({
      optItms: [{ optGrpCd: first.optGrpCd, optItmCd: item.optItmCd }],
      addPrc: 0,
      stockQty: 0,
    }))
    if (rest.length === 0) return base
    return base.flatMap((unit) =>
      rest[0].items.map((item) => ({
        optItms: [...unit.optItms, { optGrpCd: rest[0].optGrpCd, optItmCd: item.optItmCd }],
        addPrc: 0,
        stockQty: 0,
      }))
    )
  }

  return cartesian(selectedGroups)
}

const calcMrgnRate = (salePrc: number, suplyPrc: number): number | null => {
  if (!suplyPrc || !salePrc || salePrc <= 0) return null
  return Math.round(((salePrc - suplyPrc) / salePrc) * 10000) / 100
}

export default function GoodsCreatePage() {
  const router = useRouter()

  const { data: categories = [] } = useCategoryList()
  const { data: brands = [] } = useBrandList()
  const { data: dlvPolicies = [] } = useDlvPolicyList()
  const { data: optGroups = EMPTY_OPT_GROUPS } = useOptGroupList()
  const { data: goodsTypeCodes = [] } = useCodeList(CODE_GROUP.GOODS_TP)
  const { data: saleStatCodes = [] } = useCodeList(CODE_GROUP.SALE_STAT)

  const { mutate: createGoods, isPending, error } = useGoodsCreate()

  const { register, handleSubmit, control, watch, formState: { errors } } = useForm<GoodsCreateFormValues>({
    defaultValues: {
      goodsNm: '',
      goodsTpCd: 'NORMAL',
      ctgNo: null,
      brandNo: null,
      salePrc: 0,
      normPrc: 0,
      suplyPrc: 0,
      goodsDesc: '',
      saleStartDtm: toDatetimeLocal(new Date()),
      saleEndDtm: SALE_END_DTM_DEFAULT,
      saleStatCd: 'SALE',
      dlvPolicyNo: '',
    },
  })

  const salePrc = watch('salePrc')
  const suplyPrc = watch('suplyPrc')
  const mrgnRate = useMemo(() => calcMrgnRate(salePrc, suplyPrc), [salePrc, suplyPrc])

  const [imgFiles, setImgFiles] = useState<File[]>([])
  const [optEnabled, setOptEnabled] = useState(false)
  const [selectedOptGrpCds, setSelectedOptGrpCds] = useState<string[]>([])
  const [selectedOptItms, setSelectedOptItms] = useState<Record<string, string[]>>({})
  const [units, setUnits] = useState<UnitRequest[]>([])

  useEffect(() => {
    if (!optEnabled) {
      setSelectedOptGrpCds([])
      setSelectedOptItms({})
      setUnits([])
      return
    }
    setUnits(buildUnits(selectedOptGrpCds, optGroups, selectedOptItms))
  }, [optEnabled, selectedOptGrpCds, optGroups, selectedOptItms])

  const handleGrpChange = (cds: string[]) => {
    const added = cds.find((cd) => !selectedOptGrpCds.includes(cd))
    const removed = selectedOptGrpCds.find((cd) => !cds.includes(cd))

    if (added) {
      const grp = optGroups.find((g) => g.optGrpCd === added)
      setSelectedOptItms((prev) => ({
        ...prev,
        [added]: grp ? grp.items.map((i) => i.optItmCd) : [],
      }))
    }
    if (removed) {
      setSelectedOptItms((prev) => {
        const next = { ...prev }
        delete next[removed]
        return next
      })
    }
    setSelectedOptGrpCds(cds)
  }

  const handleItmChange = (grpCd: string, itmCds: string[]) => {
    setSelectedOptItms((prev) => ({ ...prev, [grpCd]: itmCds }))
  }

  const onSubmit = (formValues: GoodsCreateFormValues) => {
    if (!formValues.ctgNo || !formValues.dlvPolicyNo) return

    const request: CreateGoodsRequest = {
      goodsNm: formValues.goodsNm,
      goodsTpCd: formValues.goodsTpCd,
      ctgNo: formValues.ctgNo,
      brandNo: formValues.brandNo ?? undefined,
      salePrc: formValues.salePrc,
      normPrc: formValues.normPrc || undefined,
      suplyPrc: formValues.suplyPrc || undefined,
      goodsDesc: formValues.goodsDesc || undefined,
      saleStartDtm: formValues.saleStartDtm || undefined,
      saleEndDtm: formValues.saleEndDtm || undefined,
      saleStatCd: formValues.saleStatCd,
      dlvPolicyNo: formValues.dlvPolicyNo,
      optGrpCds: selectedOptGrpCds,
      units,
    }

    createGoods({ data: request, images: imgFiles }, {
      onSuccess: () => router.push('/admin/goods'),
    })
  }

  const errorMessage = error instanceof Error ? error.message : null

  return (
    <div className="mx-auto max-w-4xl">
      <div className="mb-6 flex items-center justify-between">
        <h1 className="text-lg font-semibold text-slate-800">상품 등록</h1>
        <button
          type="button"
          onClick={() => router.back()}
          className="text-sm text-slate-500 hover:text-slate-700"
        >
          ← 뒤로
        </button>
      </div>

      {errorMessage && (
        <div className="mb-4 rounded border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-600">
          {errorMessage}
        </div>
      )}

      <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-6">
        <section className="rounded-lg border border-slate-200 bg-white p-6">
          <h2 className="mb-4 text-sm font-semibold text-slate-700">기본 정보</h2>
          <GoodsBasicInfoForm
            control={control}
            register={register}
            errors={errors}
            mrgnRate={mrgnRate}
            categories={categories}
            brands={brands}
            dlvPolicies={dlvPolicies}
            goodsTypeCodes={goodsTypeCodes}
            saleStatCodes={saleStatCodes}
          />
        </section>

        <section className="rounded-lg border border-slate-200 bg-white p-6">
          <h2 className="mb-4 text-sm font-semibold text-slate-700">상품 이미지</h2>
          <GoodsImageInput files={imgFiles} onChange={setImgFiles} />
        </section>

        <section className="rounded-lg border border-slate-200 bg-white p-6">
          <div className="mb-4 flex items-center justify-between">
            <h2 className="text-sm font-semibold text-slate-700">옵션 설정</h2>
            <div className="flex gap-4 text-sm">
              <label className="flex cursor-pointer items-center gap-1.5">
                <input
                  type="radio"
                  name="optEnabled"
                  checked={!optEnabled}
                  onChange={() => setOptEnabled(false)}
                  className="accent-blue-600"
                />
                옵션 없음
              </label>
              <label className="flex cursor-pointer items-center gap-1.5">
                <input
                  type="radio"
                  name="optEnabled"
                  checked={optEnabled}
                  onChange={() => setOptEnabled(true)}
                  className="accent-blue-600"
                />
                옵션 사용
              </label>
            </div>
          </div>

          {optEnabled && (
            <GoodsOptionSelector
              optGroups={optGroups}
              selectedCds={selectedOptGrpCds}
              selectedItms={selectedOptItms}
              onChange={handleGrpChange}
              onItemChange={handleItmChange}
            />
          )}
        </section>

        {optEnabled && (
          <section className="rounded-lg border border-slate-200 bg-white p-6">
            <h2 className="mb-4 text-sm font-semibold text-slate-700">단품 (재고 / 추가금액)</h2>
            <GoodsUnitTable units={units} optGroups={optGroups} onChange={setUnits} />
          </section>
        )}

        <div className="flex justify-end gap-3 pb-8">
          <button
            type="button"
            onClick={() => router.back()}
            className="rounded border border-slate-300 px-6 py-2.5 text-sm text-slate-600 hover:bg-slate-50"
          >
            취소
          </button>
          <button
            type="submit"
            disabled={isPending}
            className="rounded bg-blue-600 px-6 py-2.5 text-sm text-white hover:bg-blue-700 disabled:opacity-50"
          >
            {isPending ? '등록 중...' : '상품 등록'}
          </button>
        </div>
      </form>
    </div>
  )
}
```

- [ ] **Step 2: 타입 체크**

```bash
cd frontend && pnpm tsc --noEmit 2>&1 | head -30
```

Expected: 에러 없음

- [ ] **Step 3: 커밋**

```bash
git add frontend/types/goods.types.ts \
        frontend/api/goods.api.ts \
        frontend/hooks/goods/useGoodsCreate.ts \
        frontend/components/goods/GoodsImageInput.tsx \
        frontend/app/admin/(main)/goods/create/page.tsx
git commit -m "feat: 상품 등록 - 옵션 토글 + 이미지 파일 업로드 UI"
```

---

## Task 9: 통합 확인

- [ ] **Step 1: 백엔드 기동**

```bash
cd backend && ./gradlew bootRun
```

- [ ] **Step 2: 프론트엔드 기동**

```bash
cd frontend && pnpm dev
```

- [ ] **Step 3: 브라우저에서 수동 검증**

`http://localhost:3000/admin/goods/create` 접속:

1. **옵션 없음 (기본)**: 옵션 섹션에 GoodsOptionSelector 미노출, 단품 섹션 미노출
2. **옵션 사용 클릭**: GoodsOptionSelector + 단품 테이블 노출
3. **옵션 사용 → 옵션 없음 전환**: 선택 상태 초기화 확인
4. **이미지 추가**: 파일 선택 후 썸네일 미리보기 확인, 첫 번째 이미지에 "대표" 뱃지 확인
5. **상품 등록 제출**: Supabase Storage에 이미지 업로드 확인 (대시보드 Storage 탭)
6. **DB 확인**: `PR_GOODS_IMG` 테이블에 이미지 URL 저장 확인
