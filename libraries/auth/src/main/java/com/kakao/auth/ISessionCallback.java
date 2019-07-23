package com.kakao.auth;

import com.kakao.util.exception.KakaoException;

/**
 * 세션의 상태 변경에 따른 콜백
 * 세션이 오픈되었을 때, 세션이 만료되어 닫혔을 때 세션 콜백을 넘기게 된다.
 * @author leoshin on 15. 9. 15.
 */
public interface ISessionCallback {

    /**
     * access token을 성공적으로 발급 받아 valid access token을 가지고 있는 상태.
     * 일반적으로 로그인 후의 다음 activity로 이동한다.
     */
    void onSessionOpened();

    /**
     * 로그인을 실패한 상태.
     * 세션이 만료된 경우와는 다르게 네트웤등 일반적인 에러로 오픈에 실패한경우 불린다.
     * @param exception 에러가 발생한 경우에 해당 exception.
     *
     */
    void onSessionOpenFailed(final KakaoException exception);
}
