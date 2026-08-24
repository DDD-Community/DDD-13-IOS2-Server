package com.bangawo.member.domain.departure;

import java.util.List;
import java.util.Optional;

public interface DeparturePlaceRepository {
    DeparturePlace save(DeparturePlace place);
    List<DeparturePlace> findAllByMemberId(Long memberId);
    List<DeparturePlace> findAllByMemberIdIn(List<Long> memberIds);
    Optional<DeparturePlace> findByIdAndMemberId(Long id, Long memberId);
    int countByMemberId(Long memberId);
    void deleteById(Long id);
    /** 해당 회원의 기본 출발지 해제 */
    void clearDefaultByMemberId(Long memberId);
    java.util.Optional<DeparturePlace> findDefaultByMemberId(Long memberId);
    /** 해당 회원의 출발지를 물리 삭제 (탈퇴 시 파기 전용) */
    void deleteAllByMemberId(Long memberId);
}
