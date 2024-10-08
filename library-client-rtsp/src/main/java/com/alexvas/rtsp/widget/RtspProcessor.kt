package com.alexvas.rtsp.widget

import android.annotation.SuppressLint
import android.media.MediaFormat
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.media3.container.NalUnitUtil
import com.alexvas.rtsp.RtspClient
import com.alexvas.rtsp.RtspClient.SdpInfo
import com.alexvas.rtsp.codec.AudioCodecType
import com.alexvas.rtsp.codec.AudioDecodeThread
import com.alexvas.rtsp.codec.AudioFrameQueue
import com.alexvas.rtsp.codec.FrameQueue
import com.alexvas.rtsp.codec.VideoCodecType
import com.alexvas.rtsp.codec.VideoDecodeThread
import com.alexvas.rtsp.codec.VideoDecodeThread.DecoderType
import com.alexvas.rtsp.codec.VideoDecodeThread.VideoDecoderListener
import com.alexvas.rtsp.codec.VideoFrameQueue
import com.alexvas.utils.NetUtils
import com.alexvas.utils.VideoCodecUtils
import java.net.Socket
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.min

class RtspProcessor(
    private var onVideoDecoderCreateRequested: ((
        videoMimeType: String,
        videoRotation: Int, // 0, 90, 180, 270
        videoFrameQueue: VideoFrameQueue,
        videoDecoderListener: VideoDecoderListener,
        videoDecoderType: DecoderType
    ) -> VideoDecodeThread)
) {

    class Statistics {
        var videoDecoderType = DecoderType.HARDWARE
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
    private var requestApplication = false
    private var rtspThread: RtspThread? = null
    private var videoFrameQueue = VideoFrameQueue(60)
    private var audioFrameQueue = AudioFrameQueue(10)
    private var videoDecodeThread: VideoDecodeThread? = null
    private var audioDecodeThread: AudioDecodeThread? = null
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
    var videoDecoderType = DecoderType.HARDWARE

    /**
     * Status listener for getting RTSP event updates.
     */
    var statusListener: RtspStatusListener? = null

    /**
     * Listener for getting raw data, e.g. for recording.
     */
    var dataListener: RtspDataListener? = null

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
                    val vps: ByteArray = sdpInfo.videoTrack?.vps ?: ByteArray(0)
                    val data = ByteArray(sps.size + pps.size + vps.size)
                    var offset = 0
                    sps.copyInto(data, offset, 0, sps.size)
                    offset += sps.size
                    pps.copyInto(data, offset, 0, pps.size)
                    offset += pps.size
                    vps.copyInto(data, offset, 0, vps.size)
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
                        val startNalOffset = if (sps[3] == 1.toByte()) 5 else 4
                        val spsData = NalUnitUtil.parseSpsNalUnitPayload(
                            data, startNalOffset, data.size - startNalOffset)
                        if (spsData.maxNumReorderFrames > 0) {
                            Log.w(
                                TAG, "SPS frame param max_num_reorder_frames=" +
                                    "${spsData.maxNumReorderFrames} is too high" +
                                    " for low latency decoding (expecting 0)."
                            )
                        }
                        if (debug) {
                            Log.d(TAG, "SPS frame: ${sps.toHexString(0, sps.size)}")
                            Log.d(TAG, "\t${spsData.spsDataToString()}")
                            Log.d(TAG, "PPS frame: ${pps.toHexString(0, pps.size)}")
                            if (vps.isNotEmpty())
                                Log.d(TAG, "VPS frame: ${vps.toHexString(0, vps.size)}")
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

        private var framesPerGop = 0

        override fun onRtspVideoNalUnitReceived(data: ByteArray, offset: Int, length: Int, timestamp: Long) {
            if (DEBUG) Log.v(TAG, "onRtspVideoNalUnitReceived(length=$length, timestamp=$timestamp)")

            val isH265 = videoMimeType == MediaFormat.MIMETYPE_VIDEO_HEVC
            // Search for NAL_IDR_SLICE within first 1KB maximum
            val isKeyframe = VideoCodecUtils.isAnyKeyFrame(data, offset, min(length, 1000), isH265)
            if (debug) {
                val nals = ArrayList<VideoCodecUtils.NalUnit>()
                VideoCodecUtils.getNalUnits(data, offset, length, nals, isH265)
                var b = StringBuilder()
                for (nal in nals) {
                    b
                    .append(if (isH265)
                            VideoCodecUtils.getH265NalUnitTypeString(nal.type)
                        else
                            VideoCodecUtils.getH264NalUnitTypeString(nal.type))
                    .append(" (${nal.length}), ")
                }
                if (b.length > 2)
                    b = b.removeRange(b.length - 2, b.length) as StringBuilder
                Log.d(TAG, "NALs: $b")
                @SuppressLint("UnsafeOptInUsageError")
                if (isKeyframe) {
                    val sps = VideoCodecUtils.getSpsNalUnitFromArray(
                        data,
                        offset,
                        // Check only first 100 bytes maximum. That's enough for finding SPS NAL unit.
                        Integer.min(length, 100),
                        isH265
                    )
                    Log.d(TAG,
                        "\tKey frame received ($length bytes, ts=$timestamp," +
                        " ${sps?.width}x${sps?.height}," +
                        " GoP=$framesPerGop," +
                        " profile=${sps?.profileIdc}, level=${sps?.levelIdc})")
                    framesPerGop = 0
                } else {
                    framesPerGop++
                }
            }
            videoFrameQueue.push(
                FrameQueue.VideoFrame(
                    VideoCodecType.H264,
                    isKeyframe,
                    data,
                    offset,
                    length,
                    timestamp,
                    capturedTimestampMs = System.currentTimeMillis()
                )
            )
            dataListener?.onRtspDataVideoNalUnitReceived(data, offset, length, timestamp)
        }

        override fun onRtspAudioSampleReceived(data: ByteArray, offset: Int, length: Int, timestamp: Long) {
            if (DEBUG) Log.v(TAG, "onRtspAudioSampleReceived(length=$length, timestamp=$timestamp)")
            if (length > 0) {
                audioFrameQueue.push(
                    FrameQueue.AudioFrame(
                        AudioCodecType.AAC_LC,
                        data, offset,
                        length,
                        timestamp
                    )
                )
            }
            dataListener?.onRtspDataAudioSampleReceived(data, offset, length, timestamp)
        }

        override fun onRtspApplicationDataReceived(data: ByteArray, offset: Int, length: Int, timestamp: Long) {
            if (DEBUG) Log.v(TAG, "onRtspApplicationDataReceived(length=$length, timestamp=$timestamp)")
            dataListener?.onRtspDataApplicationDataReceived(data, offset, length, timestamp)
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

    inner class RtspThread: Thread() {
        private var rtspStopped = AtomicBoolean(false)

        fun stopAsync() {
            if (DEBUG) Log.v(TAG, "stopAsync()")
            rtspStopped.set(true)
            // Wake up sleep() code
            interrupt()
        }

        override fun run() {
            onRtspClientStarted()
            val port = if (uri.port == -1) DEFAULT_RTSP_PORT else uri.port
            var socket: Socket? = null
            try {
                if (DEBUG) Log.d(TAG, "Connecting to ${uri.host.toString()}:$port...")

                socket = if (uri.scheme?.lowercase() == "rtsps")
                    NetUtils.createSslSocketAndConnect(uri.host.toString(), port, 5000)
                else
                    NetUtils.createSocketAndConnect(uri.host.toString(), port, 5000)

                // Blocking call until stopped variable is true or connection failed
                val rtspClient = RtspClient.Builder(socket, uri.toString(), rtspStopped, proxyClientListener)
                    .requestVideo(requestVideo)
                    .requestAudio(requestAudio)
                    .requestApplication(requestApplication)
                    .withDebug(debug)
                    .withUserAgent(userAgent)
                    .withCredentials(username, password)
                    .build()
                rtspClient.execute()
            } catch (e: Exception) {
                e.printStackTrace()
                uiHandler.post { proxyClientListener.onRtspFailed(e.message) }
            } finally {
                NetUtils.closeSocket(socket)
            }
            onRtspClientStopped()
        }
    }

    private val videoDecoderListener = object: VideoDecoderListener {
        override fun onVideoDecoderStarted() {
            if (DEBUG) Log.v(TAG, "onVideoDecoderStarted()")
        }

        override fun onVideoDecoderStopped() {
            if (DEBUG) Log.v(TAG, "onVideoDecoderStopped()")
        }

        override fun onVideoDecoderFailed(message: String?) {
            if (DEBUG) Log.e(TAG, "onVideoDecoderFailed(message='$message')")
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


    private fun onRtspClientStarted() {
        if (DEBUG) Log.v(TAG, "onRtspClientStarted()")
//        uiHandler.post { statusListener?.onRtspStatusConnected() }
    }

    private fun onRtspClientConnected() {
        if (DEBUG) Log.v(TAG, "onRtspClientConnected()")
        if (videoMimeType.isNotEmpty()) {
            firstFrameRendered = false
            Log.i(TAG, "Starting video decoder with mime type \"$videoMimeType\"")
            videoDecodeThread = onVideoDecoderCreateRequested.invoke(
                videoMimeType,
                videoRotation,
                videoFrameQueue,
                videoDecoderListener,
                videoDecoderType,
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
        stopDecoders()
        rtspThread = null
//        uiHandler.post { statusListener?.onRtspStatusDisconnected() }
    }

    fun init(uri: Uri, username: String?, password: String?, userAgent: String? = null) {
        if (DEBUG) Log.v(TAG, "init(uri='$uri', username='$username', password='$password', userAgent='$userAgent')")
        this.uri = uri
        this.username = username
        this.password = password
        this.userAgent = userAgent
    }

    fun start(requestVideo: Boolean, requestAudio: Boolean, requestApplication: Boolean = false) {
        if (DEBUG) Log.v(TAG, "start(requestVideo=$requestVideo, requestAudio=$requestAudio, requestApplication=$requestApplication)")
        if (rtspThread != null) rtspThread?.stopAsync()
        this.requestVideo = requestVideo
        this.requestAudio = requestAudio
        this.requestApplication = requestApplication
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

    fun stopDecoders() {
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
        private val TAG: String = RtspProcessor::class.java.simpleName
        private const val DEBUG = false

        private const val DEFAULT_RTSP_PORT = 554
    }

}
