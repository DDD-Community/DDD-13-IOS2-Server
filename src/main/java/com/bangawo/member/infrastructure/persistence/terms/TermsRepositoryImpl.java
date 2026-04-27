package com.bangawo.member.infrastructure.persistence.terms;

import com.bangawo.member.domain.terms.Terms;
import com.bangawo.member.domain.terms.TermsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class TermsRepositoryImpl implements TermsRepository {

    private final TermsJpaRepository jpaRepository;

    @Override
    public List<Terms> findAllCurrent() {
        return jpaRepository.findAll().stream().map(TermsJpaEntity::toDomain).toList();
    }

    @Override
    public List<Terms> findAllById(List<Long> ids) {
        return jpaRepository.findAllByIdIn(ids).stream().map(TermsJpaEntity::toDomain).toList();
    }
}
