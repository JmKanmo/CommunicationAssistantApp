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
package com.kakao.auth.authorization.accesstoken;

import android.content.Context;
import android.support.annotation.NonNull;

import com.kakao.auth.AccessTokenCallback;
import com.kakao.auth.ApiResponseCallback;
import com.kakao.auth.ApprovalType;
import com.kakao.auth.network.response.AccessTokenInfoResponse;
import com.kakao.network.NetworkService;
import com.kakao.common.KakaoContextService;

import java.util.concurrent.Future;

/**
 * Interface responsible for getting access token from Kakao OAuth server. Access token can be acquired
 * with either authorization code or refresh token.
 *
 * Authorization code is usually used when user tries login via AuthCodeManager, and refresh token
 * is used when a new access token is needed due to expired one.
 *
 * @author kevin.kang. Created on 2017. 5. 25..
 */

public interface AccessTokenManager {
    /**
     * Requests access token with auth code retrieved from Kakao OAuth server.
     *
     * @param authCode Authorization code previously acquired from Kakao OAuth server
     * @param accessTokenCallback Success/failure callback for access token
     * @return Future instance containing AccessToken
     */
    Future<AccessToken> requestAccessTokenByAuthCode(final String authCode, final AccessTokenCallback accessTokenCallback);

    /**
     * Requests access token with refresh token. This usually takes place when access token is expired but refresh token isn't.
     *
     * @param refreshToken Refresh token acquired when user previously logged in.
     * @param accessTokenCallback Success/failure callback for access token
     * @return Future instance containng access token
     */
    Future<AccessToken> refreshAccessToken(final String refreshToken, final AccessTokenCallback accessTokenCallback);

    /**
     * Request information about current access token. The information includes:
     *  - id of the currently logged in user
     *  - remaining time of the access token (in millis)
     *
     * @param responseCallback Response callback
     * @return Future instance containing AccessTokenInfoResponse
     * @deprecated Use {@link com.kakao.auth.AuthService#requestAccessTokenInfo(ApiResponseCallback)} instead
     */
    @Deprecated
    Future<AccessTokenInfoResponse> requestAccessTokenInfo(final ApiResponseCallback<AccessTokenInfoResponse> responseCallback);

    /**
     * Initializes and provides singleton instance for AccessTokenManager.
     */
    class Factory {
        private static AccessTokenManager accessTokenManager;

        public static AccessTokenManager initialize(final Context context, final @NonNull ApprovalType approvalType) {
            if (accessTokenManager == null) {
                KakaoContextService.getInstance().initialize(context);
                KakaoContextService contextService = KakaoContextService.getInstance();
                accessTokenManager = new DefaultAccessTokenManager(contextService, NetworkService.Factory.getInstance(), approvalType);
            }
            return accessTokenManager;
        }

        public static AccessTokenManager getInstance() {
            return accessTokenManager;
        }
    }
}
