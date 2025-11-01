package com.alexvas.rtsp.widget

import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.container.NalUnitUtil
import com.alexvas.rtsp.codec.VideoDecodeThread.DecoderType
import com.alexvas.rtsp.codec.VideoDecoderSurfaceThread
import com.alexvas.rtsp.widget.RtspProcessor.Statistics
import com.limelight.binding.video.MediaCodecHelper

/**
 * Low latency RTSP stream playback on surface view.
 */
open class RtspSurfaceView: SurfaceView {

    private var surfaceWidth = 1920
    private var surfaceHeight = 1080

    private var rtspProcessor = RtspProcessor(
        onVideoDecoderCreateRequested = {
            videoMimeType, videoRotation, videoFrameQueue, videoDecoderListener, videoDecoderType, isDebounceEnable ->
            VideoDecoderSurfaceThread(
                holder.surface,
                videoMimeType,
                surfaceWidth,
                surfaceHeight,
                videoRotation,
                videoFrameQueue,
                videoDecoderListener,
                videoDecoderType,
                isDebounceEnable
            )
        }
    )

    var statistics = Statistics()
        get() = rtspProcessor.statistics
        private set

    var videoRotation: Int
        get() = rtspProcessor.videoRotation
        set(value) { rtspProcessor.videoRotation = value }

    var videoDecoderType: DecoderType
        get() = rtspProcessor.videoDecoderType
        set(value) { rtspProcessor.videoDecoderType = value }

    var experimentalUpdateSpsFrameWithLowLatencyParams: Boolean
        get() = rtspProcessor.experimentalUpdateSpsFrameWithLowLatencyParams
        set(value) { rtspProcessor.experimentalUpdateSpsFrameWithLowLatencyParams = value }

    var debug: Boolean
        get() = rtspProcessor.debug
        set(value) { rtspProcessor.debug = value }

    var isDebounceEnable: Boolean
        get() = rtspProcessor.isDebounceEnable
        set(value) { rtspProcessor.isDebounceEnable = value }

    private val surfaceCallback = object: SurfaceHolder.Callback {
        override fun surfaceCreated(holder: SurfaceHolder) {
            if (DEBUG) Log.v(TAG, "surfaceCreated()")
        }

        override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            if (DEBUG) Log.v(TAG, "surfaceChanged(format=$format, width=$width, height=$height)")
            surfaceWidth = width
            surfaceHeight = height
        }

        override fun surfaceDestroyed(holder: SurfaceHolder) {
            if (DEBUG) Log.v(TAG, "surfaceDestroyed()")
            rtspProcessor.stopDecoders()
        }
    }

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
        MediaCodecHelper.initialize(context, /*glRenderer*/ "")
        holder.addCallback(surfaceCallback)
    }

    fun init(
        uri: Uri,
        username: String? = null,
        password: String? = null,
        userAgent: String? = null,
        socketTimeout: Int? = null
    ) {
        if (DEBUG) Log.v(TAG, "init(uri='$uri', username='$username', password='$password', userAgent='$userAgent', socketTimeout=$socketTimeout)")
        rtspProcessor.init(
            uri,
            username,
            password,
            userAgent,
            socketTimeout ?: RtspProcessor.DEFAULT_SOCKET_TIMEOUT
        )
    }

    /**
     * Start RTSP client.
     *
     * @param requestVideo request video track
     * @param requestAudio request audio track
     * @param requestApplication request application track
     * @see https://datatracker.ietf.org/doc/html/rfc4566#section-5.14
     */
    fun start(requestVideo: Boolean, requestAudio: Boolean, requestApplication: Boolean = false) {
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
        private val TAG: String = RtspSurfaceView::class.java.simpleName
        private const val DEBUG = false
    }

}

@OptIn(UnstableApi::class)
fun NalUnitUtil.SpsData.spsDataToString(): String {
    return "" +
        "width=${this.width}, " +
        "height=${this.height}, " +
        "profile_idc=${this.profileIdc}, " +
        "constraint_set_flags=${this.constraintsFlagsAndReservedZero2Bits}, " +
        "level_idc=${this.levelIdc}, " +
        "max_num_ref_frames=${this.maxNumRefFrames}, " +
        "frame_mbs_only_flag=${this.frameMbsOnlyFlag}, " +
        "log2_max_frame_num=${this.frameNumLength}, " +
        "pic_order_cnt_type=${this.picOrderCountType}, " +
        "log2_max_pic_order_cnt_lsb=${this.picOrderCntLsbLength}, " +
        "delta_pic_order_always_zero_flag=${this.deltaPicOrderAlwaysZeroFlag}, " +
        "max_reorder_frames=${this.maxNumReorderFrames}"
}

fun ByteArray.toHexString(offset: Int, maxLength: Int): String {
    val length = minOf(maxLength, size - offset)
    return sliceArray(offset until (offset + length))
        .joinToString(separator = "") { byte ->
            "%02x ".format(byte).uppercase()
        }
}
