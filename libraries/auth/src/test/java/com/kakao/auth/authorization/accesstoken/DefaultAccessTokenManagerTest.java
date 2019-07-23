package com.kakao.auth.authorization.accesstoken;

import android.net.Uri;

import com.kakao.auth.AccessTokenCallback;
import com.kakao.auth.ApiErrorCode;
import com.kakao.auth.ApprovalType;
import com.kakao.auth.StringSet;
import com.kakao.auth.mocks.TestNetworkService;
import com.kakao.auth.network.AuthorizedRequest;
import com.kakao.auth.network.request.AccessTokenInfoRequest;
import com.kakao.auth.network.response.AuthResponseError;
import com.kakao.network.ErrorResult;
import com.kakao.network.IRequest;
import com.kakao.network.NetworkService;
import com.kakao.network.ServerProtocol;
import com.kakao.network.response.ResponseBody;
import com.kakao.test.common.KakaoTestCase;
import com.kakao.test.common.TestAppConfiguration;
import com.kakao.test.common.TestPhaseInfo;
import com.kakao.common.IConfiguration;
import com.kakao.common.KakaoContextService;
import com.kakao.common.PhaseInfo;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.net.HttpURLConnection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.mockito.ArgumentMatchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author kevin.kang. Created on 2017. 5. 19..
 */

public class DefaultAccessTokenManagerTest extends KakaoTestCase {
    private ApprovalType approvalType = ApprovalType.INDIVIDUAL;
    private IConfiguration configuration;
    private PhaseInfo phaseInfo;
    private NetworkService networkService;
    private DefaultAccessTokenManager manager;

    @Before
    public void setup() {
        super.setup();
        configuration = spy(new TestAppConfiguration());
        phaseInfo = spy(new TestPhaseInfo());
        KakaoContextService contextService = new KakaoContextService(configuration, phaseInfo);
        networkService = spy(new TestNetworkService());
        manager = spy(new DefaultAccessTokenManager(contextService, networkService, approvalType));
    }

    @Test
    public void createAccessTokenRequest() {
        IRequest request = new AccessTokenRequest(phaseInfo, configuration, "auth_code", null, ApprovalType.INDIVIDUAL.toString());
        Uri uri = Uri.parse(request.getUrl());
        Map<String, String> params = request.getParams();

        assertEquals("POST", request.getMethod());
        assertEquals("/" + ServerProtocol.ACCESS_TOKEN_PATH, uri.getPath());
        assertEquals(StringSet.authorization_code, params.get(StringSet.grant_type));
        assertEquals(phaseInfo.appKey(), params.get(StringSet.client_id));
        assertEquals("auth_code", params.get(StringSet.code));
        assertNull(params.get(StringSet.refresh_token));
        assertEquals(phaseInfo.clientSecret(), params.get(StringSet.client_secret));
        assertEquals(String.format("%s%s%s", StringSet.REDIRECT_URL_PREFIX, phaseInfo.appKey(), StringSet.REDIRECT_URL_POSTFIX), params.get(StringSet.redirect_uri));

        doReturn(null).when(phaseInfo).clientSecret();
        AccessTokenRequest refreshRequest = new AccessTokenRequest(phaseInfo, configuration, null, "refresh_token", ApprovalType.INDIVIDUAL.toString());
        uri = Uri.parse(refreshRequest.getUrl());
        params = refreshRequest.getParams();

        assertEquals("POST", refreshRequest.getMethod());
        assertEquals("/" + ServerProtocol.ACCESS_TOKEN_PATH, uri.getPath());
        assertEquals(StringSet.refresh_token, params.get(StringSet.grant_type));
        assertEquals(phaseInfo.appKey(), params.get(StringSet.client_id));
        assertNull(params.get(StringSet.code));
        assertEquals("refresh_token", params.get(StringSet.refresh_token));
        assertNull(params.get(StringSet.client_secret));
    }

    @Test
    public void createAccessTokenInfoRequest() {
        String accessToken = "access_token";
        AuthorizedRequest request = new AccessTokenInfoRequest();
        request.setAccessToken(accessToken);
        Uri uri = Uri.parse(request.getUrl());
        Map<String, String> headers = request.getHeaders();

        assertEquals("GET", request.getMethod());
        assertEquals(String.format("/%s", ServerProtocol.USER_ACCESS_TOKEN_INFO_PATH), uri.getPath());
        assertEquals(String.format("%s %s", ServerProtocol.AUTHORIZATION_BEARER, accessToken), headers.get(ServerProtocol.AUTHORIZATION_HEADER_KEY));
    }

    @Test
    public void requestAccessTokenByAuthCode() {

//        doThrow(new AuthResponseError()).when(networkService)
//        try {
//            AccessTokenImpl accessToken = new AccessTokenImpl("access_token", "refresh_token", new Date(), new Date());
//            doReturn(accessToken).when(authApi).requestAccessToken(context, appKey, authCode, null, clientSecret, approvalType.toString());
//            manager.requestAccessTokenByAuthCode(authCode, null).get();
//            verify(authApi).requestAccessToken(context, appKey, authCode, null, clientSecret, approvalType.toString());
//        } catch (Exception e) {
//
//        }
    }

    @Test
    public void refreshTokenWithExpiredToken() throws InterruptedException, ExecutionException {
        JSONObject errorResponse = new JSONObject();
        try {
            errorResponse.put(StringSet.error, "invalid_grant");
            errorResponse.put(StringSet.error_description, "expired_or_invalid_refresh_token");
        } catch (JSONException e) {
            fail(e.getMessage());
        }

        ResponseBody errorBody = new ResponseBody(errorResponse.toString());
        final AuthResponseError error = new AuthResponseError(HttpURLConnection.HTTP_BAD_REQUEST, errorBody);
        doAnswer(new Answer() {
            @Override
            public Future<AccessToken> answer(InvocationOnMock invocation) {
                AccessTokenCallback callback = invocation.getArgument(3);
                callback.onFailure(new ErrorResult(error));
                return CompletableFuture.completedFuture(null);
            }
        }).when(networkService).request(any(IRequest.class), eq(AccessToken.Factory.CONVERTER), any(AuthResponseError.CONVERTER.getClass()), any(AccessTokenCallback.class));
        AccessToken token = manager.refreshAccessToken("refresh_token", new AccessTokenCallback() {
            @Override
            public void onAccessTokenReceived(AccessToken accessToken) {
                fail("onAccessTokenReceived() should not be called");
            }

            @Override
            public void onAccessTokenFailure(ErrorResult errorResult) {
                assertEquals(ApiErrorCode.AUTH_ERROR_CODE, errorResult.getErrorCode());
                assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, errorResult.getHttpStatus());
                assertEquals("expired_or_invalid_refresh_token", errorResult.getErrorMessage());
            }
        }).get();
        assertNull(token);
    }
}
