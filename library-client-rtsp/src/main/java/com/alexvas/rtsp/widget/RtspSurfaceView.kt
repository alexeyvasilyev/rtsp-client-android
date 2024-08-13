package com.alexvas.rtsp.widget

import android.annotation.SuppressLint
import android.content.Context
import android.media.MediaFormat
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.container.NalUnitUtil
import com.alexvas.rtsp.RtspClient
import com.alexvas.rtsp.RtspClient.SdpInfo
import com.alexvas.rtsp.codec.AudioCodecType
import com.alexvas.rtsp.codec.AudioDecodeThread
import com.alexvas.rtsp.codec.AudioFrameQueue
import com.alexvas.rtsp.codec.FrameQueue
import com.alexvas.rtsp.codec.VideoCodecType
import com.alexvas.rtsp.codec.VideoDecodeThread
import com.alexvas.rtsp.codec.VideoFrameQueue
import com.alexvas.utils.NetUtils
import com.alexvas.utils.VideoCodecUtils
import java.net.Socket
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.min


open class RtspSurfaceView: SurfaceView {

    class Statistics {
        var videoDecoderType = VideoDecodeThread.DecoderType.HARDWARE
        var videoDecoderName: String? = null
        var videoDecoderLatencyMsec = -1
        var networkLatencyMsec = -1
    }

    private lateinit var uri: Uri
    private var username: String? = null
    private var password: String? = null
    private var userAgent: String? = null
    private var requestVideo = true
    private var requestAudio = true
    private var rtspThread: RtspThread? = null
    private var videoFrameQueue = VideoFrameQueue(60)
    private var audioFrameQueue = AudioFrameQueue(10)
    private var videoDecodeThread: VideoDecodeThread? = null
    private var audioDecodeThread: AudioDecodeThread? = null
    private var surfaceWidth = 1920
    private var surfaceHeight = 1080
    private var statusListener: RtspStatusListener? = null
    private val uiHandler = Handler(Looper.getMainLooper())
    private var videoMimeType: String = "video/avc"
    private var audioMimeType: String = ""
    private var audioSampleRate: Int = 0
    private var audioChannelCount: Int = 0
    private var audioCodecConfig: ByteArray? = null
    private var firstFrameRendered = false
    var statistics = Statistics()
        get() {
            videoDecodeThread?.let { decoder ->
                field.apply {
                    networkLatencyMsec = decoder.getCurrentNetworkLatencyMsec()
                    videoDecoderLatencyMsec = decoder.getCurrentVideoDecoderLatencyMsec()
                    videoDecoderType = decoder.getCurrentVideoDecoderType()
                    videoDecoderName = decoder.getCurrentVideoDecoderName()
                }
            }
            return field
        }
        private set

    /**
     * Show more debug info on console on runtime.
     */
    var debug = false

    /**
     * Video rotation in degrees. Allowed values: 0, 90, 180, 270.
     * Note that not all hardware video decoders support rotation.
     */
    var videoRotation = 0
        set(value) {
            if (value == 0 || value == 90 || value == 180 || value == 270)
                field = value
        }

    /**
     * Requested video decoder type.
     */
    var videoDecoderType = VideoDecodeThread.DecoderType.HARDWARE

    interface RtspStatusListener {
        fun onRtspStatusConnecting() {}
        fun onRtspStatusConnected() {}
        fun onRtspStatusDisconnecting() {}
        fun onRtspStatusDisconnected() {}
        fun onRtspStatusFailedUnauthorized() {}
        fun onRtspStatusFailed(message: String?) {}
        fun onRtspFirstFrameRendered() {}
    }

    private val proxyClientListener = object: RtspClient.RtspClientListener {

        override fun onRtspConnecting() {
            if (DEBUG) Log.v(TAG, "onRtspConnecting()")
            uiHandler.post {
                statusListener?.onRtspStatusConnecting()
            }
        }

        override fun onRtspConnected(sdpInfo: SdpInfo) {
            if (DEBUG) Log.v(TAG, "onRtspConnected()")
            if (sdpInfo.videoTrack != null) {
                videoFrameQueue.clear()
                when (sdpInfo.videoTrack?.videoCodec) {
                    RtspClient.VIDEO_CODEC_H264 -> videoMimeType = MediaFormat.MIMETYPE_VIDEO_AVC
                    RtspClient.VIDEO_CODEC_H265 -> videoMimeType = MediaFormat.MIMETYPE_VIDEO_HEVC
                }
                when (sdpInfo.audioTrack?.audioCodec) {
                    RtspClient.AUDIO_CODEC_AAC -> audioMimeType = MediaFormat.MIMETYPE_AUDIO_AAC
                    RtspClient.AUDIO_CODEC_OPUS -> audioMimeType = MediaFormat.MIMETYPE_AUDIO_OPUS
                }
                val sps: ByteArray? = sdpInfo.videoTrack?.sps
                val pps: ByteArray? = sdpInfo.videoTrack?.pps
                // Initialize decoder
                @SuppressLint("UnsafeOptInUsageError")
                if (sps != null && pps != null) {
                    val data = ByteArray(sps.size + pps.size)
                    sps.copyInto(data, 0, 0, sps.size)
                    pps.copyInto(data, sps.size, 0, pps.size)
                    videoFrameQueue.push(
                        FrameQueue.VideoFrame(
                            VideoCodecType.H264,
                            isKeyframe = true,
                            data,
                            0,
                            data.size,
                            0
                        )
                    )
                    try {
                        val offset = if (sps[3] == 1.toByte()) 5 else 4
                        val spsData = NalUnitUtil.parseSpsNalUnitPayload(
                            data, offset, data.size - offset)
                        if (spsData.maxNumReorderFrames > 0) {
                            Log.w(TAG, "SPS frame param max_num_reorder_frames=" +
                                        "${spsData.maxNumReorderFrames} is too high" +
                                        " for low latency decoding (expecting 0)."
                            )
                        }
                        if (debug) {
                            Log.d(TAG, "SPS frame: " + sps.toHexString(0, sps.size))
                            Log.d(TAG, "\t${spsData.spsDataToString()}")
                            Log.d(TAG, "PPS frame: " + pps.toHexString(0, pps.size))
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } else {
                    if (DEBUG) Log.d(TAG, "RTSP SPS and PPS NAL units missed in SDP")
                }
            }
            if (sdpInfo.audioTrack != null) {
                audioFrameQueue.clear()
                when (sdpInfo.audioTrack?.audioCodec) {
                    RtspClient.AUDIO_CODEC_AAC -> audioMimeType = MediaFormat.MIMETYPE_AUDIO_AAC
                    RtspClient.AUDIO_CODEC_OPUS -> audioMimeType = MediaFormat.MIMETYPE_AUDIO_OPUS
                }
                audioSampleRate = sdpInfo.audioTrack?.sampleRateHz!!
                audioChannelCount = sdpInfo.audioTrack?.channels!!
                audioCodecConfig = sdpInfo.audioTrack?.config
            }
            onRtspClientConnected()
            uiHandler.post {
                statusListener?.onRtspStatusConnected()
            }
        }

        override fun onRtspVideoNalUnitReceived(data: ByteArray, offset: Int, length: Int, timestamp: Long) {
            if (length > 0)
                videoFrameQueue.push(
                    FrameQueue.VideoFrame(
                        VideoCodecType.H264,
                        // Search for NAL_IDR_SLICE within first 1KB maximum
                        isKeyframe = VideoCodecUtils.isAnyH264KeyFrame(data, offset, min(length, 1000)),
                        data,
                        offset,
                        length,
                        timestamp,
                        capturedTimestampMs = System.currentTimeMillis()
                    )
                )
        }

        override fun onRtspAudioSampleReceived(data: ByteArray, offset: Int, length: Int, timestamp: Long) {
            if (length > 0)
                audioFrameQueue.push(
                    FrameQueue.AudioFrame(
                        AudioCodecType.AAC_LC,
                        data, offset,
                        length,
                        timestamp
                    )
                )
        }

        override fun onRtspDisconnecting() {
            if (DEBUG) Log.v(TAG, "onRtspDisconnecting()")
            uiHandler.post {
                statusListener?.onRtspStatusDisconnecting()
            }
        }

        override fun onRtspDisconnected() {
            if (DEBUG) Log.v(TAG, "onRtspDisconnected()")
            uiHandler.post {
                statusListener?.onRtspStatusDisconnected()
            }
        }

        override fun onRtspFailedUnauthorized() {
            if (DEBUG) Log.v(TAG, "onRtspFailedUnauthorized()")
            uiHandler.post {
                statusListener?.onRtspStatusFailedUnauthorized()
            }
        }

        override fun onRtspFailed(message: String?) {
            if (DEBUG) Log.v(TAG, "onRtspFailed(message='$message')")
            uiHandler.post {
                statusListener?.onRtspStatusFailed(message)
            }
        }
    }

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
            stopDecoders()
        }
    }

    private val videoDecoderListener = object: VideoDecodeThread.VideoDecoderListener {
        override fun onVideoDecoderStarted() {
            if (DEBUG) Log.v(TAG, "onVideoDecoderStarted()")
        }

        override fun onVideoDecoderStopped() {
            if (DEBUG) Log.v(TAG, "onVideoDecoderStopped()")
        }

        override fun onVideoDecoderFailed(message: String?) {
            if (DEBUG) Log.v(TAG, "onVideoDecoderFailed(message='$message')")
        }

        override fun onVideoDecoderFormatChanged(width: Int, height: Int) {
            if (DEBUG) Log.v(TAG, "onVideoDecoderFormatChanged(width=$width, height=$height)")
        }

        override fun onVideoDecoderFirstFrameRendered() {
            if (DEBUG) Log.v(TAG, "onVideoDecoderFirstFrameDecoded()")
            if (!firstFrameRendered) statusListener?.onRtspFirstFrameRendered()
            firstFrameRendered = true
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
        holder.addCallback(surfaceCallback)
    }

    fun init(uri: Uri, username: String?, password: String?) {
        init(uri, username, password, null)
    }

    fun init(uri: Uri, username: String?, password: String?, userAgent: String?) {
        if (DEBUG) Log.v(TAG, "init(uri='$uri', username=$username, password=$password, userAgent='$userAgent')")
        this.uri = uri
        this.username = username
        this.password = password
        this.userAgent = userAgent
    }

    fun start(requestVideo: Boolean, requestAudio: Boolean) {
        if (DEBUG) Log.v(TAG, "start(requestVideo=$requestVideo, requestAudio=$requestAudio)")
        if (rtspThread != null) rtspThread?.stopAsync()
        this.requestVideo = requestVideo
        this.requestAudio = requestAudio
        rtspThread = RtspThread().apply {
            name = "RTSP IO thread [${getUriName()}]"
            start()
        }
    }

    fun stop() {
        if (DEBUG) Log.v(TAG, "stop()")
        rtspThread?.stopAsync()
        rtspThread = null
    }

    fun isStarted(): Boolean {
        return rtspThread != null
    }

    inner class RtspThread: Thread() {
        private var rtspStopped: AtomicBoolean = AtomicBoolean(false)

        fun stopAsync() {
            if (DEBUG) Log.v(TAG, "stopAsync()")
            rtspStopped.set(true)
            // Wake up sleep() code
            interrupt()
        }

        override fun run() {
            onRtspClientStarted()
            val port = if (uri.port == -1) DEFAULT_RTSP_PORT else uri.port
            try {
                if (DEBUG) Log.d(TAG, "Connecting to ${uri.host.toString()}:$port...")

                val socket: Socket = if (uri.scheme?.lowercase() == "rtsps")
                    NetUtils.createSslSocketAndConnect(uri.host.toString(), port, 5000)
                else
                    NetUtils.createSocketAndConnect(uri.host.toString(), port, 5000)

                // Blocking call until stopped variable is true or connection failed
                val rtspClient = RtspClient.Builder(socket, uri.toString(), rtspStopped, proxyClientListener)
                    .requestVideo(requestVideo)
                    .requestAudio(requestAudio)
                    .withDebug(debug)
                    .withUserAgent(userAgent)
                    .withCredentials(username, password)
                    .build()
                rtspClient.execute()

                NetUtils.closeSocket(socket)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            onRtspClientStopped()
        }
    }

    fun setStatusListener(listener: RtspStatusListener?) {
        if (DEBUG) Log.v(TAG, "setStatusListener()")
        this.statusListener = listener
    }

    private fun onRtspClientStarted() {
        if (DEBUG) Log.v(TAG, "onRtspClientStarted()")
        uiHandler.post { statusListener?.onRtspStatusConnected() }
    }

    private fun onRtspClientConnected() {
        if (DEBUG) Log.v(TAG, "onRtspClientConnected()")
        if (videoMimeType.isNotEmpty()) {
            firstFrameRendered = false
            Log.i(TAG, "Starting video decoder with mime type \"$videoMimeType\"")
            videoDecodeThread = VideoDecodeThread(
                holder.surface,
                videoMimeType,
                surfaceWidth,
                surfaceHeight,
                videoRotation,
                videoFrameQueue,
                videoDecoderListener,
                videoDecoderType
            )
            videoDecodeThread!!.apply {
                name = "RTSP video thread [${getUriName()}]"
                start()
            }
        }
        if (audioMimeType.isNotEmpty() /*&& checkAudio!!.isChecked*/) {
            Log.i(TAG, "Starting audio decoder with mime type \"$audioMimeType\"")
            audioDecodeThread = AudioDecodeThread(
                audioMimeType, audioSampleRate, audioChannelCount, audioCodecConfig, audioFrameQueue)
            audioDecodeThread!!.apply {
                name = "RTSP audio thread [${getUriName()}]"
                start()
            }
        }
    }

    private fun onRtspClientStopped() {
        if (DEBUG) Log.v(TAG, "onRtspClientStopped()")
        uiHandler.post { statusListener?.onRtspStatusDisconnected() }
        stopDecoders()
        rtspThread = null
    }

    private fun stopDecoders() {
        if (DEBUG) Log.v(TAG, "stopDecoders()")
        videoDecodeThread?.stopAsync()
        videoDecodeThread = null
        audioDecodeThread?.stopAsync()
        audioDecodeThread = null
    }

    private fun getUriName(): String {
        val port = if (uri.port == -1) DEFAULT_RTSP_PORT else uri.port
        return "${uri.host.toString()}:$port"
    }

    companion object {
        private val TAG: String = RtspSurfaceView::class.java.simpleName
        private const val DEBUG = false
        private const val DEFAULT_RTSP_PORT = 554
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
