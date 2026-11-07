package com.alexvas.utils

import androidx.media3.container.NalUnitUtil
import org.junit.Assert.*
import org.junit.Test
import java.util.Base64

class VideoCodecUtilsTest {
    private val hevcSps = Base64.getDecoder().decode(
        "QgEBIUAAAAMAgAAAAwAAAwCWoAFQIAXxY2uSQiZL5NwUGBQQAAADABAAAAMA8IA="
    )
    private val avcSps = Base64.getDecoder().decode("Z01AKZpmBkCb8uAtQEBAQXpw")
    private val prefix = byteArrayOf(0, 0, 0, 1)

    @Test fun readsHevcCameraDimensionsAndProfile() {
        val data = prefix + hevcSps
        val sps = requireNotNull(VideoCodecUtils.getH265SpsNalUnitFromArray(data, 0, data.size))
        assertEquals(2688, sps.width)
        assertEquals(1520, sps.height)
        assertEquals(1, sps.profileTierLevel!!.generalProfileIdc)
        assertEquals(150, sps.profileTierLevel!!.generalLevelIdc)
        assertEquals(2688 to 1520, VideoCodecUtils.getWidthHeightFromArray(data, 0, data.size, true))
        assertNull(VideoCodecUtils.getSpsNalUnitFromArray(data, 0, data.size, true))
    }

    @Test fun readsHevcAfterVpsWithThreeBytePrefixesAndNonzeroOffset() {
        val vps = Base64.getDecoder().decode("QAEMAf//IUAAAAMAgAAAAwAAAwCWrAk=")
        val shortPrefix = byteArrayOf(0, 0, 1)
        val frame = shortPrefix + vps + shortPrefix + hevcSps + shortPrefix + byteArrayOf(0x44, 1, 0)
        val data = ByteArray(13) + frame + ByteArray(7)
        assertEquals(2688 to 1520, VideoCodecUtils.getWidthHeightFromArray(data, 13, frame.size, true))
    }

    @Test fun preservesAvcParsing() {
        val frame = prefix + avcSps + prefix + byteArrayOf(0x68, 0)
        val data = ByteArray(9) + frame
        val expected = NalUnitUtil.parseSpsNalUnit(avcSps, 0, avcSps.size)
        val actual = requireNotNull(VideoCodecUtils.getSpsNalUnitFromArray(data, 9, frame.size, false))
        assertEquals(expected.width, actual.width)
        assertEquals(expected.height, actual.height)
        assertEquals(expected.profileIdc, actual.profileIdc)
        assertEquals(expected.levelIdc, actual.levelIdc)
        assertEquals(expected.width to expected.height,
            VideoCodecUtils.getWidthHeightFromArray(data, 9, frame.size, false))
    }

    @Test fun doesNotReadSpsOutsideRequestedRange() {
        val leadingNal = prefix + byteArrayOf(0x44, 1, 0)
        val data = leadingNal + prefix + hevcSps
        assertNull(VideoCodecUtils.getH265SpsNalUnitFromArray(data, 0, leadingNal.size))
        // The requested slice ends halfway through the following start code.
        assertNull(VideoCodecUtils.getH265SpsNalUnitFromArray(data, 0, leadingNal.size + 2))
    }

    // SEI (6 bytes) followed by IDR (5 bytes).
    private val seiIdr = prefix + byteArrayOf(0x06, 0x55) + prefix + byteArrayOf(0x65)

    @Test fun isAnyKeyFrameStaysInsideRequestedRange() {
        assertFalse(VideoCodecUtils.isAnyKeyFrame(seiIdr, 0, 6, false))
        assertTrue(VideoCodecUtils.isAnyKeyFrame(seiIdr, 0, seiIdr.size, false))
        // Start code at the very end of the range, its NAL header is outside.
        assertFalse(VideoCodecUtils.isAnyKeyFrame(prefix + byteArrayOf(0x65), 0, 4, false))
    }

    @Test fun searchByTypeStaysInsideRequestedRange() {
        assertEquals(-1, VideoCodecUtils.searchForH264NalUnitByType(seiIdr, 0, 6, 5))
        assertEquals(6, VideoCodecUtils.searchForH264NalUnitByType(seiIdr, 0, seiIdr.size, 5))
    }

    @Test fun getNalUnitsStaysInsideRequestedRange() {
        val found = ArrayList<VideoCodecUtils.NalUnit>()
        assertEquals(1, VideoCodecUtils.getNalUnits(seiIdr, 0, 6, found, false))
        assertEquals(listOf(VideoCodecUtils.NalUnit(6, 0, 6)), found)
    }

    @Test fun getNalUnitsFindsAllUnitsAtNonzeroOffset() {
        val found = ArrayList<VideoCodecUtils.NalUnit>()
        val data = ByteArray(13) + seiIdr + ByteArray(7)
        assertEquals(2, VideoCodecUtils.getNalUnits(data, 13, seiIdr.size, found, false))
        assertEquals(listOf(
            VideoCodecUtils.NalUnit(6, 13, 6),
            VideoCodecUtils.NalUnit(5, 19, 5),
        ), found)
    }

    @Test fun getNalUnitsIgnoresTrailingStartCode() {
        val found = ArrayList<VideoCodecUtils.NalUnit>()
        val data = prefix + byteArrayOf(0x09) + prefix
        assertEquals(1, VideoCodecUtils.getNalUnits(data, 0, data.size, found, false))
        assertEquals(listOf(VideoCodecUtils.NalUnit(9, 0, 5)), found)
    }

    @Test fun missingAndIncompleteNalHeadersReturnNull() {
        assertNull(VideoCodecUtils.getH265SpsNalUnitFromArray(byteArrayOf(), 0, 0))
        val data = prefix + byteArrayOf(0x42)
        assertNull(VideoCodecUtils.getH265SpsNalUnitFromArray(data, 0, data.size))
    }
}
