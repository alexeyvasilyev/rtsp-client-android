package com.alexvas.rtsp.codec.color

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.Rect
import android.media.Image
import androidx.camera.core.ImageInfo
import androidx.camera.core.ImageProcessingUtil
import androidx.camera.core.ImageProxy
import androidx.camera.core.ImmutableImageInfo
import androidx.camera.core.impl.TagBundle
import java.nio.ByteBuffer

/**
 * Convert Image YUV 4:2:0 888 to Bitmap ARGB 8888.
 */
class ColorConverterImageAndroidX: ColorConverterImage() {

    @SuppressLint("RestrictedApi")
    override fun getBitmapFromImage(image: Image): Bitmap {
        // YUV 4:2:0 888 -> ARGB 8888
        return ImageProcessingUtil.convertYUVToBitmap(AndroidImageProxy(image))
    }

    override fun release() {
    }

}

internal class AndroidImageProxy(private val image: Image) : ImageProxy {

    private val planes: Array<AndroidPlaneProxy> = if (image.planes != null) {
        Array(image.planes.size) { i -> AndroidPlaneProxy(image.planes[i]) }
    } else {
        emptyArray()
    }
    @SuppressLint("RestrictedApi")
    private val imageInfo: ImageInfo = ImmutableImageInfo.create(
        TagBundle.emptyBundle(),
        image.timestamp,
        0,
        Matrix()
    )

    override fun close() {
        image.close()
    }

    override fun getCropRect(): Rect {
        return image.cropRect
    }

    override fun setCropRect(rect: Rect?) {
        image.cropRect = rect
    }

    override fun getFormat(): Int {
        return image.format
    }

    override fun getHeight(): Int {
        return image.height
    }

    override fun getWidth(): Int {
        return image.width
    }

    override fun getPlanes(): Array<ImageProxy.PlaneProxy> {
        @Suppress("UNCHECKED_CAST")
        return planes as Array<ImageProxy.PlaneProxy>
    }

    /** An [ImageProxy.PlaneProxy] which wraps around an [Image.Plane].  */
    private class AndroidPlaneProxy(private val mPlane: Image.Plane) : ImageProxy.PlaneProxy {
        override fun getRowStride(): Int {
            return mPlane.rowStride
        }

        override fun getPixelStride(): Int {
            return mPlane.pixelStride
        }

        override fun getBuffer(): ByteBuffer {
            return mPlane.buffer
        }
    }

    override fun getImageInfo(): ImageInfo {
        return imageInfo
    }

    @SuppressLint("UnsafeOptInUsageError")
    override fun getImage(): Image {
        return image
    }
}
