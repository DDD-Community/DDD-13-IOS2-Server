# Component Methods — 중간지점 역 후보 추출 (MVP2)

## subway 컨텍스트

### SubwayStationRepository
```java
List<StationCandidate> findCandidatesNearMeetingCenter(Long meetingId, int limit);
// meetingId의 참여자 좌표로 centroid 계산 후 2km 이내 역 후보 반환
// native PostGIS SQL (ST_Centroid, ST_Collect, ST_DWithin, ST_DistanceSphere)
```

### StationCandidate (value object)
```java
record StationCandidate(String stationName, String lines, double distanceKm) {}
```

---

## meeting 컨텍스트

### MeetingParticipant (domain model)
```java
static MeetingParticipant create(Long meetingId, Long memberId,
                                  double latitude, double longitude,
                                  String attendanceStatus)
double getLatitude()
double getLongitude()
String getAttendanceStatus()
```

### MeetingParticipantRepository
```java
void saveAll(List<MeetingParticipant> participants);
List<MeetingParticipant> findByMeetingId(Long meetingId);
```

### MidpointStationCandidate (domain model)
```java
static MidpointStationCandidate of(Long meetingId, int rank,
                                    String stationName, String lines,
                                    double distanceKm)
```

### MidpointStationCandidateRepository
```java
void saveAll(List<MidpointStationCandidate> candidates);
List<MidpointStationCandidate> findByMeetingIdOrderByRank(Long meetingId);
```

### Meeting (domain model 추가)
```java
void startLocationPhase();
// BEFORE → IN_PROGRESS, 이미 IN_PROGRESS/COMPLETED면 BusinessException
```

### MidpointCalculationService
```java
List<StationCandidate> calculate(Long meetingId);
// SubwayStationRepository.findCandidatesNearMeetingCenter(meetingId, 3) 위임
// 결과 없으면 BusinessException(MIDPOINT_STATION_NOT_FOUND)
```

### LocationService
```java
void startLocationPhase(Long meetingId, Long requestMemberId);
// 1. meeting 조회 + 호스트 검증
// 2. meeting.startLocationPhase() 호출
// 3. MidpointCalculationService.calculate(meetingId)
// 4. MidpointStationCandidateRepository.saveAll()
// 5. MeetingRepository.save(meeting)

List<MidpointStationCandidate> getMidpointStations(Long meetingId, Long requestMemberId);
// 1. meeting 조회 + 그룹 멤버 검증
// 2. MidpointStationCandidateRepository.findByMeetingIdOrderByRank(meetingId)
```

### LocationController
```java
@PostMapping("/meetings/{meetingId}/location/start")
ResponseEntity<Void> startLocationPhase(@PathVariable Long meetingId,
                                         @AuthenticationPrincipal Long memberId)

@GetMapping("/meetings/{meetingId}/midpoint-stations")
ResponseEntity<MidpointStationCandidateResponse> getMidpointStations(
    @PathVariable Long meetingId,
    @AuthenticationPrincipal Long memberId)
```
