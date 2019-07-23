package com.kakao.message.template;

import com.kakao.test.common.KakaoTestCase;

import org.junit.Test;
import static org.junit.jupiter.api.Assertions.*;
/**
 * @author kevin.kang. Created on 2017. 3. 14..
 */

public class SocialObjectTest extends KakaoTestCase {
    @Test
    public void testMembersAreNull() {
        SocialObject socialObject = SocialObject.newBuilder().build();
        assertNull(socialObject.getCommentCount());
        assertNull(socialObject.getLikeCount());
        assertNull(socialObject.getSharedCount());
        assertNull(socialObject.getSubscriberCount());
        assertNull(socialObject.getViewCount());
    }

    @Test
    public void testMembersAreCorrectlySet() {
        SocialObject socialObject = SocialObject.newBuilder()
                .setLikeCount(100)
                .setSharedCount(50)
                .setCommentCount(10)
                .setViewCount(500)
                .setSubscriberCount(30)
                .build();
        assertEquals(100, socialObject.getLikeCount().longValue());
        assertEquals(50, socialObject.getSharedCount().longValue());
        assertEquals(10, socialObject.getCommentCount().longValue());
        assertEquals(500, socialObject.getViewCount().longValue());
        assertEquals(30, socialObject.getSubscriberCount().longValue());

    }
}
