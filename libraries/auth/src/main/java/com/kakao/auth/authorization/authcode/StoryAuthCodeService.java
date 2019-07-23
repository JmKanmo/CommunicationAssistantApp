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

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.kakao.auth.ApprovalType;
import com.kakao.auth.ISessionConfig;
import com.kakao.common.KakaoContextService;
import com.kakao.util.KakaoUtilService;


/**
 * @author kevin.kang. Created on 2017. 5. 30..
 */

class StoryAuthCodeService extends TalkAuthCodeService {
    StoryAuthCodeService(final Context context, KakaoContextService contextService, ISessionConfig sessionConfig, final KakaoUtilService protocolService) {
        super(context, contextService, sessionConfig, protocolService);
    }

    @Override
    public boolean handleActivityResult(int requestCode, int resultCode, Intent data, AuthCodeListener listener) {
        return super.handleActivityResult(requestCode, resultCode, data, listener);
    }

    @Override
    public boolean isLoginAvailable() {
        return createLoggedInActivityIntent(null) != null;
    }

    @Override
    protected Intent createLoggedInActivityIntent(Bundle extras) {
        final Intent intent = createIntent(INTENT_ACTION_STORY_LOGGED_IN_ACTIVITY, contextService.phaseInfo().appKey(), redirectUriString(), extras);
        return protocolService.resolveIntent(context, intent, sessionConfig.getApprovalType() == ApprovalType.PROJECT ? STORY_MIN_VERSION_SUPPORT_PROJEC_LOGIN : STORY_MIN_VERSION_SUPPORT_CAPRI);
    }


    private static final int STORY_MIN_VERSION_SUPPORT_CAPRI = 80; // android 2.6.0
    private static final String INTENT_ACTION_STORY_LOGGED_IN_ACTIVITY = "com.kakao.story.intent.action.CAPRI_LOGGED_IN_ACTIVITY";
    private static final int STORY_MIN_VERSION_SUPPORT_PROJEC_LOGIN = 99; // android 2.9.0
}
