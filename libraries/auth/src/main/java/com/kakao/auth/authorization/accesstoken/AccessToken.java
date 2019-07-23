package com.kakao.auth.authorization.accesstoken;

import com.kakao.auth.ISessionConfig;
import com.kakao.auth.helper.Encryptor;
import com.kakao.auth.network.response.AuthResponseError;
import com.kakao.network.response.ResponseBody;
import com.kakao.network.response.ResponseStringConverter;
import com.kakao.util.helper.PersistentKVStore;

import java.util.Date;

/**
 * Stores access token and refresh token data used by Kakao API.
 *
 * @author kevin.kang. Created on 2017. 7. 24..
 */

public interface AccessToken {

    /**
     * Get access token.
     *
     * @return access token
     */
    String getAccessToken();

    /**
     * Get refresh token.
     * @return refresh token
     */
    String getRefreshToken();

    /**
     * Get expire time for access token
     * @return Expire time for access token as Date object
     */
    Date accessTokenExpiresAt();

    /**
     * Get expire time for refresh token
     * @return Expire time for refresh token as Date object
     */
    Date refreshTokenExpiresAt();

    /**
     * Get remaining expire time for access token in millisceonds
     * @return Remaining expire time for access token in milliseconds
     */
    int getRemainingExpireTime();

    /**
     * check if access token is valid or not by its expiration time.
     *
     * @return true if valid, false otherwise.
     */
    boolean hasValidAccessToken();

    /**
     * check if refresh token is available.
     *
     * @return true if there is refresh token, false otherwise.
     *
     * @deprecated use {@link AccessToken#hasValidRefreshToken()} instead.
     */
    @Deprecated
    boolean hasRefreshToken();

    /**
     * Check if refresh token is valid or not by its expiration time.
     * @return true if there is a valid refresh token, false otherwise.
     */
    boolean hasValidRefreshToken();

    /**
     * Update global access token with the given one (which could just be POJO).
     *
     * @param accessToken AccessToken object
     */
    void updateAccessToken(AccessToken accessToken);

    /**
     * Clear access token and its expire time.
     */
    void clearAccessToken();

    /**
     * Clear refresh token and its expire time.
     */
    void clearRefreshToken();

    /**
     * Factory class for instantiating Access token
     */
    class Factory {
        private static AccessToken instance;
        private static final Date MIN_DATE = new Date(Long.MIN_VALUE);
        private static final Date ALREADY_EXPIRED_EXPIRATION_TIME = MIN_DATE;

        /**
         * Create a dummy empty token info, usually used to clear access token.
         *
         * @return empty dummy token info
         */
        public static AccessToken createEmptyToken() {
            return new AccessTokenImpl("", "", ALREADY_EXPIRED_EXPIRATION_TIME, ALREADY_EXPIRED_EXPIRATION_TIME);
        }

        @Deprecated
        public static AccessToken createFromResponse(ResponseBody body) throws ResponseBody.ResponseBodyException {
            return new AccessTokenImpl(body);
        }

        /**
         * Create an access token from a successful response from Kakao OAuth server. This
         * @param stringData response
         * @return AccessToken instance
         * @throws AuthResponseError if there was an error response (http status code other than 200)
         * @throws ResponseBody.ResponseBodyException if response body is malformed
         */
        public static AccessToken createFromResponse(String stringData) {
            return new AccessTokenImpl(stringData);
        }

        /**
         * Create an access token from cache to be used globally by Session.
         * This is currently only called when initializing session.
         *
         * @param sessionConfig ISessionConfig instance
         * @param cache Persistent storage to store access token
         * @return Access token singleton object
         */
        public static AccessToken createFromCache(ISessionConfig sessionConfig, final PersistentKVStore cache) {
            PersistentAccessToken persistentAccessToken = new PersistentAccessToken(null, cache);
            boolean currentSecureMode = sessionConfig.isSecureMode();
            EncryptedAccessToken encryptableAccessToken = new EncryptedAccessToken(persistentAccessToken, null, currentSecureMode, cache);
            if ((currentSecureMode || encryptableAccessToken.getLastSecureMode()) && Encryptor.Factory.getInstnace() != null) {
                Encryptor encryptor = Encryptor.Factory.getInstnace();
                encryptableAccessToken.setEncryptor(encryptor);
                instance = new AccessTokenImpl(encryptableAccessToken);
            } else {
                instance = new AccessTokenImpl(persistentAccessToken);
            }
            return instance;
        }

        public static AccessToken getInstance() {
            return instance;
        }

        public static ResponseStringConverter<AccessToken> CONVERTER = new ResponseStringConverter<AccessToken>() {
            @Override
            public AccessToken convert(String o) throws ResponseBody.ResponseBodyException {
                return AccessToken.Factory.createFromResponse(o);
            }
        };
    }
}
