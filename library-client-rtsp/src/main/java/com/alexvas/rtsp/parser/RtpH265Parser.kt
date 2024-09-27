package com.alexvas.rtsp.parser

import android.util.Log

class RtpH265Parser: RtpParser() {

    override fun processRtpPacketAndGetNalUnit(data: ByteArray, length: Int, marker: Boolean): ByteArray? {
        if (DEBUG) Log.v(TAG, "processRtpPacketAndGetNalUnit(length=$length, marker=$marker)")

        // NAL Unit Header.type (RFC7798 Section 1.1.4).
        val nalType = ((data[0].toInt() shr 1) and 0x3F).toByte()
        var nalUnit: ByteArray? = null

//        Log.d(TAG, "\t\tNAL type: ${VideoCodecUtils.getH265NalUnitTypeString(nalType)}")

        if (nalType in 0..<RTP_PACKET_TYPE_AP) {
            nalUnit = processSingleFramePacket(data, length)
            clearFragmentedBuffer()
            if (DEBUG) Log.d(TAG, "Single NAL (${nalUnit.size})")
        } else if (nalType == RTP_PACKET_TYPE_AP) {
            // TODO: Support AggregationPacket mode.
            Log.e(TAG, "need to implement processAggregationPacket")
        } else if (nalType == RTP_PACKET_TYPE_FU) {
            nalUnit = processFragmentationUnitPacket(data, length, marker)
        } else {
            Log.e(TAG, "RTP H265 payload type [${nalType}] not supported.")
        }

        return nalUnit
    }

    private fun processFragmentationUnitPacket(data: ByteArray, length: Int, marker: Boolean): ByteArray? {
        if (DEBUG) Log.v(TAG, "processFragmentationUnitPacket(length=$length, marker=$marker)")

        val fuHeader = data[2].toInt()
        val isFirstFuPacket = (fuHeader and 0x80) > 0
        val isLastFuPacket = (fuHeader and 0x40) > 0

        if (isFirstFuPacket) {
            addStartFragmentedPacket(data, length)
        } else if (isLastFuPacket || marker) {
            return addEndFragmentedPacketAndCombine(data, length)
        } else {
            addMiddleFragmentedPacket(data, length)
        }
        return null
    }

    private fun addStartFragmentedPacket(data: ByteArray, length: Int) {
        if (DEBUG) Log.v(TAG, "addStartFragmentedPacket(data.size=${data.size}, length=$length)")
        fragmentedPackets = 0
        fragmentedBufferLength = length - 1
        fragmentedBuffer[0] = ByteArray(fragmentedBufferLength).apply {

            val tid = (data[1].toInt() and 0x7)
            val fuHeader = data[2].toInt()
            val nalUnitType = fuHeader and 0x3F

            // Convert RTP header into HEVC NAL Unit header accoding to RFC7798 Section 1.1.4.
            // RTP byte 0: ignored.
            // RTP byte 1: repurposed as HEVC HALU byte 0, copy NALU type.
            // RTP Byte 2: repurposed as HEVC HALU byte 1, layerId required to be zero, copying only tid.
            // Set data position from byte 1 as byte 0 is ignored.
            this[0] = (((nalUnitType shl 1) and 0x7F).toByte())
            this[1] = tid.toByte()
        }
        System.arraycopy(data, 3, fragmentedBuffer[0]!!, 2, length - 3)
    }

    private fun addMiddleFragmentedPacket(data: ByteArray, length: Int) {
        if (DEBUG) Log.v(TAG, "addMiddleFragmentedPacket(data.size=${data.size}, length=$length)")
        fragmentedPackets++
        if (fragmentedPackets >= fragmentedBuffer.size) {
            Log.e(TAG, "Too many middle packets. No RTP_PACKET_TYPE_FU end packet received. Skipped RTP packet.")
            fragmentedBuffer[0] = null
        } else {
            fragmentedBufferLength += length - 3
            fragmentedBuffer[fragmentedPackets] = ByteArray(length - 3).apply {
                System.arraycopy(data, 3, this, 0, length - 3)
            }
        }
    }

    private fun addEndFragmentedPacketAndCombine(data: ByteArray, length: Int): ByteArray? {
        if (DEBUG) Log.v(TAG, "addEndFragmentedPacketAndCombine(data.size=${data.size}, length=$length)")
        var nalUnit: ByteArray? = null
        if (fragmentedBuffer[0] == null) {
            Log.e(TAG, "No NAL FU_A start packet received. Skipped RTP packet.")
        } else {
            nalUnit = ByteArray(fragmentedBufferLength + length + 3)
            writeNalPrefix0001(nalUnit)
            var tmpLen = 4
            // Write start and middle packets
            for (i in 0 until fragmentedPackets + 1) {
                fragmentedBuffer[i]!!.apply {
                    System.arraycopy(
                        this,
                        0,
                        nalUnit,
                        tmpLen,
                        this.size
                    )
                    tmpLen += this.size
                }
            }
            // Write end packet
            System.arraycopy(data, 3, nalUnit, tmpLen, length - 3)
            clearFragmentedBuffer()
            if (DEBUG) Log.d(TAG, "Fragmented NAL (${nalUnit.size})")
        }
        return nalUnit
    }

    private fun clearFragmentedBuffer() {
        if (DEBUG) Log.v(TAG, "clearFragmentedBuffer()")
        for (i in 0 until fragmentedPackets + 1) {
            fragmentedBuffer[i] = null
        }
    }

    companion object {
        private val TAG: String = RtpH265Parser::class.java.simpleName
        private const val DEBUG = false

        /** Aggregation Packet. RFC7798 Section 4.4.2.  */
        private const val RTP_PACKET_TYPE_AP: Byte = 48
        /** Fragmentation Unit. RFC7798 Section 4.4.3. */
        private const val RTP_PACKET_TYPE_FU: Byte = 49
    }

}
