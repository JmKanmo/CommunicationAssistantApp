package com.kakao.auth.authorization.accesstoken;

import java.util.Date;

/**
 * @author kevin.kang. Created on 2017. 8. 9..
 */

public class TestAccessTokenFactory {
    static AccessToken createTestToken() {
        return createTestToken("access_token", "refresh_token");
    }

    static AccessToken createTestToken(String accessToken, String refreshToken) {
        Date accessTokenDate = new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24);
        Date refreshTokenDate = new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24 * 30);
        AccessToken updateToken = new AccessTokenImpl(accessToken, refreshToken, accessTokenDate, refreshTokenDate);
        return updateToken;
    }

    static AccessToken createEmptyAccessToken() {
        return new AccessTokenImpl(null, null, null, null);
    }

    static AccessToken createWithAccessTokenOnly() {
        return new AccessTokenImpl("access_token", null, new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24), null);
    }
}
