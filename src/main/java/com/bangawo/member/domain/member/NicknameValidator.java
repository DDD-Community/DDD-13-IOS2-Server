package com.bangawo.member.domain.member;

import com.bangawo.global.error.BusinessException;
import com.bangawo.global.error.ErrorCode;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.regex.Pattern;

/** 닉네임 금칙어 필터 (정적 리스트 + 정규식 + 자모 우회 대응) */
@Component
public class NicknameValidator {

    private static final List<String> FORBIDDEN_WORDS = List.of("관리자", "admin", "운영자");
    private static final List<Pattern> FORBIDDEN_PATTERNS = List.of(Pattern.compile("[ㅅㅆ][ㅂ]"));

    public void validate(String nickname) {
        String lower = nickname.toLowerCase();
        for (String word : FORBIDDEN_WORDS) {
            if (lower.contains(word.toLowerCase()))
                throw new BusinessException(ErrorCode.NICKNAME_FORBIDDEN_WORD);
        }
        for (Pattern p : FORBIDDEN_PATTERNS) {
            if (p.matcher(nickname).find())
                throw new BusinessException(ErrorCode.NICKNAME_FORBIDDEN_WORD);
        }
    }
}
