package com.kakao.auth.helper;

import android.content.Context;
import android.os.Build;

import com.kakao.auth.KakaoSDK;
import com.kakao.util.helper.log.Logger;

import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 * Interface used for encrypting and decrypting access token if required.
 *
 * @author kevin.kang. Created on 2017. 7. 25..
 */

public interface Encryptor {

    /**
     * Encrypt the given string.
     *
     * @param value string to be encrypted
     * @return encrypted string
     * @throws GeneralSecurityException
     * @throws IOException
     */
    String encrypt(final String value) throws GeneralSecurityException, IOException;

    /**
     * Decrypt the given string.
     *
     * @param encrypted string to be decrypted
     * @return decrypted string
     * @throws GeneralSecurityException
     * @throws IOException
     */
    String decrypt(final String encrypted) throws GeneralSecurityException, IOException;

    /**
     * Factory class responsible for instantiating encryptor for access token
     */
    class Factory {
        private static Encryptor encryptor = null;

        public static Encryptor getInstnace() {
            Context context = KakaoSDK.getAdapter().getApplicationConfig().getApplicationContext();
            if (encryptor == null) {
                byte[] salt;
                try {
                    salt = AESEncryptor.AndroidIdUtils.generateAndroidId(context);
                } catch (Exception e) {
                    salt = ("xxxx" + Build.PRODUCT + "a23456789012345bcdefg").getBytes();
                }

                try {
                    encryptor = new AESEncryptor(context, salt);
                } catch (GeneralSecurityException e) {
                    Logger.e("Failed to generate encryptor for Access token...");
                    Logger.e(e.toString());
                }
            }
            return encryptor;
        }
    }
}
