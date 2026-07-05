package com.bangawo.meeting.application;

import com.bangawo.auth.domain.Member;
import com.bangawo.auth.domain.MemberRepository;
import com.bangawo.global.error.BusinessException;
import com.bangawo.global.error.ErrorCode;
import com.bangawo.group.domain.AttendanceStatus;
import com.bangawo.group.domain.GroupMember;
import com.bangawo.group.domain.GroupMemberRepository;
import com.bangawo.group.domain.GroupMemberRole;
import com.bangawo.meeting.domain.*;
import com.bangawo.meeting.presentation.dto.PlaceTravelBurdenResponse;
import com.bangawo.meeting.presentation.dto.PlaceVoteStatusResponse;
import com.bangawo.meeting.presentation.dto.VoteParticipantsResponse;
import com.bangawo.place.domain.Place;
import com.bangawo.place.domain.PlaceRepository;
import com.bangawo.place.presentation.dto.PlaceSummary;
import com.bangawo.subway.domain.StationCoordinate;
import com.bangawo.subway.domain.SubwayGraph;
import com.bangawo.subway.domain.SubwayStationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 장소 투표 유스케이스.
 *
 * 전체 플로우:
 * 1) startVoting : (HOST) 투표 시작 — 상태/권한/마감일 검증 후 VOTING 전환 + 세션 생성
 * 2) createSession : 후보 백필 → 세션 저장 → 이동부담 스냅샷 계산/저장
 * 3) submitVote : 참여자 투표 제출(재투표 시 덮어쓰기) → 전원 투표 시 자동 확정
 * 4) getVoteStatus : 투표 현황 조회(후보별 득표·이동부담, 멤버별 참여 현황)
 * 5) getPlaceTravelBurden : 특정 후보 장소에 대한 멤버별 이동부담 상세 조회
 * - isAllVoted / computeAndSaveTravelBurdens 등은 위 흐름을 보조한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PlaceVoteService {

        private static final int DEFAULT_DURATION_DAYS = 3;
        private static final int MIN_CANDIDATES = 3;
        private static final Set<Integer> VALID_DURATION_DAYS = Set.of(1, 3, 7);

        private final MeetingRepository meetingRepository;
        private final GroupMemberRepository groupMemberRepository;
        private final MeetingParticipantRepository meetingParticipantRepository;
        private final MeetingPlacePickRepository meetingPlacePickRepository;
        private final MeetingPlaceVoteSessionRepository voteSessionRepository;
        private final MeetingPlaceVoteRepository voteRepository;
        private final MeetingTravelBurdenRepository travelBurdenRepository;
        private final MeetingPlaceRecommendationRepository recommendationRepository;
        private final SubwayGraph subwayGraph;
        private final SubwayStationRepository subwayStationRepository;
        private final PlaceConfirmService placeConfirmService;
        private final PlaceRepository placeRepository;
        private final MemberRepository memberRepository;

        /**
         * 장소 투표 시작 (HOST 전용).
         * 흐름: 모임 조회 → 호스트 권한 확인 → 사전 조건 검증(추천완료 상태/중복 세션/유효 기간)
         * → 마감일이 모임날짜보다 빠른지 검증 → VOTING 전환 → 세션 생성.
         */
        @Transactional
        public void startVoting(Long meetingId, Long memberId, int durationDays) {
                // 1. 모임 존재 확인
                Meeting meeting = meetingRepository.findById(meetingId)
                                .orElseThrow(() -> new BusinessException(ErrorCode.MEETING_NOT_FOUND));

                // 2. 호출자가 해당 그룹의 HOST인지 확인
                GroupMember caller = groupMemberRepository
                                .findByGroupIdAndMemberId(meeting.getGroupId(), memberId)
                                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_GROUP_MEMBER));
                if (caller.getRole() != GroupMemberRole.HOST) {
                        throw new BusinessException(ErrorCode.NOT_GROUP_HOST);
                }

                // 3. 사전 조건 검증: 추천 완료 상태여야 하고, 이미 진행 중인 세션이 없어야 하며, 기간은 1/3/7일만 허용
                if (meeting.getLocationStatus() != LocationStatus.RECOMMENDED) {
                        throw new BusinessException(ErrorCode.LOCATION_NOT_RECOMMENDED);
                }
                if (voteSessionRepository.findByMeetingId(meetingId).isPresent()) {
                        throw new BusinessException(ErrorCode.PLACE_VOTE_ALREADY_STARTED);
                }
                if (!VALID_DURATION_DAYS.contains(durationDays)) {
                        throw new BusinessException(ErrorCode.INVALID_DURATION_DAYS);
                }

                // 4. 투표 마감일이 모임 확정일보다 늦거나 같으면 불가
                LocalDate voteDeadlineDate = LocalDate.now().plusDays(durationDays);
                if (meeting.getConfirmedDate() != null
                                && !voteDeadlineDate.isBefore(meeting.getConfirmedDate().toLocalDate())) {
                        throw new BusinessException(ErrorCode.PLACE_VOTE_DEADLINE_INVALID);
                }

                // 5. 상태 전환(RECOMMENDED → VOTING) 후 세션 생성
                meeting.toVoting(); // 투표 상태 변경
                meetingRepository.save(meeting);
                createSession(meetingId, durationDays);
        }

        /**
         * 투표 세션 생성 (모든 세션 생성 경로의 공통 진입점).
         * 흐름: 후보 부족 시 추천으로 백필 → 세션 엔티티 저장 → 멤버×후보 이동부담 스냅샷 계산/저장.
         */
        @Transactional
        public MeetingPlaceVoteSession createSession(Long meetingId, int durationDays) {
                backfillCandidatesIfNeeded(meetingId);
                MeetingPlaceVoteSession session = MeetingPlaceVoteSession.create(meetingId, durationDays);
                MeetingPlaceVoteSession saved = voteSessionRepository.save(session);
                computeAndSaveTravelBurdens(meetingId, saved.getId());
                return saved;
        }

        /** 기본 기간(3일)으로 세션 생성하는 편의 메서드. */
        @Transactional
        public MeetingPlaceVoteSession createSessionWithDefaultDuration(Long meetingId) {
                return createSession(meetingId, DEFAULT_DURATION_DAYS);
        }

        /**
         * 투표 후보(담긴 장소 distinct)가 최소치(3) 미만이면 추천을 rank 오름차순으로
         * 시스템 백필한다. 추천 총량이 부족하면 가능한 만큼만. 모든 세션 생성 경로의 단일 진입점.
         */
        private void backfillCandidatesIfNeeded(Long meetingId) {
                // 1. 현재 후보(담긴 장소) distinct 집계 — 입력 순서 유지(LinkedHashSet)
                List<MeetingPlacePick> picks = meetingPlacePickRepository.findByMeetingId(meetingId);
                Set<Long> candidatePlaceIds = picks.stream()
                                .map(MeetingPlacePick::getPlaceId).collect(Collectors.toCollection(LinkedHashSet::new));
                // 2. 최소치 충족 시 백필 불필요
                if (candidatePlaceIds.size() >= MIN_CANDIDATES) {
                        return;
                }

                // 3. 추천을 rank 오름차순으로 순회하며 중복 제외하고 최소치까지 시스템 후보로 추가
                List<MeetingPlacePick> backfills = new ArrayList<>();
                for (MeetingPlaceRecommendation rec : recommendationRepository.findByMeetingIdOrderByRank(meetingId)) {
                        if (candidatePlaceIds.contains(rec.getPlaceId())) {
                                continue;
                        }
                        backfills.add(MeetingPlacePick.ofSystem(meetingId, rec.getPlaceId()));
                        candidatePlaceIds.add(rec.getPlaceId());
                        if (candidatePlaceIds.size() >= MIN_CANDIDATES) {
                                break;
                        }
                }
                if (!backfills.isEmpty()) {
                        meetingPlacePickRepository.saveAll(backfills);
                        log.info("후보 백필 meetingId={} 추가={}건", meetingId, backfills.size());
                }
        }

        /**
         * 투표 제출 (재투표 시 기존 표 덮어쓰기).
         * 흐름: 모임/그룹원 검증 → 진행 중 세션 확인 → 표 개수/후보 유효성 검증
         *      → 내 기존 표 삭제 후 새 표 저장 → 활성 참여자 전원 투표 완료면 자동 확정.
         */
        @Transactional
        public List<MeetingPlaceVote> submitVote(Long meetingId, Long memberId, List<Long> placeIds) {
                // 1. 모임 + 그룹원 검증
                Meeting meeting = meetingRepository.findById(meetingId)
                                .orElseThrow(() -> new BusinessException(ErrorCode.MEETING_NOT_FOUND));
                requireActiveParticipant(meetingId, memberId);

                // 2. 투표 진행 중 상태 확인
                if (meeting.getLocationStatus() != LocationStatus.VOTING) {
                        throw new BusinessException(ErrorCode.PLACE_VOTE_NOT_IN_PROGRESS);
                }

                // 3. 세션 존재 + 미마감(CLOSED 아님) 확인
                MeetingPlaceVoteSession session = voteSessionRepository.findByMeetingId(meetingId)
                                .orElseThrow(() -> new BusinessException(ErrorCode.PLACE_VOTE_NOT_IN_PROGRESS));

                if ("CLOSED".equals(session.getStatus())) {
                        throw new BusinessException(ErrorCode.PLACE_VOTE_NOT_IN_PROGRESS);
                }

                // 4. 표 개수 제한(후보의 절반, 최소 1) + 후보 유효성 검증
                Set<Long> candidatePlaceIds = meetingPlacePickRepository.findByMeetingId(meetingId).stream()
                                .map(MeetingPlacePick::getPlaceId).collect(Collectors.toSet());
                int maxVotes = Math.max(1, candidatePlaceIds.size() / 2);

                if (placeIds.size() > maxVotes) {
                        throw new BusinessException(ErrorCode.PLACE_VOTE_LIMIT_EXCEEDED);
                }
                if (!candidatePlaceIds.containsAll(placeIds)) {
                        throw new BusinessException(ErrorCode.PLACE_VOTE_INVALID_CANDIDATE);
                }

                // 5. 재투표 대비 내 기존 표 삭제 후 새 표 저장
                voteRepository.deleteBySessionIdAndMemberId(session.getId(), memberId);

                List<MeetingPlaceVote> votes = placeIds.stream()
                                .map(placeId -> MeetingPlaceVote.of(session.getId(), memberId, placeId))
                                .toList();

                if (!votes.isEmpty()) {
                        voteRepository.saveAll(votes);
                }

                // 6. 활성 참여자 전원 투표 완료 시 모임 확정 + 장소 확정 트리거
                if (isAllVotedInternal(meetingId, session.getId())) {
                        meeting.toConfirmed();
                        meetingRepository.save(meeting);
                        placeConfirmService.confirmPlace(meetingId);
                }

                return votes;
        }

        /**
         * 투표 현황 조회.
         * 흐름: 검증 → 후보/표/장소/참여자 데이터 로드 → 집계(득표수·내 투표 여부·활성 참여 수)
         *      → 후보 정렬(미투표=가나다순, 투표후=득표순) → 후보별/멤버별 응답 DTO 조립.
         * 이동부담은 포함하지 않는다(PRD 12-3) — 상세는 거리보기 API(getPlaceTravelBurden) 참조.
         */
        @Transactional(readOnly = true)
        public PlaceVoteStatusResponse getVoteStatus(Long meetingId, Long memberId) {
                // 1. 모임 + 그룹원 + 투표 진행 상태 검증
                Meeting meeting = meetingRepository.findById(meetingId)
                                .orElseThrow(() -> new BusinessException(ErrorCode.MEETING_NOT_FOUND));
                requireParticipant(meetingId, memberId);

                if (meeting.getLocationStatus() != LocationStatus.VOTING) {
                        throw new BusinessException(ErrorCode.PLACE_VOTE_NOT_IN_PROGRESS);
                }

                MeetingPlaceVoteSession session = voteSessionRepository.findByMeetingId(meetingId)
                                .orElseThrow(() -> new BusinessException(ErrorCode.PLACE_VOTE_NOT_IN_PROGRESS));

                // 2. 후보 장소(담긴 장소 distinct) 목록
                List<MeetingPlacePick> picks = meetingPlacePickRepository.findByMeetingId(meetingId);
                List<Long> candidatePlaceIds = picks.stream()
                                .map(MeetingPlacePick::getPlaceId).distinct().toList();

                // 3. 전체 표 집계 — 후보별 득표수 + 내가 투표한 장소 집합
                List<MeetingPlaceVote> allVotes = voteRepository.findBySessionId(session.getId());
                Map<Long, Long> voteCountByPlaceId = allVotes.stream()
                                .collect(Collectors.groupingBy(MeetingPlaceVote::getPlaceId, Collectors.counting()));
                Set<Long> myVotedPlaceIds = allVotes.stream()
                                .filter(v -> memberId.equals(v.getMemberId()))
                                .map(MeetingPlaceVote::getPlaceId).collect(Collectors.toSet());

                // 4. 후보 장소 상세 로드 (이동부담은 투표 현황에 미포함 — 거리보기 API에서 별도 제공)
                Map<Long, Place> placeById = placeRepository.findByIds(candidatePlaceIds)
                                .stream().collect(Collectors.toMap(Place::getId, p -> p));

                // 5. 활성 참여자(ABSENT 제외) 기준 투표 진행률 계산
                List<MeetingParticipant> participants = meetingParticipantRepository.findByMeetingId(meetingId);
                Set<Long> voterIds = allVotes.stream().map(MeetingPlaceVote::getMemberId).collect(Collectors.toSet());
                List<MeetingParticipant> activeParticipants = participants.stream()
                                .filter(p -> !"ABSENT".equals(p.getAttendanceStatus())).toList();
                Set<Long> activeIds = activeParticipants.stream()
                                .map(MeetingParticipant::getMemberId).collect(Collectors.toSet());
                int totalActive = activeIds.size();
                int votedCount = (int) activeIds.stream().filter(voterIds::contains).count();

                // 정렬: 내가 미투표면 가나다순, 투표했으면 득표순(동점 시 가나다순)
                boolean voted = !myVotedPlaceIds.isEmpty();
                Comparator<Long> nameAsc = Comparator.comparing(
                                id -> placeById.get(id) != null ? placeById.get(id).getName() : "");
                Comparator<Long> ordering = voted
                                ? Comparator.comparingLong((Long id) -> voteCountByPlaceId.getOrDefault(id, 0L))
                                                .reversed().thenComparing(nameAsc)
                                : nameAsc;
                List<Long> sortedPlaceIds = candidatePlaceIds.stream().sorted(ordering).toList();

                // 6. 후보별 응답 조립 — 득표수 + 내 투표 여부 (이동부담 제외)
                List<PlaceVoteStatusResponse.CandidateVoteInfo> candidates = sortedPlaceIds.stream()
                                .map(placeId -> new PlaceVoteStatusResponse.CandidateVoteInfo(
                                                PlaceSummary.from(placeById.get(placeId)),
                                                voteCountByPlaceId.getOrDefault(placeId, 0L).intValue(),
                                                myVotedPlaceIds.contains(placeId)))
                                .toList();

                // 멤버별 참여 현황 — 모든 호출자에게 제공(전원 공개). 활성 참여자 기준, 투표 대상은 비공개.
                Map<Long, Member> memberById = memberRepository.findAllById(activeIds).stream()
                                .collect(Collectors.toMap(Member::getId, m -> m));
                List<PlaceVoteStatusResponse.MemberVoteStatus> memberStatuses = activeParticipants.stream()
                                .map(p -> {
                                        Member m = memberById.get(p.getMemberId());
                                        String name = m != null ? m.getNickname() : "";
                                        return new PlaceVoteStatusResponse.MemberVoteStatus(
                                                        p.getMemberId(), name, voterIds.contains(p.getMemberId()));
                                })
                                .toList();

                return new PlaceVoteStatusResponse(session.getDeadline(), session.getStatus(),
                                totalActive, votedCount, memberStatuses, candidates);
        }

        /**
         * 특정 후보 장소의 멤버별 이동부담 상세 조회 ("친구들 거리보기").
         * 멤버 기준 = 모임 활성 참여자(ABSENT 제외) 전원(요청자 포함). 스냅샷 없는 멤버도 포함.
         * 흐름: 검증 → 활성 참여자 조회 → 장소 스냅샷/멤버/출발지 배치조회 → 참여자별 DTO 조립.
         */
        @Transactional(readOnly = true)
        public PlaceTravelBurdenResponse getPlaceTravelBurden(Long meetingId, Long placeId, Long requestMemberId) {
                // 1. 모임 + 그룹원 검증
                Meeting meeting = meetingRepository.findById(meetingId)
                                .orElseThrow(() -> new BusinessException(ErrorCode.MEETING_NOT_FOUND));
                requireParticipant(meetingId, requestMemberId);

                // 2. 멤버 기준 = 활성 참여자(ABSENT 제외) 전원
                List<MeetingParticipant> activeParticipants = meetingParticipantRepository.findByMeetingId(meetingId)
                                .stream()
                                .filter(p -> !"ABSENT".equals(p.getAttendanceStatus()))
                                .toList();

                // 3. 해당 장소 이동부담 스냅샷 → memberId별 매핑 + 최대 소요시간(가장 멀리서 오는 사람)
                Map<Long, MeetingTravelBurden> burdenByMemberId = travelBurdenRepository
                                .findByMeetingIdAndPlaceId(meetingId, placeId).stream()
                                .collect(Collectors.toMap(MeetingTravelBurden::getMemberId, b -> b, (a, b) -> a));
                int maxSec = burdenByMemberId.values().stream()
                                .mapToInt(MeetingTravelBurden::getSeconds).max().orElse(Integer.MIN_VALUE);

                // 4. 멤버 닉네임 배치 조회 (출발지 이름은 참여자 스냅샷에서 직접 사용)
                List<Long> memberIds = activeParticipants.stream()
                                .map(MeetingParticipant::getMemberId).toList();
                Map<Long, Member> memberById = memberRepository.findAllById(new HashSet<>(memberIds)).stream()
                                .collect(Collectors.toMap(Member::getId, m -> m));

                Place place = placeRepository.findByIds(List.of(placeId)).stream().findFirst().orElse(null);

                // 4-1. 도착역(경로 마지막 역) 역명만 조회 — 모든 멤버 도착역은 같은 장소 최근접역이라 보통 1건
                List<Long> arrivalStationIds = burdenByMemberId.values().stream()
                                .map(MeetingTravelBurden::getStationPath)
                                .filter(sp -> !sp.isEmpty())
                                .map(sp -> sp.get(sp.size() - 1).stationId())
                                .distinct()
                                .toList();
                Map<Long, String> arrivalNameById = subwayStationRepository.findNamesByIds(arrivalStationIds);

                // 5. 참여자별 DTO 조립 (스냅샷 없는 멤버는 seconds/transfers=null, path=[])
                List<PlaceTravelBurdenResponse.MemberBurden> memberBurdens = activeParticipants.stream()
                                .map(p -> {
                                        Long mid = p.getMemberId();
                                        Member m = memberById.get(mid);
                                        String name = m != null ? m.getNickname() : "";
                                        MeetingTravelBurden b = burdenByMemberId.get(mid);

                                        Integer seconds = b != null ? b.getSeconds() : null;
                                        Integer transfers = b != null ? b.getTransfers() : null;
                                        boolean isLongest = b != null && b.getSeconds() == maxSec;
                                        List<TravelPathPoint> pts = b != null ? b.getStationPath() : List.of();
                                        List<PlaceTravelBurdenResponse.PathPoint> path = java.util.stream.IntStream
                                                        .range(0, pts.size())
                                                        .mapToObj(i -> {
                                                                TravelPathPoint pt = pts.get(i);
                                                                boolean isArrival = i == pts.size() - 1;
                                                                return new PlaceTravelBurdenResponse.PathPoint(
                                                                                pt.stationId(),
                                                                                isArrival ? arrivalNameById.get(pt.stationId()) : null,
                                                                                pt.latitude(), pt.longitude(),
                                                                                i,
                                                                                i == 0,
                                                                                isArrival);
                                                        })
                                                        .toList();
                                        String departureName = p.departureName();

                                        return new PlaceTravelBurdenResponse.MemberBurden(
                                                        mid, name, departureName, mid.equals(requestMemberId),
                                                        seconds, transfers, isLongest, path);
                                })
                                .toList();

                return new PlaceTravelBurdenResponse(PlaceSummary.from(place), memberBurdens);
        }

        /**
         * 현재 장소투표에 참여중인 팀원 조회.
         * 흐름: 모임/그룹원/VOTING 검증 → 세션 확인 → 활성 참여자 + 표(투표자) + 회원 배치조회
         *      → 참여자별 {이름·프로필·출발지명·본인여부·투표여부} 조립(등록순).
         */
        @Transactional(readOnly = true)
        public VoteParticipantsResponse getVoteParticipants(Long meetingId, Long memberId) {
                // 1. 모임 + 그룹원 검증
                Meeting meeting = meetingRepository.findById(meetingId)
                                .orElseThrow(() -> new BusinessException(ErrorCode.MEETING_NOT_FOUND));
                requireParticipant(meetingId, memberId);

                // 2. 투표 진행 중 상태 + 세션 확인
                if (meeting.getLocationStatus() != LocationStatus.VOTING) {
                        throw new BusinessException(ErrorCode.PLACE_VOTE_NOT_IN_PROGRESS);
                }
                MeetingPlaceVoteSession session = voteSessionRepository.findByMeetingId(meetingId)
                                .orElseThrow(() -> new BusinessException(ErrorCode.PLACE_VOTE_NOT_IN_PROGRESS));

                // 3. 활성 참여자(ABSENT 제외) + 투표 제출자(distinct) 집합
                List<MeetingParticipant> activeParticipants = meetingParticipantRepository.findByMeetingId(meetingId)
                                .stream()
                                .filter(p -> !"ABSENT".equals(p.getAttendanceStatus()))
                                .toList();
                Set<Long> voterIds = voteRepository.findBySessionId(session.getId()).stream()
                                .map(MeetingPlaceVote::getMemberId).collect(Collectors.toSet());

                // 4. 회원(닉네임/프로필) 배치 조회
                List<Long> memberIds = activeParticipants.stream()
                                .map(MeetingParticipant::getMemberId).toList();
                Map<Long, Member> memberById = memberRepository.findAllById(new HashSet<>(memberIds)).stream()
                                .collect(Collectors.toMap(Member::getId, m -> m));

                // 5. 참여자별 DTO 조립 (참여 등록순 유지)
                List<VoteParticipantsResponse.Participant> participants = activeParticipants.stream()
                                .map(p -> {
                                        Long mid = p.getMemberId();
                                        Member m = memberById.get(mid);
                                        String name = m != null ? m.getNickname() : "";
                                        String profileImageUrl = m != null ? m.getProfileImageUrl() : null;
                                        return new VoteParticipantsResponse.Participant(
                                                        mid, name, profileImageUrl, p.departureName(),
                                                        mid.equals(memberId), voterIds.contains(mid));
                                })
                                .toList();

                return new VoteParticipantsResponse(participants);
        }

        private MeetingParticipant requireActiveParticipant(Long meetingId, Long memberId) {
                MeetingParticipant participant = meetingParticipantRepository
                                .findByMeetingIdAndMemberId(meetingId, memberId)
                                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_MEETING_PARTICIPANT));
                if (AttendanceStatus.ABSENT.name().equals(participant.getAttendanceStatus())) {
                        throw new BusinessException(ErrorCode.ABSENT_PARTICIPANT_CANNOT_ACT);
                }
                return participant;
        }

        private void requireParticipant(Long meetingId, Long memberId) {
                meetingParticipantRepository.findByMeetingIdAndMemberId(meetingId, memberId)
                                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_MEETING_PARTICIPANT));
        }

        /** 세션 존재 시 활성 참여자 전원이 투표했는지 외부 노출용 판정. (세션 없으면 false) */
        public boolean isAllVoted(Long meetingId) {
                MeetingPlaceVoteSession session = voteSessionRepository.findByMeetingId(meetingId).orElse(null);
                if (session == null)
                        return false;
                return isAllVotedInternal(meetingId, session.getId());
        }

        /**
         * 전원 투표 완료 판정 내부 로직.
         * 활성 참여자(ABSENT 제외) 수 대비 distinct 투표자 수가 같거나 많으면 true. (활성 0명이면 false)
         */
        private boolean isAllVotedInternal(Long meetingId, Long sessionId) {
                List<MeetingParticipant> participants = meetingParticipantRepository.findByMeetingId(meetingId);
                long activeCount = participants.stream()
                                .filter(p -> !"ABSENT".equals(p.getAttendanceStatus())).count();
                if (activeCount == 0)
                        return false;
                long voterCount = voteRepository.countDistinctVotersBySessionId(sessionId);
                return voterCount >= activeCount;
        }

        /**
         * 이동부담 스냅샷 계산/저장.
         *
         * 무엇을 하는가:
         *  - "활성 참여자 각자가 각 후보 장소까지 지하철로 얼마나 걸리는지(소요시간 초 + 환승횟수)"를
         *    세션 생성 시점에 미리 전부 계산해 meeting_travel_burden 테이블에 스냅샷으로 저장한다.
         *  - 이렇게 미리 계산해두면 getVoteStatus / getPlaceTravelBurden 조회 시 재계산 없이 바로 읽어 쓴다.
         *
         * 계산 방식 (참여자 N명 × 후보 M개 = N×M 행 생성):
         *  1) 지하철 그래프 미로드면 계산 불가 → 스냅샷 생략(경고 로그).
         *  2) 활성 참여자(ABSENT 제외) 중 좌표 가진 사람만 대상.
         *  3) 후보 장소 → 각 장소의 최근접 지하철역(추천 테이블에 미리 매핑된 nearestStationId) 매핑.
         *  4) 참여자별: 내 위치 최근접역(출발역)에서 Dijkstra로 전체 역까지 최단 비용(시간/환승) + 경로(prev) 1회 계산.
         *  5) 각 후보 도착역까지 비용 조회 + 경로(역 id 순서) 복원 → 중간 보관.
         *     - 도달 불가 시 seconds는 사실상 무한대(Integer.MAX_VALUE/2), 경로는 빈 리스트.
         *  6) 전체 경로의 역 좌표를 subway_station에서 1회 배치 조회 → 경로를 {stationId,lat,lng}로 채움.
         *  7) MeetingTravelBurden 행 생성 후 일괄 저장.
         */
        private void computeAndSaveTravelBurdens(Long meetingId, Long sessionId) {
                // 1. 지하철 그래프가 로드되지 않았으면 계산 불가 → 스냅샷 생략
                if (!subwayGraph.isLoaded()) {
                        log.warn("SubwayGraph 미로드 meetingId={} 이동부담 스냅샷 생략", meetingId);
                        return;
                }

                // 2. 대상 참여자: 활성(ABSENT 제외) + 좌표 보유자만
                List<MeetingParticipant> participants = meetingParticipantRepository
                                .findByMeetingId(meetingId).stream()
                                .filter(p -> !"ABSENT".equals(p.getAttendanceStatus()))
                                .filter(MeetingParticipant::hasCoordinate).toList();

                // 3. 후보(담긴 장소+백필) distinct → 추천 테이블에서 placeId별 최근접역(도착역) 매핑
                Set<Long> candidatePlaceIds = meetingPlacePickRepository.findByMeetingId(meetingId).stream()
                                .map(MeetingPlacePick::getPlaceId).collect(Collectors.toSet());
                Map<Long, Long> nearestStationByPlaceId = recommendationRepository
                                .findByMeetingIdOrderByRank(meetingId).stream()
                                .filter(r -> candidatePlaceIds.contains(r.getPlaceId()))
                                .filter(r -> r.getNearestStationId() != null)
                                .collect(Collectors.toMap(MeetingPlaceRecommendation::getPlaceId,
                                                MeetingPlaceRecommendation::getNearestStationId, (a, b) -> a));

                // 비용/경로(역 id) 중간 결과 + 전체 경로에 등장한 역 id 수집(좌표 배치조회용)
                List<BurdenDraft> drafts = new ArrayList<>();
                Set<Long> pathStationIds = new LinkedHashSet<>();

                // 4. 참여자별로 출발역 기준 Dijkstra 1회 → 모든 후보 도착역까지의 비용 + 경로 산출
                for (MeetingParticipant participant : participants) {
                        // 4-1. 참여자 좌표의 최근접역(출발역)
                        Optional<Long> nearestOpt = subwayStationRepository
                                        .findNearestStationId(participant.getLatitude(), participant.getLongitude());

                        if (nearestOpt.isEmpty()) {
                                log.warn("nearest station 없음 memberId={}", participant.getMemberId());
                                continue;
                        }

                        // 4-2. 출발역에서 전체 역까지 최단 비용(int[]{소요초,환승}) + 경로복원용 prev 계산
                        Long sourceStation = nearestOpt.get();
                        SubwayGraph.DijkstraResult result = subwayGraph.dijkstra(sourceStation);

                        // 4-3. 각 후보 도착역까지 비용 조회 + 경로(역 id 순서) 복원 → 중간 보관
                        for (Map.Entry<Long, Long> e : nearestStationByPlaceId.entrySet()) {
                                Long placeId = e.getKey();
                                Long destStation = e.getValue();
                                int[] d = result.dist().get(destStation);
                                int seconds = d != null ? d[0] : Integer.MAX_VALUE / 2;
                                int transfers = d != null ? d[1] : 0;
                                List<Long> pathIds = SubwayGraph.reconstructPath(result.prev(),
                                                sourceStation, destStation);
                                pathStationIds.addAll(pathIds);
                                drafts.add(new BurdenDraft(participant.getMemberId(), placeId,
                                                seconds, transfers, pathIds));
                        }
                }

                // 5. 경로에 등장한 모든 역 좌표를 1회 배치 조회
                Map<Long, StationCoordinate> coordById = subwayStationRepository
                                .findCoordinatesByIds(new ArrayList<>(pathStationIds)).stream()
                                .collect(Collectors.toMap(StationCoordinate::stationId, c -> c));

                // 6. 경로 역 id → {stationId,lat,lng} 변환하여 이동부담 행 생성
                List<MeetingTravelBurden> burdens = drafts.stream()
                                .map(draft -> MeetingTravelBurden.of(meetingId, draft.memberId(), draft.placeId(),
                                                draft.seconds(), draft.transfers(),
                                                toPathPoints(draft.pathIds(), coordById)))
                                .toList();

                // 7. 계산된 스냅샷 일괄 저장
                if (!burdens.isEmpty()) {
                        travelBurdenRepository.saveAll(burdens);
                }
        }

        /** 경로 역 id 리스트를 좌표 맵으로 {stationId,lat,lng} 리스트로 변환(좌표 없는 역은 제외). */
        private List<TravelPathPoint> toPathPoints(List<Long> pathIds, Map<Long, StationCoordinate> coordById) {
                return pathIds.stream()
                                .map(coordById::get)
                                .filter(Objects::nonNull)
                                .map(c -> new TravelPathPoint(c.stationId(), c.latitude(), c.longitude()))
                                .toList();
        }

        /** 좌표 배치조회 전 비용/경로(역 id) 중간 결과 보관용. */
        private record BurdenDraft(Long memberId, Long placeId, int seconds, int transfers, List<Long> pathIds) {
        }
}
