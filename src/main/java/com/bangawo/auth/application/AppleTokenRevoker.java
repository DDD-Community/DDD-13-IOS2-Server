package com.bangawo.auth.application;

/** Apple 연동 해제(Sign in with Apple 계정 삭제 시 토큰 폐기) 추상화. */
public interface AppleTokenRevoker {

    /**
     * Apple 연동을 해제한다.
     * 자격증명 미설정, authorization code 무효, 네트워크 실패 등 어떤 이유로든
     * 예외를 던지지 않고 false를 반환한다 (best-effort — 실패해도 탈퇴는 진행되어야 함).
     */
    boolean revoke(String authorizationCode);
}
