package com.kakao.auth;


import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Locale;

class MiscTest {
    @Disabled
    @Test
    void testLocaleRoot() {
        assertEquals("individual", ApprovalType.INDIVIDUAL.toString());
        assertEquals("project", ApprovalType.PROJECT.toString());

        assertTrue(Arrays.asList("ko", "en").contains(Locale.getDefault().getLanguage().toLowerCase(Locale.ROOT))); // 다른 언어로 설정되어 있는 머신에서는 테스트 수정해야 함.
        assertEquals("KR", Locale.getDefault().getCountry().toUpperCase(Locale.ROOT)); // 다른 지역 머신에서는 테스트 수정해야 함.
    }
}
