package com.kakao.push;

/**
 * ApiErrorCode related to push module.
 *
 * @author kevin.kang. Created on 2017. 11. 30..
 */

public class ApiErrorCode extends com.kakao.auth.ApiErrorCode {
    /**
     * 등록된 푸시토큰이 없는 기기로 푸시 메시지를 보낸 경우 발생한다. code = -901
     */
    public static final int NOT_EXIST_PUSH_TOKEN = -901;
}
