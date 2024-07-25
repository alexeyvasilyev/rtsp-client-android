package com.alexvas.utils

import android.util.Log
import android.util.Range
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.mediacodec.MediaCodecInfo
import androidx.media3.exoplayer.mediacodec.MediaCodecUtil
import java.lang.Exception

object MediaCodecUtils {

    // key - codecs mime type
    // value - list of codecs able to handle this mime type
    private val decoderInfosMap = HashMap<String, List<MediaCodecInfo>>()

    private val TAG: String = MediaCodecUtils::class.java.simpleName

    private fun getDecoderInfos(mimeType: String): List<MediaCodecInfo> {
        val list = decoderInfosMap[mimeType]
        return if (list.isNullOrEmpty()) {
            val decoderInfos = try {
                MediaCodecUtil.getDecoderInfos(mimeType, false, false)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize '$mimeType' decoders list (${e.message})", e)
                ArrayList()
            }
            decoderInfosMap[mimeType] = decoderInfos
            decoderInfos
        } else {
            list
        }
    }

    /**
     * Get software decoders list. Usually used as fallback.
     */
    @Synchronized
    fun getSoftwareDecoders(mimeType: String): List<MediaCodecInfo> {
        val decoderInfos = getDecoderInfos(mimeType)
        val list = ArrayList<MediaCodecInfo>()
        for (codec in decoderInfos) {
            if (codec.softwareOnly)
                list.add(codec)
        }
        return list
    }

    /**
     * Get hardware accelerated decoders list. Used as default.
     */
    @Synchronized
    fun getHardwareDecoders(mimeType: String): List<MediaCodecInfo> {
        val decoderInfos = getDecoderInfos(mimeType)
        val list = ArrayList<MediaCodecInfo>()
        for (codec in decoderInfos) {
            if (codec.hardwareAccelerated)
                list.add(codec)
        }
        return list
    }

    /**
     * Look through all decoders (if there are multiple)
     * and select the one which supports low-latency.
     */
    @OptIn(UnstableApi::class)
    fun getLowLatencyDecoder(decoders: List<MediaCodecInfo>): MediaCodecInfo? {
        // Some devices can have several decoders, e.g.
        // Samsung Fold 5:
        //   "c2.qti.avc.decoder"
        //   "c2.qti.avc.decoder.low_latency"
        for (decoder in decoders) {
            if (decoder.name.contains("low_latency"))
                return decoder
        }
        // Another approach to find decoder with low-latency is to call
        // MediaCodec.createByCodecName(name) for every decoder to get decoder instance and then call
        // decoder.codecInfo.getCapabilitiesForType(mimeType).isFeatureSupported(MediaCodecInfo.CodecCapabilities.FEATURE_LowLatency)

        // No low-latency decoder found.
        return null
    }

}

fun android.media.MediaCodecInfo.CodecCapabilities.capabilitiesToString(): String {
    var heights = videoCapabilities?.supportedHeights
    if (heights == null)
        heights = Range(-1, -1)
    var widths = videoCapabilities?.supportedWidths
    if (widths == null)
        widths = Range(-1, -1)
    return "max instances: ${maxSupportedInstances}, max resolution: ${heights.upper}x${widths.upper}"
}
