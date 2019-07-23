/*
  Copyright 2014-2016 Kakao Corp.

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
package com.kakao.auth;

import android.support.annotation.Nullable;

/**
 * Session에서 사용되는 정보값을 받아오기 위한 class.
 * @author leo.shin
 */
public interface ISessionConfig {
    /**
     * 로그인시 인증받을 타입을 지정한다. 지정하지 않을 시 가능한 모든 옵션이 지정된다. 예시) AuthType.KAKAO_TALK
     * @return {@link AuthType} Kakao SDK 로그인을 하는 방식
     */
    AuthType[] getAuthTypes();

    /**
     * SDK 로그인시 사용되는 WebView에서 pause와 resume시에 Timer를 설정하여 CPU소모를 절약한다.
     * true 를 리턴할경우 webview로그인을 사용하는 화면서 모든 webview에 onPause와 onResume 시에 Timer를 설정해 주어야 한다.
     * @return true is set timer, false otherwise. default false.
     */
    boolean isUsingWebviewTimer();

    /**
     * 로그인시 access token과 refresh token을 저장할 때 암호화 여부를 결정한다.
     * @return true if using secure mode, false otherwise. default false.
     */
    boolean isSecureMode();

    /**
     * 일반 사용자가 아닌 Kakao와 제휴된 앱에서 사용되는 값으로, 값을 채워주지 않을경우 ApprovalType.INDIVIDUAL 값을 사용하게 된다.
     * @return 설정한 ApprovalType. default ApprovalType.INDIVIDUAL
     */
    @Nullable ApprovalType getApprovalType();

    /**
     * Kakao SDK 에서 사용되는 WebView에서 email 입력폼에서 data를 save할지여부를 결정한다.
     * true일경우 SQLite의 접근이 제한되는 경우가 있음.
     * @return  SDK에서 사용되는 WebView에서 email 입력폼에서 data를 save할지여부. default true.
     *
     * @deprecated since Android O. For more information, see <a href="https://developer.android.com/about/versions/o/android-8.0-changes.html">Android 8.0 Behavior changes</a>
     */
    @Deprecated
    boolean isSaveFormData();
}
