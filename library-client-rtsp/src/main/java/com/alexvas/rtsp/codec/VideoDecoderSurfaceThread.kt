package com.alexvas.rtsp.codec

import android.media.MediaCodec
import android.media.MediaFormat
import android.util.Log
import android.view.Surface

class VideoDecoderSurfaceThread(
    private val surface: Surface,
    mimeType: String,
    width: Int,
    height: Int,
    rotation: Int, // 0, 90, 180, 270
    videoFrameQueue: VideoFrameQueue,
    videoDecoderListener: VideoDecoderListener,
    videoDecoderType: DecoderType = DecoderType.HARDWARE,
    isDebounceEnable: Boolean = false
) : VideoDecodeThread(
    mimeType, width, height, rotation, videoFrameQueue, videoDecoderListener, videoDecoderType,
    isDebounceEnable
) {

    private val MIN_BUFFER_NS: Long = 60_000_000L    // 60ms
    private val MAX_BUFFER_NS: Long = 120_000_000L   // 120ms

    private var anchorNs: Long = -1
    private var bufferOffsetNs: Long = 90_000_000L   // 90ms

    override fun decoderCreated(mediaCodec: MediaCodec, mediaFormat: MediaFormat) {
        if (!surface.isValid) {
            Log.e(TAG, "Surface invalid")
        }
        mediaCodec.configure(mediaFormat, surface, null, 0)
    }

    override fun releaseOutputBuffer(
        mediaCodec: MediaCodec,
        outIndex: Int,
        info: MediaCodec.BufferInfo,
        render: Boolean
    ) {
        if (!render || !surface.isValid) {
            mediaCodec.releaseOutputBuffer(outIndex, false)
            return
        }

        if (!isDebounceEnable) {
            mediaCodec.releaseOutputBuffer(outIndex, true)
            return
        }

        val nowNs = System.nanoTime()
        val ptsNs = info.presentationTimeUs * 1000L

        if (anchorNs == -1L) {
            anchorNs = nowNs - ptsNs
        }

        val targetNs = anchorNs + ptsNs
        val scheduleNs = targetNs + bufferOffsetNs

        // On time / Early: Scheduled to reach the synthesizer as planned (the system automatically aligns with vsync).
        if (nowNs <= scheduleNs) {
            mediaCodec.releaseOutputBuffer(outIndex, scheduleNs)
            // If it frequently arrives too early, the buffer is slightly reduced (by 1 ms each time; it will not go below 50 ms).
            val earlyNs = scheduleNs - nowNs
            if (earlyNs > 30_000_000L) { // 提前超过 30ms，认为缓冲偏大
                bufferOffsetNs = (bufferOffsetNs - 1_000_000L).coerceAtLeast(MIN_BUFFER_NS)
            }
        } else {
            // Already late: Render immediately; at the same time, slightly increase the buffer (by 1 ms each time; it will not exceed 150 ms).
            // Slightly late: As long as it is “slightly late,” increase the buffer by 1 ms; if it is frequently “slightly late,”
            // the buffer will gradually increase until it becomes more stable.
            val lateness = nowNs - scheduleNs
            if (lateness >= 2_000_000L /* ≥2ms */) {
                bufferOffsetNs = (bufferOffsetNs + 1_000_000L).coerceAtMost(MAX_BUFFER_NS)
            }
            mediaCodec.releaseOutputBuffer(outIndex, true)
        }
    }

    override fun decoderDestroyed(mediaCodec: MediaCodec) {
    }

}
