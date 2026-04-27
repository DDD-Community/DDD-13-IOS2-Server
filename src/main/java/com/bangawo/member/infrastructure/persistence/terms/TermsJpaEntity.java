package com.bangawo.member.infrastructure.persistence.terms;

import com.bangawo.member.domain.terms.Terms;
import com.bangawo.member.domain.terms.TermsType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "terms", uniqueConstraints = @UniqueConstraint(columnNames = {"type", "version"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TermsJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TermsType type;

    @Column(nullable = false, length = 20)
    private String version;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "is_required", nullable = false)
    private boolean isRequired;

    @Column(name = "effective_from", nullable = false)
    private LocalDateTime effectiveFrom;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Terms toDomain() {
        return Terms.builder()
                .id(id).type(type).version(version).title(title)
                .content(content).isRequired(isRequired).effectiveFrom(effectiveFrom)
                .build();
    }
}
