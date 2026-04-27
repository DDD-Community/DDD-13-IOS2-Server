package com.bangawo.member.infrastructure.persistence.departure;

import com.bangawo.member.domain.departure.DeparturePlace;
import com.bangawo.member.domain.departure.DeparturePlaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class DeparturePlaceRepositoryImpl implements DeparturePlaceRepository {

    private final DeparturePlaceJpaRepository jpaRepository;

    @Override
    public DeparturePlace save(DeparturePlace place) {
        return jpaRepository.save(DeparturePlaceJpaEntity.from(place)).toDomain();
    }

    @Override
    public List<DeparturePlace> findAllByMemberId(Long memberId) {
        return jpaRepository.findAllByMemberId(memberId).stream()
                .map(DeparturePlaceJpaEntity::toDomain).toList();
    }

    @Override
    public Optional<DeparturePlace> findByIdAndMemberId(Long id, Long memberId) {
        return jpaRepository.findByIdAndMemberId(id, memberId).map(DeparturePlaceJpaEntity::toDomain);
    }

    @Override
    public int countByMemberId(Long memberId) {
        return jpaRepository.countByMemberId(memberId);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public void clearDefaultByMemberId(Long memberId) {
        jpaRepository.clearDefaultByMemberId(memberId);
    }
}
