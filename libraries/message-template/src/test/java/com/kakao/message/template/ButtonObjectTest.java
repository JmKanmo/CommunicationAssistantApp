package com.kakao.message.template;

import com.kakao.test.common.KakaoTestCase;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import static org.junit.jupiter.api.Assertions.*;
/**
 * @author kevin.kang. Created on 2017. 3. 14..
 */

public class ButtonObjectTest extends KakaoTestCase {
    @Test
    public void testToJSONObject() throws JSONException {
        ButtonObject buttonObject = new ButtonObject("title", LinkObject.newBuilder().build());
        JSONObject buttonJson = buttonObject.toJSONObject();
        assertNotNull(buttonJson);
        assertEquals(buttonObject.getTitle(), buttonJson.getString(MessageTemplateProtocol.TITLE));
        assertNotNull(buttonJson.get(MessageTemplateProtocol.LINK));
    }
}
