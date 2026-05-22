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

    private static final int MAX_PLACES = 3;
    private final DeparturePlaceRepository repository;

    @Transactional
    public DeparturePlace create(Long memberId, String label, String address,
                                  double latitude, double longitude, boolean isDefault) {
        int count = repository.countByMemberId(memberId);
        if (count >= MAX_PLACES)
            throw new BusinessException(ErrorCode.DEPARTURE_PLACE_LIMIT_EXCEEDED);

        if (count == 0) isDefault = true;
        if (isDefault) repository.clearDefaultByMemberId(memberId);

        return repository.save(DeparturePlace.builder()
                .memberId(memberId).label(label).address(address)
                .coordinate(new Coordinate(latitude, longitude))
                .isDefault(isDefault)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build());
    }

    @Transactional
    public DeparturePlace update(Long memberId, Long placeId, String label, String address,
                                  double latitude, double longitude) {
        DeparturePlace place = repository.findByIdAndMemberId(placeId, memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DEPARTURE_PLACE_NOT_FOUND));
        place.update(label, address, latitude, longitude);
        return repository.save(place);
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
