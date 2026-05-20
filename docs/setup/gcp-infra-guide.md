# GCP 인프라 구성 가이드

## 개요

Bangawo 서버는 GCP(Google Cloud Platform) 위에서 운영됩니다.
main 브랜치에 머지되면 GitHub Actions가 자동으로 빌드 → 배포합니다.

---

## 사용하는 GCP 리소스 4가지

### 1. Artifact Registry — Docker 이미지 저장소

빌드한 서버 이미지(Docker)를 저장하는 창고입니다.
GitHub Actions가 이미지를 빌드하면 여기에 업로드하고, Cloud Run이 여기서 꺼내서 실행합니다.

- 📦 저장 위치: `asia-northeast3-docker.pkg.dev/project-bcdbc10f-15a5-46b6-bb3/bangawo/bangawo-server`
- 💰 비용: 저장 용량 기준 ~$0.1/GB/월 (이미지 몇 개 수준이면 거의 무료)
- 📖 공식 문서: https://cloud.google.com/artifact-registry/docs/overview

---

### 2. Cloud SQL — 관리형 PostgreSQL 데이터베이스

GCP가 관리해주는 PostgreSQL 서버입니다.
직접 DB 서버를 설치/운영할 필요 없이 GCP가 백업, 패치, 가용성을 알아서 처리합니다.

- 🗄️ 스펙: PostgreSQL 15, `db-f1-micro` (1 vCPU, 0.6GB RAM), SSD 10GB
- 🌏 리전: asia-northeast3 (서울)
- 💰 비용: ~$7-9/월 (가장 저렴한 인스턴스 타입)
- 📖 공식 문서: https://cloud.google.com/sql/docs/postgres/introduction

---

### 3. Cloud Run — 서버리스 컨테이너 실행 환경

Docker 이미지를 실행하는 서버입니다.
요청이 없으면 인스턴스 수를 0으로 줄여서 비용을 아끼고 (scale-to-zero), 요청이 오면 자동으로 늘립니다.

- ⚙️ 스펙: 512MB RAM, 1 CPU, 최소 0개 ~ 최대 3개 인스턴스
- 🌏 리전: asia-northeast3 (서울)
- 🔌 포트: 8080
- 💰 비용: 요청 없으면 $0, 트래픽 있을 때만 과금 (~$1-3/월)
- 📖 공식 문서: https://cloud.google.com/run/docs/overview/what-is-cloud-run

---

### 4. Secret Manager — 시크릿(비밀값) 관리

JWT 키, DB 패스워드, 카카오 API 키 같은 민감한 정보를 안전하게 보관하는 금고입니다.
코드나 환경변수에 직접 넣지 않고, Cloud Run이 실행될 때 여기서 꺼내 씁니다.

- 🔐 저장 시크릿: JWT_SECRET_KEY, KAKAO_REST_API_KEY, DB 접속 정보 (5개)
- 💰 비용: 시크릿 7개 기준 ~$0.06/월 (사실상 무료)
- 📖 공식 문서: https://cloud.google.com/secret-manager/docs/overview

---

## 예상 비용 요약

| 리소스 | 월 비용 |
|---|---|
| Artifact Registry | ~$0.1 |
| Cloud SQL (db-f1-micro) | ~$7-9 |
| Cloud Run | ~$1-3 |
| Secret Manager | ~$0.06 |
| **합계** | **~$10-12/월** |

> $300 크레딧 기준 약 **25개월** 사용 가능

---

## 배포 흐름

```
개발자 → PR → main 머지
    ↓
GitHub Actions 자동 실행
    ↓
1. 코드 체크아웃
2. Docker 이미지 빌드
3. Artifact Registry에 이미지 푸시
4. Cloud Run에 새 이미지로 배포
    ↓
서비스 자동 업데이트 완료
```

---

## 생성 순서 (최초 1회)

1. Artifact Registry 저장소 생성
2. Cloud SQL 인스턴스 생성 (약 5-10분 소요)
3. Cloud SQL에 DB 및 유저 생성
4. GitHub Actions용 Service Account 생성 및 권한 부여
5. Service Account 키 발급

---

## GitHub Secrets 등록 목록 (최초 1회, 직접 등록)

GitHub 저장소 → Settings → Secrets and variables → Actions 에서 등록

| Secret 이름 | 설명 |
|---|---|
| `GCP_SA_KEY` | Service Account JSON 키 (GCP 인증용) |
| `CLOUD_SQL_CONNECTION_NAME` | Cloud SQL 연결 이름 (예: `project-id:asia-northeast3:instance-name`) |
| `PROD_JWT_SECRET_KEY` | JWT 서명 시크릿 키 |
| `PROD_KAKAO_REST_API_KEY` | 카카오 로그인 REST API 키 |
| `PROD_DB_HOST` | Cloud SQL 소켓 경로 (`/cloudsql/연결이름`) |
| `PROD_DB_PORT` | `5432` |
| `PROD_DB_NAME` | 데이터베이스 이름 |
| `PROD_DB_USERNAME` | DB 접속 유저명 |
| `PROD_DB_PASSWORD` | DB 접속 패스워드 |
