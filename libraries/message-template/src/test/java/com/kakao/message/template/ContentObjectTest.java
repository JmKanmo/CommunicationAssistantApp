package com.kakao.message.template;

import com.kakao.test.common.KakaoTestCase;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import static org.junit.jupiter.api.Assertions.*;
/**
 * @author kevin.kang. Created on 2017. 3. 14..
 */

public class ContentObjectTest extends KakaoTestCase {
    private final String title = "title";
    private final String imageUrl = "imageUrl";
    private final String description = "description";
    private final int imageHeight = 200;
    private final int imageWidth = 200;

    @Test
    public void testMembersAreNull() {
        ContentObject contentObject = ContentObject.newBuilder(title, imageUrl,
                LinkObject.newBuilder().build()).build();
        assertNull(contentObject.getDescription());
        assertNull(contentObject.getImageHeight());
        assertNull(contentObject.getImageWidth());
    }

    @Test
    public void testMembersAreCorrectlySet() {
        ContentObject contentObject = ContentObject.newBuilder(title, imageUrl,
                LinkObject.newBuilder().build())
                .setDescrption(description)
                .setImageHeight(imageHeight)
                .setImageWidth(imageWidth).build();

        assertEquals(title, contentObject.getTitle());
        assertEquals(imageUrl, contentObject.getImageUrl());
        assertEquals(description, contentObject.getDescription());
        assertEquals(imageHeight, contentObject.getImageHeight().intValue());
        assertEquals(imageWidth, contentObject.getImageWidth().intValue());
    }

    @Test
    public void testToJSONObject() throws JSONException {
        ContentObject.Builder builder = ContentObject.newBuilder("title", "imageUrl",
                LinkObject.newBuilder().build());
        ContentObject contentObject = builder.build();
        JSONObject contentJson = contentObject.toJSONObject();
        assertNotNull(contentJson);
        assertEquals(3, contentJson.length());
        assertEquals(contentObject.getTitle(), contentJson.getString(MessageTemplateProtocol.TITLE));
        assertEquals(contentObject.getImageUrl(), contentJson.getString(MessageTemplateProtocol.IMAGE_URL));
        assertNotNull(contentJson.get(MessageTemplateProtocol.LINK));
    }
}
