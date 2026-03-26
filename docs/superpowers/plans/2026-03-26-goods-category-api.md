# 카테고리 API (어드민) Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 어드민용 카테고리 조회/등록/수정 REST API 구현 (최대 3단계 계층, `PR_CTG_BASE` 테이블)

**Architecture:** MyBatis로 전체 카테고리를 flat 조회 후 Java 재귀(`buildTree`)로 트리 조립. 등록 시 부모 레벨 검증으로 3단계 초과 차단. 수정은 `CTG_NM`, `USE_YN`, `SORT_ORD`만 허용.

**Tech Stack:** Java 21, Spring Boot 3, MyBatis, PostgreSQL, JUnit 5, Mockito, MockMvc

**Spec:** `docs/superpowers/specs/2026-03-26-goods-category-api-design.md`

---

## 파일 목록

| 역할 | 경로 |
|------|------|
| Exception | `backend/src/main/java/com/vibemyself/global/exception/CategoryNotFoundException.java` |
| Entity | `backend/src/main/java/com/vibemyself/entity/PrCtgBase.java` |
| DTO (GET 응답) | `backend/src/main/java/com/vibemyself/dto/goods/CategoryResponse.java` |
| DTO (POST 요청) | `backend/src/main/java/com/vibemyself/dto/goods/CreateCategoryRequest.java` |
| DTO (POST 응답) | `backend/src/main/java/com/vibemyself/dto/goods/CreateCategoryResponse.java` |
| DTO (PUT 요청) | `backend/src/main/java/com/vibemyself/dto/goods/UpdateCategoryRequest.java` |
| Mapper | `backend/src/main/java/com/vibemyself/mapper/goods/CategoryMapper.java` |
| Mapper XML | `backend/src/main/resources/mapper/goods/CategoryMapper.xml` |
| Service | `backend/src/main/java/com/vibemyself/service/goods/CategoryService.java` |
| Controller | `backend/src/main/java/com/vibemyself/controller/goods/CategoryController.java` |
| Service Test | `backend/src/test/java/com/vibemyself/service/goods/CategoryServiceTest.java` |
| Controller Test | `backend/src/test/java/com/vibemyself/controller/goods/CategoryControllerTest.java` |

---

## Chunk 1: 도메인·DTO·예외·Mapper

### Task 1: Exception — CategoryNotFoundException

**Files:**
- Create: `backend/src/main/java/com/vibemyself/global/exception/CategoryNotFoundException.java`

- [ ] **Step 1: 파일 생성**

```java
package com.vibemyself.global.exception;

public class CategoryNotFoundException extends BusinessException {
    public CategoryNotFoundException() {
        super("카테고리를 찾을 수 없습니다.", 404);
    }
}
```

- [ ] **Step 2: 빌드 확인**

```bash
cd backend && ./gradlew compileJava -q
```
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: 커밋**

```bash
git add backend/src/main/java/com/vibemyself/global/exception/CategoryNotFoundException.java
git commit -m "feat: add CategoryNotFoundException"
```

---

### Task 2: Entity — PrCtgBase

**Files:**
- Create: `backend/src/main/java/com/vibemyself/entity/PrCtgBase.java`

- [ ] **Step 1: 파일 생성**

`PR_CTG_BASE` 컬럼과 1:1 매핑. `ctgLvl`은 `String`으로 매핑 (`CHAR(1)` → `"1"/"2"/"3"`).

```java
package com.vibemyself.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrCtgBase {
    @Setter                  // MyBatis useGeneratedKeys 주입을 위해 필요
    private Long ctgNo;
    private Long upCtgNo;
    private String ctgLvl;   // "1", "2", "3"
    private String ctgNm;
    private int sortOrd;
    private String useYn;
}
```

- [ ] **Step 2: 빌드 확인**

```bash
cd backend && ./gradlew compileJava -q
```
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: 커밋**

```bash
git add backend/src/main/java/com/vibemyself/entity/PrCtgBase.java
git commit -m "feat: add PrCtgBase entity"
```

---

### Task 3: DTO 4종

**Files:**
- Create: `backend/src/main/java/com/vibemyself/dto/goods/CategoryResponse.java`
- Create: `backend/src/main/java/com/vibemyself/dto/goods/CreateCategoryRequest.java`
- Create: `backend/src/main/java/com/vibemyself/dto/goods/CreateCategoryResponse.java`
- Create: `backend/src/main/java/com/vibemyself/dto/goods/UpdateCategoryRequest.java`

- [ ] **Step 1: CategoryResponse 생성** — GET 트리 노드용, `children` 포함

```java
package com.vibemyself.dto.goods;

import com.vibemyself.entity.PrCtgBase;
import lombok.Getter;

import java.util.List;

@Getter
public class CategoryResponse {
    private final Long ctgNo;
    private final Long upCtgNo;
    private final String ctgLvl;
    private final String ctgNm;
    private final int sortOrd;
    private final String useYn;
    private final List<CategoryResponse> children;

    private CategoryResponse(PrCtgBase category, List<CategoryResponse> children) {
        this.ctgNo = category.getCtgNo();
        this.upCtgNo = category.getUpCtgNo();
        this.ctgLvl = category.getCtgLvl();
        this.ctgNm = category.getCtgNm();
        this.sortOrd = category.getSortOrd();
        this.useYn = category.getUseYn();
        this.children = children;
    }

    public static CategoryResponse of(PrCtgBase category, List<CategoryResponse> children) {
        return new CategoryResponse(category, children);
    }
}
```

- [ ] **Step 2: CreateCategoryRequest 생성** — POST 요청 바디

```java
package com.vibemyself.dto.goods;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateCategoryRequest {
    private Long upCtgNo;          // null이면 최상위

    @NotBlank(message = "카테고리명은 필수입니다.")
    private String ctgNm;

    private int sortOrd;           // 기본값 0 (int 기본값)
}
```

- [ ] **Step 3: CreateCategoryResponse 생성** — POST 응답용 (`ctgNo`만)

```java
package com.vibemyself.dto.goods;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CreateCategoryResponse {
    private final Long ctgNo;
}
```

- [ ] **Step 4: UpdateCategoryRequest 생성** — PUT 요청 바디

```java
package com.vibemyself.dto.goods;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCategoryRequest {
    @NotBlank(message = "카테고리명은 필수입니다.")
    private String ctgNm;

    @NotNull(message = "사용여부는 필수입니다.")
    @Pattern(regexp = "[YN]", message = "사용여부는 Y 또는 N이어야 합니다.")
    private String useYn;

    @NotNull(message = "정렬순서는 필수입니다.")
    private Integer sortOrd;
}
```

- [ ] **Step 5: 빌드 확인**

```bash
cd backend && ./gradlew compileJava -q
```
Expected: BUILD SUCCESSFUL

- [ ] **Step 6: 커밋**

```bash
git add backend/src/main/java/com/vibemyself/dto/goods/
git commit -m "feat: add category DTOs"
```

---

### Task 4: Mapper 인터페이스 + XML

**Files:**
- Create: `backend/src/main/java/com/vibemyself/mapper/goods/CategoryMapper.java`
- Create: `backend/src/main/resources/mapper/goods/CategoryMapper.xml`

- [ ] **Step 1: CategoryMapper 인터페이스 생성**

```java
package com.vibemyself.mapper.goods;

import com.vibemyself.entity.PrCtgBase;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CategoryMapper {
    List<PrCtgBase> selectAll();
    PrCtgBase selectByCtgNo(Long ctgNo);
    void insertCategory(PrCtgBase category);
    void updateCategory(PrCtgBase category);
}
```

- [ ] **Step 2: CategoryMapper.xml 생성**

`resources/mapper/goods/` 디렉토리가 없으면 생성 후 파일 작성.

`selectAll`: 전체 카테고리를 WITH RECURSIVE로 정렬 포함 flat 조회 (USE_YN 필터 없음 — 어드민은 전체 조회)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.vibemyself.mapper.goods.CategoryMapper">

    <resultMap id="categoryResultMap" type="com.vibemyself.entity.PrCtgBase">
        <id     property="ctgNo"    column="CTG_NO"/>
        <result property="upCtgNo"  column="UP_CTG_NO"/>
        <result property="ctgLvl"   column="CTG_LVL"/>
        <result property="ctgNm"    column="CTG_NM"/>
        <result property="sortOrd"  column="SORT_ORD"/>
        <result property="useYn"    column="USE_YN"/>
    </resultMap>

    <select id="selectAll" resultMap="categoryResultMap">
        WITH RECURSIVE ctg_tree AS (
            SELECT CTG_NO, UP_CTG_NO, CTG_LVL, CTG_NM, SORT_ORD, USE_YN
            FROM PR_CTG_BASE
            WHERE UP_CTG_NO IS NULL
            UNION ALL
            SELECT c.CTG_NO, c.UP_CTG_NO, c.CTG_LVL, c.CTG_NM, c.SORT_ORD, c.USE_YN
            FROM PR_CTG_BASE c
            INNER JOIN ctg_tree ct ON c.UP_CTG_NO = ct.CTG_NO
        )
        SELECT CTG_NO, UP_CTG_NO, CTG_LVL, CTG_NM, SORT_ORD, USE_YN
        FROM ctg_tree
        ORDER BY SORT_ORD
    </select>

    <select id="selectByCtgNo" resultMap="categoryResultMap">
        SELECT CTG_NO, UP_CTG_NO, CTG_LVL, CTG_NM, SORT_ORD, USE_YN
        FROM PR_CTG_BASE
        WHERE CTG_NO = #{ctgNo}
    </select>

    <insert id="insertCategory" useGeneratedKeys="true" keyProperty="ctgNo" keyColumn="CTG_NO">
        INSERT INTO PR_CTG_BASE (UP_CTG_NO, CTG_LVL, CTG_NM, SORT_ORD, USE_YN, REG_ID, MOD_ID)
        VALUES (#{upCtgNo}, #{ctgLvl}, #{ctgNm}, #{sortOrd}, 'Y', 'ADMIN', 'ADMIN')
    </insert>

    <update id="updateCategory">
        UPDATE PR_CTG_BASE
        SET CTG_NM  = #{ctgNm},
            USE_YN  = #{useYn},
            SORT_ORD = #{sortOrd},
            MOD_DTM = NOW(),
            MOD_ID  = 'ADMIN'
        WHERE CTG_NO = #{ctgNo}
    </update>

</mapper>
```

> **Note:** `REG_ID`/`MOD_ID`는 현재 로그인 어드민 정보 연동 전까지 `'ADMIN'` 하드코딩 허용. 추후 `@AuthenticationPrincipal`로 교체.

- [ ] **Step 3: 빌드 확인**

```bash
cd backend && ./gradlew compileJava -q
```
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: 커밋**

```bash
git add backend/src/main/java/com/vibemyself/mapper/goods/CategoryMapper.java
git add backend/src/main/resources/mapper/goods/CategoryMapper.xml
git commit -m "feat: add CategoryMapper interface and XML"
```

---

## Chunk 2: Service + 테스트

### Task 5: CategoryService 테스트 작성 (TDD — 먼저 실패)

**Files:**
- Create: `backend/src/test/java/com/vibemyself/service/goods/CategoryServiceTest.java`

- [ ] **Step 1: 테스트 파일 작성**

패턴: `@ExtendWith(MockitoExtension.class)`, `@Mock CategoryMapper`, `@InjectMocks CategoryService`

```java
package com.vibemyself.service.goods;

import com.vibemyself.dto.goods.CreateCategoryRequest;
import com.vibemyself.dto.goods.CategoryResponse;
import com.vibemyself.dto.goods.UpdateCategoryRequest;
import com.vibemyself.global.exception.CategoryNotFoundException;
import com.vibemyself.mapper.goods.CategoryMapper;
import com.vibemyself.entity.PrCtgBase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock CategoryMapper categoryMapper;
    @InjectMocks CategoryService categoryService;

    // ── 조회 ─────────────────────────────────────────────

    @Test
    void getCategoryTree_빈목록_빈리스트반환() {
        given(categoryMapper.selectAll()).willReturn(List.of());

        List<CategoryResponse> result = categoryService.getCategoryTree();

        assertThat(result).isEmpty();
    }

    @Test
    void getCategoryTree_1단계만_루트1개반환() {
        PrCtgBase root = prCtgBase(1L, null, "1", "의류", 1);
        given(categoryMapper.selectAll()).willReturn(List.of(root));

        List<CategoryResponse> result = categoryService.getCategoryTree();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCtgNm()).isEqualTo("의류");
        assertThat(result.get(0).getChildren()).isEmpty();
    }

    @Test
    void getCategoryTree_3단계트리_재귀조립() {
        PrCtgBase lv1 = prCtgBase(1L, null, "1", "의류", 1);
        PrCtgBase lv2 = prCtgBase(11L, 1L, "2", "상의", 1);
        PrCtgBase lv3 = prCtgBase(111L, 11L, "3", "반팔", 1);
        given(categoryMapper.selectAll()).willReturn(List.of(lv1, lv2, lv3));

        List<CategoryResponse> result = categoryService.getCategoryTree();

        assertThat(result).hasSize(1);
        CategoryResponse child = result.get(0).getChildren().get(0);
        assertThat(child.getCtgNm()).isEqualTo("상의");
        assertThat(child.getChildren().get(0).getCtgNm()).isEqualTo("반팔");
    }

    @Test
    void getCategoryTree_sortOrd기준정렬() {
        PrCtgBase a = prCtgBase(1L, null, "1", "신발", 2);
        PrCtgBase b = prCtgBase(2L, null, "1", "의류", 1);
        given(categoryMapper.selectAll()).willReturn(List.of(a, b));

        List<CategoryResponse> result = categoryService.getCategoryTree();

        assertThat(result.get(0).getCtgNm()).isEqualTo("의류");
        assertThat(result.get(1).getCtgNm()).isEqualTo("신발");
    }

    // ── 등록 ─────────────────────────────────────────────

    @Test
    void createCategory_최상위_CTG_LVL1로등록() {
        CreateCategoryRequest request = new CreateCategoryRequest(null, "의류", 1);

        categoryService.createCategory(request);

        then(categoryMapper).should().insertCategory(argThat(c ->
                c.getCtgLvl().equals("1") && c.getUpCtgNo() == null
        ));
    }

    @Test
    void createCategory_부모없음_400예외() {
        CreateCategoryRequest request = new CreateCategoryRequest(999L, "하위", 1);
        given(categoryMapper.selectByCtgNo(999L)).willReturn(null);

        assertThatThrownBy(() -> categoryService.createCategory(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("상위 카테고리");
    }

    @Test
    void createCategory_부모레벨3_400예외() {
        CreateCategoryRequest request = new CreateCategoryRequest(111L, "4단계", 1);
        given(categoryMapper.selectByCtgNo(111L))
                .willReturn(category(111L, 11L, "3", "반팔", 1));

        assertThatThrownBy(() -> categoryService.createCategory(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("3단계");
    }

    @Test
    void createCategory_최상위_생성된ctgNo반환() {
        CreateCategoryRequest request = new CreateCategoryRequest(null, "의류", 1);
        willAnswer(invocation -> {
            PrCtgBase c = invocation.getArgument(0);
            c.setCtgNo(1L);
            return null;
        }).given(categoryMapper).insertCategory(any());

        Long ctgNo = categoryService.createCategory(request);

        assertThat(ctgNo).isEqualTo(1L);
    }

    @Test
    void createCategory_2단계등록_CTG_LVL2로등록() {
        CreateCategoryRequest request = new CreateCategoryRequest(1L, "상의", 1);
        given(categoryMapper.selectByCtgNo(1L))
                .willReturn(category(1L, null, "1", "의류", 1));

        categoryService.createCategory(request);

        then(categoryMapper).should().insertCategory(argThat(c ->
                c.getCtgLvl().equals("2") && c.getUpCtgNo().equals(1L)
        ));
    }

    // ── 수정 ─────────────────────────────────────────────

    @Test
    void updateCategory_존재하지않는카테고리_404예외() {
        given(categoryMapper.selectByCtgNo(999L)).willReturn(null);
        UpdateCategoryRequest request = new UpdateCategoryRequest("수정명", "Y", 1);

        assertThatThrownBy(() -> categoryService.updateCategory(999L, request))
                .isInstanceOf(CategoryNotFoundException.class);
    }

    @Test
    void updateCategory_정상수정_updateMapper호출() {
        given(categoryMapper.selectByCtgNo(1L))
                .willReturn(category(1L, null, "1", "의류", 1));
        UpdateCategoryRequest request = new UpdateCategoryRequest("의류수정", "N", 2);

        categoryService.updateCategory(1L, request);

        then(categoryMapper).should().updateCategory(argThat(c ->
                c.getCtgNo().equals(1L)
                        && c.getCtgNm().equals("의류수정")
                        && c.getUseYn().equals("N")
                        && c.getSortOrd() == 2
        ));
    }

    // ── 헬퍼 ─────────────────────────────────────────────

    private PrCtgBase prCtgBase(Long ctgNo, Long upCtgNo, String ctgLvl, String ctgNm, int sortOrd) {
        return PrCtgBase.builder()
                .ctgNo(ctgNo).upCtgNo(upCtgNo).ctgLvl(ctgLvl)
                .ctgNm(ctgNm).sortOrd(sortOrd).useYn("Y").build();
    }
}
```

- [ ] **Step 2: 테스트 실행 — 반드시 실패 확인**

```bash
cd backend && ./gradlew test --tests "com.vibemyself.service.goods.CategoryServiceTest" 2>&1 | tail -20
```
Expected: FAILED (CategoryService 클래스 없음)

---

### Task 6: CategoryService 구현

**Files:**
- Create: `backend/src/main/java/com/vibemyself/service/goods/CategoryService.java`

- [ ] **Step 1: CategoryService 구현**

```java
package com.vibemyself.service.goods;

import com.vibemyself.dto.goods.CategoryResponse;
import com.vibemyself.dto.goods.CreateCategoryRequest;
import com.vibemyself.dto.goods.UpdateCategoryRequest;
import com.vibemyself.global.exception.CategoryNotFoundException;
import com.vibemyself.mapper.goods.CategoryMapper;
import com.vibemyself.entity.PrCtgBase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryMapper categoryMapper;

    @Transactional(readOnly = true)
    public List<CategoryResponse> getCategoryTree() {
        List<PrCtgBase> all = categoryMapper.selectAll();
        return buildTree(all, null);
    }

    @Transactional
    public Long createCategory(CreateCategoryRequest request) {
        String ctgLvl = resolveLevel(request.getUpCtgNo());
        PrCtgBase category = PrCtgBase.builder()
                .upCtgNo(request.getUpCtgNo())
                .ctgLvl(ctgLvl)
                .ctgNm(request.getCtgNm())
                .sortOrd(request.getSortOrd())
                .build();
        categoryMapper.insertCategory(category);
        return category.getCtgNo();
    }

    @Transactional
    public void updateCategory(Long ctgNo, UpdateCategoryRequest request) {
        if (categoryMapper.selectByCtgNo(ctgNo) == null) {
            throw new CategoryNotFoundException();
        }
        PrCtgBase category = PrCtgBase.builder()
                .ctgNo(ctgNo)
                .ctgNm(request.getCtgNm())
                .useYn(request.getUseYn())
                .sortOrd(request.getSortOrd())
                .build();
        categoryMapper.updateCategory(category);
    }

    private String resolveLevel(Long upCtgNo) {
        if (upCtgNo == null) {
            return "1";
        }
        PrCtgBase parent = categoryMapper.selectByCtgNo(upCtgNo);
        if (parent == null) {
            throw new IllegalArgumentException("상위 카테고리가 존재하지 않습니다.");
        }
        if ("3".equals(parent.getCtgLvl())) {
            throw new IllegalArgumentException("3단계 이하로는 카테고리를 등록할 수 없습니다.");
        }
        return String.valueOf(Integer.parseInt(parent.getCtgLvl()) + 1);
    }

    private List<CategoryResponse> buildTree(List<PrCtgBase> all, Long upCtgNo) {
        return all.stream()
                .filter(c -> Objects.equals(c.getUpCtgNo(), upCtgNo))
                .sorted(Comparator.comparingInt(PrCtgBase::getSortOrd))
                .map(c -> CategoryResponse.of(c, buildTree(all, c.getCtgNo())))
                .toList();
    }
}
```

- [ ] **Step 2: 테스트 실행 — 전체 통과 확인**

```bash
cd backend && ./gradlew test --tests "com.vibemyself.service.goods.CategoryServiceTest" 2>&1 | tail -20
```
Expected: 8 tests, BUILD SUCCESSFUL

- [ ] **Step 3: 커밋**

```bash
git add backend/src/main/java/com/vibemyself/service/goods/CategoryService.java
git add backend/src/test/java/com/vibemyself/service/goods/CategoryServiceTest.java
git commit -m "feat: implement CategoryService with TDD"
```

---

## Chunk 3: Controller + 테스트

### Task 7: CategoryController 테스트 작성 (TDD — 먼저 실패)

**Files:**
- Create: `backend/src/test/java/com/vibemyself/controller/goods/CategoryControllerTest.java`

- [ ] **Step 1: 테스트 파일 작성**

패턴: `@WebMvcTest(CategoryController.class)`, `@AutoConfigureMockMvc(addFilters = false)`, `@MockBean CategoryService`

```java
package com.vibemyself.controller.goods;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vibemyself.dto.goods.CategoryResponse;
import com.vibemyself.dto.goods.CreateCategoryRequest;
import com.vibemyself.dto.goods.UpdateCategoryRequest;
import com.vibemyself.global.exception.CategoryNotFoundException;
import com.vibemyself.entity.PrCtgBase;
import com.vibemyself.service.goods.CategoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryController.class)
@AutoConfigureMockMvc(addFilters = false)
class CategoryControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean CategoryService categoryService;

    @Test
    void getCategories_성공_200() throws Exception {
        PrCtgBase c = PrCtgBase.builder().ctgNo(1L).ctgNm("의류").ctgLvl("1").sortOrd(1).useYn("Y").build();
        given(categoryService.getCategoryTree()).willReturn(List.of(CategoryResponse.of(c, List.of())));

        mockMvc.perform(get("/api/admin/goods/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].ctgNm").value("의류"));
    }

    @Test
    void createCategory_성공_201() throws Exception {
        CreateCategoryRequest request = new CreateCategoryRequest(null, "의류", 1);
        given(categoryService.createCategory(any())).willReturn(1L);

        mockMvc.perform(post("/api/admin/goods/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.ctgNo").value(1));
    }

    @Test
    void createCategory_ctgNm빈값_400() throws Exception {
        CreateCategoryRequest request = new CreateCategoryRequest(null, "", 1);

        mockMvc.perform(post("/api/admin/goods/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createCategory_부모없음_400() throws Exception {
        CreateCategoryRequest request = new CreateCategoryRequest(999L, "하위", 1);
        given(categoryService.createCategory(any()))
                .willThrow(new IllegalArgumentException("상위 카테고리가 존재하지 않습니다."));

        mockMvc.perform(post("/api/admin/goods/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateCategory_성공_200() throws Exception {
        UpdateCategoryRequest request = new UpdateCategoryRequest("의류수정", "N", 2);

        mockMvc.perform(put("/api/admin/goods/categories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(org.hamcrest.Matchers.nullValue()));
    }

    @Test
    void updateCategory_ctgNm빈값_400() throws Exception {
        UpdateCategoryRequest request = new UpdateCategoryRequest("", "Y", 1);

        mockMvc.perform(put("/api/admin/goods/categories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateCategory_존재하지않음_404() throws Exception {
        UpdateCategoryRequest request = new UpdateCategoryRequest("없는카테", "Y", 1);
        willThrow(new CategoryNotFoundException()).given(categoryService).updateCategory(eq(999L), any());

        mockMvc.perform(put("/api/admin/goods/categories/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateCategory_useYn잘못된값_400() throws Exception {
        UpdateCategoryRequest request = new UpdateCategoryRequest("의류", "X", 1);

        mockMvc.perform(put("/api/admin/goods/categories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
```

- [ ] **Step 2: 테스트 실행 — 반드시 실패 확인**

```bash
cd backend && ./gradlew test --tests "com.vibemyself.controller.goods.CategoryControllerTest" 2>&1 | tail -20
```
Expected: FAILED (CategoryController 없음)

---

### Task 8: CategoryController 구현

**Files:**
- Create: `backend/src/main/java/com/vibemyself/controller/goods/CategoryController.java`

- [ ] **Step 1: GlobalExceptionHandler에 IllegalArgumentException 핸들러 추가 (필수 선행)**

> ⚠️ 이 단계를 건너뛰면 `createCategory_부모없음_400` 테스트가 500을 반환해 실패한다.

`backend/src/main/java/com/vibemyself/global/GlobalExceptionHandler.java` 에 추가:

```java
@ExceptionHandler(IllegalArgumentException.class)
public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException e) {
    return ResponseEntity.badRequest().body(ApiResponse.fail(e.getMessage()));
}
```

- [ ] **Step 3: CategoryController 구현**

```java
package com.vibemyself.controller.goods;

import com.vibemyself.common.response.ApiResponse;
import com.vibemyself.dto.goods.CategoryResponse;
import com.vibemyself.dto.goods.CreateCategoryRequest;
import com.vibemyself.dto.goods.CreateCategoryResponse;
import com.vibemyself.dto.goods.UpdateCategoryRequest;
import com.vibemyself.service.goods.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/goods/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getCategories() {
        return ResponseEntity.ok(ApiResponse.ok(categoryService.getCategoryTree()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CreateCategoryResponse>> createCategory(
            @Valid @RequestBody CreateCategoryRequest request) {
        Long ctgNo = categoryService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(new CreateCategoryResponse(ctgNo)));
    }

    @PutMapping("/{ctgNo}")
    public ResponseEntity<ApiResponse<Void>> updateCategory(
            @PathVariable Long ctgNo,
            @Valid @RequestBody UpdateCategoryRequest request) {
        categoryService.updateCategory(ctgNo, request);
        return ResponseEntity.ok(ApiResponse.ok());
    }
}
```

- [ ] **Step 4: 테스트 실행 — 전체 통과 확인**

```bash
cd backend && ./gradlew test --tests "com.vibemyself.controller.goods.CategoryControllerTest" 2>&1 | tail -20
```
Expected: 8 tests, BUILD SUCCESSFUL

- [ ] **Step 5: 전체 테스트 통과 확인**

```bash
cd backend && ./gradlew test 2>&1 | tail -20
```
Expected: BUILD SUCCESSFUL, 0 failures

- [ ] **Step 6: 커밋**

```bash
git add backend/src/main/java/com/vibemyself/controller/goods/CategoryController.java
git add backend/src/main/java/com/vibemyself/global/GlobalExceptionHandler.java
git add backend/src/test/java/com/vibemyself/controller/goods/CategoryControllerTest.java
git commit -m "feat: implement CategoryController with TDD"
```
