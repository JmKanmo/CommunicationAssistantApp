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

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.kakao.auth.ApiResponseCallback;
import com.kakao.network.ErrorResult;
import com.kakao.util.helper.Utility;
import com.kakao.util.helper.log.Logger;

import java.io.IOException;

/**
 * 푸시 토큰 등록/삭제를 도와주는 Activity.
 * 카카오계정 로그인과 함께 푸시 서비스를 쓰는 앱의 로그인 후 보이는 activity에서 extends한다.
 * 푸시 토큰 등록/삭제를 대행해준다.
 * @author MJ
 */
public abstract class PushActivity extends Activity {
    private static final String GCM_PROJECT_ID_KEY = "com.kakao.sdk.GcmProjectId";
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    private GoogleCloudMessaging gcm;
    private String regId;
    private int appVer;
    protected String deviceUUID;
    protected Activity self;

    /**
     * GCM으로부터 푸시 토큰을 얻어 카카오 푸시 서버에 등록하고 이를 SharedPreference에 저장한다.
     * @param savedInstanceState activity 내려갈 때 저장해둔 정보
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        self = PushActivity.this;
        if (checkPlayServices()) {
            gcm = GoogleCloudMessaging.getInstance(this);
            regId = PushToken.getRegistrationId(this);
            appVer = Utility.getAppVersion(this);
            deviceUUID = getDeviceUUID();
            if (regId.isEmpty()) {
                registerPushToken(null);
            }
        } else {
            Logger.w("No valid Google Play Services APK found.");
        }
    }

    /**
     * GCM을 쓸수 있는 앱인지 판단한다.
     */
    @Override
    protected void onResume() {
        super.onResume();
        checkPlayServices();
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Logger.w("This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    /**
     * kakao_strings.xml에 등록한 gcm_project_number를 이용하여 푸시 토큰을 받고, 로그인 후 알수 있는 사용자 id와 해당 기기의 유일한 device id를 이용하여 푸시 토큰을 등록한다.
     * @param callback 푸시 토큰 등록에 대한 콜백 처리를 직접하고 싶은 경우 handler를 넘겨준다. 넘겨주지 않는 경우는 기본 handler가 동작한다.
     */
    protected void registerPushToken(final ApiResponseCallback<Integer> callback) {
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(PushActivity.this);
                    }
                    String gcmProjectNumber = Utility.getMetadata(PushActivity.this, GCM_PROJECT_ID_KEY);
                    regId = gcm.register(gcmProjectNumber);
                    return Boolean.TRUE;
                } catch (IOException ex) {
                    Logger.w(ex);
                    return Boolean.FALSE;
                }
            }

            @Override
            protected void onPostExecute(final Boolean registerationResult){
                if(registerationResult) {
                    Logger.d("regId : " + regId);
                    Logger.d("appVer : " + appVer);
                    Logger.d("regId : " + regId);
                    Logger.d("deviceUUID : " + deviceUUID);

                    ApiResponseCallback<Integer> responseCallback = callback;
                    if(responseCallback == null) {
                        responseCallback = new ApiResponseCallback<Integer>() {
                            @Override
                            public void onFailure(ErrorResult errorResult) {
                                Toast.makeText(getApplicationContext(), errorResult.toString(), Toast.LENGTH_LONG).show();
                            }

                            @Override
                            public void onSessionClosed(ErrorResult errorResult) {
                                redirectLoginActivity();
                            }

                            @Override
                            public void onNotSignedUp() {
                                Logger.e("You should signup first");
                            }

                            @Override
                            public void onSuccess(Integer result) {

                            }
                        };
                    }

                    PushService.getInstance().registerPushToken(responseCallback, regId, deviceUUID, appVer);
                }
            }

        }.execute(null, null, null);
    }

    /**
     * 푸시 서비스 사용중 세션이 닫히면, 즉 다시 로그인이 필요한 상태이면 호출되는 메쏘드로 로그인 페이지로 전환해주도록 구현해야 한다.
     */
    protected abstract void redirectLoginActivity() ;

    /**
     * 한 사용자에게 여러 기기를 허용하기 위해 기기별 id가 필요하다.
     * 최대 64자 지원되므로 생성시 주의한다.
     * @return 기기의 unique id
     */
    protected abstract String getDeviceUUID();
}
