package com.kakao.auth.network;

import android.app.Activity;
import android.os.Bundle;

import com.kakao.auth.AccessTokenCallback;
import com.kakao.auth.ApiErrorCode;
import com.kakao.auth.AuthCodeCallback;
import com.kakao.auth.AuthService;
import com.kakao.auth.AuthType;
import com.kakao.auth.StringSet;
import com.kakao.auth.ageauth.AgeAuthService;
import com.kakao.auth.ageauth.TestAgeAuthService;
import com.kakao.auth.authorization.accesstoken.AccessToken;
import com.kakao.auth.authorization.accesstoken.AccessTokenManager;
import com.kakao.auth.authorization.accesstoken.TestAccessToken;
import com.kakao.auth.authorization.accesstoken.TestAccessTokenManager;
import com.kakao.auth.authorization.authcode.AuthCodeManager;
import com.kakao.auth.helper.CurrentActivityProvider;
import com.kakao.auth.helper.StartActivityWrapper;
import com.kakao.auth.mocks.TestAuthCodeManager;
import com.kakao.auth.mocks.TestSession;
import com.kakao.auth.network.response.InsufficientScopeException;
import com.kakao.network.ErrorResult;
import com.kakao.network.response.ApiResponseStatusError;
import com.kakao.network.response.ResponseBody;
import com.kakao.test.common.KakaoTestCase;
import com.kakao.util.exception.KakaoException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.robolectric.Robolectric;

import java.net.HttpURLConnection;
import java.util.concurrent.CompletableFuture;


import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author kevin.kang. Created on 2018. 1. 5..
 */

public class DefaultApiErrorHandlingServiceTest extends KakaoTestCase {
    private DefaultApiErrorHandlingService service;
    private AuthCodeManager authCodeManager;
    private AccessTokenManager accessTokenManager;
    private AccessToken tokenInfo;
    private TestSession session;
    private AgeAuthService ageAuthService;
    private CurrentActivityProvider activityProvider;

    @Override
    public void setup() {
        super.setup();
        service = new DefaultApiErrorHandlingService();
        authCodeManager = spy(new TestAuthCodeManager());
        accessTokenManager = spy(new TestAccessTokenManager());
        tokenInfo = spy(new TestAccessToken());
        ageAuthService = spy(new TestAgeAuthService());
        final Activity activity = Robolectric.setupActivity(Activity.class);
        activityProvider = new CurrentActivityProvider() {
            @Override
            public Activity getCurrentActivity() {
                return activity;
            }
        };

        session = spy(new TestSession());
        doReturn(accessTokenManager).when(session).getAccessTokenManager();
        doReturn(authCodeManager).when(session).getAuthCodeManager();
        doReturn(tokenInfo).when(session).getTokenInfo();
        service.setSession(session);

        service.setActivityProvider(activityProvider);
        service.setAgeAuthService(ageAuthService);
    }

    /**
     * Tests handling of {@link ApiErrorCode#INVALID_SCOPE_CODE} when scope is successfully updated.
     */
    @Test
    public void handleInvalidScope() {
        ApiResponseStatusError error = new ApiResponseStatusError(ApiErrorCode.INVALID_SCOPE_CODE,
                "error_message", HttpURLConnection.HTTP_FORBIDDEN,
                getErrorResponseBody(ApiErrorCode.INVALID_SCOPE_CODE));
        boolean retry = service.shouldRetryWithApiError(error);
        assertTrue(retry);
        verify(accessTokenManager).requestAccessTokenByAuthCode(anyString(), any(AccessTokenCallback.class));
    }

    /**
     * Tests handling of {@link ApiErrorCode#INVALID_SCOPE_CODE} when user pressed back button or
     * Disagree button when updating scopes.
     */
    @Test(expected = InsufficientScopeException.class)
    public void handleInvalidScopeWithUserCancel() {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                AuthCodeCallback callback = invocation.getArgument(3);
                callback.onAuthCodeFailure(new ErrorResult(new KakaoException(KakaoException.ErrorType.CANCELED_OPERATION, "error_msg")));
                return null;
            }
        }).when(authCodeManager).requestAuthCodeWithScopes(
                any(AuthType.class),
                any(StartActivityWrapper.class),
                ArgumentMatchers.<String>anyList(),
                any(AuthCodeCallback.class)
        );
        ApiResponseStatusError error = new ApiResponseStatusError(ApiErrorCode.INVALID_SCOPE_CODE,
                "error_message", HttpURLConnection.HTTP_FORBIDDEN,
                getErrorResponseBody(ApiErrorCode.INVALID_SCOPE_CODE));
        service.shouldRetryWithApiError(error);
    }

    /**
     * Tests handling of generic error when updating scopes.
     */
    @Test(expected = InsufficientScopeException.class)
    public void handleInvalidScopeWithClientError() {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                AuthCodeCallback callback = invocation.getArgument(3);
                callback.onAuthCodeFailure(new ErrorResult(new KakaoException(KakaoException.ErrorType.UNSPECIFIED_ERROR, "error_msg")));
                return null;
            }
        }).when(authCodeManager).requestAuthCodeWithScopes(
                any(AuthType.class),
                any(StartActivityWrapper.class),
                ArgumentMatchers.<String>anyList(),
                any(AuthCodeCallback.class)
        );
        ApiResponseStatusError error = new ApiResponseStatusError(ApiErrorCode.INVALID_SCOPE_CODE,
                "error_message", HttpURLConnection.HTTP_FORBIDDEN,
                getErrorResponseBody(ApiErrorCode.INVALID_SCOPE_CODE));
        service.shouldRetryWithApiError(error);
    }

    /**
     * Tests handling of {@link ApiErrorCode#INVALID_TOKEN_CODE} when token can be refreshed.
     * This is the usual case
     */
    @Test
    public void handleInvalidToken() {
        ApiResponseStatusError error = new ApiResponseStatusError(ApiErrorCode.INVALID_TOKEN_CODE, "error_message", HttpURLConnection.HTTP_UNAUTHORIZED);
        boolean retry = service.shouldRetryWithApiError(error);
        assertTrue(retry);
        verify(session).refreshAccessToken(null);
    }

    /**
     * Tests handling of {@link ApiErrorCode#INVALID_TOKEN_CODE} when there is no refresh token.
     * This is
     */
    @Test
    public void handleInvalidTokenWithNoRefreshToken() {
        doReturn(false).when(tokenInfo).hasValidRefreshToken();
        ApiResponseStatusError error = new ApiResponseStatusError(ApiErrorCode.INVALID_TOKEN_CODE, "error_message", HttpURLConnection.HTTP_UNAUTHORIZED);
        boolean retry = service.shouldRetryWithApiError(error);
        assertFalse(retry);
    }

    /**
     * Tests handling of {@link ApiErrorCode#INVALID_TOKEN_CODE}
     */
    @Test
    public void handleInvalidTokenWithExpiredRefreshToken() {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return CompletableFuture.completedFuture(null);
            }
        }).when(session).refreshAccessToken(null);
        ApiResponseStatusError error = new ApiResponseStatusError(ApiErrorCode.INVALID_TOKEN_CODE, "error_message", HttpURLConnection.HTTP_UNAUTHORIZED);
        boolean retry = service.shouldRetryWithApiError(error);
        assertFalse(retry);
        verify(session).refreshAccessToken(null);

    }

    @Test
    public void handleInvalidTokenWithNPE() {
        doThrow(new NullPointerException())
                .when(session).refreshAccessToken(null);
        ApiResponseStatusError error = new ApiResponseStatusError(ApiErrorCode.INVALID_TOKEN_CODE, "error_message", HttpURLConnection.HTTP_UNAUTHORIZED);
        boolean retry = service.shouldRetryWithApiError(error);
        assertFalse(retry);
        verify(session).refreshAccessToken(null);

    }

    @Test
    public void handleAgeAuthErrorWithSuccess() {
        ApiResponseStatusError error = new ApiResponseStatusError(ApiErrorCode.NEED_TO_AGE_AUTHENTICATION, "error_message", HttpURLConnection.HTTP_UNAUTHORIZED);
        assertTrue(service.shouldRetryWithApiError(error));
    }

    @Test
    public void handleUnderAgeLimitError() {
        ApiResponseStatusError error = new ApiResponseStatusError(ApiErrorCode.UNDER_AGE_LIMIT, "error_message", HttpURLConnection.HTTP_UNAUTHORIZED);
        assertFalse(service.shouldRetryWithApiError(error));
    }

    @Test
    public void handleAgeAuthErrorWithUnderAgeLimit() {
        doReturn(AuthService.AgeAuthStatus.LOWER_AGE_LIMIT.getValue()).when(ageAuthService)
                .requestAgeAuth(any(Bundle.class),
                        eq(activityProvider.getCurrentActivity()));
        ApiResponseStatusError error = new ApiResponseStatusError(ApiErrorCode.UNDER_AGE_LIMIT,
                "error_message", HttpURLConnection.HTTP_UNAUTHORIZED);
        assertFalse(service.shouldRetryWithApiError(error));
    }

    @Test
    public void handleBadReuest() {
        ApiResponseStatusError error = new ApiResponseStatusError(ApiErrorCode.NOT_REGISTERED_USER_CODE,
                "error_message", HttpURLConnection.HTTP_BAD_REQUEST);
        assertFalse(service.shouldRetryWithApiError(error));
    }

    ResponseBody getErrorResponseBody(int errorCode) {
        ResponseBody body = null;
        JSONObject jsonObject = new JSONObject();
        JSONArray scopes = new JSONArray();
        try {
            jsonObject.put(StringSet.code, errorCode);
            jsonObject.put(StringSet.msg, "some random error message");
            scopes.put("story_publish");
            scopes.put("talk_chats");
            jsonObject.put(StringSet.required_scopes, scopes);
            body = new ResponseBody(jsonObject.toString());
        } catch (JSONException e) {
            fail(e.toString());
        }
        return body;
    }
}
