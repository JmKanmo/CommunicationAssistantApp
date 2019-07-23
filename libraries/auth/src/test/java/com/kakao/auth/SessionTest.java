package com.kakao.auth;

import android.app.Activity;
import android.content.Context;

import com.kakao.auth.authorization.accesstoken.AccessToken;
import com.kakao.auth.authorization.accesstoken.AccessTokenManager;
import com.kakao.auth.authorization.accesstoken.TestAccessToken;
import com.kakao.auth.authorization.authcode.AuthCodeManager;
import com.kakao.auth.authorization.accesstoken.TestAccessTokenManager;
import com.kakao.auth.helper.StartActivityWrapper;
import com.kakao.auth.mocks.TestAuthCodeManager;
import com.kakao.auth.network.response.AuthResponseError;
import com.kakao.network.ErrorResult;
import com.kakao.network.response.ResponseBody;
import com.kakao.test.common.KakaoTestCase;
import com.kakao.test.common.TestAppConfiguration;
import com.kakao.test.common.TestPhaseInfo;
import com.kakao.common.IConfiguration;
import com.kakao.common.KakaoContextService;
import com.kakao.common.PhaseInfo;
import com.kakao.util.exception.KakaoException;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.mockito.stubbing.Stubber;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;


import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
/**
 * @author kevin.kang. Created on 2017. 4. 26..
 */

public class SessionTest extends KakaoTestCase {
    private Activity activity;

    private AuthCodeManager authCodeManager;
    private AccessTokenManager accessTokenManager;
    private Session currentSession;
    private IConfiguration configuration = new TestAppConfiguration();
    private PhaseInfo phaseInfo = new TestPhaseInfo();
    private KakaoContextService contextService = new KakaoContextService(configuration, phaseInfo);
    private ISessionCallback callback;

    private List<String> events = new ArrayList<>();
    private KakaoException exception;

    @Before
    public void setup() {
        super.setup();
        Context context = RuntimeEnvironment.application.getApplicationContext();
        activity = Robolectric.setupActivity(Activity.class);

        ISessionConfig sessionConfig = new ISessionConfig() {
            @Override
            public AuthType[] getAuthTypes() {
                return new AuthType[] { AuthType.KAKAO_TALK };
            }

            @Override
            public boolean isUsingWebviewTimer() {
                return false;
            }

            @Override
            public boolean isSecureMode() {
                return false;
            }

            @Override
            public ApprovalType getApprovalType() {
                return ApprovalType.INDIVIDUAL;
            }

            @Override
            public boolean isSaveFormData() {
                return false;
            }
        };

        authCodeManager = spy(new TestAuthCodeManager());
        accessTokenManager = spy(new TestAccessTokenManager());
        currentSession = new Session(context, contextService, sessionConfig, authCodeManager, accessTokenManager);

        assertTrue(currentSession.isClosed());
        assertNull(currentSession.getTokenInfo().getAccessToken());
        assertNull(currentSession.getTokenInfo().getRefreshToken());

        callback = spy(new ISessionCallback() {
            @Override
            public void onSessionOpened() {
                events.add("success");
            }

            @Override
            public void onSessionOpenFailed(KakaoException e) {
                exception = e;
                events.add("failure");
            }
        });
        currentSession.addCallback(callback);
    }

    @After
    public void cleanup() {
        events.clear();
        exception = null;
        currentSession.clearCallbacks();
    }

    @Test
    public void implicitOpen() {
        assertFalse(currentSession.implicitOpen());
        assertFalse(currentSession.isOpened());
        assertFalse(currentSession.isOpenable());
        assertTrue(currentSession.isClosed());
    }

    @Test
    public void checkAndImplicitOpen() {
        assertFalse(currentSession.checkAndImplicitOpen());
        assertFalse(currentSession.isOpened());
        assertFalse(currentSession.isOpenable());
        assertTrue(currentSession.isClosed());
    }

    @Test
    public void openWithActivity() {
        assertTrue(events.isEmpty());
        currentSession.open(AuthType.KAKAO_LOGIN_ALL, activity);
        assertTrue(events.contains("success"));
        assertTrue(currentSession.isOpened());
    }

    @Test
    public void openWithAuthCode() {
        assertTrue(events.isEmpty());
        String authCode = "auth_code";
        currentSession.openWithAuthCode(authCode);
        assertTrue(events.contains("success"));
        assertTrue(currentSession.isOpened());
    }

    /**
     * Test if refresh token is working correctly. ImplicitOpen() should not actually refresh access
     * token when access token is still valid but refreshAccessToken() should. Success/failure
     * will be delivered to the ISessionCallback registered to the session.
     */
    @Test
    public void refreshToken() {
        assertTrue(events.isEmpty());
        currentSession.refreshAccessToken(null);
        assertTrue(currentSession.isClosed());
        verify(callback).onSessionOpenFailed(any(KakaoException.class));

        currentSession.open(AuthType.KAKAO_LOGIN_ALL, activity);
        assertTrue(currentSession.isOpened());
        verify(callback).onSessionOpened();

        verify(accessTokenManager, times(0)).refreshAccessToken(anyString(), any(AccessTokenCallback.class));
        currentSession.checkAndImplicitOpen();
        verify(accessTokenManager, times(0)).refreshAccessToken(anyString(), any(AccessTokenCallback.class));
        verify(callback, times(2)).onSessionOpened();
        currentSession.refreshAccessToken(null);
        verify(accessTokenManager).refreshAccessToken(anyString(), any(AccessTokenCallback.class));
    }

    @Test
    public void openWithAuthorizationFailed() {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                AuthCodeCallback callback = invocation.getArgument(2);
                callback.onAuthCodeFailure(new ErrorResult(new KakaoException(KakaoException.ErrorType.AUTHORIZATION_FAILED, "Authorization failed mock.")));
                return null;
            }
        }).when(authCodeManager).requestAuthCode(AuthType.KAKAO_LOGIN_ALL, activity, currentSession.getAuthCodeCallback());

        currentSession.open(AuthType.KAKAO_LOGIN_ALL, activity);
        assertTrue(events.contains("failure"));
        assertTrue(currentSession.isClosed());
        assertEquals(KakaoException.ErrorType.AUTHORIZATION_FAILED, exception.getErrorType());
        verify(callback).onSessionOpenFailed(any(KakaoException.class));
    }

    @Test
    public void openWithCanceledOperation() {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                AuthCodeCallback callback = invocation.getArgument(2);
                callback.onAuthCodeFailure(new ErrorResult(new KakaoException(KakaoException.ErrorType.CANCELED_OPERATION, "Canceled operation mock.")));
                return null;
            }
        }).when(authCodeManager).requestAuthCode(AuthType.KAKAO_LOGIN_ALL, activity, currentSession.getAuthCodeCallback());

        currentSession.open(AuthType.KAKAO_LOGIN_ALL, activity);
        assertTrue(events.contains("failure"));
        assertTrue(currentSession.isClosed());
        assertEquals(KakaoException.ErrorType.CANCELED_OPERATION, exception.getErrorType());
        verify(callback).onSessionOpenFailed(any(KakaoException.class));
    }

    @Test
    public void openWithNetworkError() {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                AuthCodeCallback callback = invocationOnMock.getArgument(2);
                callback.onAuthCodeFailure(new ErrorResult(new IllegalArgumentException("Error message")));
                return null;
            }
        }).when(authCodeManager).requestAuthCode(AuthType.KAKAO_LOGIN_ALL, activity, currentSession.getAuthCodeCallback());
        currentSession.open(AuthType.KAKAO_LOGIN_ALL, activity);
        assertTrue(events.contains("failure"));
        assertNotNull(exception);
        verify(callback).onSessionOpenFailed(any(KakaoException.class));
    }

    @Test
    public void openWithAuthCodeWithError() {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                AccessTokenCallback callback = invocationOnMock.getArgument(1);
                callback.onAccessTokenFailure(new ErrorResult(new IllegalArgumentException("error message")));
                return null;
            }
        }).when(accessTokenManager).requestAccessTokenByAuthCode("auth_code", currentSession.getAccessTokenCallback());
        currentSession.openWithAuthCode("auth_code");

        assertTrue(events.contains("failure"));
        verify(callback).onSessionOpenFailed(any(KakaoException.class));
    }

    @Test
    public void addAndRemoveCallback() {
        ISessionCallback callback1 = createCallback();
        ISessionCallback callback2 = createCallback();

        currentSession.clearCallbacks();
        assertTrue(currentSession.getCallbacks().isEmpty());
        currentSession.addCallback(callback1);
        assertEquals(1, currentSession.getCallbacks().size());
        currentSession.addCallback(callback1);
        assertEquals(1, currentSession.getCallbacks().size());
        currentSession.removeCallback(callback2);
        assertEquals(1, currentSession.getCallbacks().size());
        currentSession.addCallback(callback2);
        assertEquals(2, currentSession.getCallbacks().size());
        currentSession.removeCallback(callback1);
        assertEquals(1, currentSession.getCallbacks().size());
        currentSession.removeCallback(callback2);
        assertTrue(currentSession.getCallbacks().isEmpty());
    }

    @Test
    public void testInternalClose() {
        currentSession.internalClose(null);
        currentSession.internalClose(null);
    }

    /**
     * This test checks if general error while refreshing token does the following things:
     *  - Does not close session
     *  - Reset requestType to null
     */
    @Test
    public void refreshAccessTokenImplicitlyWithError() {
        mockAcquiringExpiredToken();
        currentSession.open(AuthType.KAKAO_TALK, activity);

        assertFalse(currentSession.isOpened());
        assertTrue(currentSession.isOpenable());
        assertNull(currentSession.getRequestType());

        mockAccessTokenFailure(Session.RequestType.REFRESHING_ACCESS_TOKEN, KakaoException.ErrorType.UNSPECIFIED_ERROR);
        currentSession.checkAndImplicitOpen();
        assertTrue(currentSession.isOpenable());
        assertNull(currentSession.getRequestType());
        verify(callback).onSessionOpenFailed(any(KakaoException.class));
        assertNull(currentSession.getRequestType());
    }

    /**
     * This test checks if Authorization failed error while refreshing token does the following things:
     *  - session is openable (has refresh token)
     *  - refreshing access token fails
     *
     * This should result in the following state:
     *  - session is closed
     *  - {@link ISessionCallback} is called because session state is changed from openable to closed
     *  - requestType is set to null
     */
    @Test
    public void refreshAccessTokenImplicitlyWithAuthorizationFailedError() {
        mockAcquiringExpiredToken();
        currentSession.open(AuthType.KAKAO_TALK, activity);

        assertFalse(currentSession.isOpened());
        assertTrue(currentSession.isOpenable());
        assertNull(currentSession.getRequestType());
        verify(callback).onSessionOpened();

        mockAccessTokenFailure(Session.RequestType.REFRESHING_ACCESS_TOKEN, KakaoException.ErrorType.AUTHORIZATION_FAILED);
        currentSession.checkAndImplicitOpen();
        assertTrue(currentSession.isClosed());
        assertNull(currentSession.getRequestType());
        verify(callback).onSessionOpenFailed(any(KakaoException.class));
    }

    /**
     * This test checks refreshing access token explicitly when:
     *  - session is open (has valid access token)
     *  - refreshing access token fails with {@link com.kakao.util.exception.KakaoException.ErrorType#AUTHORIZATION_FAILED}
     *
     * If refreshing access token failed with {@link HttpURLConnection#HTTP_BAD_REQUEST} or
     * {@link HttpURLConnection#HTTP_UNAUTHORIZED}, it means successive token refreshing will fail
     * at any cost, and access token will expire eventually without a new one. Therefore, just close
     * session at this moment.
     *
     * This should result in the following state:
     *  - session should be closed
     *  - {@link ISessionCallback} should be called
     */
    @Test
    public void explicitRefreshFailsWhenAccessTokenIsInvalid() {
        // check session is currently closed
        assertTrue(currentSession.isClosed());
        assertFalse(currentSession.isOpened() || currentSession.isOpenable());

        // open session
        currentSession.open(AuthType.KAKAO_TALK, activity);

        // check session is opened
        assertTrue(currentSession.isOpened());
        assertFalse(currentSession.isOpenable() || currentSession.isClosed());
        verify(callback).onSessionOpened();

        // token refresh will fail
        mockAccessTokenFailure(Session.RequestType.REFRESHING_ACCESS_TOKEN, KakaoException.ErrorType.AUTHORIZATION_FAILED);
        currentSession.refreshAccessToken(createAccessTokenCallback());

        verify(callback).onSessionOpenFailed(any(KakaoException.class));
        assertTrue(currentSession.isClosed());
        assertFalse(currentSession.isOpened() || currentSession.isOpenable());
    }

    /**
     * This test checks refreshing access token explicitly when:
     *  - session is openable (access token has expired)
     *  - refreshing access token fails with {@link HttpURLConnection#HTTP_UNAUTHORIZED} or {@link HttpURLConnection#HTTP_BAD_REQUEST}
     *
     * This should result in the following sate:
     *  - session should be closed since there is no way to refresh token from now on
     *  - any registered {@link ISessionCallback} should be called
     */
    @Test
    public void explicitRefreshFailsWithExpiredAccessAndRefreshToken() {
        // check session is currently closed
        assertTrue(currentSession.isClosed());
        assertFalse(currentSession.isOpened() || currentSession.isOpenable());

        // open session
        mockAcquiringExpiredToken();
        currentSession.open(AuthType.KAKAO_TALK, activity);

        // check session is openable (since token is expired)
        assertTrue(currentSession.isOpenable());
        assertFalse(currentSession.isOpened() || currentSession.isClosed());
        verify(callback).onSessionOpened();
        assertNull(currentSession.getRequestType());

        // token refresh will fail
        mockAccessTokenFailure(Session.RequestType.REFRESHING_ACCESS_TOKEN, KakaoException.ErrorType.AUTHORIZATION_FAILED);
        currentSession.refreshAccessToken(createAccessTokenCallback());

        // check end results
        verify(callback).onSessionOpenFailed(any(KakaoException.class));
        assertTrue(currentSession.isClosed());
        assertFalse(currentSession.isOpenable() || currentSession.isOpened());
        assertNull(currentSession.getRequestType());
        assertNull(currentSession.getTokenInfo().getAccessToken());
    }

    /**
     * This test checks updating scopes when:
     *  - access token is valid
     */
    @Test
    public void updateScopes() {
        currentSession.open(AuthType.KAKAO_TALK, activity);
        // check session is opened
        assertTrue(currentSession.isOpened());
        assertNull(currentSession.getRequestType());
        verify(callback).onSessionOpened();

        List<String> scopes = new ArrayList<>();
        scopes.add("story_publish");
        scopes.add("talk_chats");
        AccessTokenCallback tokenCallback = spy(createAccessTokenCallback());
        currentSession.updateScopes(activity, scopes, tokenCallback);


        // check session is open
        assertTrue(currentSession.isOpened());
        verify(tokenCallback).onAccessTokenReceived(any(AccessToken.class));
        verify(callback).onSessionOpened();
        verify(callback, times(0)).onSessionOpenFailed(any(KakaoException.class));
        assertNull(currentSession.getRequestType());
    }

    /**
     * Tests updating scopes when:
     *  - access token is valid
     */
    @Test
    public void updateScopesFailsWithCancelOperation() {
        currentSession.open(AuthType.KAKAO_TALK, activity);
        // check session is opened
        assertTrue(currentSession.isOpened());
        assertNull(currentSession.getRequestType());
        verify(callback).onSessionOpened();
        verify(callback, times(0)).onSessionOpenFailed(any(KakaoException.class));

        List<String> scopes = new ArrayList<>();
        scopes.add("story_publish");
        scopes.add("talk_chats");
        AccessTokenCallback tokenCallback = spy(createAccessTokenCallback());
        mockAuthCodeFailure();
        currentSession.updateScopes(activity, scopes, tokenCallback);

        // check end results
        assertTrue(currentSession.isOpened());
        assertFalse(currentSession.isOpenable() || currentSession.isClosed());
        assertNull(currentSession.getRequestType());

        verify(tokenCallback).onAccessTokenFailure(any(ErrorResult.class));
        verify(callback, times(0)).onSessionOpenFailed(any(KakaoException.class));
    }

    @Test
    public void updateScopesFailsWithTokenError() {
        currentSession.open(AuthType.KAKAO_TALK, activity);
        // check session is opened
        assertTrue(currentSession.isOpened());
        assertNull(currentSession.getRequestType());
        verify(callback).onSessionOpened();
        verify(callback, times(0)).onSessionOpenFailed(any(KakaoException.class));

        List<String> scopes = new ArrayList<>();
        scopes.add("story_publish");
        scopes.add("talk_chats");

        AccessTokenCallback tokenCallback = spy(createAccessTokenCallback());
        mockAccessTokenFailure(Session.RequestType.GETTING_ACCESS_TOKEN, KakaoException.ErrorType.UNSPECIFIED_ERROR);
        currentSession.updateScopes(activity, scopes, tokenCallback);

        // check end results
        assertTrue(currentSession.isOpened()); // session is kept open
        assertFalse(currentSession.isOpenable() || currentSession.isClosed());
        assertNull(currentSession.getRequestType()); // request state should be cleared

        verify(tokenCallback).onAccessTokenFailure(any(ErrorResult.class));
        verify(callback, times(0)).onSessionOpenFailed(any(KakaoException.class));
    }

    private void mockAuthCodeFailure() {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                AuthCodeCallback authCodeCallback = invocation.getArgument(3);
                authCodeCallback.onAuthCodeFailure(new ErrorResult(new KakaoException(KakaoException.ErrorType.CANCELED_OPERATION, "user canceled operation")));
                return null;
            }
        }).when(authCodeManager).requestAuthCodeWithScopes(eq(AuthType.KAKAO_ACCOUNT), any(StartActivityWrapper.class), ArgumentMatchers.<String>anyList(), any(AuthCodeCallback.class));
    }

    private void mockAcquiringExpiredToken() {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                AccessTokenCallback callback = invocationOnMock.getArgument(1);
                AccessToken expired = TestAccessToken.createExpiredAccessToken();
                callback.onAccessTokenReceived(expired);
                return CompletableFuture.completedFuture(expired);
            }
        }).when(accessTokenManager).requestAccessTokenByAuthCode("auth_code", currentSession.getAccessTokenCallback());
    }


    private void mockAccessTokenFailure(final Session.RequestType requestType, final KakaoException.ErrorType errorType) {
        Stubber stubber = doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                AccessTokenCallback callback = invocationOnMock.getArgument(1);
                if (errorType == KakaoException.ErrorType.AUTHORIZATION_FAILED) {
                    JSONObject errorResponse = new JSONObject();
                    try {
                        errorResponse.put(StringSet.error, "error");
                        errorResponse.put(StringSet.error_description, "error_description");
                    } catch (JSONException e) {
                        fail(e.getMessage());
                    }
                    ResponseBody errorBody = new ResponseBody(errorResponse.toString());
                    final AuthResponseError error = new AuthResponseError(HttpURLConnection.HTTP_BAD_REQUEST, errorBody);
                    callback.onAccessTokenFailure(new ErrorResult(error));
                } else {
                    callback.onAccessTokenFailure(new ErrorResult(new KakaoException(errorType, "error message")));
                }

                return null;
            }
        });

        if (requestType == Session.RequestType.GETTING_ACCESS_TOKEN) {
            stubber.when(accessTokenManager).requestAccessTokenByAuthCode(anyString(), any(AccessTokenCallback.class));
        } else if (requestType == Session.RequestType.REFRESHING_ACCESS_TOKEN) {
            stubber.when(accessTokenManager).refreshAccessToken(anyString(), any(AccessTokenCallback.class));
        } else {
            fail("error");
        }
    }

    ISessionCallback createCallback() {
        return new ISessionCallback() {
            @Override
            public void onSessionOpened() {

            }

            @Override
            public void onSessionOpenFailed(KakaoException exception) {

            }
        };
    }

    AccessTokenCallback createAccessTokenCallback() {
        return new AccessTokenCallback() {
            @Override
            public void onAccessTokenReceived(AccessToken accessToken) {

            }

            @Override
            public void onAccessTokenFailure(ErrorResult errorResult) {

            }
        };
    }
}
