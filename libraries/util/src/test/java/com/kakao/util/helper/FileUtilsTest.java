package com.kakao.util.helper;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
/**
 * @author kevin.kang
 * Created by kevin.kang on 2017. 2. 23..
 */
class FileUtilsTest {
    @Test
    void toFileName() {
        assertEquals("abc" + FileUtils.FILE_NAME_AVAIL_CHARACTER + "def", FileUtils.toFileName("abc\"def", FileUtils.FILE_NAME_AVAIL_CHARACTER));
        assertEquals("abc" + FileUtils.FILE_NAME_AVAIL_CHARACTER + "def", FileUtils.toFileName("abc*def", FileUtils.FILE_NAME_AVAIL_CHARACTER));
        assertEquals("abc" + FileUtils.FILE_NAME_AVAIL_CHARACTER + "def", FileUtils.toFileName("abc/def", FileUtils.FILE_NAME_AVAIL_CHARACTER));
        assertEquals("abc" + FileUtils.FILE_NAME_AVAIL_CHARACTER + "def", FileUtils.toFileName("abc:def", FileUtils.FILE_NAME_AVAIL_CHARACTER));
        assertEquals("abc" + FileUtils.FILE_NAME_AVAIL_CHARACTER + "def", FileUtils.toFileName("abc<def", FileUtils.FILE_NAME_AVAIL_CHARACTER));
        assertEquals("abc" + FileUtils.FILE_NAME_AVAIL_CHARACTER + "def", FileUtils.toFileName("abc>def", FileUtils.FILE_NAME_AVAIL_CHARACTER));
        assertEquals("abc" + FileUtils.FILE_NAME_AVAIL_CHARACTER + "def", FileUtils.toFileName("abc?def", FileUtils.FILE_NAME_AVAIL_CHARACTER));
        assertEquals("abc" + FileUtils.FILE_NAME_AVAIL_CHARACTER + "def", FileUtils.toFileName("abc\\def", FileUtils.FILE_NAME_AVAIL_CHARACTER));
        assertEquals("abc" + FileUtils.FILE_NAME_AVAIL_CHARACTER + "def", FileUtils.toFileName("abc|def", FileUtils.FILE_NAME_AVAIL_CHARACTER));
    }
}
