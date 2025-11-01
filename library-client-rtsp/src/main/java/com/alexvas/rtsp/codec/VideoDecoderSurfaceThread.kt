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
    enableVideoStabilization: Boolean = true,
) : VideoDecodeThread(
    mimeType, width, height, rotation, videoFrameQueue, videoDecoderListener, videoDecoderType
){

    init {
        setVideoStabilizationEnabled(enableVideoStabilization)
    }

    override fun decoderCreated(mediaCodec: MediaCodec, mediaFormat: MediaFormat) {
        if (!surface.isValid) {
            Log.e(TAG, "Surface invalid")
        }
        mediaCodec.configure(mediaFormat, surface, null, 0)
        resetFrameTiming()
    }

    override fun releaseOutputBuffer(
        mediaCodec: MediaCodec,
        outIndex: Int,
        bufferInfo: MediaCodec.BufferInfo,
        render: Boolean
    ) {
        if (!render || !surface.isValid) {
            mediaCodec.releaseOutputBuffer(outIndex, false)
            return
        }

        if (!isVideoStabilizationEnabled()) {
            mediaCodec.releaseOutputBuffer(outIndex, true)
            return
        }

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

        if (lastFrameReleaseTimeNs != Long.MIN_VALUE) {
            // Ensure we never schedule two frames closer together than the min spacing.
            targetNs = max(targetNs, lastFrameReleaseTimeNs + MIN_FRAME_SPACING_NS)
        }

        adjustedNowNs = System.nanoTime()
        val latenessNs = adjustedNowNs - targetNs

        if (latenessNs >= FRAME_DROP_THRESHOLD_NS) {
            // Frame is critically late; drop to keep playback responsive.
            mediaCodec.releaseOutputBuffer(outIndex, false)
            lastFrameReleaseTimeNs = adjustedNowNs
            return
        }

        var correctedTargetNs = targetNs
        if (latenessNs > 0) {
            // For mild lateness, shift the playback baseline forward so future frames stay aligned.
            val correction = minOf(latenessNs, FRAME_DROP_THRESHOLD_NS)
            playbackStartRealtimeNs = playbackStartRealtimeNs?.plus(correction)
            correctedTargetNs += correction
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

    override fun decoderDestroyed(mediaCodec: MediaCodec) {
        resetFrameTiming()
    }

    private fun resetFrameTiming() {
        streamStartPtsUs = null
        playbackStartRealtimeNs = null
        lastFrameReleaseTimeNs = Long.MIN_VALUE
        lastPresentationTimeUs = Long.MIN_VALUE
    }

    // Presentation time (in RTP units converted to microseconds) of the first frame used as the PTS baseline.
    private var streamStartPtsUs: Long? = null

    // Monotonic clock timestamp corresponding to streamStartPtsUs, used to map future frames to real time.
    private var playbackStartRealtimeNs: Long? = null

    // Timestamp of the most recently released frame to enforce minimum spacing between consecutive frames.
    private var lastFrameReleaseTimeNs: Long = Long.MIN_VALUE

    // Last presentation timestamp we processed; used to detect wrap-around or backwards jumps.
    private var lastPresentationTimeUs: Long = Long.MIN_VALUE

    companion object {
        private val FRAME_DROP_THRESHOLD_NS = TimeUnit.MILLISECONDS.toNanos(80)
        private val MIN_FRAME_SPACING_NS = TimeUnit.MILLISECONDS.toNanos(1)
        private val RENDER_EARLY_MARGIN_NS = TimeUnit.MILLISECONDS.toNanos(2)
    }

}
