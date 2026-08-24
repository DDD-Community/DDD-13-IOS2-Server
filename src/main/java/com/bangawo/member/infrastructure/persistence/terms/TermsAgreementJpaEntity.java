package com.bangawo.member.infrastructure.persistence.terms;

import com.bangawo.member.domain.terms.TermsAgreement;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/** 일반 DELETE 금지 — INSERT만 허용(법적 증적). 단, 회원 탈퇴 시 파기는 예외. */
@Entity
@Table(name = "terms_agreement",
        uniqueConstraints = @UniqueConstraint(columnNames = {"member_id", "terms_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TermsAgreementJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "terms_id", nullable = false)
    private Long termsId;

    @Column(name = "agreed_at", nullable = false)
    private LocalDateTime agreedAt;

    public static TermsAgreementJpaEntity from(TermsAgreement domain) {
        TermsAgreementJpaEntity e = new TermsAgreementJpaEntity();
        e.id = domain.getId();
        e.memberId = domain.getMemberId();
        e.termsId = domain.getTermsId();
        e.agreedAt = domain.getAgreedAt();
        return e;
    }

    public TermsAgreement toDomain() {
        return TermsAgreement.builder()
                .id(id).memberId(memberId).termsId(termsId).agreedAt(agreedAt)
                .build();
    }
}
