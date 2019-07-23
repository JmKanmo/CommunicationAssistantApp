package com.kakao.auth.authorization.authcode;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.kakao.auth.ApprovalType;
import com.kakao.auth.AuthCodeCallback;
import com.kakao.auth.AuthType;
import com.kakao.auth.ISessionConfig;
import com.kakao.auth.StringSet;
import com.kakao.auth.authorization.AuthorizationResult;
import com.kakao.auth.helper.StartActivityWrapper;
import com.kakao.network.ErrorResult;
import com.kakao.test.common.KakaoTestCase;
import com.kakao.test.common.TestAppConfiguration;
import com.kakao.test.common.TestKakaoUtilService;
import com.kakao.test.common.TestPhaseInfo;
import com.kakao.common.IConfiguration;
import com.kakao.common.KakaoContextService;
import com.kakao.util.KakaoUtilService;
import com.kakao.common.PhaseInfo;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author kevin.kang. Created on 2017. 6. 1..
 */

public class TalkAuthCodeServiceTest extends KakaoTestCase {
    private IConfiguration appConfig = new TestAppConfiguration();
    private PhaseInfo phaseInfo = new TestPhaseInfo();
    private KakaoContextService contextService = new KakaoContextService(appConfig, phaseInfo);
    private ISessionConfig sessionConfig;
    private Activity activity;
    private TalkAuthCodeService service;
    private KakaoUtilService protocolService;
    private List<String> events;

    private String redirectUri = StringSet.REDIRECT_URL_PREFIX + phaseInfo.appKey() + StringSet.REDIRECT_URL_POSTFIX;
    private String wrongRedirectUri = StringSet.REDIRECT_URL_PREFIX + phaseInfo.appKey() + "2" + StringSet.REDIRECT_URL_POSTFIX;
    private String expectedAuthCode = "12345";
    private String authCodePostfix = "?code=" + expectedAuthCode;
    private String correctRedirectUri = redirectUri + authCodePostfix;

    @Before
    public void setup() {
        super.setup();

        events = new ArrayList<>();

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

        activity = Robolectric.buildActivity(Activity.class).get();
        protocolService = Mockito.spy(new TestKakaoUtilService());
        service = Mockito.spy(new TalkAuthCodeService(RuntimeEnvironment.application, contextService, sessionConfig, protocolService));
    }

    @Test
    public void createLoginIntent() {
        Bundle bundle = new Bundle();
        bundle.putString(StringSet.approval_type, ApprovalType.INDIVIDUAL.toString());
        Intent intent = service.createLoggedInActivityIntent(bundle);

        assertEquals(TalkAuthCodeService.INTENT_ACTION_LOGGED_IN_ACTIVITY, intent.getAction());
        assertTrue(intent.getCategories().contains(Intent.CATEGORY_DEFAULT));
        assertEquals(phaseInfo.appKey(), intent.getExtras().get(TalkAuthCodeService.EXTRA_APPLICATION_KEY));
        assertEquals(service.redirectUriString(), intent.getExtras().get(TalkAuthCodeService.EXTRA_REDIRECT_URI));
        assertEquals(appConfig.getKAHeader(), intent.getExtras().get(TalkAuthCodeService.EXTRA_KA_HEADER));
        assertEquals(TalkAuthCodeService.PROTOCOL_VERSION, intent.getExtras().get(TalkAuthCodeService.EXTRA_PROTOCOL_VERSION));

        assertTrue(intent.getExtras().get(TalkAuthCodeService.EXTRA_EXTRAPARAMS) instanceof Bundle);

        Bundle extras = (Bundle) intent.getExtras().get(TalkAuthCodeService.EXTRA_EXTRAPARAMS);
        assertNotNull(extras);
        assertEquals(ApprovalType.INDIVIDUAL.toString(), extras.get(StringSet.approval_type));

    }

    @Test
    public void isLoginAvailable() {
        assertTrue(service.isLoginAvailable());
        Mockito.doReturn(null).when(protocolService).resolveIntent(ArgumentMatchers.any(Context.class), ArgumentMatchers.any(Intent.class), ArgumentMatchers.anyInt());
        assertFalse(service.isLoginAvailable());
    }

    @Test
    public void requestAuthCode() {
        final AuthCodeRequest request = TestAuthCodeRequestFactory.createAuthCodeRequest(2, phaseInfo.appKey(), sessionConfig, getAuthCodeCallback());
        StartActivityWrapper wrapper = new StartActivityWrapper(activity);

        final AuthCodeListener listener = new AuthCodeListener() {
            @Override
            public void onAuthCodeReceived(int requestCode, AuthorizationResult result) {
                events.add("success");
            }
        };
        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                listener.onAuthCodeReceived(2, AuthorizationResult.createSuccessAuthCodeResult(correctRedirectUri));
                return true;
            }
        }).when(service).startActivityForResult(ArgumentMatchers.any(StartActivityWrapper.class), ArgumentMatchers.any(Intent.class), ArgumentMatchers.anyInt());
        service.requestAuthCode(request, wrapper, listener);

        assertTrue(events.contains("success"));
    }

    @Test
    public void requestAuthCodeWithPass() {
        final AuthCodeRequest request = TestAuthCodeRequestFactory.createAuthCodeRequest(2, phaseInfo.appKey(), sessionConfig, getAuthCodeCallback());
        StartActivityWrapper wrapper = new StartActivityWrapper(activity);

        final AuthCodeListener listener = new AuthCodeListener() {
            @Override
            public void onAuthCodeReceived(int requestCode, AuthorizationResult result) {
                events.add("success");
            }
        };
        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                return false;
            }
        }).when(service).startActivityForResult(ArgumentMatchers.any(StartActivityWrapper.class), ArgumentMatchers.any(Intent.class), ArgumentMatchers.anyInt());
        service.requestAuthCode(request, wrapper, listener);
        assertFalse(events.contains("success"));
    }

    @Test
    public void requestAuthCodeWithoutTalk() {
        final AuthCodeRequest request = TestAuthCodeRequestFactory.createAuthCodeRequest(2, phaseInfo.appKey(), sessionConfig, getAuthCodeCallback());
        StartActivityWrapper wrapper = new StartActivityWrapper(activity);

        final AuthCodeListener listener = new AuthCodeListener() {
            @Override
            public void onAuthCodeReceived(int requestCode, AuthorizationResult result) {
                events.add("success");
            }
        };
        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                return false;
            }
        }).when(service).startActivityForResult(ArgumentMatchers.any(StartActivityWrapper.class), ArgumentMatchers.any(Intent.class), ArgumentMatchers.anyInt());
        Mockito.doReturn(null).when(service).createLoggedInActivityIntent(ArgumentMatchers.any(Bundle.class));
        assertFalse(service.requestAuthCode(request, wrapper, listener));
    }

    private AuthCodeCallback getAuthCodeCallback() {
        return new AuthCodeCallback() {
            @Override
            public void onAuthCodeReceived(String authCode) {
            }

            @Override
            public void onAuthCodeFailure(ErrorResult errorResult) {
            }
        };
    }
}
