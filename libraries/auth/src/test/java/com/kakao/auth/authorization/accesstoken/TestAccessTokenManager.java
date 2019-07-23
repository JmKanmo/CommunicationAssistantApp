package com.kakao.auth.authorization.accesstoken;

import com.kakao.auth.AccessTokenCallback;
import com.kakao.auth.ApiResponseCallback;
import com.kakao.auth.network.response.AccessTokenInfoResponse;
import com.kakao.util.helper.log.Logger;

import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

/**
 * @author kevin.kang. Created on 2017. 5. 25..
 */

public class TestAccessTokenManager implements AccessTokenManager {
    private static String accessTokenString = "new_access_token";
    private static String refreshTokenString = "new_refresh_token";

    @Override
    public Future<AccessToken> requestAccessTokenByAuthCode(String authCode, AccessTokenCallback accessTokenCallback) {
        AccessToken accessToken = createAccessToken();
        if (accessTokenCallback != null) {
            accessTokenCallback.onAccessTokenReceived(accessToken);
        }
        return CompletableFuture.completedFuture(accessToken);
    }

    @Override
    public Future<AccessToken> refreshAccessToken(String refreshToken, AccessTokenCallback accessTokenCallback) {
        AccessToken accessToken = createAccessToken();
        if (accessTokenCallback != null) {
            accessTokenCallback.onAccessTokenReceived(accessToken);
        }
        return CompletableFuture.completedFuture(accessToken);
    }

    @Override
    public Future<AccessTokenInfoResponse> requestAccessTokenInfo(ApiResponseCallback<AccessTokenInfoResponse> responseCallback) {
        return null;
    }

    public static AccessToken createAccessToken() {
        Date accessTokenExpireDate = new Date(new Date().getTime() + 12 * 60 * 60 * 1000);
        Date refreshTokenExpireDate = new Date(new Date().getTime() + 30L * 24 * 60 * 60 * 1000);
        return new AccessTokenImpl(accessTokenString, refreshTokenString, accessTokenExpireDate, refreshTokenExpireDate);
    }
}
