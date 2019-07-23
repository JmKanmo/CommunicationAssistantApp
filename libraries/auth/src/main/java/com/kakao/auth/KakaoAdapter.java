/**
 * Copyright 2014-2017 Kakao Corp.
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
package com.kakao.auth;

import android.content.Context;

/**
 * Kakao SDK와 Application을 연결하는 class.
 * @author leoshin, created at 15. 7. 20..
 */
public abstract class KakaoAdapter {
    /**
     * KakaoSDK에서 Application에 필요한 정보를 받는 용도로 사용된다.
     * @return Application context 정보를 가져올 수 있는 IApplicationConfig 객체
     */
    public abstract IApplicationConfig getApplicationConfig();

    /**
     * Session 설정에 필요한 option 값들을 받는다.
     * @return default값들로 설정된 ISessionConfig
     */
    public ISessionConfig getSessionConfig() {
        return new ISessionConfig() {
            @Override
            public AuthType[] getAuthTypes() {
                // 1. 카카오톡으로만 로그인을 유도하고 싶다면 Session.initializeSession(this, AuthType.KAKAO_TALK)
                // 2. 카카오톡으로만 로그인을 유도하고 싶으면서 계정이 없을때 계정생성을 위한 버튼도 같이 제공을 하고 싶다면 Session.initializeSession(this, AuthType.KAKAO_TALK_EXCLUDE_NATIVE_LOGIN)
                return new AuthType[] {AuthType.KAKAO_LOGIN_ALL};
            }

            @Override
            public boolean isUsingWebviewTimer() {
                // SDK 로그인시 사용되는 WebView에서 pause와 resume시에 Timer를 설정하여 CPU소모를 절약한다.
                // true 를 리턴할경우 webview로그인을 사용하는 화면서 모든 webview에 onPause와 onResume 시에 Timer를 설정해 주어야 한다.
                return false;
            }

            @Override
            public boolean isSecureMode() {
                return false;
            }

            @Override
            public ApprovalType getApprovalType() {
                return ApprovalType.INDIVIDUAL;
            }

            @Override
            public boolean isSaveFormData() {
                return true;
            }
        };
    }

    /**
     * Push 설정에 필요한 값/콜백을 받는다.
     * @return default 값들로 설정된 IPushConfig
     */
    public IPushConfig getPushConfig() {
        return new IPushConfig() {
            @Override
            public String getDeviceUUID() {
                return null;
            }

            @Override
            public ApiResponseCallback<Integer> getTokenRegisterCallback() {
                return null;
            }
        };
    }
}
