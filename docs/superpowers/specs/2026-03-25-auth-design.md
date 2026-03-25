# Auth 설계 문서 (관리자 + 고객 로그인)

**작성일:** 2026-03-25
**범위:** member 모듈 (고객 로그인), system 모듈 (관리자 로그인), common 공통 JWT/Redis 인프라

---

## 1. 개요

관리자 로그인과 고객 로그인을 각 모듈에서 별도 구현하되, JWT 발급/검증 및 Redis 처리 로직은 `common` 패키지에서 공유한다.

- **Access Token:** 1시간, HttpOnly Cookie
- **Refresh Token:** 14일, HttpOnly Cookie + Redis 저장, **갱신 시 Rotation**
- **세션 캐시:** 로그인 사용자 상세 정보를 Redis에 캐싱 (TTL 1시간). 만료 시 JwtAuthFilter에서 DB 재조회 후 재저장
- **단일 세션 정책:** 동일 계정으로 새 로그인 시 이전 refresh token이 덮어써짐 (다중 기기 동시 로그인 미지원)
- **모든 API에서 `@AuthenticationPrincipal LoginUser`로 로그인 유저 정보(등급/역할 포함) 접근 가능**
- **Spring Security:** `SessionCreationPolicy.STATELESS` (HttpSession 미사용)

---

## 2. 패키지 구조

> `common/`은 인프라 도구 패키지로 모듈이 아님. `docs/backend/structure.md` 에 `common/` 항목 추가 필요.
> `system` 모듈 추가로 `structure.md`의 각 레이어(controller/system, service/system 등) 업데이트 필요.

```
com.vibemyself/
├── common/
│   ├── jwt/
│   │   ├── JwtProvider.java          # 토큰 생성/검증/파싱
│   │   └── JwtAuthFilter.java        # Security filter (Cookie → SecurityContextHolder)
│   ├── redis/
│   │   └── RedisService.java         # refresh token / session 저장·조회·삭제
│   └── security/
│       └── LoginUser.java            # 공통 Principal 객체
│
├── config/
│   └── SecurityConfig.java           # JwtAuthFilter 등록, URL 권한 설정, STATELESS (수정)
│
├── controller/
│   ├── member/
│   │   └── MemberAuthController.java
│   └── system/
│       └── AdminAuthController.java
├── service/
│   ├── member/
│   │   └── MemberAuthService.java
│   └── system/
│       └── AdminAuthService.java
├── mapper/
│   ├── member/
│   │   └── MemberMapper.java
│   └── system/
│       └── AdminMapper.java
├── model/
│   ├── member/
│   │   └── Member.java
│   └── system/
│       └── Admin.java
└── dto/
    ├── member/
    │   └── LoginMemberRequest.java   # {Action}{Module}Request 컨벤션
    └── system/
        └── LoginAdminRequest.java   # 모듈명은 system이지만 DTO 대상이 '관리자'이므로 Admin 사용. 예외 문서화.
```

**LoginUser (공통 Principal):**
```java
// common/security/LoginUser.java
public class LoginUser {
    private String id;       // member: memberId(Long→String), admin: loginId(String)
    private String loginId;  // 로그인 아이디
    private String name;     // 이름
    private String type;     // "member" | "admin"
    private String role;     // "ROLE_USER" | "ROLE_ADMIN" | "ROLE_SUPER"
    private String grade;    // 고객 등급 (admin은 null)
}
```

> Admin의 PK는 `LOGIN_ID VARCHAR(30)` (자연키). Member는 `BIGINT GENERATED ALWAYS AS IDENTITY`.
> `LoginUser.id`는 String으로 통일하여 두 타입 모두 수용.

---

## 3. API 엔드포인트

| 모듈 | 메서드 | URL | 설명 | 인증 필요 |
|------|--------|-----|------|-----------|
| member | POST | `/api/member/login` | 고객 로그인 | X |
| member | POST | `/api/member/logout` | 고객 로그아웃 | △ (만료 토큰 허용) |
| member | POST | `/api/member/token/refresh` | Access Token 갱신 | X (Refresh Cookie) |
| system | POST | `/api/admin/system/login` | 관리자 로그인 | X |
| system | POST | `/api/admin/system/logout` | 관리자 로그아웃 | △ (만료 토큰 허용) |
| system | POST | `/api/admin/system/token/refresh` | Access Token 갱신 | X (Refresh Cookie) |

**응답:**
- 로그인/갱신: `ApiResponse<Void>` (토큰은 Set-Cookie 헤더로 전달)
- 로그아웃: `ApiResponse<Void>`

**응답 Cookie:**

| 쿠키명 | 값 | 옵션 |
|--------|-----|------|
| `access_token` | Access JWT | HttpOnly, Secure, SameSite=Lax, Max-Age=3600 |
| `refresh_token` | Refresh JWT | HttpOnly, Secure, SameSite=Lax, Max-Age=1209600 |

> **SameSite 전략:** 로컬 개발 환경에서 프론트엔드(3000)의 모든 `/api/*` 요청은 `next.config.ts` rewrites를 통해 백엔드(8080)로 프록시됨. 브라우저는 `localhost:3000`만 보므로 동일 출처. `SameSite=Lax`로 충분하며 `Secure`는 로컬에서 `false`, 프로덕션에서 `true`로 환경별 분기.
> **프로덕션:** 동일 도메인 배포 시 `SameSite=Lax` 유지.

---

## 4. Redis 구조

| Key | 값 | TTL |
|-----|----|-----|
| `refresh:member:{memberId}` | refresh token 문자열 | 14일 |
| `refresh:admin:{loginId}` | refresh token 문자열 | 14일 |
| `session:member:{memberId}` | JSON (id, loginId, name, type, role, grade) | 1시간 |
| `session:admin:{loginId}` | JSON (id, loginId, name, type, role) | 1시간 |

**세션 JSON 예시:**
```json
// member
{ "id": "1", "loginId": "user@email.com", "name": "홍길동", "type": "member", "role": "ROLE_USER", "grade": "GOLD" }

// admin
{ "id": "admin01", "loginId": "admin01", "name": "관리자1", "type": "admin", "role": "ROLE_SUPER", "grade": null }
```

> Jackson ObjectMapper로 직렬화/역직렬화. `null` 필드 포함하여 저장.

---

## 5. 데이터 흐름

### 로그인
```
Client → POST /login (id/password)
  → AuthController → AuthService
  → DB 사용자 조회 + BCrypt 비밀번호 검증
  → 실패 시 401 반환
  → JwtProvider.generateAccessToken() + generateRefreshToken()
  → RedisService.save(refresh:type:id, refreshToken, TTL 14일)  # 기존 덮어쓰기 (단일 세션)
  → RedisService.save(session:type:id, userInfo JSON, TTL 1시간)
  → Response: Set-Cookie (access_token, refresh_token), ApiResponse<Void>
```

### 매 요청 (JwtAuthFilter)
```
요청
→ JwtAuthFilter
  → access_token Cookie 없음 → 인증 없이 통과 (SecurityContext 비어있음)
  → access_token Cookie 있음
    → JwtProvider.validate(accessToken)
    → 유효하지 않으면 → 401 반환
    → 만료 & 로그아웃 엔드포인트 → 예외적으로 통과 (아래 로그아웃 참조)
    → userId, role, type 추출
    → RedisService.get(session:type:id) 조회
    → 없으면 → AuthService.loadUser(type, id)로 DB 조회 후 Redis 재저장
    → LoginUser 생성 → SecurityContextHolder 세팅
→ 컨트롤러 @AuthenticationPrincipal LoginUser로 접근
```

> **DB 조회 책임:** JwtAuthFilter는 `MemberAuthService`, `AdminAuthService`를 주입받아 type에 따라 분기 호출. Filter가 Mapper를 직접 참조하지 않음.
> **loadUser 계약:** 두 서비스 모두 `LoginUser loadUser(String id)` 메서드를 구현. DB에 존재하지 않거나 비활성화된 계정이면 `null` 반환 → Filter는 401 처리.
> **Filter 등록:** `SecurityConfig`에서 `JwtAuthFilter`를 `@Bean`으로 명시 선언 후 `addFilterBefore(jwtAuthFilter(), UsernamePasswordAuthenticationFilter.class)` 방식으로 등록. `@Component` 미사용 (Spring Security 이중 등록 방지).

### Access Token 갱신 (Refresh Token Rotation)
```
Client → POST /token/refresh (refresh_token Cookie)
  → AuthService
  → JwtProvider.validate(refreshToken) → userId, type 추출
  → RedisService.get(refresh:type:id) → Cookie값과 일치 확인
  → 불일치 시 401 반환
  → 새 accessToken + 새 refreshToken 생성 (Rotation)
  → RedisService.save(refresh:type:id, newRefreshToken, TTL 14일)  # 기존 교체
  → RedisService.save(session:type:id, userInfo, TTL 1시간)  # TTL 갱신
  → Response: Set-Cookie (access_token, refresh_token 모두 갱신), ApiResponse<Void>
```

### 로그아웃
```
Client → POST /logout (access_token Cookie, 만료되어도 허용)
  → JwtAuthFilter: 로그아웃 URL은 만료된 토큰도 파싱 허용 (서명만 검증)
  → AuthService
  → JwtProvider에서 userId, type 추출
  → RedisService.delete(refresh:type:id)
  → RedisService.delete(session:type:id)
  → Response: Set-Cookie (access_token Max-Age=0, refresh_token Max-Age=0), ApiResponse<Void>
```

> access_token이 아예 없거나 서명 자체가 유효하지 않은 경우 → 401.
> 만료만 된 경우 → 서명 검증 후 정상 로그아웃 처리.

---

## 6. JWT 클레임 구조

```json
{
  "sub": "1",
  "role": "ROLE_USER",
  "type": "member",
  "iat": 1234567890,
  "exp": 1234571490
}
```

**JWT 서명 설정:**
- 알고리즘: HS256
- Secret: `application-local.yml`의 `jwt.secret` 프로퍼티로 관리 (git 제외). 환경변수 또는 비밀 관리 도구로 주입. 최소 256bit(32자 이상) 권장
- `JwtProvider`는 `@Value("${jwt.secret}")`, `@Value("${jwt.access-expiration}")`, `@Value("${jwt.refresh-expiration}")` 주입

---

## 7. Security URL 권한 설정

```
공개 (permitAll) - JwtAuthFilter 검증 없이 통과
  POST /api/member/login
  POST /api/member/logout        # 만료 토큰도 허용, filter에서 별도 처리
  POST /api/member/token/refresh
  POST /api/admin/system/login
  POST /api/admin/system/logout  # 만료 토큰도 허용, filter에서 별도 처리
  POST /api/admin/system/token/refresh

고객 인증 필요 (ROLE_USER)
  /api/member/** (login, refresh 제외)
  /api/cart/**
  /api/order/**

관리자 인증 필요 (ROLE_ADMIN 또는 ROLE_SUPER)
  /api/admin/** (system/login, system/token/refresh 제외)

슈퍼 관리자 전용 (ROLE_SUPER)
  현재 단계에서 별도 지정 없음. 필요 시 API별 @PreAuthorize로 처리.
```

> `ROLE_SUPER`는 URL 레벨 제한 없이 `ROLE_ADMIN` 범위 전체 접근 가능.
> 특정 민감 API(관리자 등록/삭제 등)에 `@PreAuthorize("hasRole('SUPER')")` 적용 예정.

---

## 8. 세션 캐시 일관성 트레이드오프

세션 캐시 TTL은 1시간. DB에서 사용자 정보(등급 등)가 변경되어도 캐시 만료 전까지 최대 1시간 동안 이전 값이 사용됨. 허용된 트레이드오프로 명시적으로 수용. 즉시 반영이 필요한 경우 해당 업데이트 로직에서 `RedisService.delete(session:...)` 직접 호출.

---

## 9. 문서 업데이트 필요 사항 (구현 시 함께 처리)

| 문서 | 변경 내용 |
|------|-----------|
| `docs/modules.md` | `system` 모듈 추가 (관리자 계정 관리, 공통코드 관리) |
| `docs/backend/structure.md` | `common/` 패키지 설명 추가, `system` 서브패키지 각 레이어에 추가 |
| `backend/src/main/resources/application.yml` | DB 비밀번호 제거 → `application-local.yml`로 이동 (현재 평문 노출 상태) |
| `backend/src/main/resources/application-local.yml` | DB 설정 + `jwt.secret`, `jwt.access-expiration`, `jwt.refresh-expiration` 추가 |
