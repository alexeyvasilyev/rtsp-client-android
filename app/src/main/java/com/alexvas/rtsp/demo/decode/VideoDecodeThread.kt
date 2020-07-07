package com.alexvas.rtsp.demo.decode

import android.media.MediaCodec
import android.media.MediaFormat
import android.util.Log
import android.view.Surface
import java.nio.ByteBuffer

class VideoDecodeThread (
        private val surface: Surface,
        private val mimeType: String,
        private val width: Int,
        private val height: Int,
        private val videoFrameQueue: FrameQueue) : Thread() {

    private val TAG: String = VideoDecodeThread::class.java.simpleName
    private val DEBUG = true

    override fun run() {
        if (DEBUG) Log.d(TAG, "VideoDecodeThread started")

        val decoder = MediaCodec.createDecoderByType(mimeType)
        val format = MediaFormat.createVideoFormat(mimeType, width, height)

        decoder.configure(format, surface, null, 0)
        decoder.start()

        val bufferInfo = MediaCodec.BufferInfo()
        while (!interrupted()) {
            val inIndex: Int = decoder.dequeueInputBuffer(10000L)
            if (inIndex >= 0) {
                // fill inputBuffers[inputBufferIndex] with valid data
                val byteBuffer: ByteBuffer? = decoder.getInputBuffer(inIndex)
                byteBuffer?.rewind()

                // Preventing BufferOverflowException
//              if (length > byteBuffer.limit()) throw DecoderFatalException("Error")

                val frame: FrameQueue.Frame?
                try {
                    frame = videoFrameQueue.pop()
                    if (frame == null) {
                        Log.d(TAG, "Empty frame")
                    } else {
                        byteBuffer!!.put(frame.data, frame.offset, frame.length)
                        decoder.queueInputBuffer(inIndex, frame.offset, frame.length, frame.timestamp, 0)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            try {
                val outIndex = decoder.dequeueOutputBuffer(bufferInfo, 10000)
                when (outIndex) {
                    MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> Log.d(TAG, "Decoder format changed: " + decoder.outputFormat)
                    MediaCodec.INFO_TRY_AGAIN_LATER -> if (DEBUG) Log.d(TAG, "No output from decoder available")
                    else -> {
                        decoder.releaseOutputBuffer(outIndex, bufferInfo.size != 0)
                    }
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }

            // All decoded frames have been rendered, we can stop playing now
            if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                Log.d(TAG, "OutputBuffer BUFFER_FLAG_END_OF_STREAM")
                break
            }
        }

        decoder.stop()
        decoder.release()
        videoFrameQueue.clear()

        if (DEBUG) Log.d(TAG, "VideoDecodeThread stopped")
    }
}

