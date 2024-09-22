package com.alexvas.rtsp.widget

interface RtspStatusListener {
    fun onRtspStatusConnecting() {}
    fun onRtspStatusConnected() {}
    fun onRtspStatusDisconnecting() {}
    fun onRtspStatusDisconnected() {}
    fun onRtspStatusFailedUnauthorized() {}
    fun onRtspStatusFailed(message: String?) {}
    fun onRtspFirstFrameRendered() {}
}
