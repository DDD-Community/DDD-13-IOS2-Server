package com.bangawo.auth.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface RefreshTokenJpaRepository extends JpaRepository<RefreshTokenJpaEntity, Long> {

    Optional<RefreshTokenJpaEntity> findByTokenHashAndRevokedAtIsNull(String tokenHash);

    /** 해당 회원의 유효한(폐기되지 않은) 모든 토큰을 폐기 처리 */
    @Modifying
    @Query("UPDATE RefreshTokenJpaEntity r SET r.revokedAt = CURRENT_TIMESTAMP WHERE r.memberId = :memberId AND r.revokedAt IS NULL")
    void revokeAllByMemberId(Long memberId);

    /** 해당 회원의 토큰을 물리 삭제 (탈퇴 시 파기 전용) */
    @Modifying
    @Query("DELETE FROM RefreshTokenJpaEntity r WHERE r.memberId = :memberId")
    void deleteAllByMemberId(Long memberId);
}
