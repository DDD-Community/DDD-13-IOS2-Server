package com.bangawo.subway.infrastructure.persistence;

import com.bangawo.subway.domain.SubwayEdge;
import com.bangawo.subway.domain.SubwayEdgeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class SubwayEdgeRepositoryImpl implements SubwayEdgeRepository {

    private final SubwayEdgeJpaRepository jpaRepository;

    @Override
    public List<SubwayEdge> findAll() {
        return jpaRepository.findAll().stream()
                .map(SubwayEdgeJpaEntity::toDomain)
                .toList();
    }
}
