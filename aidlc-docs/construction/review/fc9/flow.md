# 처리 흐름 — FC-9 담기

```mermaid
sequenceDiagram
    Mem->>API: GET places 역탭 카테고리 필터
    API->>Graph: 보는사람 출발지 단일출발 최단경로 1회
    API-->>Mem: 카드 목록 카드거리 담기수
    Mem->>API: POST places id pick
    API->>DB: 담기 저장
    API->>DB: 전원 담기완료 확인
    alt 전원 담기완료
        API->>Meeting: VOTING 전환 + 투표세션 기본 +3일
    end
```

## 담기 → 전환
1. 담기/취소 토글 → 담기완료/미완료 갱신
2. 전환 트리거: 전원완료 / 마감배치(+3일) / 호스트 투표생성
3. 마감 0개 → 스코어 top3 자동등록 후 VOTING

## 상태 전이
- RECOMMENDED → VOTING

## 스케줄러
- 담기마감(+3일) 배치: 후보≥1 VOTING / 후보0 top3 후 VOTING
