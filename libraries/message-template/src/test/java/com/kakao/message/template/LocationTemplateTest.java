package com.kakao.message.template;

import com.kakao.test.common.KakaoTestCase;

import org.junit.Test;

import static org.junit.jupiter.api.Assertions.*;
/**
 * @author kevin.kang. Created on 2017. 3. 14..
 */

public class LocationTemplateTest extends KakaoTestCase {
    @Test
    public void testObjectType() {
        LocationTemplate params =
                LocationTemplate.newBuilder("address",
                        ContentObject.newBuilder("title", "imageUrl", LinkObject.newBuilder().build()).build()).build();
        assertEquals(MessageTemplateProtocol.TYPE_LOCATION, params.getObjectType());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testUnsupportedBuilderMethod() {
        LocationTemplate.newBuilder(ContentObject.newBuilder("title", "iamgeUrl",
                LinkObject.newBuilder().build()).build()).build();
    }
}
