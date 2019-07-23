/**
 * Copyright 2014-2015 Kakao Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.kakao.push;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

import com.kakao.auth.Session;
import com.kakao.util.helper.SharedPreferencesCache;
import com.kakao.util.helper.Utility;
import com.kakao.util.helper.log.Logger;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 푸시 토큰에 대한 utility 객체
 * @author MJ
 */
public class PushToken {
    private static final String PROPERTY_FCM_TOKEN = "fcm_token";
    private static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private static final String PROPERTY_EXPIRES_AT = "expires_at";
    private static final long DAY_TO_MSEC =  24 * 60 * 60 * 1000;
    private static final long SPARE_TIME =  5 * 60 * 1000;  //5분

    /**
     * 푸시 서버로 토큰 등록을 성공한 후에 기기에도 다음번 푸시 토큰을 얻어올 때 GCM으로부터 다시 얻어 오지 않아도 되도록 저장한다.
     * @param regId 저장할 푸시 토큰
     * @param appVer Current app version
     * @param expiresIn 토큰이 만료되는 기간. 단위 (일)
     */
    public static void savePushTokenToCache(final String regId, final int appVer, final Integer expiresIn) {
        final SharedPreferencesCache cache = Session.getCurrentSession().getAppCache();

        Bundle bundle = new Bundle();
        bundle.putString(PROPERTY_REG_ID, regId);
        bundle.putInt(PROPERTY_APP_VERSION, appVer);
        long pushTokenExpiresAt = new Date().getTime() + expiresIn * DAY_TO_MSEC;
        bundle.putLong(PROPERTY_EXPIRES_AT, pushTokenExpiresAt);
        cache.save(bundle);
    }

        /**
         * 푸시 서버로 토큰 삭제를 성공한 후에 기기에도 삭제한다.
         */
    public static void clearRegistrationId() {
        final SharedPreferencesCache cache = Session.getCurrentSession().getAppCache();

        List<String> keys = new ArrayList<>();
        keys.add(PROPERTY_REG_ID);
        keys.add(PROPERTY_APP_VERSION);
        keys.add(PROPERTY_EXPIRES_AT);
        cache.clear(keys);
    }

    /**
     * 기기에 저장된 푸시 토큰을 얻어 온다.
     *
     * @param context Current application context for getting app meta data (i.e. app version)
     * @return 저장된 푸시 토큰
     */
    public static String getRegistrationId(final Context context) {
        final SharedPreferencesCache cache = Session.getCurrentSession().getAppCache();
        final String registrationId = cache.getString(PROPERTY_REG_ID);
        if (TextUtils.isEmpty(registrationId)) {
            Logger.w("Registration not found.");
            return "";
        }

        int registeredVersion = cache.getInt(PROPERTY_APP_VERSION);
        int currentVersion = Utility.getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Logger.w("App version changed.");
            return "";
        }

        long registeredExpiresAt = cache.getLong(PROPERTY_EXPIRES_AT);
        long currentTime = new Date().getTime();
        if(currentTime > registeredExpiresAt + SPARE_TIME) { // 안전하게 5분
            Logger.w("push token is expired.");
            return "";
        }
        return registrationId;
    }

    /**
     * 발급받은 FCM 토큰을 앱 캐시에 임시적으로 저장한다.
     * @param fcmToken FirebaseInstanceIdService의 onTokenRefresh로 발급받은 토큰
     */
    public static void saveFcmTokenToCache(final String fcmToken) {
        final SharedPreferencesCache cache = Session.getCurrentSession().getAppCache();
        Bundle bundle = new Bundle();
        bundle.putString(PROPERTY_FCM_TOKEN, fcmToken);
        cache.save(bundle);
    }

    public static void clearFcmTokenFromCache() {

        final SharedPreferencesCache cache = Session.getCurrentSession().getAppCache();
        List<String> keys = new ArrayList<String>();
        keys.add(PROPERTY_FCM_TOKEN);
        cache.clear(keys);
    }

    public static String getFcmTokenFromCache() {
        final SharedPreferencesCache cache = Session.getCurrentSession().getAppCache();
        return cache.getString(PROPERTY_FCM_TOKEN);
    }

}
