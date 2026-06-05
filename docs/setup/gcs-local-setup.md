# GCS 로컬 개발 환경 설정

이미지 업로드 기능(Signed URL)을 로컬에서 개발/테스트하기 위한 설정 가이드입니다.

---

## 사전 조건

- gcloud CLI 설치 (`brew install --cask google-cloud-sdk`)
- GCP 프로젝트 `aqueous-cargo-457101-h6` 접근 권한

---

## Step 1. gcloud 인증

```bash
gcloud auth login
gcloud auth application-default login
```

브라우저가 열리면 팀 Google 계정으로 로그인합니다.
로그인 후 크레덴셜이 `~/.config/gcloud/application_default_credentials.json`에 저장됩니다.

---

## Step 2. 프로젝트 설정

```bash
gcloud config set project aqueous-cargo-457101-h6
```

---

## Step 3. 로컬 서버 실행

별도 설정 없이 바로 실행하면 됩니다.
ADC(Application Default Credentials) 방식이라 키 파일이 필요 없습니다.

```bash
./gradlew bootRun
```

---

## 동작 방식

로컬과 Cloud Run 모두 **서비스 계정 키 파일 없이** 동작합니다.

| 환경 | 인증 방식 |
|---|---|
| 로컬 | `gcloud auth application-default login` 으로 발급된 사용자 크레덴셜 |
| Cloud Run (prod) | 인스턴스에 붙은 서비스 계정(`bangawo-github-actions@...`) 자동 사용 |

---

## Signed URL 흐름

```
iOS 클라이언트
  │
  ├─ POST /api/v1/storage/images   ← Signed Upload URL 요청
  │      └─ 서버가 GCS에 PUT Signed URL 생성 (유효 15분)
  │
  ├─ PUT {signedUploadUrl}         ← GCS에 직접 이미지 업로드
  │
  └─ PATCH /api/v1/members/me/profile-image  ← objectKey 서버에 저장
         └─ 서버가 GET Signed URL 생성 후 응답 (유효 1시간)
```

---

## 트러블슈팅

| 문제 | 원인 | 해결 |
|---|---|---|
| `STORAGE_002` 에러 | ADC 인증 안 됨 | `gcloud auth application-default login` 재실행 |
| Signed URL 서명 실패 | 서비스 계정에 권한 없음 | GCP IAM에서 `serviceAccountTokenCreator` 역할 확인 |
| 버킷 접근 불가 | 서비스 계정에 권한 없음 | GCP IAM에서 `storage.objectAdmin` 역할 확인 |
