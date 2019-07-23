package com.kakao.auth.authorization.authcode;

import android.content.Intent;

import com.kakao.auth.authorization.AuthorizationResult;
import com.kakao.auth.helper.StartActivityWrapper;

/**
 * @author kevin.kang. Created on 2017. 6. 1..
 */

public class TestTalkAuthCodeService implements AuthCodeService {
    @Override
    public boolean requestAuthCode(AuthCodeRequest request, StartActivityWrapper wrapper, AuthCodeListener listener) {
        return true;
    }

    @Override
    public boolean handleActivityResult(int requestCode, int resultCode, Intent data, AuthCodeListener listener) {
        listener.onAuthCodeReceived(requestCode, AuthorizationResult.createSuccessAuthCodeResult("kakaosample_app_key://oauth?code=12345"));
        return true;
    }

    @Override
    public boolean isLoginAvailable() {
        return true;
    }
}
