---
name: backend-layer
description: "vibe-myself Spring Boot 백엔드 레이어(Entity/DTO/Mapper/Service/Controller)를 생성하는 스킬. 'API 만들어줘', '백엔드 레이어 추가', 'Controller 추가', 'Service 구현', 'Mapper 작성', '목록 API', '상세 API', '등록 API', '수정 API', '삭제 API' 요청 시 반드시 이 스킬을 사용한다."
---

# Backend Layer Skill

vibe-myself coding-guide 기준으로 Spring Boot 레이어를 생성한다.

## 사전 확인

1. `docs/backend/coding-guide.md` Read
2. `docs/backend/structure.md` Read
3. 대상 모듈의 기존 파일 탐색 (충돌 방지)

## 레이어별 생성 가이드

### Entity (`entity/`)

```java
// 테이블명의 PascalCase가 클래스명
@Getter @Setter
@NoArgsConstructor
public class {TablePascalCase} extends CommonEntity {
    // PK
    // 업무 컬럼 (DB 컬럼명과 동일 — MyBatis의 camelCase 매핑 활용)
}
```
- `CommonEntity`에 공통 컬럼(REG_DTM 등)이 있으면 상속, 없으면 직접 선언

### DTO (`dto/{module}/`)

Request:
```java
public record {Action}{Module}Request(
    @NotBlank String fieldName,
    ...
) {}
```

Response:
```java
@Getter @Builder
public class {Module}{Detail}Response {
    private final String fieldName;
    ...

    public static {Module}{Detail}Response from({Entity} entity) { ... }
}
```

### Mapper (`mapper/{module}/`)

```java
@Mapper
public interface {Module}Mapper {
    List<{Response}> select{Module}List({Request} request);
    {Response}       select{Module}ById(Long id);
    int              insert{Module}({Entity} entity);
    int              update{Module}Status({Command} command);
    int              delete{Module}(Long id);
}
```

### Mapper XML (`resources/mapper/{module}/{Module}Mapper.xml`)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
    "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.vibemyself.mapper.{module}.{Module}Mapper">

    <select id="select{Module}List" resultType="...">
        SELECT ...
        FROM {TABLE}
        <where>
            <if test="keyword != null and keyword != ''">
                AND {COL} LIKE CONCAT('%', #{keyword}, '%')
            </if>
        </where>
        LIMIT #{size} OFFSET #{offset}
    </select>

</mapper>
```

### Service (`service/{module}/`)

```java
@Service
@RequiredArgsConstructor
public class {Module}{Action}Service {

    private final {Module}Mapper {module}Mapper;

    @Transactional(readOnly = true)
    public PageResponse<{Response}> get{Module}List({Request} request) {
        // Early Return
        // 정상 흐름
    }
}
```

조회 전용: `@Transactional(readOnly = true)`
변경: `@Transactional`

### Controller (`controller/{module}/`)

```java
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/{module}")   // 어드민: /api/admin/{module}, 쇼핑몰: /api/{module}
public class {Module}Controller {

    private final {Module}{Action}Service service;

    @GetMapping
    public ApiResponse<PageResponse<{Response}>> list(@ModelAttribute {Request} request) {
        return ApiResponse.ok(service.get{Module}List(request));
    }
}
```

### 단위 테스트

```java
@ExtendWith(MockitoExtension.class)
class {Module}{Action}ServiceTest {

    @InjectMocks private {Module}{Action}Service service;
    @Mock        private {Module}Mapper mapper;

    @Test
    void test_{scenario}() {
        // given
        // when
        // then
    }
}
```

## 출력 산출물

생성한 모든 파일 경로와 API 스펙을 `_workspace/03_backend_spec.md`에 기록:
- 생성 파일 목록 (절대 경로)
- API 목록: `{HTTP} {URL}` + Request/Response DTO 필드 목록
- frontend-scaffolder가 타입 정의 시 참조할 수 있도록 Response 필드명과 타입을 명시