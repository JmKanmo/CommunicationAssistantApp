package com.kakao.auth.network.response;

import com.kakao.auth.ApiErrorCode;
import com.kakao.auth.StringSet;
import com.kakao.network.exception.ResponseStatusError;
import com.kakao.network.response.CustomErrorConverter;
import com.kakao.network.response.ResponseBody;


/**
 * @author kevin.kang. Created on 2017. 12. 4..
 */
public class AuthResponseError extends ResponseStatusError {
    private static final long serialVersionUID = 3702596857996303483L;
    private final int httpStatusCode;
    private final String error;
    private final String errorDescription;
    private final ResponseBody errorResponse;

    public AuthResponseError(int httpStatusCode, ResponseBody errorResponse) throws ResponseBody.ResponseBodyException {
        super(errorResponse.toString());
        this.httpStatusCode = httpStatusCode;
        this.errorResponse = errorResponse;
        this.error = errorResponse.getString(StringSet.error);
        this.errorDescription = errorResponse.optString(StringSet.error_description, "");
    }

    @Override
    public int getErrorCode() {
        return ApiErrorCode.AUTH_ERROR_CODE;
    }

    public String getError() {
        return error;
    }

    public String getErrorMsg() {
        return errorDescription;
    }

    public int getHttpStatusCode() {
        return httpStatusCode;
    }

    public static final CustomErrorConverter<AuthResponseError> CONVERTER = new CustomErrorConverter<AuthResponseError>() {
        @Override
        public AuthResponseError convert(int httpStatusCode, String errorResponse) {
            return new AuthResponseError(httpStatusCode, new ResponseBody(errorResponse));
        }
    };

    public ResponseBody getErrorResponse() {
        return errorResponse;
    }
}
