package com.kakao.auth.authorization.accesstoken;

import android.os.Bundle;

import com.kakao.util.helper.PersistentKVStore;
import com.kakao.util.helper.Utility;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Persistent AccessToken implementation.
 *
 * @author kevin.kang. Created on 2017. 7. 24..
 */

class PersistentAccessToken implements AccessToken {

    private AccessToken tokenInfo;
    private PersistentKVStore cache;

    private static final String CACHE_ACCESS_TOKEN = "com.kakao.token.AccessToken";
    private static final String CACHE_ACCESS_TOKEN_EXPIRES_AT = "com.kakao.token.AccessToken.ExpiresAt";
    private static final String CACHE_REFRESH_TOKEN = "com.kakao.token.RefreshToken";
    private static final String CACHE_REFRESH_TOKEN_EXPIRES_AT = "com.kakao.token.RefreshToken.ExpiresAt";

    PersistentAccessToken(AccessToken tokenInfo, final PersistentKVStore cache) {
        this.tokenInfo = tokenInfo;
        this.cache = cache;
    }

    @Override
    public String getAccessToken() {
        return cache.getString(CACHE_ACCESS_TOKEN);
    }

    @Override
    public String getRefreshToken() {
        return cache.getString(CACHE_REFRESH_TOKEN);
    }

    @Override
    public Date accessTokenExpiresAt() {
        return cache.getDate(CACHE_ACCESS_TOKEN_EXPIRES_AT);
    }

    @Override
    public Date refreshTokenExpiresAt() {
        return cache.getDate(CACHE_REFRESH_TOKEN_EXPIRES_AT);
    }

    @Override
    public void clearAccessToken() {
        final List<String> keysToRemove = new ArrayList<String>();
        keysToRemove.add(CACHE_ACCESS_TOKEN);
        keysToRemove.add(CACHE_ACCESS_TOKEN_EXPIRES_AT);
        cache.clear(keysToRemove);
    }

    @Override
    public void clearRefreshToken() {
        final List<String> keysToRemove = new ArrayList<String>();
        keysToRemove.add(CACHE_REFRESH_TOKEN);
        keysToRemove.add(CACHE_REFRESH_TOKEN_EXPIRES_AT);
        cache.clear(keysToRemove);
    }

    @Override
    public boolean hasValidAccessToken() {
        return !Utility.isNullOrEmpty(getAccessToken()) && !new Date().after(accessTokenExpiresAt());
    }

    @Override
    public boolean hasRefreshToken() {
        return !Utility.isNullOrEmpty(getRefreshToken());
    }

    @Override
    public boolean hasValidRefreshToken() {
        return !Utility.isNullOrEmpty(getRefreshToken()) && ! new Date().after(refreshTokenExpiresAt());
    }

    @Override
    public void updateAccessToken(AccessToken token) {
        if (tokenInfo != null) {
            tokenInfo.updateAccessToken(token);
        }
        Bundle bundle = new Bundle();
        bundle.putString(CACHE_ACCESS_TOKEN, token.getAccessToken());
        bundle.putString(CACHE_REFRESH_TOKEN, token.getRefreshToken());
        Date accessTokenExpiresAt = token.accessTokenExpiresAt();
        if (accessTokenExpiresAt != null) {
            bundle.putLong(CACHE_ACCESS_TOKEN_EXPIRES_AT, token.accessTokenExpiresAt().getTime());
        }
        Date refreshTokenExpiresAt = token.refreshTokenExpiresAt();
        if (refreshTokenExpiresAt != null) {
            bundle.putLong(CACHE_REFRESH_TOKEN_EXPIRES_AT, token.refreshTokenExpiresAt().getTime());
        }
        cache.save(bundle);
    }

    @Override
    public int getRemainingExpireTime() {
        if (accessTokenExpiresAt() == null || !hasValidAccessToken()) {
            return 0;
        }
        return (int) (accessTokenExpiresAt().getTime() - new Date().getTime());
    }
}
