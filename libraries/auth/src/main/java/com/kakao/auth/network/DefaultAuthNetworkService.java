package com.kakao.auth.network;

import com.kakao.auth.authorization.accesstoken.AccessToken;
import com.kakao.auth.network.response.AuthorizedApiResponse;
import com.kakao.network.NetworkService;
import com.kakao.network.callback.ResponseCallback;
import com.kakao.network.response.ApiResponseStatusError;
import com.kakao.network.response.ResponseStringConverter;
import com.kakao.network.tasks.ITaskQueue;
import com.kakao.network.tasks.KakaoResultTask;
import com.kakao.common.KakaoContextService;

import java.util.List;
import java.util.concurrent.Future;

/**
 * @author kevin.kang. Created on 2017. 11. 28..
 */

class DefaultAuthNetworkService implements AuthNetworkService {
    private final NetworkService networkService;
    private final ITaskQueue taskQueue;
    private ApiErrorHandlingService errorHandlingService;
    private AccessToken tokenInfo;
    private KakaoContextService contextService;

    public void setErrorHandlingService(ApiErrorHandlingService service) {
        this.errorHandlingService = service;
    }

    public void setTokenInfo(AccessToken tokenInfo) {
        this.tokenInfo = tokenInfo;
    }

    public void setConfService(KakaoContextService contextService) {
        this.contextService = contextService;
    }

    DefaultAuthNetworkService(final NetworkService networkService,
                              final ITaskQueue taskQueue) {
        this.networkService = networkService;
        this.taskQueue = taskQueue;
    }

    @Override
    public <T> Future<T> request(final AuthorizedRequest request, final ResponseStringConverter<T> converter, final ResponseCallback<T> callback) {
        return taskQueue.addTask(new KakaoResultTask<T>(callback) {
            @Override
            public T call() throws Exception {
                return request(request, converter);
            }
        });
    }

    @Override
    public <T> T request(AuthorizedRequest request, ResponseStringConverter<T> converter) throws Exception {
        if (!tokenInfo.hasValidAccessToken() &&
                !errorHandlingService.shouldRetryAfterTryingRefreshToken()) {
            throw new AuthorizedApiResponse.SessionClosedException("Could not refresh access token.");
        }
        request.setConfiguration(contextService.phaseInfo(), contextService.getAppConfiguration());
        request.setAccessToken(tokenInfo.getAccessToken());
        try {
            return networkService.request(request, converter);
        } catch (ApiResponseStatusError e) {
            if (errorHandlingService.shouldRetryWithApiError(e)) {
                return request(request, converter);
            }
            throw e;
        }
    }

    @Override
    public <T> Future<List<T>> requestList(final AuthorizedRequest request, final ResponseStringConverter<T> converter, ResponseCallback<List<T>> callback) {
        return taskQueue.addTask(new KakaoResultTask<List<T>>(callback) {
            @Override
            public List<T> call() throws Exception {
                return requestList(request, converter);
            }
        });
    }

    @Override
    public <T> List<T> requestList(AuthorizedRequest request, ResponseStringConverter<T> converter) throws Exception {
        if (!tokenInfo.hasValidAccessToken() &&
                !errorHandlingService.shouldRetryAfterTryingRefreshToken()) {
                throw new AuthorizedApiResponse.SessionClosedException("Could not refresh access token.");
        }
        request.setAccessToken(tokenInfo.getAccessToken());
        request.setConfiguration(contextService.phaseInfo(), contextService.getAppConfiguration());
        try {
            return networkService.requestList(request, converter);
        } catch (ApiResponseStatusError e) {
            if (errorHandlingService.shouldRetryWithApiError(e)) {
                return requestList(request, converter);
            }
            throw e;
        }
    }
}
