package com.kakao.test.common;

import android.content.Context;
import android.content.Intent;

import com.kakao.util.KakaoUtilService;

public class TestKakaoUtilService implements KakaoUtilService {
    @Override
    public Intent resolveIntent(Context context, Intent intent, int minVersion) {
        return intent;
    }
}
