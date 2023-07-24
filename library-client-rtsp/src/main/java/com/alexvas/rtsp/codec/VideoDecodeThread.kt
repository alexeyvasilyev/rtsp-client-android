package com.alexvas.rtsp.codec

import android.annotation.SuppressLint
import android.media.MediaCodec
import android.media.MediaCodec.OnFrameRenderedListener
import android.media.MediaFormat
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Surface
import com.alexvas.utils.MediaCodecUtils
import com.alexvas.utils.capabilitiesToString
import com.google.android.exoplayer2.util.Util
import java.nio.ByteBuffer
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class VideoDecodeThread (
        private val surface: Surface,
        private val mimeType: String,
        private val width: Int,
        private val height: Int,
        private val rotation: Int, // 0, 90, 180, 270
        private val videoFrameQueue: FrameQueue,
        private val videoDecoderListener: VideoDecoderListener) : Thread() {

    private val uiHandler = Handler(Looper.getMainLooper())
    private var exitFlag = AtomicBoolean(false)
    private var firstFrameRendered = false

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

    fun stopAsync() {
        if (DEBUG) Log.v(TAG, "stopAsync()")
        exitFlag.set(true)
        // Wake up sleep() code
        interrupt()
    }

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
            Log.d(TAG, "Configuring surface ${safeWidthHeight.first}x${safeWidthHeight.second} w/ '$mimeType', $surface valid=${surface.isValid}")
        else
            Log.i(TAG, "Configuring surface ${safeWidthHeight.first}x${safeWidthHeight.second} w/ '$mimeType'")
        format.setInteger(MediaFormat.KEY_ROTATION, rotation)
        return format
    }

    private enum class DecoderType {
        HARDWARE,
        SOFTWARE // fallback
    }

    private fun createVideoDecoderAndStart(decoderType: DecoderType): MediaCodec {
        if (DEBUG) Log.v(TAG, "createVideoDecoderAndStart(decoderType=$decoderType)")

        val decoder = when (decoderType) {
            DecoderType.HARDWARE -> {
                val hwDecoders = MediaCodecUtils.getHardwareDecoders(mimeType)
                if (hwDecoders.isEmpty()) {
                    Log.w(TAG, "Cannot get hardware video decoders for mime type '$mimeType'. Using default one.")
                    MediaCodec.createDecoderByType(mimeType)
                } else {
                    val name = hwDecoders[0].name
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
        decoder.configure(format, surface, null, 0)
        decoder.start()
        return decoder
    }

    private fun stopAndReleaseVideoDecoder(decoder: MediaCodec) {
        if (DEBUG) Log.v(TAG, "stopAndReleaseVideoDecoder()")
        Log.i(TAG, "Stopping decoder...")
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
    }


    override fun run() {
        if (DEBUG) Log.d(TAG, "$name started")

        videoDecoderListener.onVideoDecoderStarted()

        try {
            Log.i(TAG, "Starting hardware video decoder...")
            var decoder = try {
                createVideoDecoderAndStart(DecoderType.HARDWARE)
            } catch (e: Throwable) {
                Log.e(TAG, "Failed to start hardware video decoder (${e.message})", e)
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

            Log.i(TAG, "Video decoder '${decoder.name}' started (${decoder.codecInfo.getCapabilitiesForType(mimeType).capabilitiesToString()})")

            val bufferInfo = MediaCodec.BufferInfo()

            try {
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
                                byteBuffer?.put(frame.data, frame.offset, frame.length)
                                decoder.queueInputBuffer(inIndex, frame.offset, frame.length, frame.timestamp, 0)
                            }
                        }

                        if (exitFlag.get()) break

                        // Get all output buffer frames until no buffer from decoder available (INFO_TRY_AGAIN_LATER).
                        // Single input buffer frame can contain several frames, e.g. SPS + PPS + IDR.
                        // Thus dequeueOutputBuffer should be called several times.
                        // First time it obtains SPS + PPS, second one - IDR frame.
                        var alreadyDequeued = false
                        do {
                            // For the first time wait for a frame within 100 msec, next times no timeout
                            val timeout = if (alreadyDequeued) 0L else DEQUEUE_OUTPUT_BUFFER_TIMEOUT_US
                            alreadyDequeued = true
                            val outIndex = decoder.dequeueOutputBuffer(bufferInfo, timeout)
                            when (outIndex) {
                                // Resolution changed
                                MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
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
                                }
                                // No any frames in queue
                                MediaCodec.INFO_TRY_AGAIN_LATER -> {
                                    if (DEBUG) Log.d(TAG, "No output from decoder available")
                                }
                                // Frame decoded
                                else -> {
                                    if (outIndex >= 0) {
                                        val render = bufferInfo.size != 0 && !exitFlag.get() && surface.isValid
                                        if (DEBUG) Log.i(TAG, "\tFrame decoded [outIndex=$outIndex, length=${bufferInfo.size}, render=$render]")
                                        decoder.releaseOutputBuffer(outIndex, render)
                                    } else {
                                        Log.e(TAG, "Obtaining frame failed w/ error code $outIndex (length: ${bufferInfo.size})")
                                    }
                                }
                            }
                        } while (outIndex != MediaCodec.INFO_TRY_AGAIN_LATER)

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
                                decoder.configure(format, surface, null, 0)
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
        private val TAG: String = VideoDecodeThread::class.java.simpleName
        private const val DEBUG = false

        private val DEQUEUE_INPUT_TIMEOUT_US = TimeUnit.MILLISECONDS.toMicros(500)
        private val DEQUEUE_OUTPUT_BUFFER_TIMEOUT_US = TimeUnit.MILLISECONDS.toMicros(100)
    }

}

