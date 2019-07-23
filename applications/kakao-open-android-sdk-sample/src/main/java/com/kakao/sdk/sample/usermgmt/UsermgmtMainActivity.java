/*
  Copyright 2014-2018 Kakao Corp.

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
package com.kakao.sdk.sample.usermgmt;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.kakao.auth.AccessTokenCallback;
import com.kakao.auth.ApiResponseCallback;
import com.kakao.auth.AuthService;
import com.kakao.auth.Session;
import com.kakao.auth.authorization.accesstoken.AccessToken;
import com.kakao.auth.network.response.AccessTokenInfoResponse;
import com.kakao.network.ErrorResult;
import com.kakao.sdk.sample.R;
import com.kakao.sdk.sample.common.BaseActivity;
import com.kakao.sdk.sample.common.log.Logger;
import com.kakao.sdk.sample.common.widget.KakaoToast;
import com.kakao.sdk.sample.common.widget.ProfileLayout;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.LogoutResponseCallback;
import com.kakao.usermgmt.callback.MeV2ResponseCallback;
import com.kakao.usermgmt.callback.UnLinkResponseCallback;
import com.kakao.usermgmt.response.MeV2Response;
import com.kakao.util.OptionalBoolean;

/**
 * 가입된 사용자가 보게되는 메인 페이지로 사용자 정보 불러오기/update, 로그아웃, 탈퇴 기능을 테스트 한다.
 */
public class UsermgmtMainActivity extends BaseActivity {
    private MeV2Response response;
    private ProfileLayout profileLayout;
    private ExtraUserPropertyLayout extraUserPropertyLayout;

    /**
     * 로그인 또는 가입창에서 넘긴 유저 정보가 있다면 저장한다.
     * @param savedInstanceState 기존 session 정보가 저장된 객체
     */
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeView();
        profileLayout.requestMe();
    }

    /**
     * 사용자의 정보를 변경 저장하는 API를 호출한다.
     */
    private void onClickUpdateProfile() {
        final Map<String, String> properties = extraUserPropertyLayout.getProperties();
        UserManagement.getInstance().requestUpdateProfile(new UsermgmtResponseCallback<Long>() {
            @Override
            public void onSuccess(Long result) {
                KakaoToast.makeToast(getApplicationContext(), "succeeded to update user profile", Toast.LENGTH_SHORT).show();
                Logger.d("succeeded to update user profile");
            }

        }, properties);
    }

    private void onClickAccessTokenInfo() {
        AuthService.getInstance().requestAccessTokenInfo(new ApiResponseCallback<AccessTokenInfoResponse>() {
            @Override
            public void onSessionClosed(ErrorResult errorResult) {
                redirectLoginActivity();
            }

            @Override
            public void onNotSignedUp() {
                // not happened
            }

            @Override
            public void onFailure(ErrorResult errorResult) {
                String message = "failed to get access token info. msg=" + errorResult;
                Logger.e(message);
                KakaoToast.makeToast(getApplicationContext(), message, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onSuccess(AccessTokenInfoResponse accessTokenInfoResponse) {
                long userId = accessTokenInfoResponse.getUserId();
                Logger.d("this access token is for userId=" + userId);

                long expiresInMilis = accessTokenInfoResponse.getExpiresInMillis();
                Logger.d("this access token expires after " + expiresInMilis + " milliseconds.");

                KakaoToast.makeToast(getApplicationContext(), "this access token for user(id="+ userId+") expires after " + expiresInMilis + " milliseconds.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void onClickLogout() {
        UserManagement.getInstance().requestLogout(new LogoutResponseCallback() {
            @Override
            public void onCompleteLogout() {
                redirectLoginActivity();
            }
        });
    }

    private void onClickUnlink() {
        final String appendMessage = getString(R.string.com_kakao_confirm_unlink);
        new AlertDialog.Builder(this)
            .setMessage(appendMessage)
            .setPositiveButton(getString(R.string.com_kakao_ok_button),
                    (dialog, which) -> {
                        UserManagement.getInstance().requestUnlink(new UnLinkResponseCallback() {
                            @Override
                            public void onFailure(ErrorResult errorResult) {
                                Logger.e(errorResult.toString());
                            }

                            @Override
                            public void onSessionClosed(ErrorResult errorResult) {
                                redirectLoginActivity();
                            }

                            @Override
                            public void onNotSignedUp() {
                                redirectSignupActivity();
                            }

                            @Override
                            public void onSuccess(Long result) {
                                redirectLoginActivity();
                            }
                        });
                        dialog.dismiss();
                    })
            .setNegativeButton(getString(R.string.com_kakao_cancel_button),
                    (dialog, which) -> dialog.dismiss()).show();

    }

    private void initializeView() {
        setContentView(R.layout.layout_usermgmt_main);
        ((TextView)findViewById(R.id.text_title)).setText(getString(R.string.text_usermgmt));
        findViewById(R.id.title_back).setOnClickListener(v -> finish());
        initializeButtons();
        initializeProfileView();
    }

    private void initializeButtons() {
        final Button buttonMe = findViewById(R.id.buttonMe);
        buttonMe.setOnClickListener(view -> updateScopes());

        final Button buttonUpdateProfile = findViewById(R.id.buttonUpdateProfile);
        buttonUpdateProfile.setOnClickListener(view -> onClickUpdateProfile());

        final Button logoutButton = findViewById(R.id.logout_button);
        logoutButton.setOnClickListener(view -> onClickLogout());

        final Button unlinkButton = findViewById(R.id.unlink_button);
        unlinkButton.setOnClickListener(view -> onClickUnlink());

        final Button tokenInfoButton = findViewById(R.id.token_info_button);
        tokenInfoButton.setOnClickListener(view -> onClickAccessTokenInfo());
    }

    private void initializeProfileView() {
        profileLayout = findViewById(R.id.com_kakao_user_profile);
        profileLayout.setMeV2ResponseCallback(new MeV2ResponseCallback() {
            @Override
            public void onFailure(ErrorResult errorResult) {
                String message = "failed to get user info. msg=" + errorResult;
                Logger.e(message);
                KakaoToast.makeToast(getApplicationContext(), message, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onSessionClosed(ErrorResult errorResult) {
                redirectLoginActivity();
            }

            @Override
            public void onSuccess(MeV2Response result) {
                response = result;
                KakaoToast.makeToast(getApplicationContext(), "succeeded to get user profile", Toast.LENGTH_SHORT).show();
                updateLayouts(result);
            }
        });

        extraUserPropertyLayout = findViewById(R.id.extra_user_property);
    }

    private void updateScopes() {
        List<String> scopes = new ArrayList<>();
        if (response.getKakaoAccount().needsScopeAccountEmail()) {
            scopes.add("account_email");
        }
        if (response.getKakaoAccount().needsScopePhoneNumber()) {
            scopes.add("phone_number");
        }
//        if (response.getKakaoAccount().needsScopeIsKakaotalkUser()) {
//            scopes.add("is_kakaotalk_user");
//        }

//        if (response.getKakaoAccount().needsScopeAgeRange()) {
//            scopes.add("age_range");
//        }

//        if (response.getKakaoAccount().needsScopeGender()) {
//            scopes.add("gender");
//        }
//
//        if (response.getKakaoAccount().needsScopeBirthday()) {
//            scopes.add("birthday");
//        }

        if (scopes.isEmpty()) {
            KakaoToast.makeToast(getApplicationContext(), "User has all the required scopes", Toast.LENGTH_LONG).show();
            return;
        }

        Session.getCurrentSession().updateScopes(UsermgmtMainActivity.this, scopes, new AccessTokenCallback() {
            @Override
            public void onAccessTokenReceived(AccessToken accessToken) {
                profileLayout.requestMe();
            }

            @Override
            public void onAccessTokenFailure(ErrorResult errorResult) {
                KakaoToast.makeToast(getApplicationContext(), "Failed to update scopes", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void updateLayouts(MeV2Response result) {
        profileLayout.setUserInfo(result);
        if (result.getProperties() != null) {
            extraUserPropertyLayout.showProperties(result.getProperties());
        }
    }

    private abstract class UsermgmtResponseCallback<T> extends ApiResponseCallback<T> {
        @Override
        public void onNotSignedUp() {
            redirectSignupActivity();
        }

        @Override
        public void onFailure(ErrorResult errorResult) {
            String message = "failed to get user info. msg=" + errorResult;
            Logger.e(message);
            KakaoToast.makeToast(getApplicationContext(), message, Toast.LENGTH_LONG).show();
        }

        @Override
        public void onSessionClosed(ErrorResult errorResult) {
            redirectLoginActivity();
        }
    }
}
