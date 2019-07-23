package com.kakao.auth.authorization.accesstoken;


import com.kakao.auth.StringSet;
import com.kakao.network.response.ResponseBody;
import com.kakao.test.common.KakaoTestCase;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This class mocks KakaoSDK, Utility, SharedPreferenceCache, ResponseBody for solely unit-testing AccessTokenImpl class.
 * @author kevin.kang
 * Created by kevin.kang on 16. 8. 11..
 */
public class AccessTokenImplTest extends KakaoTestCase {

    private AccessTokenImpl tokenInfo;
    @Before
    public void setup() {
        super.setup();
    }

    @Test
    public void createEmptyToken() {
        tokenInfo = (AccessTokenImpl) TestAccessTokenFactory.createEmptyAccessToken();
        assertNotNull(tokenInfo);
        assertNull(tokenInfo.getAccessToken());
        assertNull(tokenInfo.getRefreshToken());
        assertFalse(tokenInfo.hasValidAccessToken());
        assertFalse(tokenInfo.hasValidRefreshToken());

        assertNotNull(tokenInfo.toString());
    }

    @Test
    public void createFromCache() {
        tokenInfo = new AccessTokenImpl(new TestAccessToken());
        assertTrue(tokenInfo.hasValidAccessToken());
        assertTrue(tokenInfo.hasRefreshToken());
        assertEquals("access_token", tokenInfo.getAccessToken());
        assertEquals("refresh_token", tokenInfo.getRefreshToken());

        assertNotNull(tokenInfo.toString());
    }

    /**
     * Old refresh token with no expiration date should be considered a valid refresh
     * token. This test code and relevant code in {@link AccessTokenImpl} can be removed after
     * some time since every refresh token will have expiration date then.(Probably at the end of 2018)
     */
    @Test
    public void createFromOldCache() {
        AccessToken mock = spy(new TestAccessToken());
        doReturn(null).when(mock).refreshTokenExpiresAt();
        tokenInfo = new AccessTokenImpl(mock);
        assertTrue(tokenInfo.hasValidRefreshToken());
    }

    @Test
    public void createFromResponse() {
        try {
            AccessToken tokenInfo = new AccessTokenImpl(createTokenResponseBody(true));

            assertEquals("access_token", tokenInfo.getAccessToken());
            assertEquals("refresh_token", tokenInfo.getRefreshToken());
            assertTrue(tokenInfo.hasValidAccessToken());
            assertTrue(tokenInfo.hasValidRefreshToken());
            assertNotNull(tokenInfo.accessTokenExpiresAt());
            assertNotNull(tokenInfo.refreshTokenExpiresAt());

            String body = createTokenResponseBody(false);
            tokenInfo = new AccessTokenImpl(body);
            assertEquals("access_token", tokenInfo.getAccessToken());
            assertNull(tokenInfo.getRefreshToken());
            assertTrue(tokenInfo.hasValidAccessToken());
            assertFalse(tokenInfo.hasValidRefreshToken());
            assertNotNull(tokenInfo.refreshTokenExpiresAt());


        } catch (ResponseBody.ResponseBodyException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @Test
    public void createFromString() {
        String string = createTokenResponseBody(true);
        AccessToken tokenInfo = new AccessTokenImpl(string);
        assertEquals("access_token", tokenInfo.getAccessToken());
        assertEquals("refresh_token", tokenInfo.getRefreshToken());
        assertTrue(tokenInfo.hasValidAccessToken());
        assertTrue(tokenInfo.hasValidRefreshToken());


        String body = createTokenResponseBody(false);
        tokenInfo = new AccessTokenImpl(body);
        assertEquals("access_token", tokenInfo.getAccessToken());
        assertNull(tokenInfo.getRefreshToken());
        assertTrue(tokenInfo.hasValidAccessToken());
        assertFalse(tokenInfo.hasValidRefreshToken());
    }

    @Test
    public void createFromResponseWithException() {
    }

    String createTokenResponseBody(boolean hasRefreshToken) {
        return tokenResponseWithRefreshExpires(hasRefreshToken, 60 * 60 * 24 * 30);
    }

    String tokenResponseWithRefreshExpires(boolean hasRefreshToken, long refreshTokenExpiresIn) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(StringSet.access_token, "access_token");
            jsonObject.put(StringSet.expires_in, 43199);
            if (hasRefreshToken) {
                jsonObject.put(StringSet.refresh_token, "refresh_token");
                jsonObject.put(StringSet.refresh_token_expires_in, refreshTokenExpiresIn);
            }
            return jsonObject.toString();
        } catch (JSONException e) {
            return null;
        }
    }
}
