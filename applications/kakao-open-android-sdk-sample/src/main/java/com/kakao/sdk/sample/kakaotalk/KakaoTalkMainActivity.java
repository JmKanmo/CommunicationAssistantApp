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
package com.kakao.sdk.sample.kakaotalk;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.kakao.friends.request.FriendsRequest.FriendType;
import com.kakao.kakaotalk.callback.TalkResponseCallback;
import com.kakao.kakaotalk.response.KakaoTalkProfile;
import com.kakao.kakaotalk.v2.KakaoTalkService;
import com.kakao.network.ErrorResult;
import com.kakao.sdk.sample.R;
import com.kakao.sdk.sample.common.BaseActivity;
import com.kakao.sdk.sample.common.widget.KakaoToast;
import com.kakao.sdk.sample.common.widget.ProfileLayout;
import com.kakao.sdk.sample.FriendsMainActivity;
import com.kakao.sdk.sample.KakaoTalkFriendListActivity;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.LogoutResponseCallback;

/**
 * 카카오톡 API인 프로필를 테스트 한다.
 * 유효한 세션이 있다는 검증을 {@link RootLoginActivity}로 부터 받고 보여지는 로그인 된 페이지이다.
 */
public class KakaoTalkMainActivity extends BaseActivity {
    private ProfileLayout profileLayout;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        Log.d("프로세스","카카오톡메인화면실행");

        super.onCreate(savedInstanceState);
        initializeView();
        onClickProfile();
    }

    // profile view에서 talk profile을 update 한다.
    private void applyTalkProfileToView(final KakaoTalkProfile talkProfile) {
        Log.d("프로세스","카카오톡메인화면실행_메소드applyTalkProfileToView");
        if (profileLayout != null) {
            final String profileImageURL = talkProfile.getProfileImageUrl();
            if (profileImageURL != null)
                profileLayout.setProfileURL(profileImageURL);

            final String nickName = talkProfile.getNickName();
            if (nickName != null)
                profileLayout.setNickname(nickName);
        }
    }

    private void onClickProfile() {
        Log.d("프로세스","카카오톡메인화면실행_메소드onClickProfile");
        KakaoTalkService.getInstance().requestProfile(new KakaoTalkResponseCallback<KakaoTalkProfile>() {
            @Override
            public void onSuccess(KakaoTalkProfile result) {
                Log.d("프로세스","카카오톡메인화면실행_메소드onClickProfile_onSuccess");

                KakaoToast.makeToast(getApplicationContext(), "success to get talk profile", Toast.LENGTH_SHORT).show();
                applyTalkProfileToView(result);
            }

            @Override
            public void onNotKakaoTalkUser() {
                Log.d("프로세스","카카오톡메인화면실행_메소드onClickProfile_onNotKakaoTalkUser");

                super.onNotKakaoTalkUser();
                if (profileLayout != null) {
                    profileLayout.setUserId("Not a KakaoTalk user");
                }
            }
        });
    }

    private void onClickLogout() {
        Log.d("프로세스","onNotKakaoTalkUser_메소드onClickLogout");

        UserManagement.getInstance().requestLogout(new LogoutResponseCallback() {
            @Override
            public void onCompleteLogout() {
                redirectLoginActivity();
            }
        });
    }

    private void initializeView() {
        Log.d("프로세스","onNotKakaoTalkUser_메소드initializeView");

        setContentView(R.layout.layout_kakaotalk_main);
        initializeButtons();
        initializeProfileView();
    }

    private void initializeButtons() {
        Log.d("프로세스","onNotKakaoTalkUser_메소드initializeButtons");

        findViewById(R.id.profile_button).setOnClickListener(v -> onClickProfile());

        findViewById(R.id.talk_friends).setOnClickListener(v -> showTalkFriendListActivity());

        findViewById(R.id.logout_button).setOnClickListener(v -> onClickLogout());

        findViewById(R.id.talk_chat_list).setOnClickListener(v -> showChatListActivity());
    }

    private void showChatListActivity() {
        Log.d("프로세스","onNotKakaoTalkUser_메소드showChatListActivity");
        Intent intent = new Intent(this, KakaoTalkChatListActivity.class);
        startActivity(intent);
    }

    private void showTalkFriendListActivity() {
        Log.d("프로세스","onNotKakaoTalkUser_메소드showTalkFriendListActivity");

        Intent intent = new Intent(this, KakaoTalkFriendListActivity.class);

        String[] friendType = {FriendType.KAKAO_TALK.name()};
        intent.putExtra(FriendsMainActivity.EXTRA_KEY_SERVICE_TYPE, friendType);
        startActivity(intent);
    }

    private void initializeProfileView() {
        Log.d("프로세스","onNotKakaoTalkUser_메소드initializeProfileView");

        profileLayout = findViewById(R.id.com_kakao_user_profile);
        profileLayout.setDefaultBgImage(R.drawable.bg_image_02);
        profileLayout.setDefaultProfileImage(R.drawable.thumb_talk);
        ((TextView)findViewById(R.id.text_title)).setText(getString(R.string.text_kakaotalk));

        findViewById(R.id.title_back).setOnClickListener(v -> finish());
    }

    public abstract class KakaoTalkResponseCallback<T> extends TalkResponseCallback<T> {

        @Override
        public void onNotKakaoTalkUser() {
            Log.d("프로세스","KakaoTalkResponseCallback_메소드onNotKakaoTalkUser");

            KakaoToast.makeToast(getApplicationContext(), "not a KakaoTalk user", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onFailure(ErrorResult errorResult) {
            Log.d("프로세스","KakaoTalkResponseCallback_메소드onFailure");

            KakaoToast.makeToast(getApplicationContext(), "failure : " + errorResult, Toast.LENGTH_LONG).show();
        }

        @Override
        public void onSessionClosed(ErrorResult errorResult) {

            Log.d("프로세스","KakaoTalkResponseCallback_메소드onSessionClosed");
            redirectLoginActivity();
        }

        @Override
        public void onNotSignedUp() {
            Log.d("프로세스","KakaoTalkResponseCallback_메소드onNotSignedUp");

            redirectSignupActivity();
        }

        @Override
        public void onDidStart() {
            Log.d("프로세스","KakaoTalkResponseCallback_메소드onDidStart");

            showWaitingDialog();
        }

        @Override
        public void onDidEnd() {
            Log.d("프로세스","KakaoTalkResponseCallback_메소드onDidEnd");

            cancelWaitingDialog();
        }
    }
}