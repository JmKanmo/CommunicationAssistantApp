package com.kakao.push;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.kakao.auth.KakaoSDK;
import com.kakao.auth.Session;
import com.kakao.util.helper.Utility;
import com.kakao.util.helper.log.Logger;

/**
 * @author kevin.kang
 * Created by kevin.kang on 2017. 1. 26..
 */

public class KakaoFirebaseInstanceIdService extends FirebaseInstanceIdService {
    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        try {
            if (Session.getCurrentSession().isOpened()) {
                PushService.getInstance().registerPushToken(
                        KakaoSDK.getAdapter().getPushConfig().getTokenRegisterCallback(),
                        FirebaseInstanceId.getInstance().getToken(),
                        KakaoSDK.getAdapter().getPushConfig().getDeviceUUID(),
                        Utility.getAppVersion(this)
                );
            } else {
                PushToken.saveFcmTokenToCache(FirebaseInstanceId.getInstance().getToken());
            }
        } catch (IllegalStateException e) {
            Logger.e("Session is not initialized. You should call KakaoSDK.init() first.");
        } catch (NullPointerException e) {
            Logger.e("There is something wrong with your KakaoAdapter settings. Check again if it is properly set.");
        }
    }
}
