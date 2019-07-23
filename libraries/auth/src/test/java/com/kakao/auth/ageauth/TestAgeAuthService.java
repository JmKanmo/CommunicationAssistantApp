package com.kakao.auth.ageauth;

import android.content.Context;
import android.os.Bundle;

import com.kakao.auth.AuthService;

/**
 * @author kevin.kang. Created on 2017. 12. 4..
 */

public class TestAgeAuthService implements AgeAuthService {
    @Override
    public int requestAgeAuth(Bundle ageAuthParams, Context context) {
        return AuthService.AgeAuthStatus.SUCCESS.getValue();
    }
}
