package com.kakao.auth.helper;

import android.app.Activity;
import android.app.Application;

import com.kakao.auth.KakaoSDK;

/**
 * @author kevin.kang. Created on 2017. 11. 28..
 */

public interface CurrentActivityProvider {
    Activity getCurrentActivity();

    class Factory {
        private static CurrentActivityProvider instance = new DefaultCurrentActivityProvider((Application) KakaoSDK.getAdapter().getApplicationConfig().getApplicationContext());

        public static CurrentActivityProvider getInstance() {
            return instance;
        }
    }
}
