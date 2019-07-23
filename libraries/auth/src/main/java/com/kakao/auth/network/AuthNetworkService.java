package com.kakao.auth.network;

import com.kakao.auth.authorization.accesstoken.AccessToken;
import com.kakao.network.NetworkService;
import com.kakao.network.callback.ResponseCallback;
import com.kakao.network.response.ApiResponseStatusError;
import com.kakao.network.response.ResponseBody;
import com.kakao.network.response.ResponseStringConverter;
import com.kakao.network.tasks.KakaoTaskQueue;
import com.kakao.common.KakaoContextService;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Future;

/**
 * @author kevin.kang. Created on 2017. 11. 28..
 */

public interface AuthNetworkService {
    /**
     *
     * @param request {@link AuthorizedRequest} object that represents API requests requiring access token
     * @param converter String converter for Response Type {@link T}
     * @param <T>
     * @return
     * @throws IOException
     * @throws ResponseBody.ResponseBodyException
     * @throws ApiResponseStatusError
     */
    <T> T request(final AuthorizedRequest request, final ResponseStringConverter<T> converter) throws Exception;

    /**
     *
     * @param request {@link AuthorizedRequest} object that represents API requests requiring access token
     * @param converter String converter for Response Type {@link T}
     * @param callback
     * @param <T>
     * @return Future containing {@link T}
     */
    <T> Future<T> request(final AuthorizedRequest request, final ResponseStringConverter<T> converter, ResponseCallback<T> callback);

    /**
     *
     * @param request {@link AuthorizedRequest} object that represents API requests requiring access token
     * @param converter String converter for Response Type {@link T}
     * @param <T>
     * @return
     * @throws Exception
     */
    <T> List<T> requestList(final AuthorizedRequest request, final ResponseStringConverter<T> converter) throws Exception;

    /**
     *
     * @param request {@link AuthorizedRequest} object that represents API requests requiring access token
     * @param converter String converter for Response Type {@link T}
     * @param callback
     * @param <T>
     * @return Future containing {@link List<T>}
     */
    <T> Future<List<T>> requestList(final AuthorizedRequest request, final ResponseStringConverter<T> converter, ResponseCallback<List<T>> callback);

    class Factory {
        private static AuthNetworkService instance;


        static {
            DefaultAuthNetworkService authNetworkService = new DefaultAuthNetworkService(
                    NetworkService.Factory.getInstance(),
                    KakaoTaskQueue.getInstance());
            authNetworkService.setTokenInfo(AccessToken.Factory.getInstance());
            authNetworkService.setErrorHandlingService(ApiErrorHandlingService.Factory.getInstance());
            authNetworkService.setConfService(KakaoContextService.getInstance());
            instance = authNetworkService;
        }

        public static AuthNetworkService getInstance() {
            return instance;
        }
    }
}
