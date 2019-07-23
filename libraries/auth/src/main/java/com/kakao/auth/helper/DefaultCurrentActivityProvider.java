package com.kakao.auth.helper;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

/**
 * @author kevin.kang. Created on 2017. 11. 28..
 */

public class DefaultCurrentActivityProvider implements CurrentActivityProvider {
    private Activity currentActivity;
    DefaultCurrentActivityProvider(final Application application) {
        application.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

            }

            @Override
            public void onActivityStarted(Activity activity) {

            }

            @Override
            public void onActivityResumed(Activity activity) {
                currentActivity = activity;
            }

            @Override
            public void onActivityPaused(Activity activity) {
                if (activity != null && activity.equals(currentActivity)) {
                    currentActivity = null;
                }
            }

            @Override
            public void onActivityStopped(Activity activity) {

            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {

            }
        });
    }
    @Override
    public Activity getCurrentActivity() {
        return currentActivity;
    }
}
