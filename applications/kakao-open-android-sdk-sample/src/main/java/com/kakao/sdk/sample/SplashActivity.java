package com.kakao.sdk.sample;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.kakao.auth.ISessionCallback;
import com.kakao.auth.Session;
import com.kakao.util.exception.KakaoException;

/**
 * @author leoshin
 * Created by leoshin on 15. 6. 18..
 */

public class SplashActivity extends Activity {
    private ISessionCallback callback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.d("프로세스", "1. 스플래쉬액티비티");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.intro_layout);

        callback = new ISessionCallback() {
            @Override
            public void onSessionOpened() {
                goToMainActivity();
            }

            @Override
            public void onSessionOpenFailed(KakaoException exception) {
                redirectToLoginActivity();
            }
        };

        Session.getCurrentSession().addCallback(callback);

        findViewById(R.id.intro).postDelayed(() -> {
            if (!Session.getCurrentSession().checkAndImplicitOpen()) {
                redirectToLoginActivity();
            }
        }, 500);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Session.getCurrentSession().removeCallback(callback);
    }

    private void goToMainActivity() {
        //Intent intent = new Intent(SplashActivity.this, KakaoServiceListActivity.class);
        Log.d("프로세스", "goToMainActivity");

        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void redirectToLoginActivity() {
        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}
