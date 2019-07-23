package com.kakao.auth.authorization.accesstoken;

import com.kakao.auth.mocks.TestPersistentKVStore;
import com.kakao.test.common.KakaoTestCase;
import com.kakao.util.helper.PersistentKVStore;

import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author kevin.kang. Created on 2017. 7. 25..
 */

public class PersistentAccessTokenTest extends KakaoTestCase {
    private PersistentAccessToken accessToken;
    private PersistentKVStore persistentKVStore;

    @Before
    public void setup() {
        super.setup();
        persistentKVStore = new TestPersistentKVStore();
        accessToken = new PersistentAccessToken(null, persistentKVStore);
    }

    @Test
    public void init() {
        assertFalse(accessToken.hasValidAccessToken());
        assertFalse(accessToken.hasRefreshToken());
        assertEquals(0, accessToken.getRemainingExpireTime());
    }

    @Test
    public void updateAccessToken() {
        accessToken.updateAccessToken(createTestToken());
        assertEquals("access_token", accessToken.getAccessToken());
        assertEquals("refresh_token", accessToken.getRefreshToken());
        assertTrue(accessToken.hasValidAccessToken());
        assertTrue(accessToken.hasRefreshToken());
        assertFalse(accessToken.getRemainingExpireTime() == 0);
    }

    @Test
    public void updateWithEmptyAccessToken() {
        accessToken.updateAccessToken(createEmptyAccessToken());

        assertFalse(accessToken.hasValidAccessToken());
        assertFalse(accessToken.hasRefreshToken());

        assertEquals(null, accessToken.getAccessToken());
        assertEquals(null, accessToken.getRefreshToken());
        assertEquals(0, accessToken.getRemainingExpireTime());
    }

    @Test
    public void updateWithAccessTokenOnly() {
        accessToken.updateAccessToken(createWithAccessTokenOnly());
        assertTrue(accessToken.hasValidAccessToken());
        assertFalse(accessToken.hasRefreshToken());

        assertEquals("access_token", accessToken.getAccessToken());
        assertNull(accessToken.getRefreshToken());
        assertTrue(accessToken.getRemainingExpireTime() != 0);
    }

    @Test
    public void clearTokens() {
        accessToken.updateAccessToken(createTestToken());
        assertTrue(accessToken.hasRefreshToken());
        assertTrue(accessToken.hasValidAccessToken());

        accessToken.clearAccessToken();
        assertFalse(accessToken.hasValidAccessToken());
        assertFalse(accessToken.hasRefreshToken());
        assertTrue(accessToken.getRemainingExpireTime() == 0);
    }

    AccessToken createTestToken() {
        Date accessTokenDate = new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24);
        Date refreshTokenDate = new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24 * 30);
        AccessToken updateToken = new AccessTokenImpl("access_token", "refresh_token", accessTokenDate, refreshTokenDate);
        return updateToken;
    }

    AccessToken createEmptyAccessToken() {
        return new AccessTokenImpl(null, null, null, null);
    }

    AccessToken createWithAccessTokenOnly() {
        return new AccessTokenImpl("access_token", null, new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24), null);
    }
}
