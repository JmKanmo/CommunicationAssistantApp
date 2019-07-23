package com.kakao.auth.network;

import com.kakao.auth.Session;
import com.kakao.auth.ageauth.DefaultAgeAuthService;
import com.kakao.auth.helper.CurrentActivityProvider;
import com.kakao.network.response.ApiResponseStatusError;

/**
 * @author kevin.kang. Created on 2018. 1. 5..
 */

public interface ApiErrorHandlingService {
    /**
     *
     * @param error {@link ApiResponseStatusError}
     * @return true if API can be requested again, false otherwise
     * @throws Exception
     */
    boolean shouldRetryWithApiError(ApiResponseStatusError error) throws Exception;

    /**
     *
     * @return true if API can be requested again after refreshing access token, false otherwise
     * @throws Exception
     */
    boolean shouldRetryAfterTryingRefreshToken() throws Exception;


    class Factory {
        private static ApiErrorHandlingService instance;
        static {
            DefaultApiErrorHandlingService service = new DefaultApiErrorHandlingService();
            service.setSession(Session.getCurrentSession());
            service.setAgeAuthService(DefaultAgeAuthService.getInstance());
            service.setActivityProvider(CurrentActivityProvider.Factory.getInstance());
            instance = service;
        }

        public static ApiErrorHandlingService getInstance() {
            return instance;
        }
    }
}
