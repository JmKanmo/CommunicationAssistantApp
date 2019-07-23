package com.kakao.auth.authorization.authcode;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.kakao.auth.ApprovalType;
import com.kakao.auth.AuthType;
import com.kakao.auth.ISessionConfig;
import com.kakao.auth.authorization.AuthorizationResult;
import com.kakao.test.common.KakaoTestCase;
import com.kakao.test.common.TestKakaoUtilService;
import com.kakao.test.common.TestPhaseInfo;
import com.kakao.common.KakaoContextService;
import com.kakao.common.PhaseInfo;
import com.kakao.common.RequestConfiguration;
import com.kakao.util.KakaoUtilService;

import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.robolectric.RuntimeEnvironment;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This test class tests if KakaoAuthCodeManager correctly parses intent outputs from
 * KakaoTalk app or KaKaoWebViewActivity.
 *
 * @author kevin.kang. Created on 2017. 5. 23..
 */

public class AuthCodeParseIntentTest extends KakaoTestCase {
    private String appKey = "sample_app_key";
    private String redirectUri = "kakao" + appKey + "://oauth";
    private String expectedAuthCode = "12345";
    private String authCodePostfix = "?code=" + expectedAuthCode;

    private String errorDescription = "error_description";

    private RequestConfiguration appConfig = new RequestConfiguration(
            "key_hash", "ka_header", "app_ver",
            "package_name", new JSONObject());
    private PhaseInfo phaseInfo = new TestPhaseInfo();
    private TalkAuthCodeService talkAuthCodeService;
    private KakaoUtilService utilService;

    @Before
    public void setup() {
        super.setup();

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
        utilService = new TestKakaoUtilService();
        Context context = spy(RuntimeEnvironment.application.getApplicationContext());
        KakaoContextService contextService = new KakaoContextService(appConfig, phaseInfo);
        talkAuthCodeService = new TalkAuthCodeService(context, contextService, sessionConfig, utilService);
    }

    @After
    public void cleanup() {
    }

    @Test
    public void testParseCancelIntent() {
        Intent intent = new Intent();
        AuthorizationResult result = talkAuthCodeService.parseAuthCodeIntent(1, Activity.RESULT_CANCELED, intent);
        assertTrue(result.isCanceled());
    }

    @Test
    public void testParseSuccessIntent() {
        Intent intent = new Intent();
        intent.putExtra(TalkAuthCodeService.EXTRA_REDIRECT_URL, redirectUri + authCodePostfix);

        AuthorizationResult result = talkAuthCodeService.parseAuthCodeIntent(1, Activity.RESULT_OK, intent);

        assertTrue(result.isSuccess());
        assertEquals(redirectUri + authCodePostfix, result.getRedirectURL());
        assertEquals(redirectUri + authCodePostfix, result.getRedirectUri().toString());
        assertNull(result.getAccessToken());
    }

    @Test
    public void testNotSupprtErrorIntent() {
        Intent intent = createErrorIntent(TalkAuthCodeService.NOT_SUPPORT_ERROR, null);
        AuthorizationResult result = talkAuthCodeService.parseAuthCodeIntent(1, Activity.RESULT_OK, intent);

        assertTrue(result.isPass());
    }

    @Test
    public void testUnknownErrorIntent() {
        Intent intent = createErrorIntent(TalkAuthCodeService.UNKNOWN_ERROR, errorDescription);

        AuthorizationResult result = talkAuthCodeService.parseAuthCodeIntent(1, Activity.RESULT_OK, intent);

        assertTrue(result.isAuthError());
        assertTrue(result.getResultMessage().contains(errorDescription));
    }

    @Test
    public void testProtocolErrorIntent() {
        Intent intent = createErrorIntent(TalkAuthCodeService.PROTOCOL_ERROR, errorDescription);
        AuthorizationResult result = talkAuthCodeService.parseAuthCodeIntent(1, Activity.RESULT_OK, intent);

        assertTrue(result.isAuthError());
        assertTrue(result.getResultMessage().contains(errorDescription));
    }

    @Test
    public void testApplicationErrorIntent() {
        Intent intent = createErrorIntent(TalkAuthCodeService.APPLICATION_ERROR, errorDescription);
        AuthorizationResult result = talkAuthCodeService.parseAuthCodeIntent(1, Activity.RESULT_OK, intent);

        assertTrue(result.isAuthError());
        assertTrue(result.getResultMessage().contains(errorDescription));
    }

    @Test
    public void testAuthCodeErrorIntent() {
        Intent intent = createErrorIntent(TalkAuthCodeService.AUTH_CODE_ERROR, errorDescription);
        AuthorizationResult result = talkAuthCodeService.parseAuthCodeIntent(1, Activity.RESULT_OK, intent);

        assertTrue(result.isAuthError());
        assertTrue(result.getResultMessage().contains(errorDescription));
    }

    @Test
    public void testClientInfoError() {
        Intent intent = createErrorIntent(TalkAuthCodeService.CLIENT_INFO_ERROR, errorDescription);
        AuthorizationResult result = talkAuthCodeService.parseAuthCodeIntent(1, Activity.RESULT_OK, intent);

        assertTrue(result.isAuthError());
        assertTrue(result.getResultMessage().contains(errorDescription));
    }


    private Intent createErrorIntent(final String errorType, final String errorDescription) {
        Intent intent = new Intent();
        intent.putExtra(TalkAuthCodeService.EXTRA_ERROR_TYPE, errorType);
        intent.putExtra(TalkAuthCodeService.EXTRA_ERROR_DESCRIPTION, errorDescription);
        return intent;
    }
}
