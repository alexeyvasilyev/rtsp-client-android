package com.alexvas.rtsp.widget

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.AttributeSet
import android.util.Log
import android.widget.ImageView
import com.alexvas.rtsp.codec.VideoDecodeThread
import com.alexvas.rtsp.codec.VideoDecoderBitmapThread
import com.alexvas.rtsp.widget.RtspProcessor.Statistics

/**
 * Low latency RTSP stream playback on image view (bitmap).
 */
class RtspImageView : ImageView {

    /** Listener to be called when bitmap obtained from video decoder. */
    var onRtspImageBitmapListener: RtspImageBitmapListener? = null

    interface RtspImageBitmapListener {
        fun onRtspImageBitmapObtained(bitmap: Bitmap) {}
    }

    private var rtspProcessor = RtspProcessor(onVideoDecoderCreateRequested = {
            videoMimeType, videoRotation, videoFrameQueue, videoDecoderListener, videoDecoderType ->
        VideoDecoderBitmapThread(
            videoMimeType,
            videoRotation,
            videoFrameQueue,
            videoDecoderListener,
            videoDecoderBitmapListener,
            videoDecoderType,
        )
    })

    private val videoDecoderBitmapListener = object : VideoDecoderBitmapThread.VideoDecoderBitmapListener {
        override fun onVideoDecoderBitmapObtained(bitmap: Bitmap) {
            onRtspImageBitmapListener?.onRtspImageBitmapObtained(bitmap)
            setImageBitmap(bitmap)
            invalidate()
        }
    }

    var statistics = Statistics()
        get() = rtspProcessor.statistics
        private set

    var videoRotation: Int
        get() = rtspProcessor.videoRotation
        set(value) { rtspProcessor.videoRotation = value }

    var videoDecoderType: VideoDecodeThread.DecoderType
        get() = rtspProcessor.videoDecoderType
        set(value) { rtspProcessor.videoDecoderType = value }

    var debug: Boolean
        get() = rtspProcessor.debug
        set(value) { rtspProcessor.debug = value }

    constructor(context: Context) : super(context) {
        initView(context, null, 0)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initView(context, attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initView(context, attrs, defStyleAttr)
    }

    private fun initView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) {
        if (DEBUG) Log.v(TAG, "initView()")
    }

    fun init(uri: Uri, username: String?, password: String?, userAgent: String?) {
        if (DEBUG) Log.v(TAG, "init(uri='$uri', username='$username', password='$password', userAgent='$userAgent')")
        rtspProcessor.init(uri, username, password, userAgent)
    }

    fun start(requestVideo: Boolean, requestAudio: Boolean) {
        if (DEBUG) Log.v(TAG, "start(requestVideo=$requestVideo, requestAudio=$requestAudio)")
        rtspProcessor.start(requestVideo, requestAudio)
    }

    fun stop() {
        if (DEBUG) Log.v(TAG, "stop()")
        rtspProcessor.stop()
    }

    fun isStarted(): Boolean {
        return rtspProcessor.isStarted()
    }

    fun setStatusListener(listener: RtspStatusListener?) {
        if (DEBUG) Log.v(TAG, "setStatusListener()")
        rtspProcessor.setStatusListener(listener)
    }


    companion object {
        private val TAG: String = RtspImageView::class.java.simpleName
        private const val DEBUG = false
    }

}
