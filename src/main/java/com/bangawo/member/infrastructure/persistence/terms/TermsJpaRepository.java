package com.bangawo.member.infrastructure.persistence.terms;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TermsJpaRepository extends JpaRepository<TermsJpaEntity, Long> {
    List<TermsJpaEntity> findAllByIdIn(List<Long> ids);
}
