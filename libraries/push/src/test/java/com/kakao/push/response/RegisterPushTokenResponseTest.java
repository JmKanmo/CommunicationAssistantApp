package com.kakao.push.response;

import com.kakao.network.response.ResponseStringConverter;
import com.kakao.test.common.KakaoTestCase;

import org.junit.Test;

/**
 * @author kevin.kang. Created on 2017. 12. 7..
 */

public class RegisterPushTokenResponseTest extends KakaoTestCase {
    @Test
    public void convert() {
        ResponseStringConverter<Integer> converter = RegisterPushTokenResponse.CONVERTER;

    }
}
