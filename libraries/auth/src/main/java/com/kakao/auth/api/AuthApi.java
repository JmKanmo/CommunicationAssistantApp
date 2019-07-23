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
package com.kakao.auth.api;


import android.content.Context;

import com.kakao.auth.authorization.accesstoken.AccessToken;
import com.kakao.auth.authorization.accesstoken.AccessTokenRequest;
import com.kakao.auth.network.AuthNetworkService;
import com.kakao.auth.network.request.AccessTokenInfoRequest;
import com.kakao.auth.network.response.AccessTokenInfoResponse;
import com.kakao.auth.network.response.AuthResponseError;
import com.kakao.network.NetworkService;
import com.kakao.common.KakaoContextService;

/**
 * Bloking으로 동작하며, 인증관련 내부 API콜을 한다.
 * @author leoshin
 */
public class AuthApi {

    private static AuthApi instance = new AuthApi(AuthNetworkService.Factory.getInstance(),
            NetworkService.Factory.getInstance());

    private AuthNetworkService authNetworkService;
    private NetworkService networkService;

    public static AuthApi getInstance() {
        return instance;
    }

    public AuthApi(AuthNetworkService authNetworkService, NetworkService networkService) {
        this.authNetworkService = authNetworkService;
        this.networkService = networkService;
    }

    public AccessToken requestAccessToken(Context context, String authCode, String refreshToken, String approvalType) throws Exception {
        AccessTokenRequest request = new AccessTokenRequest(KakaoContextService.getInstance().phaseInfo(), KakaoContextService.getInstance().getAppConfiguration(), authCode, refreshToken, approvalType);
        return networkService.request(request, AccessToken.Factory.CONVERTER, AuthResponseError.CONVERTER);
    }

    public AccessTokenInfoResponse requestAccessTokenInfo() throws Exception {
        return authNetworkService.request(new AccessTokenInfoRequest(), AccessTokenInfoResponse.CONVERTER);
    }
}
