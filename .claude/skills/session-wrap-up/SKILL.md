---
name: session-wrap-up
description: "Use when the user signals they are done working for the day or ending a session — phrases like '오늘 마무리', '작업 끝', '오늘 여기까지', '마무리할게', 'wrap up', 'done for today'. Summarizes today's work and saves it to docs/progress/YYYY-MM-DD.md so the next session can immediately pick up where things left off. Always use this skill when the user is wrapping up — do not skip it just because the session was short."
---

# Session Wrap-Up

세션을 마무리하고 다음 날 바로 이어서 작업할 수 있도록 오늘의 작업 내용을 기록한다.

## 진행 순서

### 1. 오늘 작업 파악

다음 소스를 참고하여 오늘 한 일을 파악한다.

- 현재 대화 내용 (가장 신뢰할 수 있는 소스)
- `git log --oneline --since="today"` — 오늘 커밋 목록
- 최근 수정된 파일 목록

```bash
git -C <project-root> log --oneline --since="today" 2>/dev/null
```

### 2. docs/progress/YYYY-MM-DD.md 생성

오늘 날짜로 파일을 생성한다. 날짜는 시스템 날짜 또는 context의 `currentDate`를 사용한다.

파일 경로: `docs/progress/YYYY-MM-DD.md`

**파일 형식:**

```md
# YYYY-MM-DD 작업 기록

## 완료
- (오늘 완료한 작업 목록)

## 진행 중
- (완료되지 않고 중단된 작업. 없으면 "없음")

## 다음 작업
- (다음 세션에서 이어서 할 작업. 모르면 "미정")

## 메모
(특이사항, 결정사항, 주의할 점 등. 없으면 이 섹션 생략)
```

내용은 간결하게 한 줄씩 작성한다. 길게 설명하지 않는다.

### 3. CLAUDE.md 참조 업데이트

`CLAUDE.md`의 progress 참조 줄을 오늘 날짜 파일로 업데이트한다.

- 이미 `@docs/progress/` 참조가 있으면 날짜만 교체한다
- 없으면 `## 상세 문서` 섹션 마지막에 추가한다

```
@docs/progress/YYYY-MM-DD.md
```

### 4. 완료 보고

사용자에게 저장된 파일 경로와 다음 작업 내용을 간단히 알려준다.
