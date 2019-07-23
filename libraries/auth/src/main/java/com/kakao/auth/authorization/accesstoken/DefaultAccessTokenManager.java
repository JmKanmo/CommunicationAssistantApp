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

import android.support.annotation.NonNull;

import com.kakao.auth.AccessTokenCallback;
import com.kakao.auth.ApiResponseCallback;
import com.kakao.auth.ApprovalType;
import com.kakao.auth.network.AuthorizedRequest;
import com.kakao.auth.network.request.AccessTokenInfoRequest;
import com.kakao.auth.network.response.AccessTokenInfoResponse;
import com.kakao.auth.network.response.AuthResponseError;
import com.kakao.network.NetworkService;
import com.kakao.common.KakaoContextService;

import java.util.concurrent.Future;

/**
 * Class for getting access token by either authroization code or refresh token.
 *
 * Comments for testing:
 *
 * Most of the interface methods do not have to be unit-tested because the logic heavily depends on
 * integrity of KakaoTaskQueue, KakaoResultTask, and AuthApi. Things that have to be tested
 * are whether resulting KakaoResultTask correctly invoke methods in AuthApi.
 *
 *
 * @author kevin.kang. Created on 2017. 5. 11..
 */
class DefaultAccessTokenManager implements AccessTokenManager {
    private KakaoContextService contextService;
    private NetworkService networkService;
    private ApprovalType approvalType;

    DefaultAccessTokenManager(final KakaoContextService contextService, final NetworkService networkService, final @NonNull ApprovalType approvalType) {
        this.contextService = contextService;
        this.networkService = networkService;
        this.approvalType = approvalType;
    }

    /**
     * Returns Future instance containing AccessToken with authorization code.
     *
     * @param authCode Authorization code
     * @param callback Success/callback failure for access token
     * @return Futurre instance containing {@link AccessToken}
     */
    @Override
    public Future<AccessToken> requestAccessTokenByAuthCode(final String authCode, final AccessTokenCallback callback) {
        AccessTokenRequest request = new AccessTokenRequest(contextService.phaseInfo(), contextService.getAppConfiguration(), authCode, null, approvalType.toString());
        return networkService.request(request, AccessToken.Factory.CONVERTER, AuthResponseError.CONVERTER, callback);
    }

    /**
     * Returns Future instance containing AccessToken with refresh token.
     *
     * @param refreshToken Refresh token for refreshing access token
     * @param callback Success/callback failure for access token
     * @return Future instance containing {@link AccessToken}
     */
    @Override
    public synchronized Future<AccessToken> refreshAccessToken(final String refreshToken, final AccessTokenCallback callback) {
        AccessTokenRequest request = new AccessTokenRequest(contextService.phaseInfo(), contextService.getAppConfiguration(), null, refreshToken, approvalType.toString());
        return networkService.request(request, AccessToken.Factory.CONVERTER, AuthResponseError.CONVERTER, callback);
    }

    /**
     *
     * @param responseCallback Response callback
     * @return Future containing {@link AccessTokenInfoResponse}
     * @deprecated Use {@link com.kakao.auth.AuthService#requestAccessTokenInfo(ApiResponseCallback)} instead.
     */
    @Deprecated
    @Override
    public Future<AccessTokenInfoResponse> requestAccessTokenInfo(final ApiResponseCallback<AccessTokenInfoResponse> responseCallback) {
        AuthorizedRequest request = new AccessTokenInfoRequest();
        return networkService.request(request, AccessTokenInfoResponse.CONVERTER, responseCallback);
    }
}
