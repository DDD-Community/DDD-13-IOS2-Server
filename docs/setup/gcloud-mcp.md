# GCP MCP 설정 가이드 (Mac)

Claude Code에서 Google Cloud Platform 리소스를 직접 사용할 수 있도록 공식 `gcloud-mcp` 서버를 연결하는 가이드입니다.

> **패키지**: [`@google-cloud/gcloud-mcp`](https://github.com/googleapis/gcloud-mcp) (Google 공식)
> API 키 불필요 — gcloud CLI 인증 기반으로 동작

---

## Step 1. gcloud CLI 설치

```bash
brew install --cask google-cloud-sdk
```

설치 확인:
```bash
gcloud --version
```

---

## Step 2. gcloud 인증

```bash
gcloud auth login
gcloud auth application-default login
```

브라우저가 열리며 Google 계정으로 로그인합니다.

---

## Step 3. GCP 프로젝트 설정

전체 프로젝트 목록 확인:
```bash
gcloud projects list
```

사용할 프로젝트 설정:
```bash
gcloud config set project 프로젝트ID
```

현재 설정된 프로젝트 확인 (전역 설정이므로 터미널 위치 무관):
```bash
gcloud config get project
```

---

## Step 4. 프로젝트 MCP 설정

`.mcp.json`을 아래 내용으로 교체 (API 키 불필요):

```json
{
  "mcpServers": {
    "gcloud": {
      "command": "npx",
      "args": ["-y", "@google-cloud/gcloud-mcp"]
    }
  }
}
```

> `npx`가 실행 시 패키지를 자동으로 받아오므로 별도 설치 불필요
> `.mcp.json`은 이미 `.gitignore` 처리되어 있음

---

## Step 5. 연결 확인

Claude Code를 재시작한 뒤:

```bash
claude mcp list
```

`gcloud` 항목이 보이면 완료.

---

## 트러블슈팅

| 문제 | 해결 |
| --- | --- |
| MCP 연결 실패 | `npx @google-cloud/gcloud-mcp` 직접 실행해서 오류 확인 |
| 인증 오류 | `gcloud auth application-default login` 재실행 |
| 프로젝트 없음 | `gcloud config set project 프로젝트ID` 확인 |
