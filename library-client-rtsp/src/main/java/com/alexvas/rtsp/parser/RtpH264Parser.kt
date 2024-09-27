package com.alexvas.rtsp.parser

import android.util.Log
import com.alexvas.utils.VideoCodecUtils
import com.alexvas.utils.VideoCodecUtils.getH264NalUnitTypeString

class RtpH264Parser: RtpParser() {

    override fun processRtpPacketAndGetNalUnit(data: ByteArray, length: Int, marker: Boolean): ByteArray? {
        if (DEBUG) Log.v(TAG, "processRtpPacketAndGetNalUnit(data.size=${data.size}, length=$length, marker=$marker)")

        val nalType = (data[0].toInt() and 0x1F).toByte()
        val packFlag = data[1].toInt() and 0xC0
        var nalUnit: ByteArray? = null

        if (DEBUG)
            Log.d(TAG, "\t\tNAL type: ${getH264NalUnitTypeString(nalType)}, pack flag: 0x${Integer.toHexString(packFlag).lowercase()}")

        when (nalType) {
            VideoCodecUtils.NAL_STAP_A, VideoCodecUtils.NAL_STAP_B -> {
                // Not supported
            }

            VideoCodecUtils.NAL_MTAP16, VideoCodecUtils.NAL_MTAP24 -> {
                // Not supported
            }

            VideoCodecUtils.NAL_FU_A -> {
                when (packFlag) {
                    0x80 -> {
                        addStartFragmentedPacket(data, length)
                    }

                    0x00 -> {
                        if (marker) {
                            // Sometimes 0x40 end packet is not arrived. Use marker bit in this case
                            // to finish fragmented packet.
                            nalUnit = addEndFragmentedPacketAndCombine(data, length)
                        } else {
                            addMiddleFragmentedPacket(data, length)
                        }
                    }

                    0x40 -> {
                        nalUnit = addEndFragmentedPacketAndCombine(data, length)
                    }
                }
            }

            VideoCodecUtils.NAL_FU_B -> {
                // Not supported
            }

            else -> {
                nalUnit = processSingleFramePacket(data, length)
                clearFragmentedBuffer()
                if (DEBUG) Log.d(TAG, "Single NAL (${nalUnit.size})")
            }
        }
        return nalUnit
    }

    private fun addStartFragmentedPacket(data: ByteArray, length: Int) {
        if (DEBUG) Log.v(TAG, "addStartFragmentedPacket(data.size=${data.size}, length=$length)")
        fragmentedPackets = 0
        fragmentedBufferLength = length - 1
        fragmentedBuffer[0] = ByteArray(fragmentedBufferLength).apply {
            this[0] = ((data[0].toInt() and 0xE0) or (data[1].toInt() and 0x1F)).toByte()
        }
        System.arraycopy(data, 2, fragmentedBuffer[0]!!, 1, length - 2)
    }

    private fun addMiddleFragmentedPacket(data: ByteArray, length: Int) {
        if (DEBUG) Log.v(TAG, "addMiddleFragmentedPacket(data.size=${data.size}, length=$length)")
        fragmentedPackets++
        if (fragmentedPackets >= fragmentedBuffer.size) {
            Log.e(TAG, "Too many middle packets. No NAL FU_A end packet received. Skipped RTP packet.")
            fragmentedBuffer[0] = null
        } else {
            fragmentedBufferLength += length - 2
            fragmentedBuffer[fragmentedPackets] = ByteArray(length - 2)
            System.arraycopy(data, 2, fragmentedBuffer[fragmentedPackets]!!, 0, length - 2)
        }
    }

    private fun addEndFragmentedPacketAndCombine(data: ByteArray, length: Int): ByteArray? {
        if (DEBUG) Log.v(TAG, "addEndFragmentedPacketAndCombine(data.size=${data.size}, length=$length)")
        var nalUnit: ByteArray? = null
        var tmpLen: Int
        if (fragmentedBuffer[0] == null) {
            Log.e(TAG, "No NAL FU_A start packet received. Skipped RTP packet.")
        } else {
            nalUnit = ByteArray(fragmentedBufferLength + length + 2)
            writeNalPrefix0001(nalUnit)
            tmpLen = 4
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
            System.arraycopy(data, 2, nalUnit, tmpLen, length - 2)
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
        private val TAG: String = RtpH264Parser::class.java.simpleName
        private const val DEBUG = false
    }

}
