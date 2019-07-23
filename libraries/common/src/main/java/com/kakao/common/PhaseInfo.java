package com.kakao.common;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Deploy phase 와 phase 별 필요한 정보를 담기 위한 클래스.
 *
 * Class for storing phase-specific information.
 */
public interface PhaseInfo {
    /**
     * SDK에서 사용할 Deploy phase
     *
     * @return current server deploy phase phase
     */
    @NonNull KakaoPhase phase();

    /**
     * Phase 에 맞는 앱의 네이티브 앱키
     * @return native app key corresponding to the deploy phase
     */
    @Nullable String appKey();

    /**
     * 앱의 카카오 OAuth 토큰 발급 시 사용할 client secret 값. 만약 client secret 기능을 설정하지 않았다면
     * null 을 사용하여야 함.
     *
     * @return current client secret value corresponding to the deploy phase
     */
    @Nullable String clientSecret();
}
