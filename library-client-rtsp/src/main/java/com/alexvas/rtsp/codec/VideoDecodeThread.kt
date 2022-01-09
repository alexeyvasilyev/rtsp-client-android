package com.alexvas.rtsp.codec

import android.media.MediaCodec
import android.media.MediaCodec.OnFrameRenderedListener
import android.media.MediaFormat
import android.util.Log
import android.view.Surface
import java.nio.ByteBuffer

class VideoDecodeThread (
        private val surface: Surface,
        private val mimeType: String,
        private val width: Int,
        private val height: Int,
        private val videoFrameQueue: FrameQueue,
        private val onFrameRenderedListener: OnFrameRenderedListener) : Thread() {

    private var isRunning = true

    fun stopAsync() {
        if (DEBUG) Log.v(TAG, "stopAsync()")
        isRunning = false
        // Wake up sleep() code
        interrupt()
    }

    private fun getSafeSurfaceDimension(dimen: Int): Int {
        // Be sure codec width and height is even
        return if (dimen % 2 == 0)
            dimen
        else
            dimen - 1
    }

    override fun run() {
        if (DEBUG) Log.d(TAG, "$name started")

        val safeWidth = getSafeSurfaceDimension(width)
        val safeHeight = getSafeSurfaceDimension(height)
        val decoder = MediaCodec.createDecoderByType(mimeType)
        val format = MediaFormat.createVideoFormat(mimeType, safeWidth, safeHeight)

        decoder.setOnFrameRenderedListener(onFrameRenderedListener, null)

        if (DEBUG) Log.d(TAG, "Configuring surface decoder ${safeWidth}x${safeHeight} w/ '$mimeType'")
        try {
            decoder.configure(format, surface, null, 0)
        } catch (e: IllegalArgumentException) {
            if (DEBUG) Log.d(TAG, "$name stopped due to '${e.message}'")
            // While configuring stopAsync can be called and surface released. Just exit.
            if (isRunning) e.printStackTrace()
            return
        }
        decoder.start()
        if (DEBUG) Log.d(TAG, "Started surface decoder")

        val bufferInfo = MediaCodec.BufferInfo()
        while (isRunning) {
            val inIndex: Int = decoder.dequeueInputBuffer(10000L)
            if (inIndex >= 0) {
                // fill inputBuffers[inputBufferIndex] with valid data
                var byteBuffer: ByteBuffer?
                try {
                    byteBuffer = decoder.getInputBuffer(inIndex)
                } catch (e: Exception) {
                    e.printStackTrace()
                    break
                }
                byteBuffer?.rewind()

                // Preventing BufferOverflowException
//              if (length > byteBuffer.limit()) throw DecoderFatalException("Error")

                try {
                    val frame = videoFrameQueue.pop()
                    if (frame != null) {
                        byteBuffer!!.put(frame.data, frame.offset, frame.length)
                        decoder.queueInputBuffer(inIndex, frame.offset, frame.length, frame.timestamp, 0)
                    }
                } catch (e: InterruptedException) {
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            try {
                if (!isRunning) break
                when (val outIndex = decoder.dequeueOutputBuffer(bufferInfo, 10000L)) {
                    MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> Log.d(TAG, "Decoder format changed: ${decoder.outputFormat}")
                    MediaCodec.INFO_TRY_AGAIN_LATER -> if (DEBUG) Log.d(TAG, "No output from decoder available")
                    else -> {
                        if (outIndex >= 0 && isRunning) {
                            decoder.releaseOutputBuffer(outIndex, bufferInfo.size != 0)
                        }
                    }
                }
            } catch (e: InterruptedException) {
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // All decoded frames have been rendered, we can stop playing now
            if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                if (DEBUG) Log.d(TAG, "OutputBuffer BUFFER_FLAG_END_OF_STREAM")
                break
            }
        }

        try {
            decoder.stop()
            decoder.release()
        } catch (e: InterruptedException) {
        } catch (e: Exception) {
            e.printStackTrace()
        }
        videoFrameQueue.clear()

        if (DEBUG) Log.d(TAG, "$name stopped")
    }

    companion object {
        private val TAG: String = VideoDecodeThread::class.java.simpleName
        private const val DEBUG = false
    }

}

