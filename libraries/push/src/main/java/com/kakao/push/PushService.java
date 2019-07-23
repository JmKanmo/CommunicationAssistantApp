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

import android.app.Dialog;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.FirebaseApp;
import com.google.firebase.iid.FirebaseInstanceId;
import com.kakao.auth.ApiResponseCallback;
import com.kakao.auth.ISessionCallback;
import com.kakao.auth.KakaoSDK;
import com.kakao.auth.Session;
import com.kakao.auth.network.AuthNetworkService;
import com.kakao.auth.network.AuthorizedRequest;
import com.kakao.network.ErrorResult;
import com.kakao.network.response.BlankApiResponse;
import com.kakao.network.tasks.ITaskQueue;
import com.kakao.network.tasks.KakaoResultTask;
import com.kakao.network.tasks.KakaoTaskQueue;
import com.kakao.push.api.PushApi;
import com.kakao.push.request.DeregisterPushTokenRequest;
import com.kakao.push.request.GetPushTokensRequest;
import com.kakao.push.request.RegisterPushTokenRequest;
import com.kakao.push.request.SendPushRequest;
import com.kakao.push.response.GetPushTokenResponse;
import com.kakao.push.response.RegisterPushTokenResponse;
import com.kakao.push.response.model.PushTokenInfo;
import com.kakao.util.exception.KakaoException;
import com.kakao.util.helper.Utility;
import com.kakao.util.helper.log.Logger;

import java.util.List;

/**
 * 푸시 서비스 API 요청을 담당한다.
 * @author MJ
 */
public class PushService {
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    public static void init() {
        Session.getCurrentSession();
        FirebaseApp.initializeApp(KakaoSDK.getAdapter().getApplicationConfig().getApplicationContext());
        ISessionCallback callback = new ISessionCallback() {
            @Override
            public void onSessionOpened() {
                String pushToken = FirebaseInstanceId.getInstance().getToken();
                if (TextUtils.isEmpty(pushToken)) {
                    Logger.w("Fcm token is manually deleted or Google play services should be updated to the latest version.");
                    return;
                }

                String cachedToken = PushToken.getFcmTokenFromCache();
                if (TextUtils.isEmpty(cachedToken)) {
                    Logger.d("FCM token is already registered to the server.");
                    return;
                }

                String deviceId = KakaoSDK.getAdapter().getPushConfig().getDeviceUUID();
                int appVer = Utility.getAppVersion(KakaoSDK.getAdapter().getApplicationConfig().getApplicationContext());
                final ApiResponseCallback<Integer> userCallback = KakaoSDK.getAdapter().getPushConfig().getTokenRegisterCallback();

                getInstance().registerPushToken(userCallback, pushToken, deviceId, appVer);
            }

            @Override
            public void onSessionOpenFailed(KakaoException exception) {
            }
        };
        Session.getCurrentSession().addCallback(callback);
    }

    /**
     * 현 기기의 푸시 토큰을 등록한다.
     * 푸시 토큰 등록 후 푸시 토큰 삭제하기 전 또는 만료되기 전까지 서버에서 관리되어 푸시 메시지를 받을 수 있다.
     * @param callback 요청 결과에 따른 콜백
     * @param pushToken 등록할 푸시 토큰
     * @param deviceId 한 사용자가 여러 기기를 사용할 수 있기 때문에 기기에 대한 유일한 id도 필요
     * @param appVer Current app version
     */
    public void registerPushToken(final ApiResponseCallback<Integer> callback, final String pushToken, final String deviceId, final int appVer) {
        if (!checkPlayServices()) {
            Logger.w("Google play services is currently not available on this device.");
            return;
        }
        taskQueue.addTask(new KakaoResultTask<Integer>(callback) {
            @Override
            public Integer call() throws Exception {
                Integer expiresAt = api.registerPushToken(pushToken, deviceId);
                PushToken.savePushTokenToCache(pushToken, appVer, expiresAt);
                PushToken.clearFcmTokenFromCache();
                return expiresAt;
            }
        });
    }

    /**
     * 현 사용자 ID로 등록된 모든 푸시토큰 정보를 반환한다.
     * @param callback 요청 결과에 따른 콜백
     */
    public void getPushTokens(final ApiResponseCallback<List<PushTokenInfo>> callback) {
        taskQueue.addTask(new KakaoResultTask<List<PushTokenInfo>>(callback) {
            @Override
            public List<PushTokenInfo> call() throws Exception {
                List<PushTokenInfo> result = api.getPushTokens();
                PushToken.clearRegistrationId();
                return result;
            }
        });
    }

    /**
     * 사용자의 해당 기기의 푸시 토큰을 삭제한다. 대게 로그아웃시에 사용할 수 있다.
     * @param callback 요청 결과에 따른 콜백
     * @param deviceId 해당기기의 푸시 토큰만 삭제하기 위해 기기 id 필요
     */
    public void deregisterPushToken(final ApiResponseCallback<Boolean> callback, final String deviceId) {
        taskQueue.addTask(new KakaoResultTask<Boolean>(callback) {
            @Override
            public Boolean call() throws Exception {
                Boolean result = api.deregisterPushToken(deviceId);
                PushToken.clearRegistrationId();
                return result;
            }
        });
    }

    /**
     * 사용자의 모든 푸시 토큰을 삭제한다. 대게 앱 탈퇴시 사용할 수 있다.
     * @param callback 요청 결과에 따른 콜백
     */
    public void deregisterPushTokenAll(final ApiResponseCallback<Boolean> callback) {
        deregisterPushToken(callback, null);
    }

    /**
     * 자기 자신에게 푸시 메시지를 전송한다. 테스트 용도로만 사용할 수 있다. 다른 사람에게 푸시를 보내기 위해서는 서버에서 어드민키로 REST API를 사용해야한다.
     * @param callback 요청 결과에 따른 콜백
     * @param pushMessage 보낼 푸시 메시지
     * @param deviceId 푸시 메시지를 보낼 기기의 id
     */
    public void sendPushMessage(final ApiResponseCallback<Boolean> callback, final String pushMessage, final String deviceId) {
        taskQueue.addTask(new KakaoResultTask<Boolean>(callback) {
            @Override
            public Boolean call() throws Exception {
                Boolean result = api.sendPushMessage(pushMessage, deviceId);
                PushToken.clearRegistrationId();
                Log.d("TAG","이건실행되나?");
                return result;
            }
        });
    }


    private static boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(KakaoSDK.getAdapter().getApplicationConfig().getApplicationContext());
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                Dialog dialog = apiAvailability.getErrorDialog(KakaoSDK.getCurrentActivity(), resultCode, PLAY_SERVICES_RESOLUTION_REQUEST);
                dialog.show();
            } else {
                Logger.w("This device is not supported for Google play services.");
            }
            return false;
        }
        return true;
    }

    private PushApi api;
    private ITaskQueue taskQueue;

    PushService(final PushApi api, final ITaskQueue taskQueue) {
        this.api = api;
        this.taskQueue = taskQueue;
    }

    private static PushService instance = new PushService(PushApi.getInstance(),
            KakaoTaskQueue.getInstance());
    public static PushService getInstance() {
        return instance;
    }
}
