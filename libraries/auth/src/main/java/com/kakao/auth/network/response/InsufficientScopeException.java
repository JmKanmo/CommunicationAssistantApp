package com.kakao.auth.network.response;

import com.kakao.auth.ApiErrorCode;
import com.kakao.network.response.ApiResponseStatusError;
import com.kakao.network.response.ResponseBody;

import java.net.HttpURLConnection;

/**
 * @author kevin.kang. Created on 2017. 12. 4..
 */
public class InsufficientScopeException extends ApiResponseStatusError {
    public InsufficientScopeException(ResponseBody body) throws ResponseBody.ResponseBodyException {
        this(body.getInt(com.kakao.network.StringSet.code), body.optString(com.kakao.network.StringSet.msg, ""), HttpURLConnection.HTTP_FORBIDDEN, body);
    }

    public InsufficientScopeException(final String errorMsg) {
        super(ApiErrorCode.INVALID_SCOPE_CODE, errorMsg, HttpURLConnection.HTTP_FORBIDDEN);
    }

    public InsufficientScopeException(int errorCode, String errorMsg, int httpStatusCode, ResponseBody body) {
        super(errorCode, errorMsg, httpStatusCode, body);
    }
}
