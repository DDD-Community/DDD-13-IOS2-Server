# Claude Code 팀 사용 가이드

이 프로젝트에서 Claude Code를 사용하는 방법을 정리한 가이드입니다.

---

## 설치

```bash
npm install -g @anthropic-ai/claude-code
```

---

## 실행

프로젝트 루트(`/Server`)에서 실행:

```bash
claude
```

> 프로젝트 루트에서 실행해야 `CLAUDE.md`(개발 가이드)가 자동으로 로드됩니다.

---

## 커맨드

### `/pr` — PR 생성

현재 브랜치를 분석해 커밋 내역 기반으로 PR 제목/본문을 자동 작성하고, push 후 GitHub PR 생성 URL을 출력합니다.

```
/pr
```

**사전 조건**: 작업 내용을 커밋까지 완료한 상태에서 실행. 미커밋 변경사항이 있으면 중단됩니다.

**브랜치별 동작**

| 현재 브랜치 | PR 대상 | 설명 |
|---|---|---|
| `feature/*` | `dev` | 기능 개발 완료 후 통합 브랜치로 PR **(일반적인 사용 케이스)** |
| `dev` | `main` | 릴리스용 PR. 여러 feature가 모인 내용을 요약 |
| `main` | — | 실행 불가, 즉시 중단 |

**일반적인 플로우**

```
feature/기능명 브랜치에서 작업
  → 커밋 완료
  → /pr 실행
  → URL 클릭해서 GitHub에서 직접 Create
```

PR은 자동으로 생성되지 않습니다. URL 클릭 후 GitHub 페이지에서 직접 Create 버튼을 눌러야 합니다.

---

## CLAUDE.md

프로젝트 루트의 `CLAUDE.md`는 Claude가 세션 시작 시 자동으로 읽는 개발 가이드입니다.

- DDD 레이어 원칙
- 네이밍 규칙
- 코드 스타일

Claude에게 코드 작성을 요청하면 이 가이드를 기반으로 동작합니다.

---

## GCP MCP (선택)

GCP 리소스를 Claude에서 직접 조회하려면 `docs/setup/gcloud-mcp.md` 참고.
