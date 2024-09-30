package com.alexvas.rtsp.widget

/**
 * Listener for getting RTSP status update.
 */
interface RtspStatusListener {
    fun onRtspStatusConnecting() {}
    fun onRtspStatusConnected() {}
    fun onRtspStatusDisconnecting() {}
    fun onRtspStatusDisconnected() {}
    fun onRtspStatusFailedUnauthorized() {}
    fun onRtspStatusFailed(message: String?) {}
    fun onRtspFirstFrameRendered() {}
}

/**
 * Listener for getting RTSP raw data, e.g. for recording.
 */
interface RtspDataListener {
    fun onRtspDataVideoNalUnitReceived(data: ByteArray, offset: Int, length: Int, timestamp: Long) {}
    fun onRtspDataAudioSampleReceived(data: ByteArray, offset: Int, length: Int, timestamp: Long) {}
    fun onRtspDataApplicationDataReceived(data: ByteArray, offset: Int, length: Int, timestamp: Long) {}
}
