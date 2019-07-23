package com.kakao.common;

import android.app.Activity;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;

import java.security.MessageDigest;

/**
 * App context 를 통하여 필요한 configuration 들을 초기화하고, Phase 에 관련된 정보들을 주입받을 수 있는 클래스.
 *
 */
public class KakaoContextService {
    private PhaseInfo phaseInfo;
    private IConfiguration configuration;

    /**
     * Application context에서 얻어오는 카카오 SDK에서 필요로 하는 정보들을 담은 {@link IConfiguration} 객체를
     * 리턴한다.
     *
     * @return app configuration
     */
    public IConfiguration getAppConfiguration() {
        return configuration;
    }

    /**
     * Meta-data를 통하여 설정된 값 말고 별도의 값을 런타임에 제공하기 위하여 사용하는 메소드
     *
     * @see PhaseInfo
     *
     * @param phaseInfo SDK에서 사용할 phase 별 정보
     */
    public void setPhaseInfo(final PhaseInfo phaseInfo) {
        if (phaseInfo != null) {
            this.phaseInfo = phaseInfo;
        }
    }

    /**
     * SDK에 현재 설정되어 있는 {@link PhaseInfo} 값을 리턴한다.
     *
     * @return current phase info used by the SDK
     */
    public PhaseInfo phaseInfo() {
        return phaseInfo;
    }

    public synchronized void initialize(final Context context) {
        if (this.configuration == null) {
            this.configuration = IConfiguration.Factory.createConfiguration(context);
        }
        if (this.phaseInfo == null) {
            this.phaseInfo = new DefaultPhaseInfo(context);
        }
    }

    public synchronized void initialize(final Context context, final PhaseInfo phaseInfo) {
        if (this.configuration == null) {
            this.configuration = IConfiguration.Factory.createConfiguration(context);
        }
        this.phaseInfo = phaseInfo;
    }

    public KakaoContextService(IConfiguration configuration, PhaseInfo phaseInfo) {
        this.configuration = configuration;
        this.phaseInfo = phaseInfo;
    }

    private KakaoContextService() {
    }

    private static KakaoContextService instance;

    public static synchronized KakaoContextService getInstance() {
        if (instance == null) {
            instance = new KakaoContextService();
        }
        return instance;
    }
}
