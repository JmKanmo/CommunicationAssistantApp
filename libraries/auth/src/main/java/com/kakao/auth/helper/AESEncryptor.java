package com.kakao.auth.helper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.provider.Settings;
import android.util.Base64;

import com.kakao.util.helper.Utility;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.KeySpec;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * AES 알고리즘을 사용하는 Encryptor 클래스.
 * reference: https://github.daumkakao.com/api-dev/kakao-api-android-sdk/blob/master/src/com/kakao/api/SecureStorage.java
 *
 * @author kevin.kang. Created on 2017. 7. 25..
 */

class AESEncryptor implements Encryptor {
    private static final byte[] initVector = {
            112, 78, 75, 55, -54, -30, -10, 44, 102, -126, -126, 92, -116, -48, -123, -55
    };
    private static final IvParameterSpec IV_PARAMETER_SPEC = new IvParameterSpec(initVector);

    private static final String keyGenAlgorithm = Utils.base64DecodeAndXor("My0oeSI1IzInbyA+LVFaW2wiNSokPAMiMipOLS4=");
    private static final String cipherAlgorithm = Utils.base64DecodeAndXor("Iio+ASgjKE4/ZSIjXDMOCUoCDww=");
    private static final String algorithm = "AES";
    private static final int ITER_COUNT = 2;
    private static final int KEY_LENGTH = 256;
    private static final String CHAR_SET = "UTF-8";

    private Cipher encryptor;
    private Cipher decryptor;

    AESEncryptor(final Context context, final byte[] salt) throws GeneralSecurityException {
        String keyValue = Utility.getKeyHash(context);
        SecretKeyFactory factory = SecretKeyFactory.getInstance(keyGenAlgorithm);
        KeySpec keySpec = new PBEKeySpec(keyValue.substring(0, Math.min(keyValue.length(), 16)).toCharArray(), salt, ITER_COUNT, KEY_LENGTH);
        SecretKey tmp = factory.generateSecret(keySpec);
        SecretKey secret = new SecretKeySpec(tmp.getEncoded(), algorithm);

        encryptor = Cipher.getInstance(cipherAlgorithm);
        decryptor = Cipher.getInstance(cipherAlgorithm);

        try {
            encryptor.init(Cipher.ENCRYPT_MODE, secret, IV_PARAMETER_SPEC);
            decryptor.init(Cipher.DECRYPT_MODE, secret, IV_PARAMETER_SPEC);
        } catch (InvalidKeyException e) {
            // Due to invalid key size. Using 128 bits instead.
            SecretKey shorterSecret = new SecretKeySpec(Arrays.copyOfRange(tmp.getEncoded(), 0, tmp.getEncoded().length / 2), algorithm);
            encryptor.init(Cipher.ENCRYPT_MODE, shorterSecret, IV_PARAMETER_SPEC);
            decryptor.init(Cipher.DECRYPT_MODE, shorterSecret, IV_PARAMETER_SPEC);
        }
    }

    /**
     * @param value 암호화 하고자 하는 스트링
     * @return 암호화된 스트링. 예외가 발생하게 되면 null을 리턴한다.
     */
    @Override
    public String encrypt(String value) throws GeneralSecurityException, IOException {
        if (value == null) return null;
        byte[] encrypted = encryptor.doFinal(value.getBytes(CHAR_SET));
        return Base64.encodeToString(encrypted, Base64.NO_WRAP);
    }

    /**
     * @param encrypted 해독하고자 하는 암호화된 스트링
     * @return 해독된 스트링. 예외가 발생하게 되면 null 리턴.
     */
    @Override
    public String decrypt(String encrypted) throws GeneralSecurityException, IOException {
        if (encrypted == null) return null;
        byte[] original = decryptor.doFinal(Base64.decode(encrypted, Base64.NO_WRAP));
        return new String(original, CHAR_SET);
    }

    private static class Utils {
        static String xorMessage(String message) {
            return xorMessage(message, "com.kakao.api");
        }

        static String xorMessage(String message, String key) {
            try {
                if (message == null || key == null) {
                    return null;
                }

                char[] keys = key.toCharArray();
                char[] msg = message.toCharArray();

                int ml = msg.length;
                int kl = keys.length;
                char[] newMsg = new char[ml];

                for (int i = 0; i < ml; i++) {
                    newMsg[i] = (char) (msg[i] ^ keys[i % kl]);
                }
                return new String(newMsg);
            } catch (Exception e) {
                return null;
            }
        }

        static String base64DecodeAndXor(String source) {
            return xorMessage(new String(Base64.decode(source, Base64.DEFAULT)));
        }
    }

    /**
     * Device 고유의 UUID를 만드는 클래스
     * reference: https://github.daumkakao.com/api-dev/kakao-api-android-sdk/blob/master/src/com/kakao/api/SecureStorage.java
     */
    static class AndroidIdUtils {
        private static final String DIGEST_ALGORITHM = "SHA-256";

        static byte[] generateAndroidId(Context context) throws NoSuchAlgorithmException {
            @SuppressLint("HardwareIds") String androidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
            if (androidId == null) throw new NullPointerException("android_id is null.");
            androidId = stripZeroOrSpace(androidId);
            androidId = String.format("SDK-%s", androidId);
            return hash(androidId);
        }

        private static String stripZeroOrSpace(String str) {
            return str == null ? null : str.replaceAll("[0\\s]", "");
        }

        private static byte[] hash(String uuid) throws NoSuchAlgorithmException {
            MessageDigest md = MessageDigest.getInstance(DIGEST_ALGORITHM);
            md.reset();
            md.update(uuid.getBytes());

            return md.digest();
        }
    }
}
