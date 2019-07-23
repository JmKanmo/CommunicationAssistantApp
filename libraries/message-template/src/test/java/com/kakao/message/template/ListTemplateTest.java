package com.kakao.message.template;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
/**
 * @author kevin.kang. Created on 2017. 3. 14..
 */

class ListTemplateTest {

    @Test
    void getObjectType() {
        ListTemplate params =
                ListTemplate.newBuilder("title", LinkObject.newBuilder().build()).build();
        assertEquals(MessageTemplateProtocol.TYPE_LIST, params.getObjectType());
    }
}
