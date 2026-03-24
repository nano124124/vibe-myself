# Backend 코딩 가이드

## 공통

- 모든 파일은 해당 모듈의 레이어 폴더 하위에 생성한다 (`backend/structure.md` 참조)
- 클래스명, 메서드명은 영문 camelCase / PascalCase를 사용한다
- 매직 넘버/문자열은 상수로 정의한다
- SOLID 원칙을 준수한다
- Early Return을 사용한다. 정상 흐름이 가장 바깥에 위치한다
- 메서드는 한 가지 일만 한다. 파라미터는 3개 이하, 초과 시 Command 객체로 묶는다
- 2중 중첩 이후는 메서드 추출 또는 Map으로 평탄화한다
- if-else 분기가 업무 타입에 따라 발생하면 전략 패턴으로 분리하는 방법을 우선 고민한다

---

## Controller

- `@RestController`, `@RequestMapping("/api/{module}")` 을 기본으로 사용한다
- 어드민 API는 `@RequestMapping("/api/admin/{module}")` 을 사용한다
- 비즈니스 로직을 작성하지 않는다. Service에 위임한다
- 응답은 공통 응답 포맷(`ApiResponse`)으로 감싼다

---

## Service

- `@Service`, `@RequiredArgsConstructor` 를 기본으로 사용한다
- 트랜잭션이 필요한 메서드에 `@Transactional` 을 명시한다
- 조회 전용 메서드에는 `@Transactional(readOnly = true)` 를 사용한다

---

## Mapper (MyBatis)

- 인터페이스에 `@Mapper` 를 선언한다
- SQL은 XML 파일에 작성한다 (`resources/mapper/{module}/` 하위)
- 메서드명은 동사로 시작한다 (`selectOrderById`, `insertOrder`, `updateOrderStatus`, `deleteOrder`)

---

## DTO

- Request DTO: `@Valid` + Bean Validation으로 입력값을 검증한다
- Response DTO: `record` 또는 `@Getter` + `@Builder` 로 불변 객체로 만든다
- DTO는 도메인 모델과 분리한다. Controller ↔ Service 경계에서 변환한다

---

## 예외 처리

- 비즈니스 예외는 커스텀 예외 클래스로 정의하고 `global/exception/` 에 위치시킨다
- `@RestControllerAdvice` 로 전역 예외를 처리한다
