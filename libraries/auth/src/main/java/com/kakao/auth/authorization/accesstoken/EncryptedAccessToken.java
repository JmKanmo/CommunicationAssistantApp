package com.kakao.auth.authorization.accesstoken;

import android.os.Bundle;

import com.kakao.auth.helper.Encryptor;
import com.kakao.util.helper.PersistentKVStore;

import java.util.Date;

/**
 * AccessToken interface responsible for encrypting or decrypting access token depending on the
 * option provided by the developer. Follows decorator pattern.
 *
 * Important:
 * What developers have to note is that getAccessToken() and getRefreshToken() always returns
 * the decrypted value. Encrypted values are only used for updating tokenInfo object, which is
 * likely to contain persistence functionality. (Encryption before persisting for better security)
 *
 * @author kevin.kang. Created on 2017. 7. 24..
 */
class EncryptedAccessToken implements AccessToken {

    private AccessToken tokenInfo;
    private boolean currentSecureMode;
    private Encryptor encryptor;
    private PersistentKVStore cache;

    private static final String CACHE_KAKAO_SECURE_MODE = "com.kakao.token.KakaoSecureMode";

    void setEncryptor(Encryptor encryptor) {
        this.encryptor = encryptor;
        initAccessToken();
    }

    EncryptedAccessToken(AccessToken accessToken, Encryptor encryptor, final boolean currentSecureMode, final PersistentKVStore cache) {
        this.tokenInfo = accessToken;
        this.encryptor = encryptor;
        this.currentSecureMode = currentSecureMode;
        this.cache = cache;
        if (encryptor != null) {
            initAccessToken();
        }
    }

    void initAccessToken() {
        AccessToken token = null;
        if (tokenInfo != null) {
            if (needsEncryption()) {
                token = processAccessToken(true, tokenInfo);
            } else if (needsDecryption()) {
                token = processAccessToken(false, tokenInfo);
            }
            if (token != null) {
                tokenInfo.updateAccessToken(token);
            }
        }
        setLastSecureMode(currentSecureMode);
    }

    @Override
    public String getAccessToken() {
        String token = tokenInfo.getAccessToken();
        if (token == null) return null;
        try {
            if (currentSecureMode) {
                token = decryptToken(token);
            }
        } catch (Exception e) {
            e.printStackTrace();
            token = null;
        }
        return token;
    }

    @Override
    public String getRefreshToken() {
        String token = tokenInfo.getRefreshToken();
        if (token == null) return null;
        try {
            if (currentSecureMode) {
                token = decryptToken(token);
            }
        } catch (Exception e) {
            e.printStackTrace();
            token= null;
        }
        return token;
    }

    @Override
    public Date accessTokenExpiresAt() {
        return tokenInfo.accessTokenExpiresAt();
    }

    @Override
    public Date refreshTokenExpiresAt() {
        return tokenInfo.refreshTokenExpiresAt();
    }

    @Override
    public void clearAccessToken() {
        tokenInfo.clearAccessToken();
    }

    @Override
    public void clearRefreshToken() {
        tokenInfo.clearRefreshToken();
    }

    @Override
    public boolean hasValidAccessToken() {
        return tokenInfo.hasValidAccessToken() && getAccessToken() != null;
    }

    @Override
    public boolean hasRefreshToken() {
        return tokenInfo.hasRefreshToken() && getRefreshToken() != null;
    }

    @Override
    public boolean hasValidRefreshToken() {
        return tokenInfo.hasValidRefreshToken() && getRefreshToken() != null;
    }

    @Override
    public void updateAccessToken(AccessToken accessToken) {
        if (currentSecureMode) {
            accessToken = processAccessToken(true, accessToken);
        }
        if (tokenInfo != null) {
            tokenInfo.updateAccessToken(accessToken);
        }
        setLastSecureMode(currentSecureMode);
    }

    private String encryptToken(String token) {
        try {
            return encryptor.encrypt(token);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String decryptToken(String token) {
        try {
            return encryptor.decrypt(token);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public int getRemainingExpireTime() {
        return tokenInfo.getRemainingExpireTime();
    }

    boolean getLastSecureMode() {
        String lastSecureModeString = cache.getString(CACHE_KAKAO_SECURE_MODE);
        return lastSecureModeString != null && lastSecureModeString.equals("true");
    }

    void setLastSecureMode(boolean secureMode) {
        Bundle bundle = new Bundle();
        bundle.putString(CACHE_KAKAO_SECURE_MODE, String.valueOf(secureMode));
        cache.save(bundle);
    }

    boolean needsEncryption() {
        return !getLastSecureMode() && currentSecureMode;
    }

    boolean needsDecryption() {
        return getLastSecureMode() && !currentSecureMode;
    }

    private AccessToken processAccessToken(final boolean encrypt, final AccessToken accessToken) {
        return new AccessToken() {
            @Override
            public String getAccessToken() {
                if (encrypt) {
                    return encryptToken(accessToken.getAccessToken());
                } else {
                    return decryptToken(accessToken.getAccessToken());
                }
            }

            @Override
            public String getRefreshToken() {
                if (encrypt) {
                    return encryptToken(accessToken.getRefreshToken());
                } else {
                    return decryptToken(accessToken.getRefreshToken());
                }
            }

            @Override
            public Date accessTokenExpiresAt() {
                return accessToken.accessTokenExpiresAt();
            }

            @Override
            public Date refreshTokenExpiresAt() {
                return accessToken.refreshTokenExpiresAt();
            }

            @Override
            public void clearAccessToken() {
            }
            @Override
            public void clearRefreshToken() {
            }
            @Override
            public void updateAccessToken(AccessToken accessToken) {
            }
            @Override
            public boolean hasValidAccessToken() {
                return false;
            }
            @Override
            public boolean hasRefreshToken() {
                return false;
            }

            @Override
            public boolean hasValidRefreshToken() {
                return false;
            }

            @Override
            public int getRemainingExpireTime() {
                return 0;
            }
        };
    }
}
