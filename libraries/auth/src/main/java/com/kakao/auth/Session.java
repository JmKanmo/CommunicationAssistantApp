/*
  Copyright 2014-2018 Kakao Corp.

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
package com.kakao.auth;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Application;
import android.app.Fragment;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.support.annotation.NonNull;

import com.kakao.auth.authorization.accesstoken.AccessToken;
import com.kakao.auth.authorization.accesstoken.AccessTokenManager;
import com.kakao.auth.authorization.authcode.AuthCodeManager;
import com.kakao.auth.authorization.authcode.AuthorizationCode;

import com.kakao.auth.helper.StartActivityWrapper;
import com.kakao.auth.network.response.AccessTokenInfoResponse;
import com.kakao.network.ErrorResult;
import com.kakao.common.KakaoContextService;
import com.kakao.util.exception.KakaoException;
import com.kakao.util.exception.KakaoException.ErrorType;
import com.kakao.util.helper.SharedPreferencesCache;
import com.kakao.util.helper.log.Logger;

/**
 * 로그인 상태를 유지 시켜주는 객체로 access token을 관리한다.
 *
 * @author MJ
 */
public class Session implements ISession {
    @SuppressLint("StaticFieldLeak")
    private static Session currentSession;

    private final Object INSTANCE_LOCK = new Object();

    private final Context context;
    private final KakaoContextService contextService;
    private final SharedPreferencesCache appCache;
    private final AlarmManager tokenAlarmManager;
    private final PendingIntent alarmIntent;

    private AuthCodeManager authCodeManager;
    private AccessTokenManager accessTokenManager;
    private AuthService authService;

    // 아래 값들은 변경되는 값으로 INSTANCE_LOCK의 보호를 받는다.
    private AuthorizationCode authorizationCode;
    private AccessToken accessToken;
    private volatile RequestType requestType;     // close시 삭제


    private final List<ISessionCallback> callbacks;
    private AuthCodeCallback authCodeCallback;
    private AccessTokenCallback accessTokenCallback;

    private static final int DEFAULT_TOKEN_REQUEST_TIME_MILLIS = 3 * 60 * 60 * 1000; // 3 hours
    private static final int RETRY_TOKEN_REQUEST_TIME_MILLIS = 5 * 60 * 1000; // 5 minutes

    /**
     * Application 이 최초 구동시, Session 을 초기화 합니다.
     *
     * @param application   세션을 접근하는 context. 여기로 부터 app key와 redirect uri를 구해온다.
     * @param approvalType Enum representing whether user is authenicated for an individual or a project app
     */
    static synchronized void initialize(final Application application, final @NonNull ApprovalType approvalType) {
        if (currentSession != null) {
            currentSession.clearCallbacks();
            currentSession.close();
        }
        KakaoContextService contextService = KakaoContextService.getInstance();
        ISessionConfig sessionConfig = KakaoSDK.getAdapter().getSessionConfig();

        AuthCodeManager authCodeManager = AuthCodeManager.Factory.initialize(application, sessionConfig);
        AccessTokenManager accessTokenManager = AccessTokenManager.Factory.initialize(application.getApplicationContext(), approvalType);
        currentSession = new Session(application.getApplicationContext(), contextService, sessionConfig, authCodeManager, accessTokenManager);
        currentSession.setAuthService(AuthService.getInstance());
    }

    /**
     * Returns current Session instance.
     *
     * @return 현재 세션 객체
     */
    public static synchronized Session getCurrentSession() {
        if (currentSession == null) {
            throw new IllegalStateException("Session is not initialized. Call KakaoSDK#init first.");
        }
        return currentSession;
    }

    /**
     * Returns an AuthCodeManager instance owned by this Session
     *
     * @return AuthCodeManager instance
     */
    @Override
    public synchronized AuthCodeManager getAuthCodeManager() {
        return authCodeManager;
    }

    /**
     * Returns an AccessTokenManager instance owned by this Session
     *
     * @return AccessTokenManager instance
     */
    @Override
    public synchronized AccessTokenManager getAccessTokenManager() {
        return accessTokenManager;
    }

    Session(final Context context, final KakaoContextService contextService, ISessionConfig sessionConfig, final AuthCodeManager authCodeManager, final AccessTokenManager accessTokenManager) {
        if (context == null) {
            throw new KakaoException(ErrorType.ILLEGAL_ARGUMENT, "cannot create Session without Context.");
        }

        this.context = context;
        this.contextService = contextService;

        appCache = new SharedPreferencesCache(context, contextService.phaseInfo().appKey());
        synchronized (INSTANCE_LOCK) {
            authorizationCode = AuthorizationCode.createEmptyCode();
            accessToken = AccessToken.Factory.createFromCache(sessionConfig, appCache);
        }

        this.authCodeManager = authCodeManager;
        this.accessTokenManager = accessTokenManager;

        this.callbacks = new ArrayList<>();
        this.tokenAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        this.alarmIntent = PendingIntent.getBroadcast(context, 0, new Intent(context, TokenAlarmReceiver.class), PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * 세션 오픈을 진행한다.
     * isOpened() 상태이면 콜백 호출 후 바로 종료.
     * isClosed() 상태이면 authorization code 요청. 에러/취소시 isClosed()
     * isOpenable 상태이면 code 또는 refresh token 이용하여  access token 을 받아온다. 에러/취소시 {isClosed()), refresh 취소시에만 isOpenable() 유지.
     * param으로 받은 콜백으로 그 결과를 전달한다.
     *
     * @param authType 인증받을 타입. 예를 들어, 카카오톡 또는 카카오스토리 또는 직접 입력한 카카오계정
     * @param callerActivity 세션오픈을 호출한 activity
     */
    public void open(final AuthType authType, final Activity callerActivity) {
         internalOpen(authType, new StartActivityWrapper(callerActivity), null);
    }

    /**
     * 세션 오픈을 진행한다.
     * isOpened() 상태이면 콜백 호출 후 바로 종료.
     * isClosed() 상태이면 authorization code 요청. 에러/취소시 isClosed()
     * isOpenable 상태이면 code 또는 refresh token 이용하여  access token 을 받아온다. 에러/취소시 {isClosed()), refresh 취소시에만 isOpenable() 유지.
     * param으로 받은 콜백으로 그 결과를 전달한다.
     *
     * @param authType 인증받을 타입. 예를 들어, 카카오톡 또는 카카오스토리 또는 직접 입력한 카카오계정
     * @param callerFragment 세션오픈을 호출한 fragment
     */
    public void open(final AuthType authType, final Fragment callerFragment) {
        internalOpen(authType, new StartActivityWrapper(callerFragment), null);
    }

    /**
     * 세션 오픈을 진행한다.
     * isOpened() 상태이면 콜백 호출 후 바로 종료.
     * isClosed() 상태이면 authorization code 요청. 에러/취소시 isClosed()
     * isOpenable 상태이면 code 또는 refresh token 이용하여  access token 을 받아온다. 에러/취소시 {isClosed()), refresh 취소시에만 isOpenable() 유지.
     * param으로 받은 콜백으로 그 결과를 전달한다.
     *
     * @param authType 인증받을 타입. 예를 들어, 카카오톡 또는 카카오스토리 또는 직접 입력한 카카오계정
     * @param fragment 세션오픈을 호출한 fragment
     */
    public void open(final AuthType authType, final android.support.v4.app.Fragment fragment) {
        internalOpen(authType, new StartActivityWrapper(fragment), null);
    }

    /**
     * Try login (open session) with authorization code.
     *
     * @param authCode Authorization code acquired by Kakao account authentication/authorization
     */
    public void openWithAuthCode(final String authCode) {
        onAuthCodeReceived(authCode);
    }

    /**
     * Refresh access token with refresh token, even if access token hasn't expired.
     *
     * This method closes session if refreshing fails with http status 400 or 401.
     * This usually happens under the following cases:
     *  - when refresh token hash expired
     *  - when user has changed his or her Kakao account password
     *
     * Developers should check if session is closed in failure callback and deal with it.
     *
     * @param callback Success/failure callback for access token
     */
    public Future<AccessToken> refreshAccessToken(final AccessTokenCallback callback) {
        if (getTokenInfo() == null || !getTokenInfo().hasValidRefreshToken()) {
            KakaoException exception = new KakaoException(ErrorType.ILLEGAL_STATE, "Refresh token has already expired. Logging user out.");
            internalClose(exception);
            if (callback != null) {
                callback.onAccessTokenFailure(new ErrorResult(exception));
            }
            return null;
        }
        synchronized (INSTANCE_LOCK) {
            requestType = RequestType.REFRESHING_ACCESS_TOKEN;
        }
        return accessTokenManager.refreshAccessToken(getTokenInfo().getRefreshToken(), new AccessTokenCallback() {
            @Override
            public void onAccessTokenReceived(AccessToken accessToken) {
                postProcessAccessToken(accessToken);
                if (callback != null) {
                    callback.onAccessTokenReceived(accessToken);
                }
            }

            @Override
            public void onAccessTokenFailure(ErrorResult errorResult) {
                if (shouldClearSessionState(errorResult)) {
                    synchronized (INSTANCE_LOCK) {
                        requestType = null;
                    }
                }
                if (callback != null) {
                    callback.onAccessTokenFailure(errorResult);
                }
            }
        });
    }

    /**
     * Update user's scopes (user's agreement to provide specific data to this application) manually.
     * Before 1.11.0, SDK requested updating user scopes automatically when API responded with
     * {@link com.kakao.auth.network.response.InsufficientScopeException}. From 1.11.0, Applications
     * can manually request updating user scopes with this method.
     *
     * Example scopes are:
     * - account_email
     * - phone_number
     *
     * @since 1.11.0
     *
     * @param activity activity
     * @param scopes List of scopes to be requested explicitly
     * @param callback access token callback
     *
     */
    public void updateScopes(final Activity activity, final List<String> scopes, final AccessTokenCallback callback) {
        updateScopes(new StartActivityWrapper(activity), scopes, callback);
    }

    /**
     * @see #updateScopes(Activity, List, AccessTokenCallback)
     * @since 1.11.0
     *
     * @param fragment fragment
     * @param scopes List of scopes to be requested explicitly
     * @param callback access token callback
     */
    @SuppressWarnings("unused")
    public void updateScopes(final Fragment fragment, final List<String> scopes, final AccessTokenCallback callback) {
        updateScopes(new StartActivityWrapper(fragment), scopes, callback);
    }

    /**
     * @see #updateScopes(Activity, List, AccessTokenCallback)
     * @since 1.11.0
     *
     * @param fragment fragment
     * @param scopes List of scopes to be requested explicitly
     * @param callback access token callback
     */
    @SuppressWarnings("unused")
    public void updateScopes(final android.support.v4.app.Fragment fragment, final List<String> scopes, final AccessTokenCallback callback) {
        updateScopes(new StartActivityWrapper(fragment), scopes, callback);
    }

    /**
     * 통합 계정 회원 가입 후 세션 오픈을 진행한다.
     *
     * @since 1.17.0
     * @param activity activity
     */
    public void createAccountAndOpen(final Activity activity) {
        authCodeManager.createAccountAndRequestAuthCode(new StartActivityWrapper(activity), getAuthCodeCallback());
    }

    /**
     * 통합 계정 회원 가입 후 세션 오픈을 진행한다.
     *
     * @since 1.17.0
     * @param fragment fragment
     */
    public void createAccountAndOpen(final android.support.v4.app.Fragment fragment) {
        authCodeManager.createAccountAndRequestAuthCode(new StartActivityWrapper(fragment), getAuthCodeCallback());
    }


    private void updateScopes(final StartActivityWrapper wrapper, final List<String> scopes, final AccessTokenCallback callback) {
        if (getTokenInfo() == null || !getTokenInfo().hasValidRefreshToken()) {
            KakaoException exception = new KakaoException(ErrorType.ILLEGAL_STATE, "Refresh token has already expired. Logging user out.");
            internalClose(exception);
            if (callback != null) {
                callback.onAccessTokenFailure(new ErrorResult(exception));
            }
            return;
        }
        synchronized (INSTANCE_LOCK) {
            requestType = RequestType.GETTING_AUTHORIZATION_CODE;
        }
        authCodeManager.requestAuthCodeWithScopes(AuthType.KAKAO_ACCOUNT, wrapper, scopes, new AuthCodeCallback() {
            @Override
            public void onAuthCodeReceived(String authCode) {
                synchronized (INSTANCE_LOCK) {
                    authorizationCode = new AuthorizationCode(authCode);
                    requestType = RequestType.GETTING_ACCESS_TOKEN;
                }
                accessTokenManager.requestAccessTokenByAuthCode(authCode, new AccessTokenCallback() {
                    @Override
                    public void onAccessTokenReceived(AccessToken accessToken) {
                        postProcessAccessToken(accessToken);
                        if (callback != null) {
                            callback.onAccessTokenReceived(accessToken);
                        }
                    }

                    @Override
                    public void onAccessTokenFailure(ErrorResult errorResult) {
                        synchronized (INSTANCE_LOCK) {
                            authorizationCode = AuthorizationCode.createEmptyCode();
                            requestType = null;
                        }
                        if (callback != null) {
                            callback.onAccessTokenFailure(errorResult);
                        }
                    }
                });
            }

            @Override
            public void onAuthCodeFailure(ErrorResult errorResult) {
                synchronized (INSTANCE_LOCK) {
                    authorizationCode = AuthorizationCode.createEmptyCode();
                    requestType = null;
                }
                if (callback != null) {
                    callback.onAccessTokenFailure(errorResult);
                }
            }
        });
    }

    /**
     * Session 의 상태를 체크후 {@link Session#isOpenable()} 상태일 때 Login을 시도한다.
     *
     * 요청에 대한 결과는 {@link KakaoAdapter}의 {@link ISessionCallback}으로 전달이 된다.
     *
     * @return true if token can be refreshed, false otherwise.
     */
    public boolean checkAndImplicitOpen() {
        return !isClosed() && implicitOpen();
    }

    /**
     * refreshToken으로 accessToken 갱신이 가능한지 여부를 반환한다.
     * 가능하다면 token갱신을 진행한다.
     * 토큰 갱신은 background로 사용자가 모르도록 진행한다. 토큰 갱신 성공 여부는 리턴값이 아닌 등록되어 있는
     * ISessionCallback으로 전달된다
     *
     * @return 토큰 갱신을 진행할 때는 true, 토큰 갱신을 하지 못할때는 false를 return 한다.
     */
    boolean implicitOpen() {
        if (getTokenInfo().hasValidRefreshToken()) {
            internalOpen(null, null, null);
            return true;
        }
        return false;
    }

    /**
     * 명시적 강제 close(로그아웃/탈퇴). request중 인 것들은 모두 실패를 받게 된다.
     * token을 삭제하기 때문에 authorization code부터(로그인 버튼) 다시 받아서 세션을 open 해야한다.
     *
     * Session callbacks are not invoked, if there are any.
     */
    @Override
    public void close() {
        internalClose(null);
    }

    /**
     * 토큰 유효성을 검사하고 만료된 경우 갱신시켜 준다.
     */
    void checkAccessTokenInfo() {
        if (isClosed()) {
            deregisterTokenManager();
            return;
        }
        if (isOpenable()) {
            implicitOpen();
            return;
        }
        authService.requestAccessTokenInfo(new ApiResponseCallback<AccessTokenInfoResponse>() {
            @Override
            public void onSessionClosed(ErrorResult errorResult) {
                deregisterTokenManager();
            }

            @Override
            public void onNotSignedUp() {
            }

            @Override
            public void onFailure(ErrorResult errorResult) {
                registerTokenManager(RETRY_TOKEN_REQUEST_TIME_MILLIS);
            }

            @Override
            public void onSuccess(AccessTokenInfoResponse accessTokenInfoResponse) {
                final long interval = Math.min(DEFAULT_TOKEN_REQUEST_TIME_MILLIS, accessTokenInfoResponse.getExpiresInMillis());
                registerTokenManager(interval);
            }
        });
    }

    Context getContext() {
        return context;
    }

    /**
     * 현재 세션이 열린 상태인지 여부를 반환한다.
     * @return 세션이 열린 상태라면 true, 그외의 경우 false를 반환한다.
     */
    public synchronized final boolean isOpened() {
        return getTokenInfo() != null && getTokenInfo().hasValidAccessToken();
    }

    /**
     * 현재 세션이 오픈중(갱신 포함) 상태인지 여부를 반환한다.
     *
     * 1. Access token이 없거나 만료되었고, auth code가 발급되어 있는 상태.
     * 2. Access token이 없거나 만료되었고, refresh token이 있는 상태.
     *
     * @return 세션 오픈 진행 중이면 true, 그외 경우는 false를 반환한다.
     */
    public synchronized boolean isOpenable() {
        return getTokenInfo() != null && !isOpened() && (authorizationCode.hasAuthorizationCode() || getTokenInfo().hasValidRefreshToken());
    }

    /**
     * 현재 세션이 닫힌 상태인지 여부를 반환한다.
     * @return 세션이 닫힌 상태라면 true, 그외의 경우 false를 반환한다.
     */
    public synchronized final boolean isClosed() {
        return !isOpened() && !isOpenable();
    }

    /**
     * Check if session is refreshing access token or not.
     *
     * @return true if session is refreshing access token, false otherwise.
     */
    @SuppressWarnings("unused")
    synchronized boolean isRefreshingAccessToken() {
        return requestType == RequestType.REFRESHING_ACCESS_TOKEN;
    }

    /**
     * Checks if exception occured during login should close current session. This happens when
     * authorization has failed due to bad requests or expiered refresh token.
     *
     * @param errorResult Exception representing login error
     * @return true if session should be closed, false otherwise.
     */
    private boolean shouldCloseSession(final ErrorResult errorResult) {
        return errorResult.getHttpStatus() == HttpURLConnection.HTTP_UNAUTHORIZED ||
                errorResult.getHttpStatus() == HttpURLConnection.HTTP_BAD_REQUEST;
    }

    /**
     * 현재 진행 중인 요청 타입
     * @return 현재 진행 중인 요청 타입
     */
    public RequestType getRequestType() {
        synchronized (INSTANCE_LOCK) {
            return requestType;
        }
    }

    /**
     * Returns currently manage {@link AccessToken} instance containing access token and refresh
     * token information.
     *
     * @return {@link AccessToken} instance
     */
    public final AccessToken getTokenInfo() {
        synchronized (INSTANCE_LOCK) {
            return accessToken;
        }
    }

    /**
     * 앱 캐시를 반환한다.
     * @return 앱 캐시
     */
    public SharedPreferencesCache getAppCache() {
        return appCache;
    }

    /**
     * RefreshToken이 내려오지 않았을 경우에는 관련 필드는 업데이트하지 않는다.
     * @param resultAccessToken 메모리/캐시에 저장할 액세스 토큰
     */
    private void updateAccessToken(AccessToken resultAccessToken) {
        synchronized (INSTANCE_LOCK) {
            getTokenInfo().updateAccessToken(resultAccessToken);
        }
    }


    private void internalOpen(final AuthType authType, final StartActivityWrapper startActivityWrapper, final String authCode) {
        if (isOpened()) {
            // 이미 open이 되어 있다.
            final List<ISessionCallback> dumpSessionCallbacks = new ArrayList<>(callbacks);
            for (ISessionCallback callback : dumpSessionCallbacks) {
                callback.onSessionOpened();
            }
            return;
        }

        //끝나지 않은 request가 있다.
        if (getRequestType() != null) {
            Logger.w(getRequestType() + " is still not finished. Just return.");
            return;
        }

        try {
            synchronized (INSTANCE_LOCK) {
                if (isClosed()) {
                    requestType = RequestType.GETTING_AUTHORIZATION_CODE;
                    requestAuthCode(authType, startActivityWrapper);
                } else if (isOpenable()) {
                    if (authCode != null) {
                        requestType = RequestType.GETTING_ACCESS_TOKEN;
                        accessTokenManager.requestAccessTokenByAuthCode(authCode, getAccessTokenCallback());
                    } else {
                        requestType = RequestType.REFRESHING_ACCESS_TOKEN;
                        accessTokenManager.refreshAccessToken(getTokenInfo().getRefreshToken(), getAccessTokenCallback());
                    }
                } else {
                    throw new KakaoException(ErrorType.AUTHORIZATION_FAILED, "current session state is not possible to open.");
                }
            }
        } catch (KakaoException e) {
            internalClose(e);
        }
    }

    /**
     * 로그인 activity를 이용하여 sdk에서 필요로 하는 activity를 띄운다.
     * 따라서 해당 activity의 결과를 로그인 activity가 받게 된다.
     * 해당 결과를 세션이 받아서 다음 처리를 할 수 있도록 로그인 activity의 onActivityResult에서 해당 method를 호출한다.
     * @param requestCode requestCode of onActivityResult callback
     * @param resultCode resultCode of onActivityResult callback
     * @param data intent data of onActivityResult callback
     * @return true if the intent originated from Kakao login, false otherwise.
     */
    public boolean handleActivityResult(int requestCode, int resultCode, Intent data) {
        return authCodeManager != null && authCodeManager.handleActivityResult(requestCode, resultCode, data);
    }

    /**
     * 세션 상태 변화 콜백을 받고자 할때 콜백을 등록한다.
     * @param callback 추가할 세션 콜백
     */
    public void addCallback(final ISessionCallback callback) {
        synchronized (callbacks) {
            if (callback != null && !callbacks.contains(callback)) {
                callbacks.add(callback);
            }
        }
    }

    /**
     * 더이상 세션 상태 변화 콜백을 받고 싶지 않을 때 삭제한다.
     * @param callback 삭제할 콜백
     */
    public void removeCallback(final ISessionCallback callback) {
        synchronized (callbacks) {
            if (callback != null) {
                callbacks.remove(callback);
            }
        }
    }

    /**
     * Remove all session callbacks.
     */
    public void clearCallbacks() {
        synchronized (callbacks) {
            callbacks.clear();
        }
    }

    List<ISessionCallback> getCallbacks() {
        return callbacks;
    }

    private void requestAuthCode(final AuthType authType, final StartActivityWrapper wrapper) {
        if (wrapper.getActivity() != null) {
            authCodeManager.requestAuthCode(authType, wrapper.getActivity(), getAuthCodeCallback());
        } else if (wrapper.getSupportFragment() != null) {
            authCodeManager.requestAuthCode(authType, wrapper.getSupportFragment(), getAuthCodeCallback());
        } else if (wrapper.getFragment() != null) {
            authCodeManager.requestAuthCode(authType, wrapper.getFragment(), getAuthCodeCallback());
        } else {
            throw new IllegalArgumentException("You should provide activity or fragment to get Authorization code.");
        }
    }

    @SuppressWarnings("WeakerAccess")
    public AuthCodeCallback getAuthCodeCallback() {
        if (authCodeCallback == null) {
            synchronized (Session.class) {
                if (authCodeCallback == null) {
                    authCodeCallback = new AuthCodeCallback() {
                        @Override
                        public void onAuthCodeReceived(String authCode) {
                            Session.this.onAuthCodeReceived(authCode);
                        }

                        @Override
                        public void onAuthCodeFailure(ErrorResult errorResult) {
                            Session.this.onAuthCodeFailure(errorResult);
                        }
                    };
                }
            }
        }
        return authCodeCallback;
    }

    @Override
    public AccessTokenCallback getAccessTokenCallback() {
        if (accessTokenCallback == null) {
            synchronized (Session.class) {
                if (accessTokenCallback == null) {
                    accessTokenCallback = new AccessTokenCallback() {
                        @Override
                        public void onAccessTokenReceived(AccessToken accessToken) {
                            Session.this.onAccessTokenReceived(accessToken);
                        }

                        @Override
                        public void onAccessTokenFailure(ErrorResult errorResult) {
                            Session.this.onAccessTokenFailure(errorResult);
                        }
                    };
                }
            }
        }
        return accessTokenCallback;
    }
    
    /**
     * 세션을 close하여 처음부터 새롭게 세션 open을 진행한다.
     * @param exception exception이 발생하여 close하는 경우 해당 exception을 넘긴다.
     */
    void internalClose(final KakaoException exception) {
        synchronized (INSTANCE_LOCK) {
            requestType = null;
            authorizationCode = AuthorizationCode.createEmptyCode();
            getTokenInfo().clearAccessToken();
            getTokenInfo().clearRefreshToken();
        }
        if (this.appCache != null) {
            this.appCache.clearAll();
        }

        try {
            deregisterTokenManager();
        } catch (Throwable e) {
            Logger.e(e);
        }

        if (exception != null) {
            final List<ISessionCallback> dumpSessionCallbacks = new ArrayList<>(callbacks);
            for (ISessionCallback callback : dumpSessionCallbacks) {
                callback.onSessionOpenFailed(exception);
            }
        }
    }

    void setAuthService(AuthService authService) {
        this.authService = authService;
    }

    private void registerTokenManager(final long interval) {
        tokenAlarmManager.cancel(alarmIntent);
        try {
            tokenAlarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + interval, interval, alarmIntent);
        } catch (Exception e) {
            Logger.w("Failed to register automatic token refresh.", e);
        }
    }

    private void deregisterTokenManager() {
        tokenAlarmManager.cancel(alarmIntent);
    }

    private void onAuthCodeReceived(String authCode) {
        if (authCode != null) {
            //  request가 성공적으로 끝났으니 request는 reset
            synchronized (INSTANCE_LOCK) {
                requestType = null;
                authorizationCode = new AuthorizationCode(authCode);
            }
            internalOpen(null, null, authCode);
        }
    }

    private void onAuthCodeFailure(ErrorResult errorResult) {
        internalClose(wrapAsKakaoException(errorResult.getException()));
    }

    private void onAccessTokenReceived(AccessToken accessToken) {
        postProcessAccessToken(accessToken);
        final List<ISessionCallback> dumpSessionCallbacks = new ArrayList<>(callbacks);
        for (ISessionCallback callback : dumpSessionCallbacks) {
            callback.onSessionOpened();
        }
    }

    private void postProcessAccessToken(final AccessToken accessToken) {
        synchronized (INSTANCE_LOCK) {
            authorizationCode = AuthorizationCode.createEmptyCode(); // auth code can be used only once.
            updateAccessToken(accessToken); // refresh 요청에는 refresh token이 내려오지 않을 수 있으므로 accessToken = resultAccessToken을 하면 안된다.
            requestType = null;
        }

        final int interval = Math.min(DEFAULT_TOKEN_REQUEST_TIME_MILLIS, getTokenInfo().getRemainingExpireTime());
        registerTokenManager(interval);
    }

    private void onAccessTokenFailure(ErrorResult errorResult) {
        if (shouldClearSessionState(errorResult)) {
            synchronized (INSTANCE_LOCK) {
                requestType = null;
            }
            final List<ISessionCallback> dumpSessionCallbacks = new ArrayList<>(callbacks);
            for (ISessionCallback callback : dumpSessionCallbacks) {
                KakaoException exception = new KakaoException(ErrorType.AUTHORIZATION_FAILED, errorResult.getErrorMessage());
                callback.onSessionOpenFailed(exception);
            }
        }
    }

    /**
     *
     * @return true if session state should be cleared afterwards, false if this method cleared it
     */
    private boolean shouldClearSessionState(ErrorResult errorResult) {
        KakaoException exception = new KakaoException(ErrorType.AUTHORIZATION_FAILED, errorResult.getErrorMessage());
        if ((requestType != null && requestType == RequestType.GETTING_ACCESS_TOKEN)) {
            // code로 요청한 경우는 code는 일회성이므로 재사용 불가. exception 종류에 상관 없이 무조건 close
            internalClose(exception);
        } else if (requestType == RequestType.REFRESHING_ACCESS_TOKEN &&
                shouldCloseSession(errorResult)) {
            // refresh token으로 요청한 경우는 서버에서 refresh token을 재사용할 수 없다고 에러를 준 경우만 close.
            internalClose(exception);
        } else {
            return true;
        }
        return false;
    }

    private KakaoException wrapAsKakaoException(Exception e) {
        if (e == null)
            return null;
        if (e instanceof KakaoException)
            return (KakaoException) e;
        return new KakaoException(e);
    }

    /**
     * AppKey를 반환한다.
     * @return App key
     *
     * @deprecated Use {@link Session#getTokenInfo()} instead
     */
    @Deprecated
    public final String getAppKey() {
        return contextService.phaseInfo().appKey();
    }

    /**
     * 현재 세션이 가지고 있는 access token을 반환한다.
     * @return access token
     *
     * @deprecated Use {@link Session#getTokenInfo()} instead
     */
    @Deprecated
    public final String getAccessToken() {
        synchronized (INSTANCE_LOCK) {
            return (accessToken == null) ? null : accessToken.getAccessToken();
        }
    }

    /**
     * 현재 세션이 가지고 있는 refresh token을 반환한다.
     * @return refresh token
     *
     * @deprecated Use {@link Session#getTokenInfo()} instead
     */
    @Deprecated
    public final String getRefreshToken() {
        synchronized (INSTANCE_LOCK) {
            return (accessToken == null) ? null : accessToken.getRefreshToken();
        }
    }

    /**
     * 현재 세션이 가지고 있는 access token이 유효한지 판단.
     * @return 현재 세션이 가지고 있는 access token이 유효한지 여부.
     *
     * @deprecated Use {@link Session#getTokenInfo()} instead
     */
    @Deprecated
    public final boolean hasValidAccessToken() {
        synchronized (INSTANCE_LOCK) {
            return accessToken != null && accessToken.hasValidAccessToken();
        }
    }

    /**
     * @deprecated Use {@link Session#getTokenInfo()} instead
     */
    @Deprecated
    public void removeAccessToken() {
        synchronized (INSTANCE_LOCK) {
            if (accessToken != null) {
                accessToken.clearAccessToken();
            }
        }
    }

    /**
     * 현재 세션이 가지고 있는 access token과 refresh token을 무효화 시킨다.
     *
     * @deprecated Use {@link Session#getTokenInfo()} instead
     */
    @Deprecated
    public void invalidateAccessToken() {
        synchronized (INSTANCE_LOCK) {
            accessToken.clearAccessToken();
            accessToken.clearRefreshToken();
        }
    }

    /**
     * @deprecated Use {@link Session#getTokenInfo()} instead
     */
    @Deprecated
    public void removeRefreshToken() {
        synchronized (INSTANCE_LOCK) {
            if (accessToken != null) {
                accessToken.clearRefreshToken();
            }
        }
    }

    /**
     * 토큰 갱신이 가능한지 여부를 반환한다.
     * 토큰 갱신은 background로 사용자가 모르도록 진행한다.
     *
     * @return 토큰 갱신을 진행할 때는 true, 토큰 갱신을 하지 못할때는 false를 return 한다.
     *
     * @deprecated Use {@link Session#getTokenInfo()} instead
     */
    @Deprecated
    public boolean isAvailableOpenByRefreshToken() {
        return isOpened() || getTokenInfo().hasValidRefreshToken();
    }

    /**
     * Session의 Token 발급이나, 갱신과정을 나타내는 상태값.
     * KakaoSDK가 내부적으로 관리하고 수행하게 된다.
     */
    enum RequestType {
        /**
         * AuthCode를 발급받고 있는 과정
         */
        GETTING_AUTHORIZATION_CODE,
        /**
         * AccessToken을 발급받고 있는 과정.
         */
        GETTING_ACCESS_TOKEN,
        /**
         * RefreshToken을 갱신히고 있는 과정.
         */
        REFRESHING_ACCESS_TOKEN
    }
}
