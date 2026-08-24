package com.bangawo.auth.infrastructure.persistence;

import com.bangawo.auth.domain.SocialProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MemberJpaRepository extends JpaRepository<MemberJpaEntity, Long> {
    Optional<MemberJpaEntity> findBySocialProviderAndSocialUserId(SocialProvider provider, String socialUserId);

    /** 활성 회원 존재 여부. status 프로젝션만 조회하여 전체 엔티티 로드를 회피 */
    @Query("select count(m) > 0 from MemberJpaEntity m where m.id = :id and m.status = 'ACTIVE'")
    boolean existsActiveById(@Param("id") Long id);
}
