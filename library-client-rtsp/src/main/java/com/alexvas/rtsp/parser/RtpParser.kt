package com.alexvas.rtsp.parser

abstract class RtpParser {

    abstract fun processRtpPacketAndGetNalUnit(data: ByteArray, length: Int, marker: Boolean): ByteArray?

    // TODO Use already allocated buffer with RtpPacket.MAX_SIZE = 65507
    // Used only for fragmented packets
    protected val fragmentedBuffer = arrayOfNulls<ByteArray>(1024)
    protected var fragmentedBufferLength = 0
    protected var fragmentedPackets = 0

    protected fun writeNalPrefix0001(buffer: ByteArray) {
        buffer[0] = 0x00
        buffer[1] = 0x00
        buffer[2] = 0x00
        buffer[3] = 0x01
    }

    protected fun processSingleFramePacket(data: ByteArray, length: Int): ByteArray {
        return ByteArray(4 + length).apply {
            writeNalPrefix0001(this)
            System.arraycopy(data, 0, this, 4, length)
        }
    }

}
