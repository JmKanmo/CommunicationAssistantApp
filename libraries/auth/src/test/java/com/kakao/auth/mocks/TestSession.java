package com.kakao.auth.mocks;

import android.app.Activity;
import android.app.Fragment;

import com.kakao.auth.AccessTokenCallback;
import com.kakao.auth.AuthType;
import com.kakao.auth.ISession;
import com.kakao.auth.ISessionCallback;
import com.kakao.auth.authorization.accesstoken.AccessToken;
import com.kakao.auth.authorization.accesstoken.AccessTokenManager;
import com.kakao.auth.authorization.accesstoken.TestAccessToken;
import com.kakao.auth.authorization.accesstoken.TestAccessTokenManager;
import com.kakao.auth.authorization.authcode.AuthCodeManager;
import com.kakao.network.ErrorResult;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

/**
 * @author kevin.kang. Created on 2017. 12. 4..
 */

public class TestSession implements ISession {
    @Override
    public void open(AuthType authType, Activity activity) {

    }

    @Override
    public void open(AuthType authType, Fragment fragment) {

    }

    @Override
    public void open(AuthType authType, android.support.v4.app.Fragment supportFragment) {

    }

    @Override
    public void openWithAuthCode(String authCode) {

    }

    @Override
    public void close() {

    }

    @Override
    public boolean isOpened() {
        return false;
    }

    @Override
    public boolean isOpenable() {
        return false;
    }

    @Override
    public boolean isClosed() {
        return false;
    }

    @Override
    public Future<AccessToken> refreshAccessToken(AccessTokenCallback callback) {
        AccessToken accessToken = TestAccessTokenManager.createAccessToken();
        if (callback != null) {
            callback.onAccessTokenReceived(accessToken);
        }
        return CompletableFuture.completedFuture(accessToken);
    }

    @Override
    public AccessToken getTokenInfo() {
        return new TestAccessToken();
    }

    @Override
    public void addCallback(ISessionCallback callback) {
    }

    @Override
    public void removeCallback(ISessionCallback callback) {

    }

    @Override
    public void clearCallbacks() {
    }

    @Override
    public AuthCodeManager getAuthCodeManager() {
        return new TestAuthCodeManager();
    }

    @Override
    public AccessTokenManager getAccessTokenManager() {
        return new TestAccessTokenManager();
    }

    @Override
    public AccessTokenCallback getAccessTokenCallback() {
        return new AccessTokenCallback() {
            @Override
            public void onAccessTokenReceived(AccessToken accessToken) {

            }

            @Override
            public void onAccessTokenFailure(ErrorResult errorResult) {

            }
        };
    }
}
