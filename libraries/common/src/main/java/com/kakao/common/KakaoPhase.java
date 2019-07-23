package com.kakao.common;

/**
 * SDK 의 deploy phase 설정을 나타내는 enum class. 인하우스 또는 파트너 서비스만 사용 가능.
 */
public enum KakaoPhase {
    /**
     * dev (alpha) phase
     */
    DEV("dev"),
    /**
     * sandbox phase
     */
    SANDBOX("sandbox"),
    /**
     * cbt (beta) phase
     */
    CBT("cbt"),
    /**
     * production (real, release) phase
     */
    PRODUCTION("production");

    KakaoPhase(final String phaseName) {
        this.phaseName = phaseName;
    }

    /**
     * meta-data의 com.kakao.sdk.Phase 키 값과 매치되어야 하는 값. dev, sandbox, cbt, production 중 하나.
     *
     * @return phase name
     */
    @SuppressWarnings("unused")
    public String phaseName() {
        return phaseName;
    }

    public static KakaoPhase ofName(final String name) {
        switch (name) {
            case "dev":
                return KakaoPhase.DEV;
            case "sandbox":
                return KakaoPhase.SANDBOX;
            case "cbt":
                return KakaoPhase.CBT;
            case "production":
            default:
                return KakaoPhase.PRODUCTION;
        }
    }

    private final String phaseName;
}
