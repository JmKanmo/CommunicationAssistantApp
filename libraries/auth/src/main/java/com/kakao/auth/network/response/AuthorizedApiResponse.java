package com.kakao.auth.network.response;

import com.kakao.auth.ApiErrorCode;
import com.kakao.network.response.ApiResponseStatusError;

import java.net.HttpURLConnection;

/**
 * @author kevin.kang. Created on 2017. 11. 30..
 */

public abstract class AuthorizedApiResponse {

    public static class SessionClosedException extends ApiResponseStatusError {
        public SessionClosedException(String errorMsg) {
            this(ApiErrorCode.INVALID_TOKEN_CODE, errorMsg, HttpURLConnection.HTTP_UNAUTHORIZED);
        }

        public SessionClosedException(int errorCode, String errorMsg, int httpStatusCode) {
            super(errorCode, errorMsg, httpStatusCode);
        }
    }
}
