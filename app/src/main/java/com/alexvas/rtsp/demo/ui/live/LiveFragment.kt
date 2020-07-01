package com.alexvas.rtsp.demo.ui.live

import android.media.MediaCodec
import android.media.MediaCodec.BufferInfo
import android.media.MediaFormat
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
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.alexvas.rtsp.RtspClient
import com.alexvas.rtsp.demo.R
import com.alexvas.utils.NetUtils
import com.alexvas.utils.VideoCodecUtils
import com.alexvas.utils.VideoCodecUtils.NalUnit
import java.net.Socket
import java.nio.ByteBuffer
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

private const val DEFAULT_RTSP_PORT = 554
private val TAG: String = LiveFragment::class.java.simpleName
private const val DEBUG = true

class LiveFragment : Fragment(), SurfaceHolder.Callback {

    private lateinit var liveViewModel: LiveViewModel
    private var videoFrameQueue: VideoFrameQueue = VideoFrameQueue()
    private var rtspThread: RtspThread? = null
    private var decodeThread: VideoDecodeThread? = null
    private var rtspStopped: AtomicBoolean = AtomicBoolean(true)
    private var btnStartStop: Button? = null
    private var surface: Surface? = null
    private var surfaceWidth: Int = 1920
    private var surfaceHeight: Int = 1080
    private var checkVideo: CheckBox? = null
    private var checkAudio: CheckBox? = null
    private var videoMimeType: String = ""
    private var audioMimeType: String = ""

    fun onRtspClientStarted() {
        if (DEBUG) Log.v(TAG, "onRtspClientStarted()")
        rtspStopped.set(false)
        btnStartStop?.text = "Stop RTSP"
    }

    fun onRtspClientStopped() {
        if (DEBUG) Log.v(TAG, "onRtspClientStopped()")
        rtspStopped.set(true)
        btnStartStop?.text = "Start RTSP"
        decodeThread?.interrupt()
        decodeThread = null
    }

    fun onRtspClientConnected() {
        if (DEBUG) Log.v(TAG, "onRtspClientConnected()")
        if (videoMimeType.isNotEmpty()) {
            Log.i(TAG, "Starting video decoder with mime type \"$videoMimeType\"")
            decodeThread = VideoDecodeThread(surface!!, videoMimeType, surfaceWidth, surfaceHeight, videoFrameQueue)
            decodeThread?.start()
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
                        when (sdpInfo.videoTrack?.videoCodec) {
                            RtspClient.VIDEO_CODEC_H264 -> videoMimeType = "video/avc";
                            RtspClient.VIDEO_CODEC_H265 -> videoMimeType = "video/hevc";
                        }
                        videoFrameQueue.clear()
                        val sps: ByteArray? = sdpInfo.videoTrack?.sps
                        val pps: ByteArray? = sdpInfo.videoTrack?.pps
                        // Initialize decoder
                        if (sps != null && pps != null) {
                            val data = ByteArray(sps.size + pps.size)
                            sps.copyInto(data, 0, 0, sps.size)
                            pps.copyInto(data, sps.size, 0, pps.size)
                            videoFrameQueue.push(VideoFrameQueue.VideoFrame(data, 0, data.size, 0))
                        } else {
                            if (DEBUG) Log.d(TAG, "RTSP SPS and PPS NAL units missed in SDP")
                        }
                    }
                    if (sdpInfo.audioTrack != null) {
                        when (sdpInfo.videoTrack?.videoCodec) {
                            RtspClient.AUDIO_CODEC_AAC -> audioMimeType = "audio/mp4a-latm";
                        }
                    }
                    onRtspClientConnected();
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
                    videoFrameQueue.push(VideoFrameQueue.VideoFrame(data, offset, length, timestamp))
                }

                override fun onRtspAudioSampleReceived(data: ByteArray, offset: Int, length: Int, timestamp: Long) {
                    if (DEBUG) Log.v(TAG, "onRtspAudioSampleReceived(length=$length, timestamp=$timestamp)")
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
                val socket: Socket = NetUtils.createSocketAndConnect(uri.host.toString(), port, 10000)

                // Blocking call until stopped variable is true or connection failed
                val rtspClient = RtspClient.Builder(socket, uri.toString(), rtspStopped, listener)
                        .requestVideo(checkVideo!!.isChecked)
                        .requestAudio(checkAudio!!.isChecked)
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

        liveViewModel.rtspRequest.observe(viewLifecycleOwner, Observer {
            if (textRtspRequest.text.toString() != it)
                textRtspRequest.setText(it)
        })
        liveViewModel.rtspUsername.observe(viewLifecycleOwner, Observer {
            if (textRtspUsername.text.toString() != it)
                textRtspUsername.setText(it)
        })
        liveViewModel.rtspPassword.observe(viewLifecycleOwner, Observer {
            if (textRtspPassword.text.toString() != it)
                textRtspPassword.setText(it)
        })

        btnStartStop = root.findViewById(R.id.button_start_stop_rtsp)
        btnStartStop?.setOnClickListener {
            if (rtspStopped.get()) {
                rtspThread = RtspThread()
                rtspThread?.start()
            } else {
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
        if (decodeThread != null) {
            decodeThread?.interrupt()
            decodeThread = VideoDecodeThread(surface!!, videoMimeType, width, height, videoFrameQueue)
            decodeThread?.start()
        }
    }

    // SurfaceHolder.Callback
    override fun surfaceDestroyed(holder: SurfaceHolder) {
        if (DEBUG) Log.v(TAG, "surfaceDestroyed()")
        decodeThread?.interrupt()
        decodeThread = null
    }

    // SurfaceHolder.Callback
    override fun surfaceCreated(holder: SurfaceHolder) {
        if (DEBUG) Log.v(TAG, "surfaceCreated()")
    }

}

private class VideoDecodeThread(
        private val surface: Surface,
        private val mimeType: String,
        private val width: Int,
        private val height: Int,
        private val videoFrameQueue: VideoFrameQueue) : Thread() {
    private var decoder: MediaCodec? = null
    private val TAG: String = VideoDecodeThread::class.java.simpleName
    private val DEBUG = true
    override fun run() {
        if (DEBUG) Log.d(TAG, "VideoDecodeThread started")
        decoder = MediaCodec.createDecoderByType(mimeType)
        val format = MediaFormat.createVideoFormat(mimeType, width, height)
        decoder!!.configure(format, surface, null, 0)
        if (decoder == null) {
            Log.e(TAG, "Can't find video info!")
            return
        }
        decoder!!.start()
        val bufferInfo = BufferInfo()
        while (!interrupted()) {
            val inIndex: Int = decoder!!.dequeueInputBuffer(10000L)
            if (inIndex >= 0) {
                // fill inputBuffers[inputBufferIndex] with valid data
                val byteBuffer: ByteBuffer? = decoder!!.getInputBuffer(inIndex)
                byteBuffer?.rewind()

                // Preventing BufferOverflowException
//              if (length > byteBuffer.limit()) throw DecoderFatalException("Error")

                val videoFrame: VideoFrameQueue.VideoFrame?
                try {
                    videoFrame = videoFrameQueue.pop()
                    if (videoFrame == null) {
                        Log.d(TAG, "Empty frame")
                    } else {
                        byteBuffer!!.put(videoFrame.data, videoFrame.offset, videoFrame.length)
                        decoder!!.queueInputBuffer(inIndex, videoFrame.offset, videoFrame.length, videoFrame.timestamp, 0)
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
                        decoder!!.releaseOutputBuffer(outIndex, bufferInfo.size != 0)
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
        videoFrameQueue.clear()
        if (DEBUG) Log.d(TAG, "VideoDecodeThread stopped")
    }
}

private class VideoFrameQueue {

    class VideoFrame(val data: ByteArray, val offset: Int, val length: Int, val timestamp: Long)

    val TAG: String = VideoFrameQueue::class.java.simpleName
    val queue: BlockingQueue<VideoFrame> = ArrayBlockingQueue(60)

    @Throws(InterruptedException::class)
    fun push(frame: VideoFrame): Boolean {
        if (queue.offer(frame, 5, TimeUnit.MILLISECONDS)) {
            return true
        }
        Log.w(TAG, "Cannot add frame, queue is full")
        return false
    }

    @Throws(InterruptedException::class)
    fun pop(): VideoFrame? {
        try {
            val frame: VideoFrame? = queue.poll(1000, TimeUnit.MILLISECONDS)
            if (frame == null) {
                Log.w(TAG, "Cannot get frame, queue is empty")
            }
            return frame
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
        }
        return null
    }

    fun clear() {
        queue.clear()
    }
}