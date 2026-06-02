---
name: code-review
description: "vibe-myself 이커머스 관점 코드 리뷰 스킬. '코드 리뷰해줘', '이 코드 괜찮아?', '리뷰 부탁해', '코드 점검', '이커머스 관점으로 봐줘', 특정 파일/모듈/PR 리뷰 요청 시 반드시 이 스킬을 사용한다."
---

# Code Review Skill

이커머스 실무 관점에서 vibe-myself 코드를 리뷰한다.

## 리뷰 대상 파악

사용자 요청에서 리뷰 대상을 파악한다:

| 요청 유형 | 대상 파악 방법 |
|---------|-------------|
| 특정 파일 | 파일 경로 직접 사용 |
| 모듈명 ("goods 리뷰해줘") | `backend/.../controller/goods/`, `backend/.../service/goods/`, `frontend/components/goods/` 등 |
| 최근 변경 | `git diff HEAD~1` 또는 `git diff {branch}` |
| PR 번호 | `gh pr diff {number}` |

## 리뷰 수행

code-reviewer 에이전트를 서브 에이전트로 호출한다:

```
Agent(
  subagent_type: "code-reviewer",
  model: "opus",
  prompt: "
    아래 파일들을 이커머스 관점으로 리뷰한다.
    code-reviewer 에이전트 정의(.claude/agents/code-reviewer.md)의 체크리스트를 따른다.
    리뷰 대상: {파일 목록 또는 경로}
    프로젝트 루트: /Users/nyj/Documents/git/vibe-myself
    산출물: /Users/nyj/Documents/git/vibe-myself/_workspace/review_report.md
  "
)
```

## 결과 보고

`_workspace/review_report.md` 내용을 다음 형식으로 요약한다:

```
## 코드 리뷰 결과

CRITICAL {N}건 / WARNING {N}건 / INFO {N}건

### CRITICAL
- {파일:라인} — {이슈} → {수정 권고}

### WARNING
- ...

### INFO
- ...
```

CRITICAL이 있으면 즉시 수정 여부를 사용자에게 확인한다.
CRITICAL이 없으면 WARNING/INFO 목록만 제시하고 마무리한다.

## 리뷰 범위 가이드

| 범위 | 파일 패턴 |
|------|---------|
| BE 전체 | `backend/src/main/java/com/vibemyself/{module}/` |
| FE 전체 | `frontend/components/{module}/`, `frontend/hooks/{module}/`, `frontend/api/` |
| 특정 레이어 | `service/`, `controller/`, `mapper/` 등 레이어 디렉토리 |
| 최근 커밋 | `git diff HEAD~{N}` 결과 |