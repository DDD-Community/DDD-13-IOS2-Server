package com.bangawo.member.application;

import com.bangawo.global.common.Coordinate;
import com.bangawo.global.error.BusinessException;
import com.bangawo.global.error.ErrorCode;
import com.bangawo.member.domain.departure.DeparturePlace;
import com.bangawo.member.domain.departure.DeparturePlaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DeparturePlaceService {

    private static final int MAX_PLACES = 10;
    private final DeparturePlaceRepository repository;

    /** 출발지 추가 (최대 10개, 기본 출발지면 기존 기본 해제) */
    @Transactional
    public DeparturePlace create(Long memberId, String label, String address,
                                  double latitude, double longitude, boolean isDefault) {
        if (repository.countByMemberId(memberId) >= MAX_PLACES)
            throw new BusinessException(ErrorCode.DEPARTURE_PLACE_LIMIT_EXCEEDED);

        if (isDefault) repository.clearDefaultByMemberId(memberId);

        return repository.save(DeparturePlace.builder()
                .memberId(memberId).label(label).address(address)
                .coordinate(new Coordinate(latitude, longitude))
                .isDefault(isDefault)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build());
    }

    public List<DeparturePlace> getAll(Long memberId) {
        return repository.findAllByMemberId(memberId);
    }

    /** 출발지 삭제 (기본 출발지는 삭제 불가) */
    @Transactional
    public void delete(Long memberId, Long placeId) {
        DeparturePlace place = repository.findByIdAndMemberId(placeId, memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DEPARTURE_PLACE_NOT_FOUND));
        if (place.isDefault())
            throw new BusinessException(ErrorCode.DEFAULT_DEPARTURE_PLACE_CANNOT_DELETE);
        repository.deleteById(placeId);
    }
}
