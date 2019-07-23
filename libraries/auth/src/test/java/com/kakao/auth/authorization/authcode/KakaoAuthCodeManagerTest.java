package com.kakao.auth.authorization.authcode;

import android.app.Activity;
import android.content.Intent;

import com.kakao.auth.ApprovalType;
import com.kakao.auth.AuthCodeCallback;
import com.kakao.auth.AuthType;
import com.kakao.auth.ISessionConfig;
import com.kakao.auth.authorization.AuthorizationResult;
import com.kakao.auth.helper.StartActivityWrapper;
import com.kakao.network.ErrorResult;
import com.kakao.test.common.KakaoTestCase;
import com.kakao.test.common.TestAppConfiguration;
import com.kakao.test.common.TestPhaseInfo;
import com.kakao.common.IConfiguration;
import com.kakao.common.KakaoContextService;
import com.kakao.common.PhaseInfo;
import com.kakao.util.exception.KakaoException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.robolectric.Robolectric;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
/**
 * @author kevin.kang. Created on 2017. 5. 19..
 */

public class KakaoAuthCodeManagerTest extends KakaoTestCase {

    // below fields are values used by KakaoAuthCodeManager
    private IConfiguration appConfig = new TestAppConfiguration();
    private PhaseInfo phaseInfo = new TestPhaseInfo();
    private String redirectUri = "kakao" + phaseInfo.appKey() + "://oauth";
    private String wrongRedirectUri = "kakao" + phaseInfo.appKey() + "2" + "://oauth";
    private String expectedAuthCode = "12345";
    private String authCodePostfix = "?code=" + expectedAuthCode;

    private Activity activity;


    private KakaoAuthCodeManager authCodeManager;

    private AuthCodeService talkAuthCodeService;
    private AuthCodeService storyAuthCodeService;
    private AuthCodeService webAuthCodeService;

    private AuthCodeCallback authCodeCallback;

    // below fields are for testing whether callbacks are correctly called
    private List<String> events;
    private Exception exception;
    private final String SUCCESS = "success";
    private final String FAILURE = "failure";

    @Before
    public void setup() {
        super.setup();
        activity = Mockito.spy(Robolectric.setupActivity(Activity.class));
        activity = Robolectric.setupActivity(Activity.class);

        events = new ArrayList<>();

        ISessionConfig sessionConfig = new ISessionConfig() {
            @Override
            public AuthType[] getAuthTypes() {
                return new AuthType[]{AuthType.KAKAO_LOGIN_ALL};
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

        authCodeCallback = getAuthCodeCallback();

        talkAuthCodeService = Mockito.spy(new TestTalkAuthCodeService());
        storyAuthCodeService = Mockito.spy(new TestStoryAuthCodeService());
        webAuthCodeService = Mockito.spy(new TestWebAuthCodeService());

        KakaoContextService contextService = new KakaoContextService(appConfig, phaseInfo);
        contextService.setPhaseInfo(new TestPhaseInfo());
        authCodeManager = Mockito.spy(new KakaoAuthCodeManager(contextService, sessionConfig, talkAuthCodeService, storyAuthCodeService, webAuthCodeService));
    }

    @After
    public void cleanup() {
        events.clear();
        exception = null;
    }

    @Test
    public void testOnReceivedResult() {

    }

    @Test
    public void testLoginAllWithTalk() {
        mockTalkLogin();
        assertTrue(events.isEmpty());
        authCodeManager.requestAuthCode(AuthType.KAKAO_LOGIN_ALL, activity, authCodeCallback);
        assertTrue(events.contains(SUCCESS));
    }

    @Test
    public void testLoginAllWithKakakoTalkCancel() {
        mockLoginCancel(talkAuthCodeService);
        assertTrue(events.isEmpty());
        authCodeManager.requestAuthCode(AuthType.KAKAO_LOGIN_ALL, activity, authCodeCallback);
        assertTrue(events.contains(FAILURE));
    }

    @Test
    public void testLoginAllWithStory() {
        mockStoryLogin();
        assertTrue(events.isEmpty());
        authCodeManager.requestAuthCode(AuthType.KAKAO_LOGIN_ALL, activity, authCodeCallback);
        assertTrue(events.contains(SUCCESS));
    }

    @Test
    public void testLoginAllWithAccount() {
        mockCorrectAccountLogin();
        assertTrue(events.isEmpty());
        authCodeManager.requestAuthCode(AuthType.KAKAO_LOGIN_ALL, activity, authCodeCallback);
        assertTrue(events.contains(SUCCESS));
    }


    @Test
    public void testKakaoTalk() {
        mockTalkLogin();
        assertTrue(events.isEmpty());
        authCodeManager.requestAuthCode(AuthType.KAKAO_TALK, activity, authCodeCallback);
        assertTrue(events.contains(SUCCESS));
    }

    @Test
    public void testKakaoStory() {
        mockStoryLogin();
        assertTrue(events.isEmpty());
        authCodeManager.requestAuthCode(AuthType.KAKAO_STORY, activity, authCodeCallback);
        assertTrue(events.contains(SUCCESS));
    }

    @Test
    public void testKakaoAccount() {
        mockCorrectAccountLogin();
        assertTrue(events.isEmpty());
        authCodeManager.requestAuthCode(AuthType.KAKAO_ACCOUNT, activity, authCodeCallback);
        assertTrue(events.contains(SUCCESS));
    }

    @Test
    public void testKakaoTalkMultipleTimes() {
        mockTalkLogin();
        assertTrue(events.isEmpty());
        authCodeManager.requestAuthCode(AuthType.KAKAO_TALK, activity, authCodeCallback);
        assertTrue(events.contains(SUCCESS));

        events.clear();

        assertTrue(events.isEmpty());
        authCodeManager.requestAuthCode(AuthType.KAKAO_TALK, activity, authCodeCallback);
        assertTrue(events.contains(SUCCESS));
    }

    @Test
    public void testKakaoStoryMultipleTimes() {
        mockStoryLogin();
        assertTrue(events.isEmpty());
        authCodeManager.requestAuthCode(AuthType.KAKAO_STORY, activity, authCodeCallback);
        assertTrue(events.contains(SUCCESS));

        events.clear();

        assertTrue(events.isEmpty());
        authCodeManager.requestAuthCode(AuthType.KAKAO_STORY, activity, authCodeCallback);
        assertTrue(events.contains(SUCCESS));
    }

    @Test
    public void testKakaoAccountMultipleTimes() {
        mockCorrectAccountLogin();
        assertTrue(events.isEmpty());
        authCodeManager.requestAuthCode(AuthType.KAKAO_ACCOUNT, activity, authCodeCallback);
        assertTrue(events.contains(SUCCESS));

        events.clear();

        assertTrue(events.isEmpty());
        authCodeManager.requestAuthCode(AuthType.KAKAO_ACCOUNT, activity, authCodeCallback);
        assertTrue(events.contains(SUCCESS));
    }

    @Test
    public void testKakaoAccountWithWrongRedirectUri() {
        mockAccountLoginWithWrongRedirectUri();
        assertTrue(events.isEmpty());
        authCodeManager.requestAuthCode(AuthType.KAKAO_ACCOUNT, activity, authCodeCallback);
        assertTrue(events.contains(FAILURE));
        assertNotNull(exception);
        assertTrue(exception instanceof KakaoException);
        KakaoException kakaoException = (KakaoException) exception;
        assertEquals(KakaoException.ErrorType.AUTHORIZATION_FAILED, kakaoException.getErrorType());
    }

    @Test
    public void testKakaoAccountWithEmptyAuthCode() {
        mockAccountLoginWithEmptyAuthCode();
        assertTrue(events.isEmpty());
        authCodeManager.requestAuthCode(AuthType.KAKAO_ACCOUNT, activity, authCodeCallback);
        assertTrue(events.contains(FAILURE));
    }

    @Test
    public void testKakaoAccountWithEmptyRedirectUriAndAuthCode() {
        mockEmptyRedirectUriAndAuthCode();
        assertTrue(events.isEmpty());
        authCodeManager.requestAuthCode(AuthType.KAKAO_ACCOUNT, activity, authCodeCallback);
        assertTrue(events.contains(FAILURE));
    }

    /**
     * Test NPE does not occur even if callback is not provided.
     */
    @Test
    public void testKakaoTalkWithoutCallback() {
        assertTrue(events.isEmpty());
        authCodeManager.requestAuthCode(AuthType.KAKAO_TALK, activity, null);
        assertTrue(events.isEmpty());
    }

    AuthCodeCallback getAuthCodeCallback() {
        AuthCodeCallback authCodeCallback = new AuthCodeCallback() {
            @Override
            public void onAuthCodeReceived(String authCode) {
                assertEquals(expectedAuthCode, authCode);
                events.add(SUCCESS);
            }

            @Override
            public void onAuthCodeFailure(ErrorResult errorResult) {
                exception = errorResult.getException();
                events.add(FAILURE);

            }
        };
        return authCodeCallback;
    }


    private void mockTalkLogin() {
        mockLogin(talkAuthCodeService);
//        talkAuthCodeService.requestAuthCode(new AuthCodeRequest(appKey, ), null, null);
//        Mockito.doAnswer(new Answer() {
//            @Override
//            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
//                AuthCodeListener listener = invocationOnMock.getArgument(3);
//                listener.onAuthCodeReceived(AuthorizationResult.createSuccessAuthCodeResult(redirectUri + "?code=12345"));
//                return true;
//            }
//        }).when(talkAuthCodeService).handleActivityResult(ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt(), ArgumentMatchers.any(Intent.class), ArgumentMatchers.any(AuthCodeListener.class));
//        final Integer requestCode = authCodeManager.getCurrentRequestCode();
//        final Intent intent = new Intent();
//
//        // This mocks handleActivityResult() from third-party app.
//        Mockito.doAnswer(new Answer() {
//            @Override
//            public Object answer(InvocationOnMock invocation) throws Throwable {
//                authCodeManager.handleActivityResult(requestCode, Activity.RESULT_OK, intent);
//                return null;
//            }
//        }).when(authCodeManager).startActivityForResult(Matchers.any(Intent.class), Matchers.any(Integer.class));
//
//        // This mocks intent from KakaoTalk. Intents are to be tested inside parseAuthCodeIntent
//        Mockito.doReturn(AuthorizationResult.createSuccessAuthCodeResult(redirectUri + "?code=12345")).when(authCodeManager).parseAuthCodeIntent(requestCode, Activity.RESULT_OK, intent);
//
//        authCodeCallback = getAuthCodeCallback();
//
//        // Below three lines mock getIntent() methods of AuthCodeRequest to return kakaotalk intent
//        AuthCodeRequest authCodeRequest = Mockito.spy(new AuthCodeRequest(context, appKey, redirectUri, requestCode, authCodeCallback));
//        Mockito.doReturn(new Intent()).when(authCodeRequest).getIntent(command);
//        Mockito.doReturn(authCodeRequest).when(authCodeManager).createAuthCodeRequest(context, appKey, authCodeCallback);
    }

    private void mockStoryLogin() {
        mockLoginError(talkAuthCodeService);
        mockLogin(storyAuthCodeService);
    }

    private void mockLogin(AuthCodeService authCodeService) {
        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                AuthCodeRequest request = invocationOnMock.getArgument(0);
                authCodeManager.handleActivityResult(request.getRequestCode(), Activity.RESULT_OK, new Intent());
                return true;
            }
        }).when(authCodeService).requestAuthCode(ArgumentMatchers.any(AuthCodeRequest.class), ArgumentMatchers.any(StartActivityWrapper.class), ArgumentMatchers.any(AuthCodeListener.class));
    }

    private void mockLoginError(AuthCodeService authCodeService) {
        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                return false;
            }
        }).when(authCodeService).requestAuthCode(ArgumentMatchers.any(AuthCodeRequest.class), ArgumentMatchers.any(StartActivityWrapper.class), ArgumentMatchers.any(AuthCodeListener.class));
    }

    private void mockLoginCancel(AuthCodeService authCodeService) {
        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                AuthCodeRequest request = invocationOnMock.getArgument(0);
                AuthCodeListener listener = invocationOnMock.getArgument(2);
                listener.onAuthCodeReceived(request.getRequestCode(), AuthorizationResult.createAuthCodeCancelResult("result_message"));
                return false;
            }
        }).when(authCodeService).requestAuthCode(ArgumentMatchers.any(AuthCodeRequest.class), ArgumentMatchers.any(StartActivityWrapper.class), ArgumentMatchers.any(AuthCodeListener.class));
    }
    private void mockNativeLoginCancel() {
//        final Integer requestCode = authCodeManager.getCurrentRequestCode();
//        final Intent intent = new Intent();
//
//        // This mocks handleActivityResult() from third-party app.
//        Mockito.doAnswer(new Answer() {
//            @Override
//            public Object answer(InvocationOnMock invocation) throws Throwable {
//                authCodeManager.handleActivityResult(requestCode, Activity.RESULT_OK, intent);
//                return null;
//            }
//        }).when(authCodeManager).startActivityForResult(Matchers.any(Intent.class), Matchers.any(Integer.class));
//
//        // This mocks intent from KakaoTalk. Intents are to be tested inside parseAuthCodeIntent
//        Mockito.doReturn(AuthorizationResult.createAuthCodeCancelResult("Pressed back button during requesting auth code.")).when(authCodeManager).parseAuthCodeIntent(requestCode, Activity.RESULT_OK, intent);
//
//        authCodeCallback = getAuthCodeCallback();
//
//        // Below three lines mock getIntent() methods of AuthCodeRequest to return kakaotalk intent
//        AuthCodeRequest authCodeRequest = Mockito.spy(new AuthCodeRequest(context, appKey, redirectUri, requestCode, authCodeCallback));
//        Mockito.doReturn(new Intent()).when(authCodeRequest).getIntent(command);
//        Mockito.doReturn(authCodeRequest).when(authCodeManager).createAuthCodeRequest(context, appKey, authCodeCallback);
    }

    private void mockCorrectAccountLogin() {
        mockAccountLogin(AuthorizationResult.createSuccessAuthCodeResult(redirectUri + authCodePostfix));
    }
    private void mockAccountLogin(final AuthorizationResult result) {
        mockLoginError(talkAuthCodeService);
        mockLoginError(storyAuthCodeService);

        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                AuthCodeRequest request = invocationOnMock.getArgument(0);
                AuthCodeListener listener = invocationOnMock.getArgument(2);
                listener.onAuthCodeReceived(request.getRequestCode(), result);
                return true;
            }
        }).when(webAuthCodeService).requestAuthCode(ArgumentMatchers.any(AuthCodeRequest.class), ArgumentMatchers.any(StartActivityWrapper.class), ArgumentMatchers.any(AuthCodeListener.class));
    }

    private void mockAccountLoginWithWrongRedirectUri() {
        mockAccountLogin(AuthorizationResult.createSuccessAuthCodeResult(wrongRedirectUri + authCodePostfix));
//        final Integer requestCode = authCodeManager.getCurrentRequestCode();
//        authCodeCallback = getAuthCodeCallback();
//        Mockito.doAnswer(new Answer() {
//            @Override
//            public Object answer(InvocationOnMock invocation) throws Throwable {
//                authCodeManager.onWebViewCompleted(authCodeManager.getAuthCodeRequest(requestCode), wrongRedirectUri + authCodePostfix, null);
//                return null;
//            }
//        }).when(authCodeManager).startActivity(Matchers.any(Intent.class));
    }

    private void mockAccountLoginWithEmptyAuthCode() {
        mockAccountLogin(AuthorizationResult.createSuccessAuthCodeResult(redirectUri));
//        final Integer requestCode = authCodeManager.getCurrentRequestCode();
//        authCodeCallback = getAuthCodeCallback();
//        Mockito.doAnswer(new Answer() {
//            @Override
//            public Object answer(InvocationOnMock invocation) throws Throwable {
//                authCodeManager.onWebViewCompleted(authCodeManager.getAuthCodeRequest(requestCode), redirectUri, null);
//                return null;
//            }
//        }).when(authCodeManager).startActivity(Matchers.any(Intent.class));
    }

    private void mockEmptyRedirectUriAndAuthCode() {
        mockAccountLogin(AuthorizationResult.createSuccessAuthCodeResult(null));
//        final Integer requestCode = authCodeManager.getCurrentRequestCode();
//        authCodeCallback = getAuthCodeCallback();
//        Mockito.doAnswer(new Answer() {
//            @Override
//            public Object answer(InvocationOnMock invocation) throws Throwable {
//                authCodeManager.onWebViewCompleted(authCodeManager.getAuthCodeRequest(requestCode), null, null);
//                return null;
//            }
//        }).when(authCodeManager).startActivity(Matchers.any(Intent.class));
    }
}
