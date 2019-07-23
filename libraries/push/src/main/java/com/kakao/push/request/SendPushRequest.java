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
package com.kakao.push.request;

import android.net.Uri;

import com.kakao.network.ServerProtocol;
import com.kakao.auth.network.AuthorizedApiRequest;
import com.kakao.push.StringSet;

import java.util.HashMap;
import java.util.Map;

/**
 * @author leoshin, created at 15. 8. 10..
 */
public class SendPushRequest extends AuthorizedApiRequest {
    private final String deviceId;
    private final String pushMessage;
    public SendPushRequest(String pushMessage, String deviceId) {
        this.deviceId = deviceId;
        this.pushMessage = pushMessage;
    }

    @Override
    public String getMethod() {
        return POST;
    }

    @Override
    public Uri.Builder getUriBuilder() {
        return super.getUriBuilder().path(ServerProtocol.PUSH_SEND_PATH);
    }

    @Override
    public Map<String, String> getParams() {
        Map<String, String> params = new HashMap<String, String>();
        params.put(StringSet.device_id, deviceId);
        params.put(StringSet.push_message, pushMessage);
        return params;
    }
}
