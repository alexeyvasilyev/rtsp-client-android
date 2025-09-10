package com.alexvas.rtsp.parser

abstract class AudioParser {
    abstract fun processRtpPacketAndGetSample(
        data: ByteArray,
        length: Int
    ): ByteArray?
}