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

import com.kakao.auth.StringSet;
import com.kakao.network.response.JSONObjectResponse;
import com.kakao.network.response.ResponseBody;
import com.kakao.util.helper.Utility;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

/**
 * Basic access token implementation in memory.
 *
 * @author MJ
 */
class AccessTokenImpl extends JSONObjectResponse implements AccessToken {
    private static final Date MAX_DATE = new Date(Long.MAX_VALUE);
    private static final Date DEFAULT_EXPIRATION_TIME = MAX_DATE;

    private String accessToken;
    private String refreshToken;
    private Date accessTokenExpiresAt;
    private Date refreshTokenExpiresAt;

    private AccessToken tokenInfo;

    public AccessTokenImpl(ResponseBody body) throws ResponseBody.ResponseBodyException {
        this(body.toString());
    }

    public AccessTokenImpl(String stringData) {
        super(stringData);
        if (!getBody().has(StringSet.access_token)) {
            throw new ResponseBody.ResponseBodyException("No Search Element : " + StringSet.access_token);
        }

        // set access token and its expire time
        accessToken = getBody().getString(StringSet.access_token);
        long expiredAt = new Date().getTime() + getBody().getLong(StringSet.expires_in) * 1000;
        accessTokenExpiresAt = new Date(expiredAt);

        // set refresh token and its expire time
        if (getBody().has(StringSet.refresh_token)) {
            refreshToken = getBody().getString(StringSet.refresh_token);
        }
        if (getBody().has(StringSet.refresh_token_expires_in)) {
            long refreshTokenExpiresAtMillis = new Date().getTime() + getBody().getLong(StringSet.refresh_token_expires_in) * 1000;
            refreshTokenExpiresAt = new Date(refreshTokenExpiresAtMillis);
        } else {
            refreshTokenExpiresAt = MAX_DATE;
        }
    }

    public AccessTokenImpl(final String accessToken, final String refreshToken, final Date accessTokenExpiresAt, final Date refreshTokenExpiresAt) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.accessTokenExpiresAt = accessTokenExpiresAt;
        this.refreshTokenExpiresAt = refreshTokenExpiresAt;
    }

    public AccessTokenImpl(AccessToken tokenInfo) {
        this(tokenInfo.getAccessToken(), tokenInfo.getRefreshToken(), tokenInfo.accessTokenExpiresAt(), tokenInfo.refreshTokenExpiresAt());
        this.tokenInfo = tokenInfo;
    }

    public void clearAccessToken() {
        this.accessToken = null;
        this.accessTokenExpiresAt = DEFAULT_EXPIRATION_TIME;
        if (tokenInfo == null) return;
        tokenInfo.clearAccessToken();
    }

    public void clearRefreshToken() {
        this.refreshToken = null;
        this.refreshTokenExpiresAt = DEFAULT_EXPIRATION_TIME;
        if (tokenInfo == null) return;
        tokenInfo.clearRefreshToken();
    }

    @Override
    public boolean hasValidAccessToken() {
        return !Utility.isNullOrEmpty(accessToken) && !new Date().after(accessTokenExpiresAt);
    }

    @Override
    public int getRemainingExpireTime() {
        if (accessTokenExpiresAt == null || !hasValidAccessToken()) {
            return 0;
        }
        return (int) (accessTokenExpiresAt.getTime() - new Date().getTime());
    }

    // access token 갱신시에는 refresh token이 내려오지 않을 수도 있다.
    @Override
    public void updateAccessToken(final AccessToken newAccessToken){
        String newRefreshToken = newAccessToken.getRefreshToken();
        if(newRefreshToken == null || newRefreshToken.length() == 0){
            this.accessToken = newAccessToken.getAccessToken();
            this.accessTokenExpiresAt = newAccessToken.accessTokenExpiresAt();
        } else {
            this.accessToken = newAccessToken.getAccessToken();
            this.refreshToken = newAccessToken.getRefreshToken();
            this.accessTokenExpiresAt = newAccessToken.accessTokenExpiresAt();
            this.refreshTokenExpiresAt = newAccessToken.refreshTokenExpiresAt();
        }
        if (tokenInfo == null) return;
        tokenInfo.updateAccessToken(this);
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    @Override
    public Date accessTokenExpiresAt() {
        return accessTokenExpiresAt;
    }

    @Override
    public Date refreshTokenExpiresAt() {
        return refreshTokenExpiresAt;
    }

    public boolean hasRefreshToken(){
        return !Utility.isNullOrEmpty(refreshToken);
    }

    @Override
    public boolean hasValidRefreshToken() {
        return !Utility.isNullOrEmpty(refreshToken) && (refreshTokenExpiresAt == null || !new Date().after(refreshTokenExpiresAt));
    }

    @Deprecated
    public int getRemainedExpiresInAccessTokenTime() {
        if (accessTokenExpiresAt == null || !hasValidAccessToken()) {
            return 0;
        }

        return (int) (accessTokenExpiresAt.getTime() - new Date().getTime());
    }

    @Override
    public String toString() {
        if (getBody() != null) {
            return getBody().toString();
        }
        JSONObject result = new JSONObject();
        try {
            result.put(StringSet.access_token, accessToken)
                    .put(StringSet.refresh_token, null);

            if (accessTokenExpiresAt != null) {
                result.put(StringSet.expires_in, (accessTokenExpiresAt.getTime() - new Date().getTime()) / 1000);
            }
            if (refreshTokenExpiresAt != null) {
                result.put(StringSet.refresh_token_expires_in, (refreshTokenExpiresAt.getTime() - new Date().getTime()) / 1000);
            }
            return result.toString();
        } catch (JSONException e){
            return result.toString();
        }
    }
}
