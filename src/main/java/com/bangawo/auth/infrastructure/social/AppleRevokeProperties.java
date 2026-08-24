package com.bangawo.auth.infrastructure.social;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Apple 연동 해제(revoke)용 자격증명.
 * 환경변수 미주입 시 모든 필드가 빈 문자열이며, {@link #isConfigured()} 가 false를 반환해
 * {@link AppleTokenRevokerImpl} 이 자동으로 no-op 처리한다.
 */
@Component
@ConfigurationProperties(prefix = "apple.revoke")
@Getter
@Setter
public class AppleRevokeProperties {

    private String teamId;
    private String keyId;
    private String clientId;
    private String privateKey;

    public boolean isConfigured() {
        return StringUtils.hasText(teamId)
                && StringUtils.hasText(keyId)
                && StringUtils.hasText(clientId)
                && StringUtils.hasText(privateKey);
    }
}
