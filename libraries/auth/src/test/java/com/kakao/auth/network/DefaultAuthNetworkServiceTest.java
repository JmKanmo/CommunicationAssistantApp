package com.kakao.auth.network;

import com.kakao.auth.ApiErrorCode;
import com.kakao.auth.StringSet;
import com.kakao.auth.authorization.accesstoken.AccessToken;
import com.kakao.auth.authorization.accesstoken.TestAccessToken;
import com.kakao.auth.mocks.TestAuthorizedRequest;
import com.kakao.auth.mocks.TestNetworkService;
import com.kakao.auth.network.response.AccessTokenInfoResponse;
import com.kakao.auth.network.response.AuthorizedApiResponse;
import com.kakao.auth.network.response.InsufficientScopeException;
import com.kakao.network.NetworkService;
import com.kakao.network.response.ApiResponseStatusError;
import com.kakao.network.response.ResponseBody;
import com.kakao.network.response.ResponseStringConverter;
import com.kakao.network.tasks.ITaskQueue;
import com.kakao.network.tasks.KakaoResultTask;
import com.kakao.test.common.KakaoTestCase;
import com.kakao.test.common.TestAppConfiguration;
import com.kakao.test.common.TestPhaseInfo;
import com.kakao.common.KakaoContextService;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests Authorized request (which means API request with access token).
 *
 * @author kevin.kang. Created on 2017. 12. 4..
 */

public class DefaultAuthNetworkServiceTest extends KakaoTestCase {
    private DefaultAuthNetworkService authNetworkService;


    private AccessToken tokenInfo;
    private NetworkService networkService;
    private ApiErrorHandlingService errorHandlingService;


    public void setup() {
        super.setup();
        networkService = spy(new TestNetworkService());
        ITaskQueue taskQueue = spy(new ITaskQueue() {
            @Override
            public <T> Future<T> addTask(KakaoResultTask<T> task) {
                return null;
            }
        });
        tokenInfo = spy(new TestAccessToken());
        errorHandlingService = spy(new TestApiErrorHandlingService());

        authNetworkService = spy(new DefaultAuthNetworkService(networkService, taskQueue));
        authNetworkService.setTokenInfo(tokenInfo);
        authNetworkService.setErrorHandlingService(errorHandlingService);
        authNetworkService.setConfService(new KakaoContextService(new TestAppConfiguration(), new TestPhaseInfo()));
    }

    /**
     * Tests normal case of API request with access token.
     */
    @Test
    public void request() throws Exception {
        AuthorizedRequest request = new TestAuthorizedRequest();
        ResponseStringConverter<AccessTokenInfoResponse> converter = AccessTokenInfoResponse.CONVERTER;
        doReturn(converter.convert(getJsonString())).when(networkService).request(request, converter);
        AccessTokenInfoResponse response =
                authNetworkService.request(request, converter);
        assertEquals(1234, response.getUserId());
    }

    /**
     * Tests when access token has expired but can be refreshed with refresh token.
     */
    @Test
    public void requestWithExpiredToken() throws Exception {
        AuthorizedRequest request = new TestAuthorizedRequest();
        ResponseStringConverter<AccessTokenInfoResponse> converter = AccessTokenInfoResponse.CONVERTER;
        doReturn(false).doReturn(true).when(tokenInfo).hasValidAccessToken();
        doReturn(true).when(errorHandlingService).shouldRetryAfterTryingRefreshToken();
        doReturn(converter.convert(getJsonString())).when(networkService).request(request, converter);
        AccessTokenInfoResponse response = authNetworkService.request(request, converter);
        assertEquals(1234, response.getUserId());
    }

    /**
     * Tests when both access token and refresh token have expired.
     */
    @Test(expected = AuthorizedApiResponse.SessionClosedException.class)
    public void requestWithExpiredTokenAndRefreshToken() throws Exception {
        AuthorizedRequest request = new TestAuthorizedRequest();
        ResponseStringConverter<AccessTokenInfoResponse> converter = AccessTokenInfoResponse.CONVERTER;
        doReturn(false).when(tokenInfo).hasValidAccessToken();
        doReturn(false).when(errorHandlingService).shouldRetryAfterTryingRefreshToken();
        authNetworkService.requestList(request, converter);
    }

    /**
     * Tests authorized request with {@link ApiErrorCode#INVALID_TOKEN_CODE} and {@link ApiErrorHandlingService#shouldRetryWithApiError(ApiResponseStatusError)}
     * returning false (meaning there is no retry).
     *
     * This should produce Exception
     */
    @Test
    public void requestWithInvalidToken() {
        AuthorizedRequest request = new TestAuthorizedRequest();
        ResponseStringConverter<AccessTokenInfoResponse> converter = AccessTokenInfoResponse.CONVERTER;
        try {
            doThrow(getInvalidTokenError())
                    .when(networkService).request(request, converter);
            authNetworkService.request(request, converter);
            fail("Should fail with ApiResponseStatusError");
        } catch (ApiResponseStatusError e) {
            assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, e.getHttpStatusCode());
            assertEquals(ApiErrorCode.INVALID_TOKEN_CODE, e.getErrorCode());
        } catch (Exception e) {
            fail("Should fail with ApiResponseStatusError");
        }
    }

    /**
     * Tests authorized request with {@link ApiErrorCode#INVALID_TOKEN_CODE} and {@link ApiErrorHandlingService#shouldRetryWithApiError(ApiResponseStatusError)}
     * returning false.
     *
     * This should produce {@link InsufficientScopeException}
     */
    @Test
    public void requestWithInvalidScope() throws Exception {
        AuthorizedRequest request = new TestAuthorizedRequest();
        ResponseStringConverter<AccessTokenInfoResponse> converter = AccessTokenInfoResponse.CONVERTER;
        ApiResponseStatusError error = getInvalidScopeError();
        try {
            doThrow(error)
                    .when(networkService).request(request, converter);
            authNetworkService.request(request, converter);
            fail("Should fail with ApiResponseStatusError");
        } catch (ApiResponseStatusError e) {
            assertEquals(HttpURLConnection.HTTP_FORBIDDEN, e.getHttpStatusCode());
            assertEquals(ApiErrorCode.INVALID_SCOPE_CODE, e.getErrorCode());
        }
    }

    /**
     * Tests authorized request with {@link ApiErrorCode#INVALID_TOKEN_CODE} and {@link ApiErrorHandlingService#shouldRetryWithApiError(ApiResponseStatusError)}
     * returning true.
     *
     * This should result in a successful response.
     */
    @Test
    public void requestWithApiErrorAndRetry() throws Exception {
        AuthorizedRequest request = new TestAuthorizedRequest();
        ResponseStringConverter<AccessTokenInfoResponse> converter = AccessTokenInfoResponse.CONVERTER;
        ApiResponseStatusError error = getInvalidTokenError();
        doReturn(true).when(errorHandlingService).shouldRetryWithApiError(error);
        doThrow(error)
                .doReturn(converter.convert(getJsonString()))
                .when(networkService).request(request, converter);
        AccessTokenInfoResponse response =
                authNetworkService.request(request, converter);
        assertEquals(1234, response.getUserId());
    }

    @Test
    public void requestList() throws Exception {
        AuthorizedRequest request = new TestAuthorizedRequest();
        ResponseStringConverter<String> converter = getStringConverter();
        List<String> response = new ArrayList<>();
        doReturn(response).when(networkService).requestList(request, converter);
        List<String> result = authNetworkService.requestList(request, converter);
        assertEquals(response, result);
    }

    @Test
    public void requestListWithInvalidToken() throws Exception {
        AuthorizedRequest request = new TestAuthorizedRequest();
        ResponseStringConverter<String> converter = getStringConverter();
        ApiResponseStatusError error = getInvalidTokenError();
        try {
            doThrow(error).when(networkService).requestList(request, converter);
            authNetworkService.requestList(request, converter);
            fail("Should fail with ApiResponseStatusError");
        } catch (ApiResponseStatusError e) {
            assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, e.getHttpStatusCode());
            assertEquals(ApiErrorCode.INVALID_TOKEN_CODE, e.getErrorCode());
        }
    }

    @Test
    public void requestListWithInvalidScope() throws Exception {
        AuthorizedRequest request = new TestAuthorizedRequest();
        ResponseStringConverter<String> converter = getStringConverter();
        ApiResponseStatusError error = getInvalidScopeError();
        try {
            doThrow(error).when(networkService).requestList(request, converter);
            authNetworkService.requestList(request, converter);
            fail("Should fail with ApiResponseStatusError");
        } catch (ApiResponseStatusError e) {
            assertEquals(HttpURLConnection.HTTP_FORBIDDEN, e.getHttpStatusCode());
            assertEquals(ApiErrorCode.INVALID_SCOPE_CODE, e.getErrorCode());
        }
    }

    @Test
    public void requestListWithInvalidScopeAndRetry() throws Exception {
        AuthorizedRequest request = new TestAuthorizedRequest();
        ResponseStringConverter<String> converter = getStringConverter();
        ApiResponseStatusError error = getInvalidScopeError();
        List<String> response = new ArrayList<>();
        doReturn(true).when(errorHandlingService).shouldRetryWithApiError(error);
        doThrow(error)
                .doReturn(response)
                .when(networkService).requestList(request, converter);
        List<String> result = authNetworkService.requestList(request, converter);
        assertEquals(response, result);
    }

    ResponseStringConverter<String> getStringConverter() {
        return spy(new ResponseStringConverter<String>() {
            @Override
            public String convert(String data) {
                return data;
            }
        });
    }

    ResponseBody getResponseBody() {
        ResponseBody body = null;
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(StringSet.id, 1234);
            jsonObject.put(StringSet.expiresInMillis, System.currentTimeMillis() + 1000 * 60 * 60 * 12);
            body = new ResponseBody(HttpURLConnection.HTTP_OK, jsonObject);
        } catch (JSONException|ResponseBody.ResponseBodyException e) {
            fail(e.toString());
        }
        return body;
    }

    String getJsonString() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(StringSet.id, 1234);
            jsonObject.put(StringSet.expiresInMillis, System.currentTimeMillis() + 1000 * 60 * 60 * 12);
        } catch (JSONException e) {
            fail(e.toString());
        }
        return jsonObject.toString();
    }

    ApiResponseStatusError getInvalidTokenError() {
        return new ApiResponseStatusError(ApiErrorCode.INVALID_TOKEN_CODE, "error_message", HttpURLConnection.HTTP_UNAUTHORIZED);
    }

    ApiResponseStatusError getInvalidScopeError() {
        return new ApiResponseStatusError(ApiErrorCode.INVALID_SCOPE_CODE, "error_message", HttpURLConnection.HTTP_FORBIDDEN);
    }
}
