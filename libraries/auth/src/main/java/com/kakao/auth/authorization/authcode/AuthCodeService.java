/*
  Copyright 2017 Kakao Corp.

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
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import com.kakao.auth.ISessionConfig;
import com.kakao.auth.helper.StartActivityWrapper;
import com.kakao.common.KakaoContextService;
import com.kakao.util.KakaoUtilService;

/**
 * This is an interface that abstracts various methods of getting authorization code from oauth server.
 * @author kevin.kang. Created on 2017. 5. 30..
 */

public interface AuthCodeService {

    /**
     * Request authorization code.
     *
     * @param request {@link AuthCodeRequest} instance containing {@link com.kakao.auth.AuthType} and app information
     *
     * @param wrapper {@link StartActivityWrapper} instance for activity or fragment
     * @param listener {@link AuthCodeListener} instance to get the result
     * @return true if this auth code service succeeded requesting auth code, false otherwise
     */
    boolean requestAuthCode(final AuthCodeRequest request, final StartActivityWrapper wrapper, AuthCodeListener listener);

    /**
     *
     * @param requestCode RequestCode of {@link Activity#onActivityResult(int, int, Intent)}
     * @param resultCode ResultCode of {@link Activity#onActivityResult(int, int, Intent)}
     * @param data {@link Intent} delievered from {@link Activity#onActivityResult(int, int, Intent)}
     * @param listener {@link AuthCodeListener} instance to get the result
     * @return true if intent received by onActivityResult() is handled with this AuthCodeService, false otherwise.
     */
    boolean handleActivityResult(int requestCode, int resultCode, Intent data, AuthCodeListener listener);

    /**
     * Checks whether authorization code can be retrieved from this service.
     *
     * @return true if this service is available on this device, false otherwise
     */
    boolean isLoginAvailable();

    /**
     * This is a factory class for various AuthCodeService types.
     */
    class Factory {
        static AuthCodeService createTalkService(final Context context, final ISessionConfig sessionConfig, final KakaoUtilService protocolService) {
            KakaoContextService contextService = KakaoContextService.getInstance();
            return new TalkAuthCodeService(context, contextService, sessionConfig, protocolService);
        }

        static AuthCodeService createStoryService(final Context context, final ISessionConfig sessionConfig, final KakaoUtilService protocolService) {
            KakaoContextService contextService = KakaoContextService.getInstance();
            return new StoryAuthCodeService(context, contextService, sessionConfig, protocolService);
        }

        static AuthCodeService createWebService(final Context context, final Handler handler, final ISessionConfig sessionConfig) {
            return new WebAuthCodeService(context, handler, sessionConfig);
        }
    }
}
