/*
  Copyright 2017-2019 Kakao Corp.

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
package com.kakao.auth.authorization.authcode;

import android.app.Activity;
import android.app.Application;
import android.app.Fragment;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;

import com.kakao.auth.AuthCodeCallback;
import com.kakao.auth.AuthType;
import com.kakao.auth.ISessionConfig;
import com.kakao.auth.helper.StartActivityWrapper;
import com.kakao.common.KakaoContextService;
import com.kakao.util.KakaoUtilService;

import java.util.List;

/**
 * Interface responsible for getting authorization code from Kakao OAuth server.
 *
 * @author kevin.kang. Created on 2017. 5. 25..
 */

public interface AuthCodeManager {

    /**
     * Request authorization code with the given {@link AuthType}.
     * @param authType {@link AuthType} enum specifying authentication method
     * @param activity Activity to show login activity upon
     * @param authCodeCallback Success/Failure callback for getting authorization code
     */
    void requestAuthCode(final AuthType authType, final Activity activity, AuthCodeCallback authCodeCallback);

    /**
     *
     * @param authType {@link AuthType} enum specifying authentication method
     * @param fragment fragment where login is happening
     * @param authCodeCallback Success/Failure callback for getting authorization code
     */
    @Deprecated
    void requestAuthCode(final AuthType authType, final Fragment fragment, AuthCodeCallback authCodeCallback);

    /**
     *
     * @param authType {@link AuthType} enum specifying authentication method
     * @param fragment fragment where login is happening
     * @param authCodeCallback Success/Failure callback for getting authorization code
     */
    void requestAuthCode(final AuthType authType, final android.support.v4.app.Fragment fragment, AuthCodeCallback authCodeCallback);

    /**
     *
     * @param authType {@link AuthType} enum specifying authentication method
     * @param wrapper {@link StartActivityWrapper} for activity or fragment
     * @param authCodeCallback Success/Failure callback for getting authorization code
     */
    void requestAuthCode(final AuthType authType, final StartActivityWrapper wrapper, AuthCodeCallback authCodeCallback);

    /**
     *
     * @param authType {@link AuthType} enum specifying authentication method
     * @param scopes List of scopes to be requested explicitly
     * @param authCodeCallback Success/Failure callback for getting authorization code
     */
    void requestAuthCodeWithScopes(final AuthType authType, final StartActivityWrapper wrapper, final List<String> scopes, AuthCodeCallback authCodeCallback);

    /**
     * 통합 계정 회원 가입 후 authorization code 를 요청.
     *
     * Request for an authorization code after creating a Kakao account.
     *
     * @param wrapper wrapper {@link StartActivityWrapper} for activity or fragment
     * @param callback Success/Failure callback for getting authorization code
     */
    void createAccountAndRequestAuthCode(final StartActivityWrapper wrapper, final AuthCodeCallback callback);

    /**
     *
     * @param requestCode RequestCode of {@link Activity#onActivityResult(int, int, Intent)}
     * @param resultCode ResultCode of {@link Activity#onActivityResult(int, int, Intent)}
     * @param data {@link Intent} delievered from {@link Activity#onActivityResult(int, int, Intent)}
     * @return true if this intent is requested by Kakao SDK, false otherwise.
     */
    boolean handleActivityResult(int requestCode, int resultCode, Intent data);

    /**
     * Check if KakaoTalk installed supports Kakao login.
     *
     * @return true if KakaoTalk installed on the device supports login, false if not or not installed at all
     */
    boolean isTalkLoginAvailable();

    /**
     * Check if KakaoStory installed supports Kakao login.
     * @return true if KakaoTalk installed on the
     */
    boolean isStoryLoginAvailable();

    class Factory {
        private static AuthCodeManager authCodeManager;

        public static AuthCodeManager initialize(final Application application, final ISessionConfig sessionConfig) {
            if (authCodeManager == null) {
                KakaoContextService contextService = KakaoContextService.getInstance();
                AuthCodeService talkManager = AuthCodeService.Factory.createTalkService(application, sessionConfig, KakaoUtilService.Factory.getInstance());
                AuthCodeService storyManager = AuthCodeService.Factory.createStoryService(application, sessionConfig, KakaoUtilService.Factory.getInstance());
                AuthCodeService webManager = AuthCodeService.Factory.createWebService(application, new Handler(Looper.getMainLooper()), sessionConfig);
                authCodeManager = new KakaoAuthCodeManager(contextService, sessionConfig, talkManager, storyManager, webManager);
            }
            return authCodeManager;
        }

        public static AuthCodeManager getInstance() {
            return authCodeManager;
        }
    }
}
