package com.kakao.auth.network;

import android.os.Bundle;

import com.kakao.auth.ApiErrorCode;
import com.kakao.auth.AuthCodeCallback;
import com.kakao.auth.AuthService;
import com.kakao.auth.AuthType;
import com.kakao.auth.ISession;
import com.kakao.auth.StringSet;
import com.kakao.auth.ageauth.AgeAuthService;
import com.kakao.auth.authorization.accesstoken.AccessToken;
import com.kakao.auth.helper.CurrentActivityProvider;
import com.kakao.auth.helper.StartActivityWrapper;
import com.kakao.auth.network.response.InsufficientScopeException;
import com.kakao.network.ErrorResult;
import com.kakao.network.response.ApiResponseStatusError;
import com.kakao.network.response.ResponseBody;
import com.kakao.network.response.ResponseStringConverter;
import com.kakao.util.helper.log.Logger;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author kevin.kang. Created on 2018. 1. 5..
 */

class DefaultApiErrorHandlingService implements ApiErrorHandlingService {
    @Override
    public boolean shouldRetryWithApiError(ApiResponseStatusError error) {
        int errorCode = error.getErrorCode();
        switch (errorCode) {
            case ApiErrorCode.INVALID_TOKEN_CODE:
                return shouldRetryAfterTryingRefreshToken();
            case ApiErrorCode.INVALID_SCOPE_CODE:
                return shouldRetryAfterScopesUpdate(error.getErrorResponse());
            case ApiErrorCode.NEED_TO_AGE_AUTHENTICATION:
                return shouldRetryAfterAgeAuth();
            default:
        }
        return false;
    }

    @Override
    public boolean shouldRetryAfterTryingRefreshToken() {
        if (!session.getTokenInfo().hasValidRefreshToken()) {
            return false;
        }
        try {
            AccessToken futureToken = session.refreshAccessToken(null).get();
            return futureToken != null && futureToken.hasValidAccessToken();
        } catch (Exception e) {
            return false;
        }
    }

    boolean shouldRetryAfterAgeAuth() {
        int state = ageAuthService.requestAgeAuth(new Bundle(), activityProvider.getCurrentActivity());
        return state == AuthService.AgeAuthStatus.SUCCESS.getValue() || state == AuthService.AgeAuthStatus.ALREADY_AGE_AUTHORIZED.getValue();
    }

    boolean shouldRetryAfterScopesUpdate(final ResponseBody errorResponse) throws InsufficientScopeException {
        List<String> requiredScopes = null;
        if (errorResponse.has(StringSet.required_scopes)) {
            try {
                requiredScopes = ResponseStringConverter.IDENTITY_CONVERTER.convertList(errorResponse.getJSONArray(StringSet.required_scopes));
            } catch (ResponseBody.ResponseBodyException e) {
                throw new InsufficientScopeException(errorResponse.toString());
            }
        }
        try {
            AccessToken futureToken = requestScopesUpdate(AuthType.KAKAO_ACCOUNT, requiredScopes).get();
            return futureToken.hasValidAccessToken();
        } catch (ExecutionException|InterruptedException e) {
            throw new InsufficientScopeException(e.getMessage());
        }
    }

    public Future<AccessToken> requestScopesUpdate(final AuthType authType, final List<String> scopes) throws RuntimeException {
        final AtomicReference<String> authCodeResult = new AtomicReference<>();
        final AtomicReference<Exception> exception;
        exception = new AtomicReference<>();
        final CountDownLatch lock = new CountDownLatch(1);

        final AuthCodeCallback callback = new ScopeAuthCodeCallback(authCodeResult, lock, exception);

        try {
            session.getAuthCodeManager().requestAuthCodeWithScopes(authType, new StartActivityWrapper(activityProvider.getCurrentActivity()), scopes, callback);
        } catch (Exception e) {
            Logger.e(e.toString());
            exception.set(e);
            lock.countDown();
        }

        // scope을 갱신할때까지 기다린다.
        // 사용자가 취소를 하여도 종료.
        try {
            lock.await();
        } catch (InterruptedException e) {
            exception.set(e);
            Logger.e(e.toString());
        }

        if (exception.get() != null) {
            throw new InsufficientScopeException(exception.get().toString());
        }

        String authCode = authCodeResult.get();
        if (authCode == null) {
            Logger.e("auth code null");
            throw new InsufficientScopeException("Failed to get authorization code while requesting dynamic scope update.");
        }

        Future<AccessToken> future;
        try {
            future = session.getAccessTokenManager().requestAccessTokenByAuthCode(authCode, session.getAccessTokenCallback());
        } catch (Exception e) {
            Logger.e(e);
            throw new InsufficientScopeException(e.toString());
        }

        return future;
    }

    private ISession session;
    private AgeAuthService ageAuthService;
    private CurrentActivityProvider activityProvider;

    public void setSession(ISession session) {
        this.session = session;
    }

    public void setAgeAuthService(AgeAuthService ageAuthService) {
        this.ageAuthService = ageAuthService;
    }

    public void setActivityProvider(CurrentActivityProvider activityProvider) {
        this.activityProvider = activityProvider;
    }

    private static class ScopeAuthCodeCallback extends AuthCodeCallback {
        private final AtomicReference<String> authCodeResult;
        private final CountDownLatch lock;
        private final AtomicReference<Exception> exception;

        ScopeAuthCodeCallback(AtomicReference<String> authCodeResult, CountDownLatch lock, AtomicReference<Exception> exception) {
            this.authCodeResult = authCodeResult;
            this.lock = lock;
            this.exception = exception;
        }

        @Override
        public void onAuthCodeReceived(String authCode) {
            authCodeResult.set(authCode);
            lock.countDown();
        }

        @Override
        public void onAuthCodeFailure(ErrorResult errorResult) {
            exception.set(errorResult.getException());
            lock.countDown();
        }
    }
}
