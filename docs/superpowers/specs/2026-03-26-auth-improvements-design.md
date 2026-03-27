# Auth Improvements Design

**Date:** 2026-03-26
**Scope:** Frontend middleware 활성화 + JWT 서명 검증, Backend Refresh Token 로테이션 확인

---

## 배경

현재 인증 구현에 두 가지 문제가 있다.

1. `proxy.ts`가 `middleware.ts`로 명명되지 않아 Next.js 미들웨어로 동작하지 않음 — 라우트 가드 무효 상태
2. `lib/authGuard.ts`에서 JWT 페이로드를 `atob()`으로 디코딩해 서명 검증 없이 신뢰

(Refresh Token 로테이션은 백엔드에 이미 구현되어 있음 — 변경 불필요)

---

## 변경 범위

### Frontend (변경 필요)

| 파일 | 변경 |
|------|------|
| `proxy.ts` → `middleware.ts` | 파일명 변경 (Next.js 미들웨어 활성화) |
| `lib/authGuard.ts` | `atob()` 제거 → `jose` `jwtVerify()`로 서명 검증 |
| `frontend/.env.local` | `JWT_SECRET` 환경변수 추가 |
| `package.json` | `jose` 패키지 추가 |

**미들웨어 검증 흐름:**

```
요청 수신 (/admin/* 또는 보호 경로)
  → 쿠키에서 access_token 추출
  → jose.jwtVerify(token, secretKey, { algorithms: ['HS256'] })
  → 서명 검증 실패 or 토큰 없음
    → /admin 요청: 통과 (로그인 페이지 자체는 허용)
    → 그 외 /admin/* 요청: /admin 리다이렉트
  → 서명 검증 성공: payload.role 확인
    → ROLE_ADMIN / ROLE_SUPER + /admin 접근: /admin/dashboard 리다이렉트
    → ROLE_ADMIN / ROLE_SUPER + /admin/* 접근: 통과
    → 그 외 role: /admin 리다이렉트
```

**환경변수:**
- `JWT_SECRET` — 서버사이드 전용 (NEXT_PUBLIC_ 접두사 없음)
- 백엔드 `application.yml`의 `jwt.secret`과 동일한 Base64 인코딩 값 사용

**jose 시크릿 처리:**

백엔드는 `Decoders.BASE64.decode(secret)`으로 Base64 디코딩 후 HMAC 키로 사용한다.
프론트에서도 동일하게 Base64 디코딩 후 `Uint8Array`로 변환한다.
`Buffer.from(..., 'base64')`는 Node.js 전용이라 Edge Runtime에서 동작하지 않으므로 `atob()` (Web API 표준) 사용:

```typescript
const secret = Uint8Array.from(atob(process.env.JWT_SECRET!), c => c.charCodeAt(0))
const { payload } = await jwtVerify(token, secret, { algorithms: ['HS256'] })
```

**알고리즘:**

- JJWT `Keys.hmacShaKeyFor()`는 키 길이에 따라 자동 선택 (≥256bit → HS256)
- 백엔드 시크릿이 32바이트(256bit) 이상이면 HS256 사용
- jose에서 `algorithms: ['HS256']`로 명시하여 알고리즘 혼동 공격 방지

### Backend (이미 구현됨 — 변경 불필요)

`AdminAuthService.refresh()`와 `MemberAuthService.refresh()`는 이미 Refresh Token 로테이션이 구현되어 있다.

```java
// 기존 토큰과 Redis 저장값 비교
String stored = redisService.get("refresh:admin:" + id);
if (!Objects.equals(refreshToken, stored)) throw new AppException(INVALID_REFRESH_TOKEN);

// 새 토큰 생성 후 Redis 덮어쓰기 (기존 토큰 무효화)
String newRefreshToken = jwtProvider.generateRefreshToken(id, type);
redisService.save("refresh:admin:" + id, newRefreshToken, REFRESH_TTL);
setCookies(response, newAccessToken, newRefreshToken);
```

Redis `SET`은 원자적 덮어쓰기이므로, 이후 기존 토큰 사용 시 값 불일치로 자동 거부된다.

**탈취 감지 시나리오 (현재 동작):**
- 탈취된 토큰으로 먼저 갱신 → Redis에 새 토큰으로 교체
- 진짜 사용자가 갱신 시도 → 저장된 값과 불일치 → `INVALID_REFRESH_TOKEN` → 강제 로그아웃

---

## 기술 결정

### JWT 검증: jose 라이브러리 선택

Edge Runtime 호환 JWT 라이브러리인 `jose`를 사용한다.

선택 이유:
- Next.js 미들웨어는 Edge Runtime에서 실행 — Node.js `jsonwebtoken`은 사용 불가
- `jose`는 Edge Runtime 공식 지원
- 백엔드 API 호출 방식 대비 레이턴시 없음
- JWT Secret은 서버사이드 환경변수로만 노출되어 안전

---

## 영향 없는 것

- 로그인 / 로그아웃 로직 변경 없음
- Access Token TTL 변경 없음
- Redis 키 구조 변경 없음
- 쿠키 설정 변경 없음 (HttpOnly, SameSite=Lax)
- 백엔드 코드 변경 없음

---

## 테스트 포인트

| 항목 | 검증 방법 |
|------|----------|
| 미들웨어 활성화 | 로그인 없이 `/admin/dashboard` 직접 접근 → `/admin` 리다이렉트 확인 |
| JWT 서명 검증 | 잘못된 서명의 access_token 쿠키로 접근 → 리다이렉트 확인 |
| 로그인 후 리다이렉트 | 로그인 성공 후 `/admin` 재접근 → `/admin/dashboard` 리다이렉트 확인 |
| 무한 루프 없음 | 비인증 상태에서 `/admin` 접근 시 루프 없이 로그인 페이지 렌더링 확인 |
