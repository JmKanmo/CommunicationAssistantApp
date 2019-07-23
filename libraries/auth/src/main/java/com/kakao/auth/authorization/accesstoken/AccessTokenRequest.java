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
package com.kakao.auth.authorization.accesstoken;

import android.net.Uri;

import com.kakao.auth.StringSet;
import com.kakao.network.ServerProtocol;
import com.kakao.auth.network.request.AuthRequest;
import com.kakao.network.IRequest;
import com.kakao.network.multipart.Part;
import com.kakao.common.IConfiguration;
import com.kakao.common.PhaseInfo;
import com.kakao.util.helper.CommonProtocol;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author leoshin, created at 15. 7. 14..
 */
public class AccessTokenRequest extends AuthRequest implements IRequest {
    @Override
    public String getMethod() {
        return POST;
    }

    @Override
    public String getUrl() {
        return new Uri.Builder().scheme(ServerProtocol.SCHEME).authority(ServerProtocol.authAuthority())
                .path(ServerProtocol.ACCESS_TOKEN_PATH).build().toString();
    }

    @Override
    public Map<String, String> getParams() {
        Map<String, String> params = new HashMap<>();
        if (isAccessTokenRequestWithAuthCode()) {
            params.put(StringSet.grant_type, StringSet.authorization_code);
            params.put(StringSet.code, getAuthCode());
        } else {
            params.put(StringSet.grant_type, StringSet.refresh_token);
            params.put(StringSet.refresh_token, getRefreshToken());
        }

        params.put(StringSet.client_id, getAppKey());

        if (phaseInfo.clientSecret() != null) {
            params.put(StringSet.client_secret, phaseInfo.clientSecret());
        }

        params.put(StringSet.android_key_hash, configuration.getKeyHash());

        params.put(StringSet.redirect_uri, getRedirectURI());

        if (approvalType != null && approvalType.length() > 0) {
            params.put(StringSet.approval_type, approvalType);
        }

        final Map<String, String> extras = getExtraParamsMap();
        if (extras != null && !extras.isEmpty()) {
            for (String key : extras.keySet()) {
                String value = extras.get(key);
                if (value != null) {
                    params.put(key, value);
                }
            }
        }
        return params;
    }

    @Override
    public Map<String, String> getHeaders() {
        Map<String, String> header = new HashMap<>();
        header.put(CommonProtocol.KA_HEADER_KEY, configuration.getKAHeader());

        if (!header.containsKey("Content-Type")) {
            header.put("Content-Type", "application/x-www-form-urlencoded");
        }

        if (!header.containsKey("Accept")) {
            header.put("Accept", "*/*");
        }

        header.put(ServerProtocol.AUTHORIZATION_HEADER_KEY, ServerProtocol.KAKAO_AK_HEADER_KEY + ServerProtocol.AUTHORIZATION_HEADER_DELIMITER + getAppKey());

        final Map<String, String> extras = getExtraHeadersMap();
        if (extras != null && !extras.isEmpty()) {
            for (String key : extras.keySet()) {
                String value = extras.get(key);
                if (value != null) {
                    header.put(key, value);
                }
            }
        }
        return header;
    }

    @Override
    public String getBodyEncoding() {
        return "UTF-8";
    }

    @Override
    public List<Part> getMultiPartList() {
        return Collections.emptyList();
    }

    final private PhaseInfo phaseInfo;
    final private IConfiguration configuration;
    final private String authCode;
    final private String refreshToken;
    final private String approvalType;

    public AccessTokenRequest(PhaseInfo phaseInfo, IConfiguration configuration, String authCode, String refreshToken, String approvalType) {
        super(phaseInfo.appKey(), String.format("%s%s%s", StringSet.REDIRECT_URL_PREFIX, phaseInfo.appKey(), StringSet.REDIRECT_URL_POSTFIX));
        this.phaseInfo = phaseInfo;
        this.configuration = configuration;
        this.authCode = authCode;
        this.refreshToken = refreshToken;
        this.approvalType = approvalType;
    }

    public String getAuthCode() {
        return authCode;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    private boolean isAccessTokenRequestWithAuthCode() {
        return authCode != null;
    }
}