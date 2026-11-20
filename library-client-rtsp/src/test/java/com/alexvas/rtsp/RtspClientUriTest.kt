package com.alexvas.rtsp

import org.junit.Assert.assertEquals
import org.junit.Test

class RtspClientUriTest {
    private fun track(request: String?) = RtspClient.VideoTrack().apply { this.request = request }

    @Test fun contentBaseUserInfoIsStripped() {
        assertEquals("rtsp://host:554/stream0/",
            RtspClient.getUriFromContentBase("rtsp://host:554/stream0", "rtsp://user:pass@host:554/stream0/"))
    }

    @Test fun missingContentBaseKeepsUri() {
        assertEquals("rtsp://host/a", RtspClient.getUriFromContentBase("rtsp://host/a", null))
        assertEquals("rtsp://host/a", RtspClient.getUriFromContentBase("rtsp://host/a", " "))
    }

    @Test fun absoluteSetupControlUserInfoIsStripped() {
        assertEquals("rtsp://host:554/stream0/trackID=1",
            RtspClient.getUriForSetup("rtsp://host:554/stream0", track("rtsp://user:pass@host:554/stream0/trackID=1")))
        assertEquals("rtsps://host/s/track1",
            RtspClient.getUriForSetup("rtsps://host/s", track("rtsps://user@host/s/track1")))
    }

    @Test fun relativeSetupControlIsAppendedToBase() {
        assertEquals("rtsp://host/stream0/trackID=1",
            RtspClient.getUriForSetup("rtsp://host/stream0", track("trackID=1")))
        assertEquals("rtsp://host/stream0/trackID=1",
            RtspClient.getUriForSetup("rtsp://host/stream0/", track("trackID=1")))
    }
}
