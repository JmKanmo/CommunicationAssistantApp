/*
  Copyright 2017-2019 Kakao Corp.

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
package com.kakao.auth.authorization.authcode;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.kakao.auth.ApprovalType;
import com.kakao.auth.AuthCodeCallback;
import com.kakao.auth.AuthType;
import com.kakao.auth.ISessionConfig;
import com.kakao.auth.Session;
import com.kakao.auth.StringSet;
import com.kakao.auth.helper.StartActivityWrapper;
import com.kakao.auth.authorization.AuthorizationResult;
import com.kakao.network.ErrorResult;
import com.kakao.common.KakaoContextService;
import com.kakao.network.ServerProtocol;
import com.kakao.util.exception.KakaoException;
import com.kakao.util.helper.Utility;
import com.kakao.util.helper.log.Logger;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * @author leo.shin
 */
class KakaoAuthCodeManager implements AuthCodeManager, AuthCodeListener {
    private KakaoContextService contextService;
    private AuthCodeRequest currentRequest;

    private final Queue<AuthCodeService> authCodeManagers = new LinkedList<>();
    private StartActivityWrapper startActivityWrapper;

    private final ISessionConfig sessionConfig;
    private AuthCodeService kakaoManager;
    private AuthCodeService storyManager;
    private AuthCodeService webManager;
    private final int requestCode = 1001;

    @Override
    public void requestAuthCode(AuthType authType, Activity activity, AuthCodeCallback authCodeCallback) {
        requestAuthCode(authType, new StartActivityWrapper(activity), authCodeCallback);
    }

    @Override
    public void requestAuthCode(AuthType authType, Fragment fragment, AuthCodeCallback authCodeCallback) {
        requestAuthCode(authType, new StartActivityWrapper(fragment), authCodeCallback);
    }

    @Override
    public void requestAuthCode(AuthType authType, android.support.v4.app.Fragment fragment, AuthCodeCallback authCodeCallback) {
        requestAuthCode(authType, new StartActivityWrapper(fragment), authCodeCallback);
    }

    @Override
    public void requestAuthCode(final AuthType authType, final StartActivityWrapper wrapper, AuthCodeCallback callback) {
        AuthCodeRequest request = createAuthCodeRequest(contextService.phaseInfo().appKey(), callback);
        request.setAccountUri(createAccountLoginUri(request));
        startTryingAuthCodeServices(authType, request, wrapper);
    }

    @Override
    public void requestAuthCodeWithScopes(AuthType authType, final StartActivityWrapper wrapper, List<String> scopes, AuthCodeCallback callback) {
        AuthCodeRequest request = createAuthCodeRequest(contextService.phaseInfo().appKey(), getRefreshToken(), scopes, callback);
        request.setAccountUri(createScopeUpdateUri(request));
        startTryingAuthCodeServices(authType, request, wrapper);
    }

    @Override
    public void createAccountAndRequestAuthCode(StartActivityWrapper wrapper, AuthCodeCallback callback) {
        AuthCodeRequest request = createAuthCodeRequest(contextService.phaseInfo().appKey(), callback);
        request.setAccountUri(createAccountCreateUri(request));
        startTryingAuthCodeServices(AuthType.KAKAO_ACCOUNT, request, wrapper);
    }

    void startTryingAuthCodeServices(final AuthType authType, final AuthCodeRequest request, final StartActivityWrapper wrapper) {
        addToAuthCodeServicesQueue(authType);
        currentRequest = request;
        startActivityWrapper = wrapper;
        tryNextAuthCodeService(request);
    }

    void tryNextAuthCodeService(final AuthCodeRequest request) {
        AuthCodeService authCodeService;
        AuthCodeCallback callback = request.getCallback();
        // just peek here because it needs to be referenced again and removed during handleActivityResult
        while ((authCodeService = authCodeManagers.peek()) != null) {
            Logger.d("trying " + authCodeService.getClass().getSimpleName());
            if (authCodeService.requestAuthCode(request, startActivityWrapper, this)) {
                // This AuthCodeService succeeded in requesting auth code.
                // Return and wait for handleActivityResult() or onAuthCodeReceived()
                return;
            } else {
                // This AuthCodeService should be pulled out from the queue since it didn't evey try.
                authCodeManagers.poll();
            }
        }

        // handler 를 끝까지 돌았는데도 authorization code 를 얻지 못했으면 error
        if (callback != null) {
            onAuthCodeReceived(request.getRequestCode(), AuthorizationResult.createAuthCodeOAuthErrorResult("Failed to get Authorization Code."));
        }
    }

    KakaoAuthCodeManager(final KakaoContextService contextService, final ISessionConfig sessionConfig, final AuthCodeService kakaoManager, final AuthCodeService storyManager, final AuthCodeService webManager) {
        this.contextService = contextService;
        this.sessionConfig = sessionConfig;
        this.kakaoManager = kakaoManager;
        this.storyManager = storyManager;
        this.webManager = webManager;
    }

    private void addToAuthCodeServicesQueue(final AuthType authType) {
        AuthType type = authType == null ? AuthType.KAKAO_TALK : authType;
        switch (type) {
            case KAKAO_TALK:
            case KAKAO_TALK_EXCLUDE_NATIVE_LOGIN:
            case KAKAO_TALK_ONLY:
                authCodeManagers.add(kakaoManager);
                break;
            case HEAR_SPEAK:
                authCodeManagers.add(storyManager);
                break;
            case KAKAO_LOGIN_ALL:
                authCodeManagers.add(kakaoManager);
                authCodeManagers.add(storyManager);
                break;
        }
        if (type != AuthType.KAKAO_TALK_ONLY) {
            authCodeManagers.add(webManager);
        }
    }

    public boolean handleActivityResult(final int requestCode, final int resultCode, final Intent data) {
        if (currentRequest == null) {
            Logger.w("Auth code was not requested or the request has already been processed.");
            return false;
        }
        AuthCodeService authCodeService = authCodeManagers.poll();
        if (authCodeService == null || !authCodeService.handleActivityResult(requestCode, resultCode, data, this)) {
            tryNextAuthCodeService(currentRequest);
        }
        return true;
    }

    @Override
    public boolean isTalkLoginAvailable() {
        return kakaoManager.isLoginAvailable();
    }

    @Override
    public boolean isStoryLoginAvailable() {
        return storyManager.isLoginAvailable();
    }

    String getScopesString(final List<String> requiredScopes) {
        String scopeParam = null;
        if (requiredScopes == null) {
            return null;
        }
        StringBuilder builder = null;
        for (String scope : requiredScopes) {
            if (builder != null) {
                builder.append(",");
            } else {
                builder = new StringBuilder();
            }

            builder.append(scope);
        }

        if (builder != null) {
            scopeParam = builder.toString();
        }

        return scopeParam;
    }

    AuthCodeRequest createAuthCodeRequest(final String appKey, final AuthCodeCallback callback) {
        String redirectUri = getRedirectUri(appKey);
        AuthCodeRequest request = new AuthCodeRequest(appKey, redirectUri, requestCode, callback);
        request.putExtraParam(StringSet.approval_type, sessionConfig.getApprovalType() == null ? ApprovalType.INDIVIDUAL.toString() : sessionConfig.getApprovalType().toString());
        return request;
    }


    private String getRedirectUri(final String appKey) {
        return StringSet.REDIRECT_URL_PREFIX + appKey + StringSet.REDIRECT_URL_POSTFIX;
    }

    AuthCodeRequest createAuthCodeRequest(final String appKey, final String refreshToken, final List<String> scopes, final AuthCodeCallback callback) {
        AuthCodeRequest request = new AuthCodeRequest(appKey, getRedirectUri(appKey), requestCode, callback);
        request.putExtraHeader(StringSet.RT, refreshToken);
        request.putExtraParam(StringSet.scope, getScopesString(scopes));
        request.putExtraParam(StringSet.approval_type, sessionConfig.getApprovalType() == null ? ApprovalType.INDIVIDUAL.toString() : sessionConfig.getApprovalType().toString());
        return request;
    }

    String getRefreshToken() {
        try {
            return Session.getCurrentSession().getTokenInfo().getRefreshToken();
        } catch (IllegalStateException|NullPointerException e) {
            return null;
        }
    }

    @Override
    public final void onAuthCodeReceived(final int requestCode, AuthorizationResult result) {
        if (currentRequest == null) {
            Logger.w("Current auth code request has already finished.");
            return;
        }
        AuthCodeCallback callback = currentRequest.getCallback();

        if (callback == null) {
            Logger.w("Callback has not been set for this auth code request. Just return.");
            return;
        }

        AuthorizationCode authCode = null;
        KakaoException exception = null;

        if (result == null) {
            exception = new KakaoException(KakaoException.ErrorType.AUTHORIZATION_FAILED, "the result of authorization code request is null.");
        } else if (result.isCanceled()) {
            exception = new KakaoException(KakaoException.ErrorType.CANCELED_OPERATION, result.getResultMessage());
        } else if (result.isAuthError() || result.isError()) {
            exception = new KakaoException(KakaoException.ErrorType.AUTHORIZATION_FAILED, result.getResultMessage());
        } else {
            final String resultRedirectURL = result.getRedirectURL();
            if (resultRedirectURL != null && resultRedirectURL.startsWith(currentRequest.getRedirectURI())) {
                authCode = AuthorizationCode.createFromRedirectedUri(result.getRedirectUri());
                // authorization code 가 포함되지 않음
                if (!authCode.hasAuthorizationCode()) {
                    authCode = null;
                    exception = new KakaoException(KakaoException.ErrorType.AUTHORIZATION_FAILED, "the result of authorization code request does not have authorization code.");
                }
            } else { // 기대 했던 redirect uri 불일치
                Logger.e(resultRedirectURL);
                exception = new KakaoException(KakaoException.ErrorType.AUTHORIZATION_FAILED, "the result of authorization code request mismatched the registered redirect uri. msg = " + result.getResultMessage());
            }
        }
        currentRequest = null;
        authCodeManagers.clear();
        if (exception != null) {
            callback.onAuthCodeFailure(new ErrorResult(exception));
            return;
        }
        callback.onAuthCodeReceived(authCode.getAuthorizationCode());
    }

    /*
        Create uri for account login page. This page bypasses account cookie.
     */
    Uri createAccountLoginUri(final AuthCodeRequest request) {
        Uri continueUri = createScopeUpdateUri(request);
        return new Uri.Builder().scheme(ServerProtocol.SCHEME).authority(ServerProtocol.accountAuthority())
                .path(ServerProtocol.ACCOUNT_LOGIN_PATH)
                .appendQueryParameter(StringSet.CONTINUE, continueUri.toString())
                .build();
    }

    Uri createAccountCreateUri(final AuthCodeRequest request) {
        Uri continueUri = createScopeUpdateUri(request);
        return new Uri.Builder().scheme(ServerProtocol.SCHEME).authority(ServerProtocol.accountAuthority())
                .path(ServerProtocol.ACCOUNT_CREATE_PATH)
                .appendQueryParameter(StringSet.CONTINUE, continueUri.toString())
                .build();
    }

    /*
        Create uri for scope update with refresh token. However, uri returned by this method does
        not contain refresh token info.
     */
    Uri createScopeUpdateUri(final AuthCodeRequest request) {
        final Bundle parameters = new Bundle();
        parameters.putString(StringSet.client_id, request.getAppKey());
        parameters.putString(StringSet.redirect_uri, request.getRedirectURI());
        parameters.putString(StringSet.response_type, StringSet.code);

        final Bundle extraParams = request.getExtraParams();
        if(extraParams != null && !extraParams.isEmpty()){
            for(String key : extraParams.keySet()){
                String value = extraParams.getString(key);
                if(value != null){
                    parameters.putString(key, value);
                }
            }
        }
        return Utility.buildUri(ServerProtocol.authAuthority(), ServerProtocol.AUTHORIZE_CODE_PATH, parameters);
    }
}
