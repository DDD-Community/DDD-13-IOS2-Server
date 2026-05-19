---
description: 현재 브랜치 분석해서 PR 생성 URL 출력 (feature→dev, dev→main)
allowed-tools: Bash
argument-hint: [추가 컨텍스트]
---

현재 브랜치에 따라 적절한 base 브랜치로 PR을 만들기 위한 정보를 준비하고, 미리 채워진 GitHub PR 생성 URL을 출력해.

## Base 브랜치 자동 결정

`git branch --show-current` 결과에 따라:
- 현재 브랜치가 `dev` → base는 **main** (정식 릴리스 PR)
- 현재 브랜치가 `main` → 즉시 중단 ("main에서는 PR 만들 수 없습니다")
- 그 외 (feature 브랜치) → base는 **dev**

이 base 브랜치를 이후 모든 단계에서 `<BASE>` 로 사용.

## 사전 점검 (실패 시 즉시 중단하고 사용자에게 한국어로 안내)

1. `git rev-parse --is-inside-work-tree` — git 레포가 아니면 중단
2. `git status --short` — 미커밋/미스테이징 변경 있으면 중단 ("커밋 먼저 해주세요")
3. 위에서 결정한 BASE 브랜치 기준:
   - `git fetch origin <BASE>` 후 `git log origin/<BASE>..HEAD --oneline` — 새 커밋 0개면 중단

## 변경사항 분석

- `git log origin/<BASE>..HEAD --oneline` — 커밋 목록
- `git diff origin/<BASE>...HEAD --stat` — 변경 파일/규모
- 필요 시 `git diff origin/<BASE>...HEAD` 일부 확인

추가 컨텍스트: $ARGUMENTS

## PR 제목 작성 규칙

- 한국어, 70자 이내
- **맨 앞에 타입 이모지 1개** (커밋과 diff 보고 가장 적합한 것 선택):
  - ✨ 새 기능 (feat)
  - 🐛 버그 수정 (fix)
  - ♻️ 리팩토링 (refactor)
  - 🎨 UI/스타일 (style)
  - 📝 문서 (docs)
  - ✅ 테스트 (test)
  - ⚡ 성능 개선 (perf)
  - 🔧 설정/빌드 (chore)
- dev → main PR인 경우 여러 작업이 모이므로 가장 핵심 변경 1개를 대표로 선택
- 예시: `✨ 카카오 로그인 연동`, `🐛 검색 필터 중복 호출 수정`
- prefix 텍스트(`[feat]`, `feat:` 등)는 붙이지 않음 — 이모지로만 구분

## PR 본문 작성 규칙

정확히 아래 양식대로, `<!-- -->` 안내문은 제거하고 실제 내용을 채움:

```
## Summary
(1-2줄 요약. dev→main이면 이번 릴리스에 포함되는 변경 전체를 요약)

## Why
(왜 필요한지 — 커밋 메시지와 diff에서 추론)

## Changes
- (주요 변경사항)
- 

## Test
- [ ] 로컬에서 동작 확인
- [ ] (필요 시 추가 항목)

## Related
(관련 이슈 모르면 "없음")
```

## 실행 단계

1. **브랜치 push**: `git push -u origin <현재브랜치>` (dev면 그냥 `git push`)
2. **레포 정보 추출**: `git remote get-url origin` 결과에서 `owner/repo` 파싱
   - SSH 형식 `git@github.com:owner/repo.git` → `owner/repo`
   - HTTPS 형식 `https://github.com/owner/repo.git` → `owner/repo`
   - 끝의 `.git` 접미사 제거
3. **URL 인코딩** (python3 사용):
   ```bash
   python3 -c "import urllib.parse,sys; print(urllib.parse.quote(sys.argv[1]))" "<텍스트>"
   ```
4. **PR 생성 URL 조합**:
   ```
   https://github.com/{owner}/{repo}/compare/<BASE>...{현재브랜치}?expand=1&title={인코딩된제목}&body={인코딩된본문}
   ```
5. **출력 형식**:
   ```
   ✅ 브랜치 push 완료: <현재브랜치> → <BASE>

   PR 제목:
   <제목>

   PR 본문 미리보기:
   ---
   <본문>
   ---

   아래 URL 클릭하면 PR 생성 페이지가 열립니다:
   <URL>
   ```
   - dev→main PR이면 출력 끝에 안내 추가:
     `⚠️ 이 PR은 승인 1명이 필요합니다 (브랜치 보호 규칙)`

## 절대 하지 말 것

- `gh` CLI 사용 금지 (팀원 환경 의존성 없애기 위함)
- 자동으로 PR을 생성/머지하지 말 것 — 사용자가 URL 클릭해서 직접 Create 버튼 눌러야 함
- main 브랜치에서 이 커맨드를 실행하면 즉시 중단
- 본문에 섹션 헤더 이모지 추가 금지 (제목에만 타입 이모지 1개)
