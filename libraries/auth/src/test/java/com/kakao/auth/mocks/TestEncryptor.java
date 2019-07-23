package com.kakao.auth.mocks;

import com.kakao.auth.helper.Encryptor;

import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 * @author kevin.kang. Created on 2017. 8. 9..
 */

public class TestEncryptor implements Encryptor {
    @Override
    public String encrypt(String value) throws GeneralSecurityException, IOException {
        return value + value;
    }

    @Override
    public String decrypt(String encrypted) throws GeneralSecurityException, IOException {
        return encrypted.substring(encrypted.length() / 2);
    }
}
