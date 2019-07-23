package com.kakao.auth.authorization.authcode;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;

import com.kakao.auth.ApprovalType;
import com.kakao.auth.AuthType;
import com.kakao.auth.ISessionConfig;
import com.kakao.auth.R;
import com.kakao.auth.StringSet;
import com.kakao.auth.authorization.AuthorizationResult;
import com.kakao.auth.helper.StartActivityWrapper;
import com.kakao.test.common.KakaoTestCase;
import com.kakao.test.common.TestPhaseInfo;
import com.kakao.common.PhaseInfo;
import com.kakao.util.exception.KakaoException;

import org.junit.Before;
import org.junit.Test;

import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author kevin.kang. Created on 2017. 6. 1..
 */

public class WebAuthCodeServiceTest extends KakaoTestCase {
    private PhaseInfo phaseInfo = new TestPhaseInfo();
    private WebAuthCodeService service;
    private Handler handler;
    private ISessionConfig sessionConfig;

    private String expectedAuthCode = "12345";
    private String authCodePostfix = "?code=" + expectedAuthCode;
    private String accessDeniedErrorString = "access_denied";
    private String otherErrorString = "other_error";
    private String errorDesc = "This is an error message.";

    private String redirectUri = StringSet.REDIRECT_URL_PREFIX + phaseInfo.appKey() + StringSet.REDIRECT_URL_POSTFIX;
    private String wrongRedirectUri = StringSet.REDIRECT_URL_PREFIX + phaseInfo.appKey() + "2" + StringSet.REDIRECT_URL_POSTFIX;
    private String correctRedirectUri = redirectUri + authCodePostfix;
    private String errorRedirectUri = redirectUri + errorDesc;

    private Integer expectedRequestCode = 1;
    private Integer actualRequestCode;
    private AuthorizationResult authorizationResult;

    @Before
    public void setup() {
        super.setup();

        sessionConfig = new ISessionConfig() {
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

        Context context = spy(RuntimeEnvironment.application.getApplicationContext());
        doReturn("mocked message").when(context).getString(R.string.auth_code_cancel);
        service = spy(new WebAuthCodeService(context, handler, sessionConfig));

        authorizationResult = null;
        actualRequestCode = -1;
    }

    @Test
    public void isLoginAvailable() {
        assertTrue(service.isLoginAvailable());
    }

    @Test
    public void handleActivityResult() {
        assertFalse(service.handleActivityResult(0, 0, new Intent(), null));
    }

    @Test
    public void requestAuthCode() {
        final AuthCodeRequest request = TestAuthCodeRequestFactory.createAuthCodeRequest(2, phaseInfo.appKey(), sessionConfig, null);
        StartActivityWrapper wrapper = new StartActivityWrapper(Robolectric.buildActivity(Activity.class).get());
        service.requestAuthCode(request, wrapper, getAuthCodeListener());

        verify(service).startActivity(eq(wrapper), any(Intent.class));

    }

    @Test
    public void createAccountLoginIntent() {
        final AuthCodeRequest request = TestAuthCodeRequestFactory.createAuthCodeRequest(2, phaseInfo.appKey(), sessionConfig, null);
        StartActivityWrapper wrapper = new StartActivityWrapper(Robolectric.buildActivity(Activity.class).get());
        Intent intent = service.createAuthorizeIntentWithUri(wrapper.getContext(), request, getAuthCodeListener());

        assertFalse(intent.hasExtra(KakaoWebViewActivity.KEY_URL));
        assertTrue(intent.hasExtra(KakaoWebViewActivity.KEY_EXTRA_HEADERS));
        assertTrue(intent.hasExtra(KakaoWebViewActivity.KEY_USE_WEBVIEW_TIMERS));
        assertTrue(intent.hasExtra(KakaoWebViewActivity.KEY_RESULT_RECEIVER));
    }

    @Test
    public void onReceivedResult() {
        Bundle bundle = new Bundle();
        bundle.putString(KakaoWebViewActivity.KEY_REDIRECT_URL, correctRedirectUri);
        service.onReceivedResult(expectedRequestCode, KakaoWebViewActivity.RESULT_SUCCESS, bundle, getAuthCodeListener());

        assertEquals(expectedRequestCode, actualRequestCode);
        assertTrue(authorizationResult.isSuccess());
        assertEquals(correctRedirectUri, authorizationResult.getRedirectURL());
        assertNull(authorizationResult.getException());
    }

    @Test
    public void onReceivedResultWithErrorRedirectUri() {

        String errorUri = redirectUri + "?error=" + otherErrorString + "&error_description=" + errorDesc;
        Bundle bundle = new Bundle();
        bundle.putString(KakaoWebViewActivity.KEY_REDIRECT_URL, errorUri);
        service.onReceivedResult(expectedRequestCode, KakaoWebViewActivity.RESULT_SUCCESS, bundle, getAuthCodeListener());

        assertEquals(expectedRequestCode, actualRequestCode);
        assertFalse(authorizationResult.isSuccess());
        assertTrue(authorizationResult.isAuthError());
        assertEquals(errorDesc, authorizationResult.getResultMessage());
        assertNull(authorizationResult.getException());
    }

    @Test
    public void onReceivedResultWithAccessDeniedError() {
        String errorUri = redirectUri + "?error=" + accessDeniedErrorString + "&error_description=" + errorDesc;
        Bundle bundle = new Bundle();
        bundle.putString(KakaoWebViewActivity.KEY_REDIRECT_URL, errorUri);
        service.onReceivedResult(expectedRequestCode, KakaoWebViewActivity.RESULT_SUCCESS, bundle, getAuthCodeListener());

        assertEquals(expectedRequestCode, actualRequestCode);
        assertFalse(authorizationResult.isSuccess());
        assertTrue(authorizationResult.isCanceled());
        assertNull(authorizationResult.getException());
    }

    @Test
    public void onReceivedResultWithKakaoException() {
        KakaoException exception = new KakaoException(KakaoException.ErrorType.AUTHORIZATION_FAILED);
        Bundle bundle = new Bundle();
        bundle.putSerializable(KakaoWebViewActivity.KEY_EXCEPTION, exception);

        service.onReceivedResult(expectedRequestCode, KakaoWebViewActivity.RESULT_ERROR, bundle, getAuthCodeListener());
        assertTrue(authorizationResult.isAuthError());
        assertFalse(authorizationResult.isSuccess());
        assertNotNull(authorizationResult.getException());
    }

    @Test
    public void onReceivedResultWithKakaoExceptionMessage() {
        String errorMsg = "Authorization failed.";
        KakaoException exception = new KakaoException(KakaoException.ErrorType.AUTHORIZATION_FAILED, errorMsg);
        Bundle bundle = new Bundle();
        bundle.putSerializable(KakaoWebViewActivity.KEY_EXCEPTION, exception);

        service.onReceivedResult(expectedRequestCode, KakaoWebViewActivity.RESULT_ERROR, bundle, getAuthCodeListener());
        assertTrue(authorizationResult.isAuthError());
        assertFalse(authorizationResult.isSuccess());
        assertNotNull(authorizationResult.getException());
        assertEquals(errorMsg, authorizationResult.getResultMessage());
        assertEquals(errorMsg, authorizationResult.getException().getMessage());
    }

    @Test
    public void onReceivedResultWithCancel() {
        KakaoException cancelException = new KakaoException(KakaoException.ErrorType.CANCELED_OPERATION, "cancel");
        Bundle bundle = new Bundle();
        bundle.putSerializable(KakaoWebViewActivity.KEY_EXCEPTION, cancelException);

        service.onReceivedResult(expectedRequestCode, KakaoWebViewActivity.RESULT_ERROR, bundle, getAuthCodeListener());

        assertTrue(authorizationResult.isCanceled());
        assertFalse(authorizationResult.isSuccess());
        assertNull(authorizationResult.getException());
        assertTrue(authorizationResult.getResultMessage().contains("cancel"));
    }

    private AuthCodeListener getAuthCodeListener() {
        return new AuthCodeListener() {
            @Override
            public void onAuthCodeReceived(int requestCode, AuthorizationResult result) {
                actualRequestCode = requestCode;
                authorizationResult = result;
            }
        };
    }
}
