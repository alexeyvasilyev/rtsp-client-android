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

    /** Optional listener to be called when bitmap obtained from video decoder. */
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

    /**
     * Start RTSP client.
     *
     * @param requestVideo request video track
     * @param requestAudio request audio track
     * @param requestApplication request application track
     * @see https://datatracker.ietf.org/doc/html/rfc4566#section-5.14
     */
    fun start(requestVideo: Boolean, requestAudio: Boolean, requestApplication: Boolean) {
        if (DEBUG) Log.v(TAG, "start(requestVideo=$requestVideo, requestAudio=$requestAudio, requestApplication=$requestApplication)")
        rtspProcessor.start(requestVideo, requestAudio, requestApplication)
    }

    /**
     * Stop RTSP client.
     */
    fun stop() {
        if (DEBUG) Log.v(TAG, "stop()")
        rtspProcessor.stop()
    }

    fun isStarted(): Boolean {
        return rtspProcessor.isStarted()
    }

    fun setStatusListener(listener: RtspStatusListener?) {
        if (DEBUG) Log.v(TAG, "setStatusListener()")
        rtspProcessor.statusListener = listener
    }

    fun setDataListener(listener: RtspDataListener?) {
        if (DEBUG) Log.v(TAG, "setDataListener()")
        rtspProcessor.dataListener = listener
    }

    companion object {
        private val TAG: String = RtspImageView::class.java.simpleName
        private const val DEBUG = false
    }

}
