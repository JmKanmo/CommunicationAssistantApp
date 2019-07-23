package com.kakao.auth.authorization.authcode;

import android.content.Intent;

import com.kakao.auth.helper.StartActivityWrapper;

/**
 * @author kevin.kang. Created on 2017. 6. 1..
 */

public class TestWebAuthCodeService implements AuthCodeService {
    @Override
    public boolean requestAuthCode(AuthCodeRequest request, StartActivityWrapper wrapper, AuthCodeListener listener) {
        return true;
    }

    @Override
    public boolean handleActivityResult(int requestCode, int resultCode, Intent data, AuthCodeListener listener) {
        return false;
    }

    @Override
    public boolean isLoginAvailable() {
        return true;
    }
}
