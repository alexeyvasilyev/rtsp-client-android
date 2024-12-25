package com.alexvas.utils

import android.annotation.SuppressLint
import android.util.Log
import androidx.media3.container.NalUnitUtil
import androidx.media3.container.NalUnitUtil.SpsData
import java.util.concurrent.atomic.AtomicInteger
import kotlin.experimental.and


object VideoCodecUtils {

    private val TAG = VideoCodecUtils::class.java.simpleName

    /** Max possible NAL SPS size in bytes */
    const val MAX_NAL_SPS_SIZE:     Int = 500

    const val NAL_SLICE:            Byte = 1
    const val NAL_DPA:              Byte = 2
    const val NAL_DPB:              Byte = 3
    const val NAL_DPC:              Byte = 4
    const val NAL_IDR_SLICE:        Byte = 5
    const val NAL_SEI:              Byte = 6
    const val NAL_SPS:              Byte = 7
    const val NAL_PPS:              Byte = 8
    const val NAL_AUD:              Byte = 9
    const val NAL_END_SEQUENCE:     Byte = 10
    const val NAL_END_STREAM:       Byte = 11
    const val NAL_FILLER_DATA:      Byte = 12
    const val NAL_SPS_EXT:          Byte = 13
    const val NAL_AUXILIARY_SLICE:  Byte = 19
    const val NAL_STAP_A:           Byte = 24 // https://tools.ietf.org/html/rfc3984 5.7.1
    const val NAL_STAP_B:           Byte = 25 // 5.7.1
    const val NAL_MTAP16:           Byte = 26 // 5.7.2
    const val NAL_MTAP24:           Byte = 27 // 5.7.2
    const val NAL_FU_A:             Byte = 28 // 5.8 fragmented unit
    const val NAL_FU_B:             Byte = 29 // 5.8

    // Table 7-3: NAL unit type codes
    const val H265_NAL_TRAIL_N:     Byte = 0
    const val H265_NAL_TRAIL_R:     Byte = 1
    const val H265_NAL_TSA_N:       Byte = 2
    const val H265_NAL_TSA_R:       Byte = 3
    const val H265_NAL_STSA_N:      Byte = 4
    const val H265_NAL_STSA_R:      Byte = 5
    const val H265_NAL_RADL_N:      Byte = 6
    const val H265_NAL_RADL_R:      Byte = 7
    const val H265_NAL_RASL_N:      Byte = 8
    const val H265_NAL_RASL_R:      Byte = 9
    const val H265_NAL_BLA_W_LP:    Byte = 16
    const val H265_NAL_BLA_W_RADL:  Byte = 17
    const val H265_NAL_BLA_N_LP:    Byte = 18
    const val H265_NAL_IDR_W_RADL:  Byte = 19
    const val H265_NAL_IDR_N_LP:    Byte = 20
    const val H265_NAL_CRA_NUT:     Byte = 21
    const val H265_NAL_VPS:         Byte = 32
    const val H265_NAL_SPS:         Byte = 33
    const val H265_NAL_PPS:         Byte = 34
    const val H265_NAL_AUD:         Byte = 35
    const val H265_NAL_EOS_NUT:     Byte = 36
    const val H265_NAL_EOB_NUT:     Byte = 37
    const val H265_NAL_FD_NUT:      Byte = 38
    const val H265_NAL_SEI_PREFIX:  Byte = 39
    const val H265_NAL_SEI_SUFFIX:  Byte = 40

    private val NAL_PREFIX1 = byteArrayOf(0x00, 0x00, 0x00, 0x01)
    private val NAL_PREFIX2 = byteArrayOf(0x00, 0x00, 0x01)


    /**
     * Search for 00 00 01 or 00 00 00 01 in byte stream.
     * @return offset to the start of NAL unit if found, otherwise -1
     */
    fun searchForNalUnitStart(
        data: ByteArray,
        offset: Int,
        length: Int,
        prefixSize: AtomicInteger
    ): Int {
        if (offset >= data.size - 3) return -1
        for (pos in 0 until length) {
            val prefix: Int = getNalUnitStartCodePrefixSize(data, pos + offset, length)
            if (prefix >= 0) {
                prefixSize.set(prefix)
                return pos + offset
            }
        }
        return -1
    }

    fun searchForH264NalUnitByType(
        data: ByteArray,
        offset: Int,
        length: Int,
        byUnitType: Int
    ): Int {
        var off = offset
        val nalUnitPrefixSize = AtomicInteger(-1)
        val timestamp = System.currentTimeMillis()
        while (true) {
            val nalUnitIndex = searchForNalUnitStart(data, off, length, nalUnitPrefixSize)
            if (nalUnitIndex >= 0) {
                val nalUnitOffset = nalUnitIndex + nalUnitPrefixSize.get()
                if (nalUnitOffset >= data.size)
                    break
                val nalUnitTypeOctet = data[nalUnitOffset]
                if ((nalUnitTypeOctet and 0x1f).toInt() == byUnitType) {
                    return nalUnitIndex
                }
                off = nalUnitOffset

                // Check that we are not too long here
                if (System.currentTimeMillis() - timestamp > 100) {
                    Log.w(TAG, "Cannot process data within 100 msec in $length bytes")
                    break
                }
            } else {
                break
            }
        }
        return -1
    }

    fun getNalUnitType(data: ByteArray?, offset: Int, length: Int, isH265: Boolean): Byte {
        if (data == null || length <= NAL_PREFIX1.size) return (-1).toByte()
        var nalUnitTypeOctetOffset = -1
        if (data[offset + NAL_PREFIX2.size - 1] == 1.toByte())
            nalUnitTypeOctetOffset =
                offset + NAL_PREFIX2.size - 1
        else if (data[offset + NAL_PREFIX1.size - 1] == 1.toByte())
            nalUnitTypeOctetOffset = offset + NAL_PREFIX1.size - 1

        return if (nalUnitTypeOctetOffset != -1) {
            val nalUnitTypeOctet = data[nalUnitTypeOctetOffset + 1]
            if (isH265)
                ((nalUnitTypeOctet.toInt() shr 1) and 0x3F).toByte()
            else
                (nalUnitTypeOctet and 0x1f)
        } else {
            (-1).toByte()
        }
    }

    private fun getNalUnitStartCodePrefixSize(
        data: ByteArray,
        offset: Int,
        length: Int
    ): Int {
        if (length < 4) return -1
        return if (memcmp(data, offset, NAL_PREFIX1, 0, NAL_PREFIX1.size))
            NAL_PREFIX1.size else
            if (memcmp(data, offset, NAL_PREFIX2, 0, NAL_PREFIX2.size))
                NAL_PREFIX2.size else
                -1
    }

    private fun memcmp(
        source1: ByteArray,
        offsetSource1: Int,
        source2: ByteArray,
        offsetSource2: Int,
        num: Int
    ): Boolean {
        if (source1.size - offsetSource1 < num) return false
        if (source2.size - offsetSource2 < num) return false
        for (i in 0 until num) {
            if (source1[offsetSource1 + i] != source2[offsetSource2 + i]) return false
        }
        return true
    }

    data class NalUnit (val type: Byte, val offset: Int, val length: Int)


    fun getNalUnits(
        data: ByteArray,
        dataOffset: Int,
        length: Int,
        foundNals: ArrayList<NalUnit>,
        isH265: Boolean
    ): Int {
        foundNals.clear()
        var nalUnits = 0
        val nextNalOffset = 0
        val nalUnitPrefixSize = AtomicInteger(-1)
        val timestamp = System.currentTimeMillis()
        var offset = dataOffset
        var stopped = false
        while (!stopped) {

            // Search for first NAL unit
            val nalUnitIndex = searchForNalUnitStart(
                data,
                offset + nextNalOffset,
                length - nextNalOffset,
                nalUnitPrefixSize
            )

            // NAL unit found
            if (nalUnitIndex >= 0) {
                nalUnits++
                val nalUnitOffset = offset + nextNalOffset + nalUnitPrefixSize.get()
                val nalUnitTypeOctet = data[nalUnitOffset]
                val nalUnitType = if (isH265)
                    ((nalUnitTypeOctet.toInt() shr 1) and 0x3F).toByte()
                else
                    (nalUnitTypeOctet and 0x1F)

                // Search for second NAL unit (optional)
                var nextNalUnitStartIndex = searchForNalUnitStart(
                    data,
                    nalUnitOffset,
                    length - nalUnitOffset,
                    nalUnitPrefixSize
                )

                // Second NAL unit not found. Use till the end.
                if (nextNalUnitStartIndex < 0) {
                    // Not found next NAL unit. Use till the end.
//                  nextNalUnitStartIndex = length - nextNalOffset + dataOffset;
                    nextNalUnitStartIndex = length + dataOffset
                    stopped = true
                }
                val l = nextNalUnitStartIndex - offset
//                if (DEBUG) Log.d(
//                    TAG,
//                    "NAL unit type: " + getH264NalUnitTypeString(nalUnitType.toInt()) +
//                            " (" + nalUnitType + ") - " + l + " bytes, offset " + offset
//                )
                foundNals.add(NalUnit(nalUnitType, offset, l))
                offset = nextNalUnitStartIndex

                // Check that we are not too long here
                if (System.currentTimeMillis() - timestamp > 200) {
                    Log.w(TAG, "Cannot process data within 200 msec in $length bytes (NALs found: " + foundNals.size + ")")
                    break
                }
            } else {
                stopped = true
            }
        }
        return nalUnits
    }

    private fun getNalUnitStartLengthFromArray(
        src: ByteArray, offset: Int, length: Int,
        isH265: Boolean,
        nalUnitType: Byte
    ): Pair<Int, Int>? {
        val nalUnitsFound = ArrayList<NalUnit>()
        if (getNalUnits(src, offset, length, nalUnitsFound, isH265) > 0) {
            for (nalUnit in nalUnitsFound) {
                if (nalUnit.type == nalUnitType) {
                    val prefixSize = AtomicInteger()
                    val nalUnitIndex = searchForNalUnitStart(
                        src,
                        nalUnit.offset,
                        nalUnit.length,
                        prefixSize
                    )
                    val nalOffset = nalUnitIndex + prefixSize.get() + 1 /* NAL unit type */
                    return Pair(nalOffset, nalUnit.length)
                }
            }
        }
        return null
    }

    @SuppressLint("UnsafeOptInUsageError")
    fun getSpsNalUnitFromArray(src: ByteArray, offset: Int, length: Int, isH265: Boolean): SpsData? {
        val spsStartLength = getNalUnitStartLengthFromArray(src, offset, length, isH265, NAL_SPS)
        spsStartLength?.let {
            return NalUnitUtil.parseSpsNalUnitPayload(
                src, spsStartLength.first, spsStartLength.first + spsStartLength.second)
        }
        return null
    }

    @SuppressLint("UnsafeOptInUsageError")
    fun getWidthHeightFromArray(src: ByteArray, offset: Int, length: Int, isH265: Boolean): Pair<Int, Int>? {
        val sps = getSpsNalUnitFromArray(src, offset, length, isH265)
        sps?.let {
            return Pair(sps.width, sps.height)
        }
        return null
    }


//    private fun isH265IRAP(nalUnitType: Byte): Boolean {
//        return nalUnitType in 16..23
//    }

    fun isAnyKeyFrame(data: ByteArray?, offset: Int, length: Int, isH265: Boolean): Boolean {
        if (data == null || length <= 0) return false
        var currOffset = offset

        val nalUnitPrefixSize = AtomicInteger(-1)
        val timestamp = System.currentTimeMillis()
        while (true) {
            val nalUnitIndex = searchForNalUnitStart(
                data,
                currOffset,
                length,
                nalUnitPrefixSize
            )

            if (nalUnitIndex >= 0) {
                val nalUnitOffset = nalUnitIndex + nalUnitPrefixSize.get()
                if (nalUnitOffset >= data.size)
                    return false
                val nalUnitTypeOctet = data[nalUnitOffset]

                if (isH265) {
                    val nalUnitType = ((nalUnitTypeOctet.toInt() and 0x7E) shr 1).toByte()
                    // Treat SEI_PREFIX as key frame.
                    if (nalUnitType == H265_NAL_IDR_W_RADL || nalUnitType == H265_NAL_IDR_N_LP)
                        return true
                } else {
                    val nalUnitType = (nalUnitTypeOctet.toInt() and 0x1f).toByte()
                    when (nalUnitType) {
                        NAL_IDR_SLICE -> return true
                        NAL_SLICE -> return false
                    }
                }
                // Continue searching
                currOffset = nalUnitOffset

                // Check that we are not too long here
                if (System.currentTimeMillis() - timestamp > 100) {
                    Log.w(TAG, "Cannot process data within 100 msec in $length bytes (index=$nalUnitIndex)")
                    break
                }
            } else {
                break
            }
        }

        return false
    }

    fun getH264NalUnitTypeString(nalUnitType: Byte): String {
        return when (nalUnitType) {
            NAL_SLICE -> "NAL_SLICE"
            NAL_DPA -> "NAL_DPA"
            NAL_DPB -> "NAL_DPB"
            NAL_DPC -> "NAL_DPC"
            NAL_IDR_SLICE -> "NAL_IDR_SLICE"
            NAL_SEI -> "NAL_SEI"
            NAL_SPS -> "NAL_SPS"
            NAL_PPS -> "NAL_PPS"
            NAL_AUD -> "NAL_AUD"
            NAL_END_SEQUENCE -> "NAL_END_SEQUENCE"
            NAL_END_STREAM -> "NAL_END_STREAM"
            NAL_FILLER_DATA -> "NAL_FILLER_DATA"
            NAL_SPS_EXT -> "NAL_SPS_EXT"
            NAL_AUXILIARY_SLICE -> "NAL_AUXILIARY_SLICE"
            NAL_STAP_A -> "NAL_STAP_A"
            NAL_STAP_B -> "NAL_STAP_B"
            NAL_MTAP16 -> "NAL_MTAP16"
            NAL_MTAP24 -> "NAL_MTAP24"
            NAL_FU_A -> "NAL_FU_A"
            NAL_FU_B -> "NAL_FU_B"
            else -> "unknown - $nalUnitType"
        }
    }

    fun getH265NalUnitTypeString(nalUnitType: Byte): String {
        return when (nalUnitType) {
            H265_NAL_TRAIL_N -> "NAL_TRAIL_N"
            H265_NAL_TRAIL_R -> "NAL_TRAIL_R"
            H265_NAL_TSA_N -> "NAL_TSA_N"
            H265_NAL_TSA_R -> "NAL_TSA_R"
            H265_NAL_STSA_N -> "NAL_STSA_N"
            H265_NAL_STSA_R -> "NAL_STSA_R"
            H265_NAL_RADL_N -> "NAL_RADL_N"
            H265_NAL_RADL_R -> "NAL_RADL_R"
            H265_NAL_RASL_N -> "NAL_RASL_N"
            H265_NAL_RASL_R -> "NAL_RASL_R"
            H265_NAL_BLA_W_LP -> "NAL_BLA_W_LP"
            H265_NAL_BLA_W_RADL -> "NAL_BLA_W_RADL"
            H265_NAL_BLA_N_LP -> "NAL_BLA_N_LP"
            H265_NAL_IDR_W_RADL -> "NAL_IDR_W_RADL"
            H265_NAL_IDR_N_LP -> "NAL_IDR_N_LP"
            H265_NAL_CRA_NUT -> "NAL_CRA_NUT"
            H265_NAL_VPS -> "NAL_VPS"
            H265_NAL_SPS -> "NAL_SPS"
            H265_NAL_PPS -> "NAL_PPS"
            H265_NAL_AUD -> "NAL_AUD"
            H265_NAL_EOS_NUT -> "NAL_EOS_NUT"
            H265_NAL_EOB_NUT -> "NAL_EOB_NUT"
            H265_NAL_FD_NUT -> "NAL_FD_NUT"
            H265_NAL_SEI_PREFIX -> "NAL_SEI_PREFIX"
            H265_NAL_SEI_SUFFIX -> "NAL_SEI_SUFFIX"
            else -> "unknown - $nalUnitType"
        }
    }

}
