package com.alexvas.rtsp.codec

import android.graphics.Bitmap
import android.graphics.Matrix
import android.media.MediaCodec
import android.media.MediaFormat
import android.util.Log
import com.alexvas.rtsp.codec.color.ColorConverterImageAndroidX

class VideoDecoderBitmapThread (
    mimeType: String,
    rotation: Int, // 0, 90, 180, 270
    videoFrameQueue: VideoFrameQueue,
    videoDecoderListener: VideoDecoderListener,
    private val videoDecoderBitmapListener: VideoDecoderBitmapListener,
    videoDecoderType: DecoderType = DecoderType.HARDWARE
): VideoDecodeThread(mimeType, 1920, 1080, rotation, videoFrameQueue, videoDecoderListener, videoDecoderType) {

    interface VideoDecoderBitmapListener {
        /** Used only when OutputType.BUFFERS is used */
        fun onVideoDecoderBitmapObtained(bitmap: Bitmap) {}
    }

    private var colorConverter: ColorConverterImageAndroidX? = null

    override fun decoderCreated(mediaCodec: MediaCodec, mediaFormat: MediaFormat) {
        mediaCodec.configure(mediaFormat, null, null, 0)
    }

    override fun releaseOutputBuffer(mediaCodec: MediaCodec, outIndex: Int, render: Boolean) {
        val image = mediaCodec.getOutputImage(outIndex)
        image?.let {
            if (colorConverter == null)
                colorConverter = ColorConverterImageAndroidX()
            // Converting YUV 4:2:0 888 to Bitmap ARGB 8888
            var bitmap = colorConverter!!.getBitmapFromImage(image)
            // Rotation does not work in VideoDecoderThread since we do not use Surface there.
            // Rotate bitmaps.
            bitmap = if (rotation != 0) {
                bitmap.rotateBitmap(rotation.toFloat())
            } else {
                bitmap.createCopy565()
            }
            uiHandler.post {
                if (!firstFrameRendered) {
                    firstFrameRendered = true
                    videoDecoderListener.onVideoDecoderFirstFrameRendered()
                }
                videoDecoderBitmapListener.onVideoDecoderBitmapObtained(bitmap)
            }
        }
        mediaCodec.releaseOutputBuffer(outIndex, false)
    }

    override fun decoderDestroyed(mediaCodec: MediaCodec) {
        colorConverter?.apply {
            try {
                Log.i(TAG, "Releasing color converter...")
                release()
                Log.i(TAG, "Color converter successfully released")
            } catch (e: Throwable) {
                Log.e(TAG, "Failed to release color converter", e)
            }
        }
    }

}

fun Bitmap.createCopy565(): Bitmap {
    return copy(
        Bitmap.Config.RGB_565,
        true
    )
}

fun Bitmap.rotateBitmap(angle: Float): Bitmap {
    val matrix = Matrix()
    matrix.postRotate(angle)
    return Bitmap.createBitmap(this, 0, 0, this.width, this.height, matrix, true)
}
