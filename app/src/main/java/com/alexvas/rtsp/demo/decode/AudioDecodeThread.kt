package com.alexvas.rtsp.demo.decode

import android.media.*
import android.util.Log
import java.nio.ByteBuffer

class AudioDecodeThread (
        private val mimeType: String,
        private val sampleRate: Int,
        private val channelCount: Int,
        private val audioFrameQueue: VideoFrameQueue) : Thread() {
    private var decoder: MediaCodec? = null
    private val TAG: String = AudioDecodeThread::class.java.simpleName
    private val DEBUG = true

    override fun run() {
        if (DEBUG) Log.d(TAG, "AudioDecodeThread started")
        decoder = MediaCodec.createDecoderByType(mimeType)
        val format = MediaFormat.createAudioFormat(mimeType, sampleRate, channelCount)
        format.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
        format.setInteger(MediaFormat.KEY_BIT_RATE, 128000);
        decoder!!.configure(format, null, null, 0)
        if (decoder == null) {
            Log.e(TAG, "Can't find audio info!")
            return
        }
        decoder!!.start()

        val bufferSize = AudioTrack.getMinBufferSize(
                sampleRate /*44100*/, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT)
        // create our AudioTrack instance
        val audioTrack = AudioTrack(
                AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build(),
                AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .setSampleRate(sampleRate)
                        .build(),
                bufferSize,
                AudioTrack.MODE_STREAM,
                0)

        val bufferInfo = MediaCodec.BufferInfo()
        while (!interrupted()) {
            val inIndex: Int = decoder!!.dequeueInputBuffer(10000L)
            if (inIndex >= 0) {
                // fill inputBuffers[inputBufferIndex] with valid data
                val byteBuffer: ByteBuffer? = decoder!!.getInputBuffer(inIndex)
                byteBuffer?.rewind()

                // Preventing BufferOverflowException
//              if (length > byteBuffer.limit()) throw DecoderFatalException("Error")

                val audioFrame: VideoFrameQueue.Frame?
                try {
                    audioFrame = audioFrameQueue.pop()
                    if (audioFrame == null) {
                        Log.d(TAG, "Empty frame")
                    } else {
                        byteBuffer!!.put(audioFrame.data, audioFrame.offset, audioFrame.length)
                        decoder!!.queueInputBuffer(inIndex, audioFrame.offset, audioFrame.length, audioFrame.timestamp, 0)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            try {
                val outIndex = decoder!!.dequeueOutputBuffer(bufferInfo, 10000)
                when (outIndex) {
                    MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> Log.d(TAG, "Decoder format changed: " + decoder!!.outputFormat)
                    MediaCodec.INFO_TRY_AGAIN_LATER -> if (DEBUG) Log.d(TAG, "No output from decoder available")
                    else -> {
                        val byteBuffer: ByteBuffer? = decoder!!.getOutputBuffer(outIndex)

                        val chunk = ByteArray(bufferInfo.size)
                        byteBuffer?.get(chunk)
                        byteBuffer?.clear()

                        if (chunk.isNotEmpty()) {
                            audioTrack.write(chunk, 0, chunk.size)
                        }
                        decoder!!.releaseOutputBuffer(outIndex, false)
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
        decoder!!.stop()
        decoder!!.release()
        audioFrameQueue.clear()
        if (DEBUG) Log.d(TAG, "AudioDecodeThread stopped")
    }
}

