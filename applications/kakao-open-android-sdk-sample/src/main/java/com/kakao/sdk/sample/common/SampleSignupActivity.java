/*
  Copyright 2014-2017 Kakao Corp.

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
 */
package com.kakao.sdk.sample.common;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.kakao.auth.ApiResponseCallback;
import com.kakao.network.ErrorResult;
import com.kakao.sdk.sample.KakaoServiceListActivity;
import com.kakao.sdk.sample.MainActivity;
import com.kakao.sdk.sample.ModeActivity;
import com.kakao.sdk.sample.R;
import com.kakao.sdk.sample.common.log.Logger;
import com.kakao.sdk.sample.common.widget.KakaoToast;
import com.kakao.sdk.sample.usermgmt.ExtraUserPropertyLayout;
import com.kakao.usermgmt.ApiErrorCode;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.MeResponseCallback;
import com.kakao.usermgmt.callback.MeV2ResponseCallback;
import com.kakao.usermgmt.response.MeV2Response;
import com.kakao.usermgmt.response.model.UserAccount;
import com.kakao.usermgmt.response.model.UserProfile;
import com.kakao.util.OptionalBoolean;

import java.util.Map;

/**
 * 유효한 세션이 있다는 검증 후
 * me를 호출하여 가입 여부에 따라 가입 페이지를 그리던지 Main 페이지로 이동 시킨다.
 */
public class SampleSignupActivity extends BaseActivity {
    /**
     * Main으로 넘길지 가입 페이지를 그릴지 판단하기 위해 me를 호출한다.
     *
     * @param savedInstanceState 기존 session 정보가 저장된 객체
     */
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        Log.d("프로세스","SampleSignupActivity실행");

        super.onCreate(savedInstanceState);
        requestMe();
    }

    protected void showSignup() {
        Log.d("프로세스","showSignup");

        setContentView(R.layout.layout_usermgmt_signup);
        final ExtraUserPropertyLayout extraUserPropertyLayout = findViewById(R.id.extra_user_property);
        Button signupButton = findViewById(R.id.buttonSignup);
        signupButton.setOnClickListener(view -> requestSignUp(extraUserPropertyLayout.getProperties()));
    }

    private void requestSignUp(final Map<String, String> properties) {
        Log.d("프로세스","requestSignUp");

        UserManagement.getInstance().requestSignup(new ApiResponseCallback<Long>() {
            @Override
            public void onNotSignedUp() {
                Log.d("프로세스","onNotSignedUp");
            }

            @Override
            public void onSuccess(Long result) {
                Log.d("프로세스","onSuccess");

                requestMe();
            }

            @Override
            public void onFailure(ErrorResult errorResult) {
                Log.d("프로세스","onFailure");
                final String message = "UsermgmtResponseCallback : failure : " + errorResult;
                com.kakao.util.helper.log.Logger.w(message);
                KakaoToast.makeToast(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                finish();
            }

            @Override
            public void onSessionClosed(ErrorResult errorResult) {
                Log.d("프로세스","onSessionClosed");
            }
        }, properties);
    }

    /**
     * 사용자의 상태를 알아 보기 위해 me API 호출을 한다.
     */
    protected void requestMe() {
        Log.d("프로세스","requestMe");
        UserManagement.getInstance().me(new MeV2ResponseCallback() {
            @Override
            public void onFailure(ErrorResult errorResult) {
                Log.d("프로세스","onFailure");
                String message = "failed to get user info. msg=" + errorResult;
                Logger.d(message);

                int result = errorResult.getErrorCode();
                if (result == ApiErrorCode.CLIENT_ERROR_CODE) {
                    KakaoToast.makeToast(getApplicationContext(), getString(R.string.error_message_for_service_unavailable), Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    redirectLoginActivity();
                }
            }

            @Override
            public void onSessionClosed(ErrorResult errorResult) {
                Log.d("프로세스","onSessionClosed");
                Logger.e("onSessionClosed");
                redirectLoginActivity();
            }

            @Override
            public void onSuccess(MeV2Response result) {
                Log.d("프로세스","onSuccess");

                if (result.hasSignedUp() == OptionalBoolean.FALSE) {
                    showSignup();
                } else {
                    redirectMainActivity();
                }
            }
        });
    }

    private void redirectMainActivity() {
        Log.d("프로세스","redirectMainActivity");
        Intent intent = new Intent(this, MainActivity.class);
        Log.d("TAG",MeV2Response.nickname);
        intent.putExtra("nickname",MeV2Response.nickname);
        intent.putExtra("id",MeV2Response.id);
        intent.putExtra("email",UserAccount.email);
        intent.putExtra("phoneNumber",UserAccount.phoneNumber);
        Log.d("TAG",String.valueOf(MeV2Response.id));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}
