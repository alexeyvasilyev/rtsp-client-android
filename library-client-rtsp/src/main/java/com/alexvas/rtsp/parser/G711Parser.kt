package com.alexvas.rtsp.parser

class G711Parser() : AudioParser() {
    override fun processRtpPacketAndGetSample(
        data: ByteArray,
        length: Int
    ): ByteArray? {
        val g711Payload = data.copyOfRange(0, length)
        return g711Payload
    }
}
