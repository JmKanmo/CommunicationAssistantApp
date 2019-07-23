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
package com.kakao.push.api;

import com.kakao.auth.network.AuthNetworkService;
import com.kakao.auth.network.AuthorizedRequest;
import com.kakao.network.response.BlankApiResponse;
import com.kakao.push.request.DeregisterPushTokenRequest;
import com.kakao.push.request.GetPushTokensRequest;
import com.kakao.push.request.RegisterPushTokenRequest;
import com.kakao.push.request.SendPushRequest;
import com.kakao.push.response.GetPushTokenResponse;
import com.kakao.push.response.RegisterPushTokenResponse;
import com.kakao.push.response.model.PushTokenInfo;

import java.util.List;

/**
 * Bloking으로 동작하며, push에 관련된 내부 API콜을 한다.
 * @author leoshin, created at 15. 8. 10..
 */
public class PushApi {

    /**
     * 현 기기의 푸시 토큰을 등록한다.
     * 푸시 토큰 등록 후 푸시 토큰 삭제하기 전 또는 만료되기 전까지 서버에서 관리되어 푸시 메시지를 받을 수 있다.
     * @param pushToken 등록할 푸시 토큰
     * @param deviceId 한 사용자가 여러 기기를 사용할 수 있기 때문에 기기에 대한 유일한 id도 필요
     * @throws Exception if Session is closed or network request fails.
     */
    public Integer registerPushToken(final String pushToken, final String deviceId) throws Exception {
        AuthorizedRequest request = new RegisterPushTokenRequest(pushToken, deviceId);
        return networkService.request(request, RegisterPushTokenResponse.CONVERTER);
    }

    /**
     * 현 사용자 ID로 등록된 모든 푸시토큰 정보를 반환한다.
     * @return GetPushTokenResponse that contains all push token information registered with user id
     * @throws Exception if Session is closed or network request fails.
     */
    public List<PushTokenInfo> getPushTokens() throws Exception {
        AuthorizedRequest request = new GetPushTokensRequest();
        return networkService.requestList(request, GetPushTokenResponse.CONVERTER);
    }

    /**
     * 사용자의 해당 기기의 푸시 토큰을 삭제한다. 대게 로그아웃시에 사용할 수 있다.
     * @param deviceId 해당기기의 푸시 토큰만 삭제하기 위해 기기 id 필요
     * @throws Exception if Session is closed or network request fails.
     */
    public Boolean deregisterPushToken(final String deviceId) throws Exception {
        AuthorizedRequest request = new DeregisterPushTokenRequest(deviceId);
        return networkService.request(request, BlankApiResponse.CONVERTER);
    }

    /**
     * 자기 자신에게 푸시 메시지를 전송한다. 테스트 용도로만 사용할 수 있다. 다른 사람에게 푸시를 보내기 위해서는 서버에서 어드민키로 REST API를 사용해야한다.
     * @param pushMessage 보낼 푸시 메시지
     * @param deviceId 푸시 메시지를 보낼 기기의 id
     * @throws Exception if Session is closed or network request fails.
     */
    public Boolean sendPushMessage(final String pushMessage, final String deviceId) throws Exception {
        AuthorizedRequest request = new SendPushRequest(pushMessage, deviceId);
        return networkService.request(request, BlankApiResponse.CONVERTER);
    }

    private static PushApi api = new PushApi(AuthNetworkService.Factory.getInstance());
    private AuthNetworkService networkService;

    public static PushApi getInstance() {
        return api;
    }

    private PushApi() {
    }

    PushApi(AuthNetworkService networkService) {
        this.networkService = networkService;
    }
}
