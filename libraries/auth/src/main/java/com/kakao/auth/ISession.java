package com.kakao.auth;

import android.app.Activity;
import android.app.Fragment;

import com.kakao.auth.authorization.accesstoken.AccessToken;
import com.kakao.auth.authorization.accesstoken.AccessTokenManager;
import com.kakao.auth.authorization.authcode.AuthCodeManager;

import java.util.concurrent.Future;

/**
 * @author kevin.kang. Created on 2017. 9. 27..
 */

public interface ISession {

    /**
     * 세션 오픈을 진행한다.
     * isOpened() 상태이면 콜백 호출 후 바로 종료.
     * isClosed() 상태이면 authorization code 요청. 에러/취소시 isClosed()
     * isOpenable 상태이면 code 또는 refresh token 이용하여  access token 을 받아온다. 에러/취소시 {isClosed()), refresh 취소시에만 isOpenable() 유지.
     * param으로 받은 콜백으로 그 결과를 전달한다.
     *
     * @param authType 인증받을 타입. 예를 들어, 카카오톡 또는 카카오스토리 또는 직접 입력한 카카오계정
     * @param activity 세션오픈을 호출한 activity
     */
    void open(final AuthType authType, final Activity activity);

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
    void open(final AuthType authType, final Fragment fragment);

    /**
     * 세션 오픈을 진행한다.
     * isOpened() 상태이면 콜백 호출 후 바로 종료.
     * isClosed() 상태이면 authorization code 요청. 에러/취소시 isClosed()
     * isOpenable 상태이면 code 또는 refresh token 이용하여  access token 을 받아온다. 에러/취소시 {isClosed()), refresh 취소시에만 isOpenable() 유지.
     * param으로 받은 콜백으로 그 결과를 전달한다.
     *
     * @param authType 인증받을 타입. 예를 들어, 카카오톡 또는 카카오스토리 또는 직접 입력한 카카오계정
     * @param supportFragment 세션오픈을 호출한 fragment
     */
    void open(final AuthType authType, final android.support.v4.app.Fragment supportFragment);

    /**
     * Try login (open session) with authorization code.
     *
     * @param authCode Authorization code acquired by Kakao account authentication/authorization
     */
    void openWithAuthCode(final String authCode);

    void close();

    /**
     * 현재 세션이 열린 상태인지 여부를 반환한다.
     * @return 세션이 열린 상태라면 true, 그외의 경우 false를 반환한다.
     */
    boolean isOpened();

    /**
     * 현재 세션이 오픈중(갱신 포함) 상태인지 여부를 반환한다.
     *
     * 1. Access token이 없거나 만료되었고, auth code가 발급되어 있는 상태.
     * 2. Access token이 없거나 만료되었고, refresh token이 있는 상태.
     *
     * @return 세션 오픈 진행 중이면 true, 그외 경우는 false를 반환한다.
     */
    boolean isOpenable();

    /**
     * 현재 세션이 닫힌 상태인지 여부를 반환한다. 세션이 닫혀 있다면 새로이 인증을 거쳐야 한다.
     * @return 세션이 닫힌 상태라면 true, 그외의 경우 false를 반환한다.
     */
    boolean isClosed();

    Future<AccessToken> refreshAccessToken(final AccessTokenCallback callback);

    /**
     * Returns currently managed {@link AccessToken} instance containing access token and refresh
     * token information.
     *
     * @return {@link AccessToken} instance
     */
    AccessToken getTokenInfo();

    /**
     * 세션 상태 변화 콜백을 받고자 할때 콜백을 등록한다.
     * @param callback 추가할 세션 콜백
     */
    void addCallback(ISessionCallback callback);

    /**
     * 더이상 세션 상태 변화 콜백을 받고 싶지 않을 때 삭제한다.
     * @param callback 삭제할 콜백
     */
    void removeCallback(ISessionCallback callback);

    /**
     * Remove all session callbacks.
     */
    void clearCallbacks();

    AuthCodeManager getAuthCodeManager();
    AccessTokenManager getAccessTokenManager();
    AccessTokenCallback getAccessTokenCallback();
}
