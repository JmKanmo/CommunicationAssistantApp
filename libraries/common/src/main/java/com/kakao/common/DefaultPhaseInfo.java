package com.kakao.common;

import android.content.Context;
import android.support.annotation.NonNull;

import com.kakao.util.helper.CommonProtocol;
import com.kakao.util.helper.Utility;

/**
 * {@link KakaoContextService#setPhaseInfo(PhaseInfo)}를 통해 별도의 정보를 입력하지 않았을 때 SDK 에서
 * 기본적으로 사용하게 되는 phase 별 정보.
 */
public class DefaultPhaseInfo implements PhaseInfo {
    private final KakaoPhase phase;
    private final String appKey;
    private final String clientSecret;

    DefaultPhaseInfo(final Context context) {
        final String phaseFromMetaData = Utility.getMetadata(context, CommonProtocol.PHASE);
        if (phaseFromMetaData != null) {
            this.phase = KakaoPhase.ofName(phaseFromMetaData);
        } else {
            this.phase = KakaoPhase.PRODUCTION;
        }
        this.appKey = Utility.getMetadata(context, CommonProtocol.APP_KEY_PROPERTY);
        this.clientSecret = Utility.getMetadata(context, CommonProtocol.CLIENT_SECRET_PROPERTY);
    }

    @NonNull
    @Override
    public KakaoPhase phase() {
        return phase;
    }

    @Override
    public String appKey() {
        return appKey;
    }

    @Override
    public String clientSecret() {
        return clientSecret;
    }
}
