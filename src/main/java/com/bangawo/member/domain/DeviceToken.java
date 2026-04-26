package com.bangawo.member.domain;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/** iOS 디바이스 토큰 (푸시 알림용, 발송은 후순위) */
@Getter
@Builder
public class DeviceToken {
    private Long id;
    private Long memberId;
    private String token;
    private String platform;     // IOS
    private String appVersion;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
