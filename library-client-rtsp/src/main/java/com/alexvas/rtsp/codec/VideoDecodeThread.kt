package com.alexvas.rtsp.codec

import android.annotation.SuppressLint
import android.media.MediaCodec
import android.media.MediaCodec.OnFrameRenderedListener
import android.media.MediaFormat
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.Process
import android.util.Log
import com.alexvas.utils.MediaCodecUtils
import com.alexvas.utils.capabilitiesToString
import androidx.media3.common.util.Util
import java.nio.ByteBuffer
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

abstract class VideoDecodeThread (
    protected val mimeType: String,
    protected val width: Int,
    protected val height: Int,
    protected val rotation: Int, // 0, 90, 180, 270
    protected val videoFrameQueue: VideoFrameQueue,
    protected val videoDecoderListener: VideoDecoderListener,
    protected var videoDecoderType: DecoderType
) : Thread() {

    enum class DecoderType {
        HARDWARE,
        SOFTWARE // fallback
    }

    interface VideoDecoderListener {
        /** Video decoder successfully started */
        fun onVideoDecoderStarted() {}
        /** Video decoder successfully stopped */
        fun onVideoDecoderStopped() {}
        /** Fatal error occurred */
        fun onVideoDecoderFailed(message: String?) {}
        /** Resolution changed */
        fun onVideoDecoderFormatChanged(width: Int, height: Int) {}
        /** First video frame rendered */
        fun onVideoDecoderFirstFrameRendered() {}
    }

    protected val uiHandler = Handler(Looper.getMainLooper())
    protected var exitFlag = AtomicBoolean(false)
    protected var firstFrameRendered = false

    /** Decoder latency used for statistics */
    @Volatile private var decoderLatency = -1
    /** Flag for allowing calculating latency */
    private var decoderLatencyRequested = false
    /** Network latency used for statistics */
    @Volatile private var networkLatency = -1
    private var videoDecoderName: String? = null
    private var firstFrameDecoded = false

    fun stopAsync() {
        if (DEBUG) Log.v(TAG, "stopAsync()")
        exitFlag.set(true)
        // Wake up sleep() code
        interrupt()
    }

    /**
     * Currently used video decoder. Video decoder can be changed on runtime.
     * If videoDecoderType set to HARDWARE, it can be switched to SOFTWARE in case of decoding issue
     * (e.g. hardware decoder does not support the stream resolution).
     * If videoDecoderType set to SOFTWARE, it will always remain SOFTWARE (no any changes).
     */
    fun getCurrentVideoDecoderType(): DecoderType {
        return videoDecoderType
    }

    fun getCurrentVideoDecoderName(): String? {
        return videoDecoderName
    }

    /**
     * Get frames decoding/rendering latency in msec. Returns -1 if not supported.
     */
    fun getCurrentVideoDecoderLatencyMsec(): Int {
        decoderLatencyRequested = true
        return decoderLatency
    }

    /**
     * Get network latency in msec. Returns -1 if not supported.
     */
    fun getCurrentNetworkLatencyMsec(): Int {
        return networkLatency
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun getDecoderSafeWidthHeight(decoder: MediaCodec): Pair<Int, Int> {
        val capabilities = decoder.codecInfo.getCapabilitiesForType(mimeType).videoCapabilities
        return if (capabilities.isSizeSupported(width, height)) {
            Pair(width, height)
        } else {
            val widthAlignment = capabilities.widthAlignment
            val heightAlignment = capabilities.heightAlignment
            Pair(
                Util.ceilDivide(width, widthAlignment) * widthAlignment,
                Util.ceilDivide(height, heightAlignment) * heightAlignment)
        }
    }

    @SuppressLint("InlinedApi")
    private fun getWidthHeight(mediaFormat: MediaFormat): Pair<Int, Int> {
        // Sometimes height obtained via KEY_HEIGHT is not valid, e.g. can be 1088 instead 1080
        // (no problems with width though). Use crop parameters to correctly determine height.
        val hasCrop =
            mediaFormat.containsKey(MediaFormat.KEY_CROP_RIGHT) && mediaFormat.containsKey(MediaFormat.KEY_CROP_LEFT) &&
            mediaFormat.containsKey(MediaFormat.KEY_CROP_BOTTOM) && mediaFormat.containsKey(MediaFormat.KEY_CROP_TOP)
        val width =
            if (hasCrop)
                mediaFormat.getInteger(MediaFormat.KEY_CROP_RIGHT) - mediaFormat.getInteger(MediaFormat.KEY_CROP_LEFT) + 1
            else
                mediaFormat.getInteger(MediaFormat.KEY_WIDTH)
        var height =
            if (hasCrop)
                mediaFormat.getInteger(MediaFormat.KEY_CROP_BOTTOM) - mediaFormat.getInteger(MediaFormat.KEY_CROP_TOP) + 1
            else
                mediaFormat.getInteger(MediaFormat.KEY_HEIGHT)
        // Fix for 1080p resolution for Samsung S21
        // {crop-right=1919, max-height=4320, sar-width=1, color-format=2130708361, mime=video/raw,
        // hdr-static-info=java.nio.HeapByteBuffer[pos=0 lim=25 cap=25],
        // priority=0, color-standard=1, feature-secure-playback=0, color-transfer=3, sar-height=1,
        // crop-bottom=1087, max-width=8192, crop-left=0, width=1920, color-range=2, crop-top=0,
        // rotation-degrees=0, frame-rate=30, height=1088}
        height = height / 16 * 16 // 1088 -> 1080
//        if (height == 1088)
//            height = 1080
        return Pair(width, height)
    }

    private fun getDecoderMediaFormat(decoder: MediaCodec): MediaFormat {
        if (DEBUG) Log.v(TAG, "getDecoderMediaFormat()")
        val safeWidthHeight = getDecoderSafeWidthHeight(decoder)
        val format = MediaFormat.createVideoFormat(mimeType, safeWidthHeight.first, safeWidthHeight.second)
        if (DEBUG)
            Log.d(TAG, "Configuring surface ${safeWidthHeight.first}x${safeWidthHeight.second} w/ '$mimeType'")
        else
            Log.i(TAG, "Configuring surface ${safeWidthHeight.first}x${safeWidthHeight.second} w/ '$mimeType'")
        format.setInteger(MediaFormat.KEY_ROTATION, rotation)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // format.setFeatureEnabled(android.media.MediaCodecInfo.CodecCapabilities.FEATURE_LowLatency, true)
            // Request low-latency for the decoder. Not all of the decoders support that.
            format.setInteger(MediaFormat.KEY_LOW_LATENCY, 1)
        }
        return format
    }

    /** Decoder created */
    abstract fun decoderCreated(mediaCodec: MediaCodec, mediaFormat: MediaFormat)

    /** Frame processed */
    abstract fun releaseOutputBuffer(mediaCodec: MediaCodec, outIndex: Int, render: Boolean)

    /** Decoder stopped and released */
    abstract fun decoderDestroyed(mediaCodec: MediaCodec)

    private fun createVideoDecoderAndStart(decoderType: DecoderType): MediaCodec {
        if (DEBUG) Log.v(TAG, "createVideoDecoderAndStart(decoderType=$decoderType)")

        @SuppressLint("UnsafeOptInUsageError")
        val decoder = when (decoderType) {
            DecoderType.HARDWARE -> {
                val hwDecoders = MediaCodecUtils.getHardwareDecoders(mimeType)
                if (hwDecoders.isEmpty()) {
                    Log.w(TAG, "Cannot get hardware video decoders for mime type '$mimeType'. Using default one.")
                    MediaCodec.createDecoderByType(mimeType)
                } else {
                    val lowLatencyDecoder = MediaCodecUtils.getLowLatencyDecoder(hwDecoders)
                    val name = lowLatencyDecoder?.let {
                        Log.i(TAG, "[$name] Dedicated low-latency decoder found '${lowLatencyDecoder.name}'")
                        lowLatencyDecoder.name
                    } ?: hwDecoders[0].name
                    MediaCodec.createByCodecName(name)
                }
            }
            DecoderType.SOFTWARE -> {
                val swDecoders = MediaCodecUtils.getSoftwareDecoders(mimeType)
                if (swDecoders.isEmpty()) {
                    Log.w(TAG, "Cannot get software video decoders for mime type '$mimeType'. Using default one .")
                    MediaCodec.createDecoderByType(mimeType)
                } else {
                    val name = swDecoders[0].name
                    MediaCodec.createByCodecName(name)
                }
            }
        }
        this.videoDecoderType = decoderType
        this.videoDecoderName = decoder.name

        val frameRenderedListener = OnFrameRenderedListener { _, _, _ ->
            if (!firstFrameRendered) {
                firstFrameRendered = true
                uiHandler.post {
                    videoDecoderListener.onVideoDecoderFirstFrameRendered()
                }
            }
        }
        decoder.setOnFrameRenderedListener(frameRenderedListener, null)
        val format = getDecoderMediaFormat(decoder)
        decoderCreated(decoder, format)
        decoder.start()

        val capabilities = decoder.codecInfo.getCapabilitiesForType(mimeType)
        val lowLatencySupport = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            capabilities.isFeatureSupported(android.media.MediaCodecInfo.CodecCapabilities.FEATURE_LowLatency)
        } else {
            false
        }
        Log.i(TAG, "[$name] Video decoder '${decoder.name}' started " +
                "(${if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) { if (decoder.codecInfo.isHardwareAccelerated) "hardware" else "software" } else ""}, " +
                "${capabilities.capabilitiesToString()}, " +
                "${if (lowLatencySupport) "w/" else "w/o"} low-latency support)")

        return decoder
    }

    private fun stopAndReleaseVideoDecoder(decoder: MediaCodec) {
        if (DEBUG) Log.v(TAG, "stopAndReleaseVideoDecoder()")
        val type = videoDecoderType.toString().lowercase()
        Log.i(TAG, "Stopping $type video decoder...")
        try {
            decoder.stop()
            Log.i(TAG, "Decoder successfully stopped")
        } catch (e3: Throwable) {
            Log.e(TAG, "Failed to stop decoder", e3)
        }
        Log.i(TAG, "Releasing decoder...")
        try {
            decoder.release()
            Log.i(TAG, "Decoder successfully released")
        } catch (e3: Throwable) {
            Log.e(TAG, "Failed to release decoder", e3)
        }
        videoFrameQueue.clear()
        decoderDestroyed(decoder)
    }

    override fun run() {
        if (DEBUG) Log.d(TAG, "$name started")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            Process.setThreadPriority(Process.THREAD_PRIORITY_VIDEO)
        }

        videoDecoderListener.onVideoDecoderStarted()

        try {
            Log.i(TAG, "Starting hardware video decoder...")
            var decoder = try {
                createVideoDecoderAndStart(videoDecoderType)
            } catch (e: Throwable) {
                Log.e(TAG, "Failed to start $videoDecoderType video decoder (${e.message})", e)
                Log.i(TAG, "Starting software video decoder...")
                try {
                    createVideoDecoderAndStart(DecoderType.SOFTWARE)
                } catch (e2: Throwable) {
                    Log.e(TAG, "Failed to start video software decoder. Exiting...", e)
                    // Unexpected behavior
                    videoDecoderListener.onVideoDecoderFailed("Cannot initialize video decoder for mime type '$mimeType'")
                    return
                }
            }
            val bufferInfo = MediaCodec.BufferInfo()

            try {
                // Map for calculating decoder rendering latency.
                // key - original frame timestamp, value - timestamp when frame was added to the map
                val keyframesTimestamps = HashMap<Long, Long>()

                var frameQueuedMsec = System.currentTimeMillis()
                var frameAlreadyDequeued = false

                // Main loop
                while (!exitFlag.get()) {
                    try {
                        val inIndex: Int = decoder.dequeueInputBuffer(DEQUEUE_INPUT_TIMEOUT_US)
                        if (inIndex >= 0) {
                            // fill inputBuffers[inputBufferIndex] with valid data
                            val byteBuffer: ByteBuffer? = decoder.getInputBuffer(inIndex)
                            byteBuffer?.rewind()

                            // Preventing BufferOverflowException
                            // if (length > byteBuffer.limit()) throw DecoderFatalException("Error")

                            val frame = videoFrameQueue.pop()
                            if (frame == null) {
                                Log.d(TAG, "Empty video frame")
                                // Release input buffer
                                decoder.queueInputBuffer(inIndex, 0, 0, 0L, 0)
                            } else {
                                // Add timestamp for keyframe to calculating latency further.
                                if ((DEBUG || decoderLatencyRequested) && frame.isKeyframe) {
                                    if (keyframesTimestamps.size > 5) {
                                        // Something wrong with map. Allow only 5 map entries.
                                        keyframesTimestamps.clear()
                                    }
                                    val l = System.currentTimeMillis()
                                    keyframesTimestamps[frame.timestampMs] = l
//                                  Log.d(TAG, "Added $l")
                                }
                                // Calculate network latency
                                networkLatency = if (frame.capturedTimestampMs > -1)
                                    (frame.timestampMs - frame.capturedTimestampMs).toInt()
                                else
                                    -1

                                byteBuffer?.put(frame.data, frame.offset, frame.length)
                                if (DEBUG) {
                                    val l = System.currentTimeMillis()
                                    Log.i(TAG, "\tFrame queued (${l - frameQueuedMsec}) ${if (frame.isKeyframe) "key frame" else ""}")
                                    frameQueuedMsec = l
                                }
                                decoder.queueInputBuffer(inIndex, frame.offset, frame.length, frame.timestampMs, 0)
                            }
                        }

                        if (exitFlag.get()) break

                        // Get all output buffer frames until no buffer from decoder available (INFO_TRY_AGAIN_LATER).
                        // Single input buffer frame can contain several frames, e.g. SPS + PPS + IDR.
                        // Thus dequeueOutputBuffer should be called several times.
                        // First time it obtains SPS + PPS, second one - IDR frame.
                        do {
                            // For the first time wait for a frame within 100 msec, next times no timeout
                            val timeout = if (frameAlreadyDequeued || !firstFrameDecoded) 0L else DEQUEUE_OUTPUT_BUFFER_TIMEOUT_US
                            val outIndex = decoder.dequeueOutputBuffer(bufferInfo, timeout)
                            when (outIndex) {
                                // Resolution changed
                                MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED, MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                                    Log.d(TAG, "Decoder format changed: ${decoder.outputFormat}")
                                    val widthHeight = getWidthHeight(decoder.outputFormat)
                                    val rotation = if (decoder.outputFormat.containsKey(MediaFormat.KEY_ROTATION)) {
                                        decoder.outputFormat.getInteger(MediaFormat.KEY_ROTATION)
                                    } else {
                                        // Some devices like Samsung SM-A505U (Android 11) do not allow
                                        // video stream rotation on decoding for hardware decoder
                                        Log.w(TAG, "Video stream rotation is not supported by this Android device (${Build.MODEL} - ${Build.DEVICE}, codec: '${decoder.name}')")
                                        0
                                    }
                                    uiHandler.post {
                                        // Run in UI thread
                                        when (rotation) {
                                            90, 270 -> videoDecoderListener.onVideoDecoderFormatChanged(widthHeight.second, widthHeight.first)
                                            else -> videoDecoderListener.onVideoDecoderFormatChanged(widthHeight.first, widthHeight.second)
                                        }
                                    }
                                    frameAlreadyDequeued = true
                                }
                                // No any frames in queue
                                MediaCodec.INFO_TRY_AGAIN_LATER -> {
                                    if (DEBUG) Log.d(TAG, "No output from decoder available")
                                    frameAlreadyDequeued = true
                                }
                                // Frame decoded
                                else -> {
                                    if (outIndex >= 0) {
                                        if (DEBUG || decoderLatencyRequested) {
                                            val ts = bufferInfo.presentationTimeUs
                                            keyframesTimestamps.remove(ts)?.apply {
                                                decoderLatency = (System.currentTimeMillis() - this).toInt()
//                                              Log.d(TAG, "Removed $this")
                                            }
                                        }

                                        val render = bufferInfo.size != 0 && !exitFlag.get()
                                        if (DEBUG) Log.i(TAG, "\tFrame decoded [outIndex=$outIndex, render=$render]")
                                        releaseOutputBuffer(decoder, outIndex, render)
                                        if (!firstFrameDecoded && render) {
                                            firstFrameDecoded = true
                                        }
                                        frameAlreadyDequeued = false
                                    } else {
                                        Log.e(TAG, "Obtaining frame failed w/ error code $outIndex")
                                    }
                                }
                            }
                        // For SPS/PPS frame request another frame (IDR)
                        } while (outIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED || outIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED)
//                      } while (outIndex != MediaCodec.INFO_TRY_AGAIN_LATER)

                        // All decoded frames have been rendered, we can stop playing now
                        if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                            if (DEBUG) Log.d(TAG, "OutputBuffer BUFFER_FLAG_END_OF_STREAM")
                            break
                        }
                    } catch (ignored: InterruptedException) {
                    } catch (e: IllegalStateException) {
                        // Restarting decoder in software mode
                        Log.e(TAG, "${e.message}", e)
                        stopAndReleaseVideoDecoder(decoder)
                        Log.i(TAG, "Starting software video decoder...")
                        decoder = createVideoDecoderAndStart(DecoderType.SOFTWARE)
                        Log.i(TAG, "Software video decoder '${decoder.name}' started (${decoder.codecInfo.getCapabilitiesForType(mimeType).capabilitiesToString()})")
                    } catch (e: MediaCodec.CodecException) {
                        Log.w(TAG, "${e.diagnosticInfo}\nisRecoverable: $${e.isRecoverable}, isTransient: ${e.isTransient}")
                        if (e.isRecoverable) {
                            // Recoverable error.
                            // Calling stop(), configure(), and start() to recover.
                            Log.i(TAG, "Recovering video decoder...")
                            try {
                                decoder.stop()
                                val format = getDecoderMediaFormat(decoder)
                                decoderCreated(decoder, format)
                                decoder.start()
                                Log.i(TAG, "Video decoder recovering succeeded")
                            } catch (e2: Throwable) {
                                Log.e(TAG, "Video decoder recovering failed")
                                Log.e(TAG, "${e2.message}", e2)
                            }
                        } else if (e.isTransient) {
                            // Transient error. Resources are temporarily unavailable and
                            // the method may be retried at a later time.
                            Log.w(TAG, "Video decoder resource temporarily unavailable")
                        } else {
                            // Fatal error. Restarting decoder in software mode.
                            stopAndReleaseVideoDecoder(decoder)
                            Log.i(TAG, "Starting video software decoder...")
                            decoder = createVideoDecoderAndStart(DecoderType.SOFTWARE)
                            Log.i(TAG, "Software video decoder '${decoder.name}' started (${decoder.codecInfo.getCapabilitiesForType(mimeType).capabilitiesToString()})")
                        }
                    } catch (e: Throwable) {
                        Log.e(TAG, "${e.message}", e)
                    }
                } // while

                // Drain decoder
                val inIndex: Int = decoder.dequeueInputBuffer(DEQUEUE_INPUT_TIMEOUT_US)
                if (inIndex >= 0) {
                    decoder.queueInputBuffer(inIndex, 0, 0, 0L, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                } else {
                    Log.w(TAG, "Not able to signal end of stream")
                }

            } catch (e2: Throwable) {
                Log.e(TAG, "${e2.message}", e2)
            } finally {
                stopAndReleaseVideoDecoder(decoder)
            }

        } catch (e: Throwable) {
            Log.e(TAG, "$name stopped due to '${e.message}'")
            videoDecoderListener.onVideoDecoderFailed(e.message)
            // While configuring stopAsync can be called and surface released. Just exit.
            if (!exitFlag.get()) e.printStackTrace()
            return
        }

        videoDecoderListener.onVideoDecoderStopped()
        if (DEBUG) Log.d(TAG, "$name stopped")
    }

    companion object {
        internal val TAG: String = VideoDecodeThread::class.java.simpleName
        private const val DEBUG = false

        private val DEQUEUE_INPUT_TIMEOUT_US = TimeUnit.MILLISECONDS.toMicros(500)
        private val DEQUEUE_OUTPUT_BUFFER_TIMEOUT_US = TimeUnit.MILLISECONDS.toMicros(100)
    }

}
