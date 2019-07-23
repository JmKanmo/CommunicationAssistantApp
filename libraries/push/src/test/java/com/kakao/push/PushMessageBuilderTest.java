package com.kakao.push;

import com.kakao.test.common.KakaoTestCase;

import org.junit.Test;

/**
 * @author kevin.kang. Created on 2018. 2. 27..
 */

public class PushMessageBuilderTest extends KakaoTestCase {
    @Override
    public void setup() {
        super.setup();
    }

    /**
     * Tests if NPE doesn't occur even with empty parameters
     */
    @Test
    public void empty() {
        String message = new PushMessageBuilder().toString();
    }
}
