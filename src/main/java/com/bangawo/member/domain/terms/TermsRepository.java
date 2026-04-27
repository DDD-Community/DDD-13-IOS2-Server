package com.bangawo.member.domain.terms;

import java.util.List;

public interface TermsRepository {
    List<Terms> findAllCurrent();
    List<Terms> findAllById(List<Long> ids);
}
