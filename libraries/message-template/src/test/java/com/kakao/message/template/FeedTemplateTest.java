package com.kakao.message.template;

import com.kakao.test.common.KakaoTestCase;

import org.junit.Test;

import static org.junit.jupiter.api.Assertions.*;
/**
 * @author kevin.kang. Created on 2017. 3. 14..
 */

public class FeedTemplateTest extends KakaoTestCase {

    @Test
    public void testObjectType() {
        FeedTemplate params =
                FeedTemplate.newBuilder(ContentObject.newBuilder("title", "imageUrl",
                        LinkObject.newBuilder().build()).build()).build();
        assertEquals(MessageTemplateProtocol.TYPE_FEED, params.getObjectType());
    }

    @Test
    public void testNewBuilder() {
        FeedTemplate params =
                FeedTemplate.newBuilder(null).build();
        assertNull(params.getContentObject());
        assertEquals(0, params.getButtons().size());
        assertNull(params.getSocial());

        params = FeedTemplate.newBuilder(ContentObject.newBuilder("title", "imageUrl",
                LinkObject.newBuilder().build()).build()).build();

        assertNotNull(params.getContentObject());
    }

    @Test
    public void testSetSocial() {
        FeedTemplate params =
                FeedTemplate.newBuilder(ContentObject.newBuilder("title", "imageUrl",
                        LinkObject.newBuilder().build()).build())
                        .setSocial(SocialObject.newBuilder().build()).build();
        assertNotNull(params.getSocial());
    }

    @Test
    public void testAddButton() {
        FeedTemplate.Builder paramsBuilder =
                FeedTemplate.newBuilder(ContentObject.newBuilder("title", "imageUrl",
                        LinkObject.newBuilder().build()).build())
                        .addButton(new ButtonObject("title", LinkObject.newBuilder().build()));

        FeedTemplate params = paramsBuilder.build();
        assertEquals(1, params.getButtons().size());

        paramsBuilder.addButton(new ButtonObject("title", LinkObject.newBuilder().build()));
        params = paramsBuilder.build();
        assertEquals(2, params.getButtons().size());

        paramsBuilder.addButton(new ButtonObject("title", LinkObject.newBuilder().build())).build();
        params = paramsBuilder.build();
        assertEquals(3, params.getButtons().size());
    }

    @Test
    public void testToJSONObject() {

    }
}
