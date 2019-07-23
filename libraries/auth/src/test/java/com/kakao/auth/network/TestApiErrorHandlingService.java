package com.kakao.auth.network;

import com.kakao.network.response.ApiResponseStatusError;

/**
 * @author kevin.kang. Created on 2018. 1. 8..
 */

public class TestApiErrorHandlingService implements ApiErrorHandlingService {
    @Override
    public boolean shouldRetryWithApiError(ApiResponseStatusError error) throws Exception {
        return false;
    }

    @Override
    public boolean shouldRetryAfterTryingRefreshToken() throws Exception {
        return false;
    }
}
