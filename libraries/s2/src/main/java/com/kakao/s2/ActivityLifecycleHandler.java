package com.kakao.s2;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import com.kakao.util.exception.KakaoException;
import com.kakao.util.helper.log.Logger;

/**
 * 앱의 포어그라운드/백그라운드 상태를 판단하기 위해 사용하는 액티비티 생명주기 핸들러.
 * @author kevin.kang
 * Created by kevin.kang on 2016. 9. 9..
 */

class ActivityLifecycleHandler implements Application.ActivityLifecycleCallbacks {
    private static int numOfActivities = 0;

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {
        numOfActivities++;
    }

    @Override
    public void onActivityPaused(Activity activity) {
    }

    @Override
    public void onActivityStopped(Activity activity) {
        numOfActivities--;
        if (numOfActivities == 0) {
            try {
                S2Service.getInstance().publishEvents();
            } catch (KakaoException e) {
                Logger.e(e.toString());
            }
        }
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }
}
