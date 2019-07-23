package com.kakao.auth;

/**
 * ApiErrorCode related to auth module.
 *
 * @author kevin.kang. Created on 2017. 11. 30..
 */

public class ApiErrorCode extends com.kakao.network.ApiErrorCode {
    /**
     * [로그인기반 API]
     * 해당 앱에 가입되지 않은 사용자가 호출한 경우 발생한다. code = -101
     */
    public static final int NOT_REGISTERED_USER_CODE = -101;
    /**
     * [사용자 관리 signup API]
     * 이미 해당 앱에 가입한 유저가 다시 가입 API를 요청한 경우 발생한다. code = -102
     */
    public static final int ALREADY_REGISTERED_USER_CODE = -101;
    /**
     * [카카오톡 API]
     * 존재하지 않는 카카오계정으로 요청한 경우 발생한다. code = -103
     */
    public static final int NOT_EXIST_KAKAO_ACCOUNT_CODE = -103;
    /**
     * 해당 API에 대한 퍼미션이 없는 앱이 요청한 경우 발생한다. code = -402
     */
    public static final int INVALID_SCOPE_CODE = -402;
    /**
     * 연령인증이 필요한 경우 발생한다. code = -405
     */
    public static final int NEED_TO_AGE_AUTHENTICATION = -405;
    /**
     * 앱에 제한된 연령이하가 증명된 사용자가 요청한 경우. (후처리 : 연령인증 페이지를 띄워줘도 통과할 수 없으므로 에러 발생)
     * code = -406
     */
    public static final int UNDER_AGE_LIMIT = -406;
    /**
     * OAuth 서버에서 발생하는 에러일때.
     */
    public static final int AUTH_ERROR_CODE = -776;
}
