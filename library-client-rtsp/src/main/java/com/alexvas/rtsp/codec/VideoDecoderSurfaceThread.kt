package com.alexvas.rtsp.codec

import android.media.MediaCodec
import android.media.MediaFormat
import android.util.Log
import android.view.Surface
import java.util.concurrent.TimeUnit
import kotlin.math.max

class VideoDecoderSurfaceThread(
    private val surface: Surface,
    mimeType: String,
    width: Int,
    height: Int,
    rotation: Int, // 0, 90, 180, 270
    videoFrameQueue: VideoFrameQueue,
    videoDecoderListener: VideoDecoderListener,
    videoDecoderType: DecoderType = DecoderType.HARDWARE,
    videoFrameRateStabilization: Boolean = false,
) : VideoDecodeThread(
    mimeType, width, height, rotation, videoFrameQueue, videoDecoderListener, videoDecoderType
) {

    /**
     * Presentation time (in RTP units converted to microseconds) of the first frame used as the
     * PTS baseline.
     */
    private var streamStartPtsUs: Long? = null

    /**
     * Monotonic clock timestamp corresponding to streamStartPtsUs, used to map future frames
     * to real time.
     */
    private var playbackStartRealtimeNs: Long? = null

    /**
     * Timestamp of the most recently rendered (not dropped) frame to enforce minimum spacing between
     * consecutive frames and to limit how long frames can be dropped in a row.
     */
    private var lastFrameReleaseTimeNs: Long = Long.MIN_VALUE

    /**
     * Last presentation timestamp we processed; used to detect wrap-around or backwards jumps.
     */
    private var lastPresentationTimeUs: Long = Long.MIN_VALUE

    init {
        setVideoFrameRateStabilization(videoFrameRateStabilization)
    }

    override fun decoderCreated(mediaCodec: MediaCodec, mediaFormat: MediaFormat) {
        if (DEBUG) Log.v(TAG, "decoderCreated()")
        if (!surface.isValid) {
            Log.e(TAG, "Surface invalid")
        }
        mediaCodec.configure(mediaFormat, surface, null, 0)
        resetFrameTiming()
    }

    private fun releaseOutputBufferWithFrameRateStabilization(
        mediaCodec: MediaCodec,
        outIndex: Int,
        bufferInfo: MediaCodec.BufferInfo
    ) {
        if (DEBUG) Log.v(TAG, "releaseOutputBufferWithFrameRateStabilization(outIndex=$outIndex)")

        val ptsUs = bufferInfo.presentationTimeUs
        val nowNs = System.nanoTime()

        if (streamStartPtsUs == null || playbackStartRealtimeNs == null) {
            // First frame (or after a reset): initialize all timing anchors.
            streamStartPtsUs = ptsUs
            playbackStartRealtimeNs = nowNs
            lastFrameReleaseTimeNs = nowNs
            lastPresentationTimeUs = ptsUs
            mediaCodec.releaseOutputBuffer(outIndex, nowNs)
            return
        }

        var targetNs = playbackStartRealtimeNs!! + (ptsUs - streamStartPtsUs!!) * 1000L
        var adjustedNowNs = System.nanoTime()

        if (lastPresentationTimeUs != Long.MIN_VALUE && ptsUs < lastPresentationTimeUs) {
            // PTS went backwards (e.g. codec reordering). Re-base the clock to avoid negative deltas.
            streamStartPtsUs = ptsUs
            playbackStartRealtimeNs = adjustedNowNs
            targetNs = adjustedNowNs
        }

        if (targetNs - adjustedNowNs > MAX_EARLY_NS) {
            // Frame scheduled too far in the future (PTS jumped forward, camera clock runs faster
            // than device one, backlog after network stall). Re-base the clock to keep latency bounded.
            streamStartPtsUs = ptsUs
            playbackStartRealtimeNs = adjustedNowNs
            targetNs = adjustedNowNs
        }

        if (lastFrameReleaseTimeNs != Long.MIN_VALUE) {
            // Ensure we never schedule two frames closer together than the min spacing.
            targetNs = max(targetNs, lastFrameReleaseTimeNs + MIN_FRAME_SPACING_NS)
        }

        adjustedNowNs = System.nanoTime()
        val latenessNs = adjustedNowNs - targetNs

        if (latenessNs >= FRAME_DROP_THRESHOLD_NS) {
            if (videoFrameQueue.size > 0 && adjustedNowNs - lastFrameReleaseTimeNs < MAX_DROP_DURATION_NS) {
                // Frame is critically late and there is a backlog of frames. Drop it and keep the
                // time base, so that next frames get closer to real time and playback catches up with live.
                mediaCodec.releaseOutputBuffer(outIndex, false)
                lastPresentationTimeUs = ptsUs
                return
            }
            // No backlog (or dropping for too long): delay became persistent, nothing to catch up.
            // Render this (most recent) frame immediately and re-base the clock to it, otherwise all
            // subsequent frames remain late and get dropped forever.
            streamStartPtsUs = ptsUs
            playbackStartRealtimeNs = adjustedNowNs
            mediaCodec.releaseOutputBuffer(outIndex, true)
            lastFrameReleaseTimeNs = adjustedNowNs
            lastPresentationTimeUs = ptsUs
            return
        }

        var correctedTargetNs = targetNs
        if (latenessNs > 0) {
            // For mild lateness (always below FRAME_DROP_THRESHOLD_NS here), shift the playback
            // baseline forward so future frames stay aligned.
            playbackStartRealtimeNs = playbackStartRealtimeNs?.plus(latenessNs)
            correctedTargetNs += latenessNs
        }

        if (correctedTargetNs <= adjustedNowNs + RENDER_EARLY_MARGIN_NS) {
            // Already at/behind the target time: render immediately using the current VSYNC.
            mediaCodec.releaseOutputBuffer(outIndex, true)
            lastFrameReleaseTimeNs = adjustedNowNs
        } else {
            // Still early enough: hand the desired release timestamp to MediaCodec for VSYNC alignment.
            mediaCodec.releaseOutputBuffer(outIndex, correctedTargetNs)
            lastFrameReleaseTimeNs = correctedTargetNs
        }

        lastPresentationTimeUs = ptsUs
    }

    override fun releaseOutputBuffer(
        mediaCodec: MediaCodec,
        outIndex: Int,
        bufferInfo: MediaCodec.BufferInfo,
        render: Boolean
    ) {
        if (DEBUG) Log.v(TAG, "releaseOutputBuffer(outIndex=$outIndex, render=$render)")
        if (!render || !surface.isValid) {
            mediaCodec.releaseOutputBuffer(outIndex, false)
            return
        }

        if (!hasVideoFrameRateStabilization()) {
            // Stabilization can be re-enabled later. Start timing from scratch then, not from stale anchors.
            if (streamStartPtsUs != null) resetFrameTiming()
            mediaCodec.releaseOutputBuffer(outIndex, true)
        } else {
            releaseOutputBufferWithFrameRateStabilization(mediaCodec, outIndex, bufferInfo)
        }
    }

    override fun decoderDestroyed(mediaCodec: MediaCodec) {
        if (DEBUG) Log.v(TAG, "decoderDestroyed()")
        resetFrameTiming()
    }

    private fun resetFrameTiming() {
        if (DEBUG) Log.v(TAG, "resetFrameTiming()")
        streamStartPtsUs = null
        playbackStartRealtimeNs = null
        lastFrameReleaseTimeNs = Long.MIN_VALUE
        lastPresentationTimeUs = Long.MIN_VALUE
    }

    companion object {
        private val FRAME_DROP_THRESHOLD_NS = TimeUnit.MILLISECONDS.toNanos(80)
        /** Max time a frame can be scheduled ahead of now. Bounds latency added by stabilization. */
        private val MAX_EARLY_NS = TimeUnit.MILLISECONDS.toNanos(150)
        /** Max time late frames can be dropped in a row while catching up with live. */
        private val MAX_DROP_DURATION_NS = TimeUnit.MILLISECONDS.toNanos(500)
        private val MIN_FRAME_SPACING_NS = TimeUnit.MILLISECONDS.toNanos(1)
        private val RENDER_EARLY_MARGIN_NS = TimeUnit.MILLISECONDS.toNanos(2)
    }

}
