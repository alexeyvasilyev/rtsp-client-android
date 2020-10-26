package com.alexvas.rtsp.demo.ui.live

import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.alexvas.rtsp.RtspClient
import com.alexvas.rtsp.demo.R
import com.alexvas.rtsp.demo.decode.AudioDecodeThread
import com.alexvas.rtsp.demo.decode.VideoDecodeThread
import com.alexvas.rtsp.demo.decode.FrameQueue
import com.alexvas.utils.NetUtils
import com.alexvas.utils.VideoCodecUtils
import com.alexvas.utils.VideoCodecUtils.NalUnit
import java.net.Socket
import java.util.concurrent.atomic.AtomicBoolean

private const val DEFAULT_RTSP_PORT = 554
private val TAG: String = LiveFragment::class.java.simpleName
private const val DEBUG = true

class LiveFragment : Fragment(), SurfaceHolder.Callback {

    private lateinit var liveViewModel: LiveViewModel
    private var videoFrameQueue: FrameQueue = FrameQueue()
    private var audioFrameQueue: FrameQueue = FrameQueue()
    private var rtspThread: RtspThread? = null
    private var videoDecodeThread: VideoDecodeThread? = null
    private var audioDecodeThread: AudioDecodeThread? = null
    private var rtspStopped: AtomicBoolean = AtomicBoolean(true)
    private var btnStartStop: Button? = null
    private var surface: Surface? = null
    private var surfaceWidth: Int = 1920
    private var surfaceHeight: Int = 1080
    private var checkVideo: CheckBox? = null
    private var checkAudio: CheckBox? = null
    private var videoMimeType: String = ""
    private var audioMimeType: String = ""
    private var audioSampleRate: Int = 0
    private var audioChannelCount: Int = 0

    fun onRtspClientStarted() {
        if (DEBUG) Log.v(TAG, "onRtspClientStarted()")
        rtspStopped.set(false)
        btnStartStop?.text = "Stop RTSP"
    }

    fun onRtspClientStopped() {
        if (DEBUG) Log.v(TAG, "onRtspClientStopped()")
        rtspStopped.set(true)
        btnStartStop?.text = "Start RTSP"
        videoDecodeThread?.interrupt()
        videoDecodeThread = null
        audioDecodeThread?.interrupt()
        audioDecodeThread = null
    }

    fun onRtspClientConnected() {
        if (DEBUG) Log.v(TAG, "onRtspClientConnected()")
        if (videoMimeType.isNotEmpty() && checkVideo!!.isChecked) {
            Log.i(TAG, "Starting video decoder with mime type \"$videoMimeType\"")
            videoDecodeThread = VideoDecodeThread(surface!!, videoMimeType, surfaceWidth, surfaceHeight, videoFrameQueue)
            videoDecodeThread?.start()
        }
        if (audioMimeType.isNotEmpty() && checkAudio!!.isChecked) {
            Log.i(TAG, "Starting audio decoder with mime type \"$audioMimeType\"")
            audioDecodeThread = AudioDecodeThread(audioMimeType, audioSampleRate, audioChannelCount, audioFrameQueue)
            audioDecodeThread?.start()
        }
    }

    inner class RtspThread: Thread() {
        override fun run() {
            Handler(Looper.getMainLooper()).post { onRtspClientStarted() }
            val listener = object: RtspClient.RtspClientListener {
                override fun onRtspDisconnected() {
                    if (DEBUG) Log.v(TAG, "onRtspDisconnected()")
                    rtspStopped.set(true)
                }

                override fun onRtspFailed(message: String?) {
                    Log.e(TAG, "onRtspFailed(message=\"$message\")")
                    rtspStopped.set(true)
                }

                override fun onRtspConnected(sdpInfo: RtspClient.SdpInfo) {
                    if (DEBUG) Log.v(TAG, "onRtspConnected()")
                    if (sdpInfo.videoTrack != null) {
                        videoFrameQueue.clear()
                        when (sdpInfo.videoTrack?.videoCodec) {
                            RtspClient.VIDEO_CODEC_H264 -> videoMimeType = "video/avc"
                            RtspClient.VIDEO_CODEC_H265 -> videoMimeType = "video/hevc"
                        }
                        when (sdpInfo.audioTrack?.audioCodec) {
                            RtspClient.AUDIO_CODEC_AAC -> audioMimeType = "audio/mp4a-latm"
                        }
                        val sps: ByteArray? = sdpInfo.videoTrack?.sps
                        val pps: ByteArray? = sdpInfo.videoTrack?.pps
                        // Initialize decoder
                        if (sps != null && pps != null) {
                            val data = ByteArray(sps.size + pps.size)
                            sps.copyInto(data, 0, 0, sps.size)
                            pps.copyInto(data, sps.size, 0, pps.size)
                            videoFrameQueue.push(FrameQueue.Frame(data, 0, data.size, 0))
                        } else {
                            if (DEBUG) Log.d(TAG, "RTSP SPS and PPS NAL units missed in SDP")
                        }
                    }
                    if (sdpInfo.audioTrack != null) {
                        audioFrameQueue.clear()
                        when (sdpInfo.audioTrack?.audioCodec) {
                            RtspClient.AUDIO_CODEC_AAC -> audioMimeType = "audio/mp4a-latm"
                        }
                        audioSampleRate = sdpInfo.audioTrack?.sampleRateHz!!
                        audioChannelCount = sdpInfo.audioTrack?.channels!!
                    }
                    onRtspClientConnected()
                }

                override fun onRtspFailedUnauthorized() {
                    Log.e(TAG, "onRtspFailedUnauthorized()")
                    rtspStopped.set(true)
                }

                override fun onRtspVideoNalUnitReceived(data: ByteArray, offset: Int, length: Int, timestamp: Long) {
                    if (DEBUG) Log.v(TAG, "onRtspVideoNalUnitReceived(length=$length, timestamp=$timestamp)")
                    if (DEBUG) {
                        val nals: ArrayList<NalUnit> = ArrayList()
                        val numNals = VideoCodecUtils.getH264NalUnits(data, offset, length - 1, nals)
                        val builder = StringBuilder()
                        for (nal in nals) {
                            builder.append(", ")
                            builder.append(VideoCodecUtils.getH264NalUnitTypeString(nal.type))
                            builder.append(" (")
                            builder.append(nal.length)
                            builder.append(" bytes)")
                        }
                        var textNals = builder.toString()
                        if (numNals > 0) {
                            textNals = textNals.substring(2)
                        }
                        Log.i(TAG, "NALs ($numNals): $textNals")
                    }
                    videoFrameQueue.push(FrameQueue.Frame(data, offset, length, timestamp))
                }

                override fun onRtspAudioSampleReceived(data: ByteArray, offset: Int, length: Int, timestamp: Long) {
                    if (DEBUG) Log.v(TAG, "onRtspAudioSampleReceived(length=$length, timestamp=$timestamp)")
                    audioFrameQueue.push(FrameQueue.Frame(data, offset, length, timestamp))
                }

                override fun onRtspConnecting() {
                    if (DEBUG) Log.v(TAG, "onRtspConnecting()")
                }
            }
            val uri: Uri = Uri.parse(liveViewModel.rtspRequest.value)
            val port = if (uri.port == -1) DEFAULT_RTSP_PORT else uri.port
            val username = liveViewModel.rtspUsername.value
            val password = liveViewModel.rtspPassword.value
            try {
                if (DEBUG) Log.d(TAG, "Connecting to ${uri.host.toString()}:$port...")
                val socket: Socket = NetUtils.createSocketAndConnect(uri.host.toString(), port, 5000)

                // Blocking call until stopped variable is true or connection failed
                val rtspClient = RtspClient.Builder(socket, uri.toString(), rtspStopped, listener)
                        .requestVideo(checkVideo!!.isChecked)
                        .requestAudio(checkAudio!!.isChecked)
                        .withDebug(true)
                        .withUserAgent("RTSP test")
                        .withCredentials(username, password)
                        .build()
                rtspClient.execute()

                NetUtils.closeSocket(socket)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            Handler(Looper.getMainLooper()).post { onRtspClientStopped() }
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        liveViewModel =
                ViewModelProvider(this).get(LiveViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_live, container, false)
//      val textView: TextView = root.findViewById(R.id.text_live)
        val textRtspRequest: EditText = root.findViewById(R.id.edit_rtsp_request)
        val textRtspUsername: EditText = root.findViewById(R.id.edit_rtsp_username)
        val textRtspPassword: EditText = root.findViewById(R.id.edit_rtsp_password)
        val surfaceView: SurfaceView = root.findViewById(R.id.surfaceView)
        checkVideo = root.findViewById(R.id.check_video)
        checkAudio = root.findViewById(R.id.check_audio)

        surfaceView.holder.addCallback(this)
        surface = surfaceView.holder.surface

        textRtspRequest.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val text = s.toString()
                if (text != liveViewModel.rtspRequest.value) {
                    liveViewModel.rtspRequest.value = text
                }
            }
        })
        textRtspUsername.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val text = s.toString()
                if (text != liveViewModel.rtspUsername.value) {
                    liveViewModel.rtspUsername.value = text
                }
            }
        })
        textRtspPassword.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val text = s.toString()
                if (text != liveViewModel.rtspPassword.value) {
                    liveViewModel.rtspPassword.value = text
                }
            }
        })

        liveViewModel.rtspRequest.observe(viewLifecycleOwner, {
            if (textRtspRequest.text.toString() != it)
                textRtspRequest.setText(it)
        })
        liveViewModel.rtspUsername.observe(viewLifecycleOwner, {
            if (textRtspUsername.text.toString() != it)
                textRtspUsername.setText(it)
        })
        liveViewModel.rtspPassword.observe(viewLifecycleOwner, {
            if (textRtspPassword.text.toString() != it)
                textRtspPassword.setText(it)
        })

        btnStartStop = root.findViewById(R.id.button_start_stop_rtsp)
        btnStartStop?.setOnClickListener {
            if (rtspStopped.get()) {
                if (DEBUG) Log.d(TAG, "Starting RTSP thread...")
                rtspThread = RtspThread()
                rtspThread?.start()
            } else {
                if (DEBUG) Log.d(TAG, "Stopping RTSP thread...")
                rtspStopped.set(true)
                rtspThread?.interrupt()
            }
        }
        return root
    }

    override fun onStart() {
        if (DEBUG) Log.v(TAG, "onStart()")
        super.onStart()
        liveViewModel.loadParams(context)
    }

    override fun onStop() {
        if (DEBUG) Log.v(TAG, "onStop()")
        super.onStop()
        liveViewModel.saveParams(context)
        onRtspClientStopped()
    }

    // SurfaceHolder.Callback
    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        if (DEBUG) Log.v(TAG, "surfaceChanged(format=$format, width=$width, height=$height)")
        surface = holder.surface
        surfaceWidth = width
        surfaceHeight = height
        if (videoDecodeThread != null) {
            videoDecodeThread?.interrupt()
            videoDecodeThread = VideoDecodeThread(surface!!, videoMimeType, width, height, videoFrameQueue)
            videoDecodeThread?.start()
        }
    }

    // SurfaceHolder.Callback
    override fun surfaceDestroyed(holder: SurfaceHolder) {
        if (DEBUG) Log.v(TAG, "surfaceDestroyed()")
        videoDecodeThread?.interrupt()
        videoDecodeThread = null
    }

    // SurfaceHolder.Callback
    override fun surfaceCreated(holder: SurfaceHolder) {
        if (DEBUG) Log.v(TAG, "surfaceCreated()")
    }

}
