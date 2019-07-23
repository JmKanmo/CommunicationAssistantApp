package com.kakao.auth.authorization.accesstoken;

import com.kakao.auth.helper.Encryptor;
import com.kakao.auth.mocks.TestEncryptor;
import com.kakao.auth.mocks.TestPersistentKVStore;
import com.kakao.test.common.KakaoTestCase;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
/**
 * @author kevin.kang. Created on 2017. 8. 9..
 */

public class EncryptedAccessTokenTest extends KakaoTestCase {
    private EncryptedAccessToken finalTokenInfo;
    private EncryptedAccessToken tokenInfo;
    private AccessToken decoratedAccessToken;

    @Before
    public void setup() {
        super.setup();
        decoratedAccessToken = TestAccessTokenFactory.createTestToken();
        tokenInfo = Mockito.spy(new EncryptedAccessToken(decoratedAccessToken, null, true, new TestPersistentKVStore()));
        finalTokenInfo = Mockito.spy(new EncryptedAccessToken(null, null, true, new TestPersistentKVStore()));
    }

    @Test
    public void setEncryptor() {
        finalTokenInfo.setEncryptor(new TestEncryptor());
        Mockito.verify(finalTokenInfo).initAccessToken();
    }

    /**
     * Tests secure mode false -> true
     */
    @Test
    public void initWithEncryption() {
        Mockito.doReturn(false).when(tokenInfo).getLastSecureMode();
        tokenInfo.setEncryptor(new TestEncryptor());
        assertEquals("access_token", tokenInfo.getAccessToken());
        assertEquals("refresh_token", tokenInfo.getRefreshToken());
        assertTrue(tokenInfo.hasValidAccessToken());
        assertTrue(tokenInfo.hasRefreshToken());

        Mockito.reset(tokenInfo);

        assertNotEquals("access_token", decoratedAccessToken.getAccessToken());
        assertNotEquals("refres_token", decoratedAccessToken.getRefreshToken());
        assertTrue(decoratedAccessToken.hasValidAccessToken());
        assertTrue(decoratedAccessToken.hasRefreshToken());
        assertTrue(tokenInfo.getLastSecureMode());
    }

    /**
     * Tests secure mode false -> false, and true -> true
     */
    @Test
    public void initFalseToFalse() {
        // create EncryptedAccessToken
        decoratedAccessToken = TestAccessTokenFactory.createEmptyAccessToken();
        tokenInfo = Mockito.spy(new EncryptedAccessToken(decoratedAccessToken, null, false, new TestPersistentKVStore()));

        // test if empty tokens are successfully created.
        assertNull(tokenInfo.getAccessToken());
        assertNull(tokenInfo.getRefreshToken());
        assertFalse(tokenInfo.hasValidAccessToken());
        assertFalse(tokenInfo.hasRefreshToken());

        // check if access token is created successfully from decorated access token
        decoratedAccessToken = TestAccessTokenFactory.createTestToken();
        tokenInfo = Mockito.spy(new EncryptedAccessToken(decoratedAccessToken, null, false, new TestPersistentKVStore()));
        Mockito.doReturn(false).when(tokenInfo).getLastSecureMode(); // initial state: false
        tokenInfo.setEncryptor(new TestEncryptor());

        assertEquals("access_token", tokenInfo.getAccessToken());
        assertEquals("refresh_token", tokenInfo.getRefreshToken());
        assertTrue(tokenInfo.hasValidAccessToken());
        assertTrue(tokenInfo.hasRefreshToken());

        assertEquals("access_token", decoratedAccessToken.getAccessToken());
        assertEquals("refresh_token", decoratedAccessToken.getRefreshToken());
        assertTrue(decoratedAccessToken.hasValidAccessToken());
        assertTrue(decoratedAccessToken.hasRefreshToken());

        assertFalse(tokenInfo.getLastSecureMode());
    }

    /**
     * Tests secure mode true -> false
     */
    @Test
    public void initWithDecryption() {
        // create encrypted decorated access token
        decoratedAccessToken = TestAccessTokenFactory.createTestToken("access_tokenaccess_token", "refresh_tokenrefresh_token");
        tokenInfo = Mockito.spy(new EncryptedAccessToken(decoratedAccessToken, null, false, new TestPersistentKVStore()));
        Mockito.doReturn(true).when(tokenInfo).getLastSecureMode(); // initial state: true
        tokenInfo.setEncryptor(new TestEncryptor());

        Mockito.reset(tokenInfo);

        assertEquals("access_token", tokenInfo.getAccessToken());
        assertEquals("refresh_token", tokenInfo.getRefreshToken());
        assertTrue(tokenInfo.hasValidAccessToken());
        assertTrue(tokenInfo.hasRefreshToken());

        assertEquals(tokenInfo.getAccessToken(), decoratedAccessToken.getAccessToken());
        assertEquals(tokenInfo.getRefreshToken(), decoratedAccessToken.getRefreshToken());
        assertEquals(tokenInfo.accessTokenExpiresAt(), decoratedAccessToken.accessTokenExpiresAt());
        assertEquals(tokenInfo.refreshTokenExpiresAt(), decoratedAccessToken.refreshTokenExpiresAt());
        assertFalse(tokenInfo.getLastSecureMode());
    }

    /**
     * Tests secure mode true -> true
     */
    @Test
    public void initTrueToTrue() {
        decoratedAccessToken = TestAccessTokenFactory.createTestToken("access_tokenaccess_token", "refresh_tokenrefresh_token");
        tokenInfo = Mockito.spy(new EncryptedAccessToken(decoratedAccessToken, null, true, new TestPersistentKVStore()));
        Mockito.doReturn(true).when(tokenInfo).getLastSecureMode(); // initiali state: true
        tokenInfo.setEncryptor(new TestEncryptor());

        assertEquals("access_token", tokenInfo.getAccessToken());
        assertEquals("refresh_token", tokenInfo.getRefreshToken());
        assertTrue(tokenInfo.hasValidAccessToken());
        assertTrue(tokenInfo.hasRefreshToken());
        assertNotEquals(tokenInfo.getAccessToken(), decoratedAccessToken.getAccessToken());
        assertNotEquals(tokenInfo.getRefreshToken(), decoratedAccessToken.getRefreshToken());

        assertTrue(tokenInfo.getLastSecureMode());
    }

    @Test
    public void updateWithEncryption() {
        decoratedAccessToken = TestAccessTokenFactory.createTestToken();
        tokenInfo = Mockito.spy(new EncryptedAccessToken(decoratedAccessToken, new TestEncryptor(), true, new TestPersistentKVStore()));

        assertEquals("access_token", tokenInfo.getAccessToken());
        assertEquals("refresh_token", tokenInfo.getRefreshToken());
        assertTrue(tokenInfo.hasValidAccessToken());
        assertTrue(tokenInfo.hasRefreshToken());
        assertNotEquals(tokenInfo.getAccessToken(), decoratedAccessToken.getAccessToken());
        assertNotEquals(tokenInfo.getRefreshToken(), decoratedAccessToken.getRefreshToken());

        tokenInfo.updateAccessToken(TestAccessTokenFactory.createTestToken("access_token2", "refresh_token2"));

        assertEquals("access_token2", tokenInfo.getAccessToken());
        assertEquals("refresh_token2", tokenInfo.getRefreshToken());
        assertTrue(tokenInfo.hasValidAccessToken());
        assertTrue(tokenInfo.hasRefreshToken());
        assertEquals("access_token2access_token2", decoratedAccessToken.getAccessToken());
        assertEquals("refresh_token2refresh_token2", decoratedAccessToken.getRefreshToken());
    }

    @Test
    public void decryptionFails() throws Exception {
        decoratedAccessToken = TestAccessTokenFactory.createTestToken("access_tokenaccess_token", "refresh_tokenrefresh_token");
        Encryptor encryptor = Mockito.spy(new TestEncryptor());
        Mockito.doThrow(new IllegalArgumentException()).when(encryptor).decrypt(ArgumentMatchers.anyString());

        tokenInfo = Mockito.spy(new EncryptedAccessToken(decoratedAccessToken, null, true, new TestPersistentKVStore()));
        Mockito.doReturn(true).when(tokenInfo).getLastSecureMode(); // initiali state: true
        tokenInfo.setEncryptor(encryptor);

        assertNull(tokenInfo.getAccessToken());
        assertNull(tokenInfo.getRefreshToken());
        assertFalse(tokenInfo.hasValidAccessToken());
        assertFalse(tokenInfo.hasRefreshToken());

        assertTrue(tokenInfo.getLastSecureMode());
    }

    @Test
    public void encryptionFails() throws Exception {
        decoratedAccessToken = TestAccessTokenFactory.createEmptyAccessToken();
        Encryptor encryptor = Mockito.spy(new TestEncryptor());
        Mockito.doThrow(new IllegalArgumentException()).when(encryptor).encrypt(ArgumentMatchers.anyString());

        tokenInfo = Mockito.spy(new EncryptedAccessToken(decoratedAccessToken, null, true, new TestPersistentKVStore()));
        Mockito.doReturn(true).when(tokenInfo).getLastSecureMode(); // initiali state: true
        tokenInfo.setEncryptor(encryptor);

        tokenInfo.updateAccessToken(TestAccessTokenFactory.createTestToken("access_token", "refresh_token"));

        assertNull(tokenInfo.getAccessToken());
        assertNull(tokenInfo.getRefreshToken());
        assertFalse(tokenInfo.hasValidAccessToken());
        assertFalse(tokenInfo.hasRefreshToken());

        assertTrue(tokenInfo.getLastSecureMode());
    }
}
