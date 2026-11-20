package com.alexvas.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class NetUtilsTest {
    @Test fun stripsUserInfo() {
        assertEquals("rtsp://192.168.10.132:554/stream0",
            NetUtils.stripUriUserInfo("rtsp://looker:secret@192.168.10.132:554/stream0"))
        assertEquals("rtsp://host/a?b=c@d",
            NetUtils.stripUriUserInfo("rtsp://user:p@ss@host/a?b=c@d"))
        assertEquals("rtsp://[fe80::1]:554",
            NetUtils.stripUriUserInfo("rtsp://user@[fe80::1]:554"))
    }

    @Test fun keepsUriWithoutUserInfo() {
        assertEquals("rtsp://host:554/path?x=a@b",
            NetUtils.stripUriUserInfo("rtsp://host:554/path?x=a@b"))
        assertEquals("not a uri", NetUtils.stripUriUserInfo("not a uri"))
    }
}
