package com.alexvas.rtsp.demo.live

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.alexvas.rtsp.RtspClient
import com.alexvas.rtsp.demo.databinding.FragmentRawBinding
import com.alexvas.rtsp.widget.toHexString
import com.alexvas.utils.NetUtils
import kotlinx.coroutines.Runnable
import java.net.Socket
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.min

@SuppressLint("LogNotTimber")
class RawFragment : Fragment() {

    private lateinit var binding: FragmentRawBinding
    private lateinit var liveViewModel: LiveViewModel

    private var statisticsTimer: Timer? = null
    private val rtspStopped = AtomicBoolean(true)

    private var rtspVideoBytesReceived: Long = 0
    private var rtspVideoFramesReceived: Long = 0
    private var rtspAudioBytesReceived: Long = 0
    private var rtspAudioSamplesReceived: Long = 0
    private var rtspApplicationBytesReceived: Long = 0
    private var rtspApplicationSamplesReceived: Long = 0

    private val rtspClientListener = object: RtspClient.RtspClientListener {
        override fun onRtspConnecting() {
            if (DEBUG) Log.v(TAG, "onRtspConnecting()")
            rtspVideoBytesReceived = 0
            rtspVideoFramesReceived = 0
            rtspAudioBytesReceived = 0
            rtspAudioSamplesReceived = 0
            rtspApplicationBytesReceived = 0
            rtspApplicationSamplesReceived = 0

            binding.apply {
                root.post {
                    updateStatistics()
                    llRtspParams.etRtspRequest.isEnabled = false
                    llRtspParams.etRtspUsername.isEnabled = false
                    llRtspParams.etRtspPassword.isEnabled = false
                    llRtspParams.cbVideo.isEnabled = false
                    llRtspParams.cbAudio.isEnabled = false
                    llRtspParams.cbApplication.isEnabled = false
                    llRtspParams.cbDebug.isEnabled = false
                    tvStatusSurface.text = "RTSP connecting"
                    bnStartStop.text = "Stop RTSP"
                }
            }
        }

        override fun onRtspConnected(sdpInfo: RtspClient.SdpInfo) {
            if (DEBUG) Log.v(TAG, "onRtspConnected()")
            binding.apply {
                root.post {
                    tvStatusSurface.text = "RTSP connected"
                }
            }
            startStatistics()
        }

        override fun onRtspVideoNalUnitReceived(data: ByteArray, offset: Int, length: Int, timestamp: Long) {
            val numBytesDump = min(length, 25) // dump max 25 bytes
            Log.i(TAG, "RTSP video data ($length bytes): ${data.toHexString(offset, offset + numBytesDump)}")
            rtspVideoBytesReceived += length
            rtspVideoFramesReceived++
        }

        override fun onRtspAudioSampleReceived(data: ByteArray, offset: Int, length: Int, timestamp: Long) {
            val numBytesDump = min(length, 25) // dump max 25 bytes
            Log.i(TAG, "RTSP audio data ($length bytes): ${data.toHexString(offset, offset + numBytesDump)}")
            rtspAudioBytesReceived += length
            rtspAudioSamplesReceived++
        }

        override fun onRtspApplicationDataReceived(data: ByteArray, offset: Int, length: Int, timestamp: Long) {
            val numBytesDump = min(length, 25) // dump max 25 bytes
            Log.i(TAG, "RTSP app data ($length bytes): ${data.toHexString(offset, offset + numBytesDump)}")
            rtspApplicationBytesReceived += length
            rtspApplicationSamplesReceived++
        }

        override fun onRtspDisconnecting() {
            if (DEBUG) Log.v(TAG, "onRtspDisconnecting()")
            binding.apply {
                root.post {
                    tvStatusSurface.text = "RTSP disconnecting"
                }
            }
            stopStatistics()
        }

        override fun onRtspDisconnected() {
            if (DEBUG) Log.v(TAG, "onRtspDisconnected()")
            binding.apply {
                root.post {
                    tvStatusSurface.text = "RTSP disconnected"
                    bnStartStop.text = "Start RTSP"
                    llRtspParams.cbVideo.isEnabled = true
                    llRtspParams.cbAudio.isEnabled = true
                    llRtspParams.cbApplication.isEnabled = true
                    llRtspParams.cbDebug.isEnabled = true
                    llRtspParams.etRtspRequest.isEnabled = true
                    llRtspParams.etRtspUsername.isEnabled = true
                    llRtspParams.etRtspPassword.isEnabled = true
                }
            }
        }

        override fun onRtspFailedUnauthorized() {
            if (DEBUG) Log.e(TAG, "onRtspFailedUnauthorized()")
            Log.e(TAG, "RTSP failed unauthorized")
            if (context == null) return
            onRtspDisconnected()
            binding.apply {
                root.post {
                    tvStatusSurface.text = "RTSP username or password invalid"
                }
            }
        }

        override fun onRtspFailed(message: String?) {
            if (DEBUG) Log.e(TAG, "onRtspFailed(message='$message')")
            Log.e(TAG, "RTSP failed with message '$message'")
            if (context == null) return
            onRtspDisconnected()
            binding.apply {
                root.post {
                    tvStatusSurface.text = "Error: $message"
                }
            }
        }
    }

    private val threadRunnable = Runnable {
        Log.i(TAG, "Thread started")
        var socket: Socket? = null
        try {
            val uri = Uri.parse(liveViewModel.rtspRequest.value)
            val port = if (uri.port == -1) DEFAULT_RTSP_PORT else uri.port
            socket = NetUtils.createSocketAndConnect(uri.host!!, port, 5000)

            val rtspClient =
                RtspClient.Builder(
                    socket,
                    uri.toString(),
                    rtspStopped,
                    rtspClientListener
                )
                    .requestVideo(binding.llRtspParams.cbVideo.isChecked)
                    .requestAudio(binding.llRtspParams.cbAudio.isChecked)
                    .requestApplication(binding.llRtspParams.cbApplication.isChecked)
                    .withDebug(binding.llRtspParams.cbDebug.isChecked)
                    .withUserAgent("rtsp-client-android")
                    .withCredentials(
                        binding.llRtspParams.etRtspUsername.text.toString(),
                        binding.llRtspParams.etRtspPassword.text.toString())
                    .build()

            rtspClient.execute()
        } catch (e: Exception) {
            e.printStackTrace()
            binding.root.post { rtspClientListener.onRtspFailed(e.message) }
        } finally {
            NetUtils.closeSocket(socket)
        }
        Log.i(TAG, "Thread stopped")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        if (DEBUG) Log.v(TAG, "onCreateView()")

        liveViewModel = ViewModelProvider(this)[LiveViewModel::class.java]
        binding = FragmentRawBinding.inflate(inflater, container, false)

        liveViewModel.initEditTexts(
            binding.llRtspParams.etRtspRequest,
            binding.llRtspParams.etRtspUsername,
            binding.llRtspParams.etRtspPassword
        )
        liveViewModel.rtspRequest.observe(viewLifecycleOwner) {
            if (binding.llRtspParams.etRtspRequest.text.toString() != it)
                binding.llRtspParams.etRtspRequest.setText(it)
        }
        liveViewModel.rtspUsername.observe(viewLifecycleOwner) {
            if (binding.llRtspParams.etRtspUsername.text.toString() != it)
                binding.llRtspParams.etRtspUsername.setText(it)
        }
        liveViewModel.rtspPassword.observe(viewLifecycleOwner) {
            if (binding.llRtspParams.etRtspPassword.text.toString() != it)
                binding.llRtspParams.etRtspPassword.setText(it)
        }

        binding.bnStartStop.setOnClickListener {
            if (DEBUG) Log.v(TAG, "onClick() rtspStopped=${rtspStopped.get()}")
            if (rtspStopped.get()) {
                rtspStopped.set(false)
                Log.i(TAG, "Thread starting...")
                Thread(threadRunnable).apply {
                    name = "RTSP raw thread"
                    start()
                }
            } else {
                Log.i(TAG, "Thread stopping...")
                rtspStopped.set(true)
            }
        }
        return binding.root
    }

    override fun onResume() {
        if (DEBUG) Log.v(TAG, "onResume()")
        super.onResume()
        liveViewModel.loadParams(requireContext())
    }

    override fun onPause() {
        if (DEBUG) Log.v(TAG, "onPause()")
        super.onPause()
        liveViewModel.saveParams(requireContext())

        stopStatistics()
        rtspStopped.set(true)
    }

    private fun updateStatistics() {
//      if (DEBUG) Log.v(TAG, "updateStatistics()")
        binding.apply {
            tvStatisticsVideo.text = "Video: $rtspVideoBytesReceived bytes, $rtspVideoFramesReceived frames"
            tvStatisticsAudio.text = "Audio: $rtspAudioBytesReceived bytes, $rtspAudioSamplesReceived samples"
            tvStatisticsApplication.text = "Application: $rtspApplicationBytesReceived bytes, $rtspApplicationSamplesReceived samples"
        }
    }

    private fun startStatistics() {
        if (DEBUG) Log.v(TAG, "startStatistics()")
        Log.i(TAG, "Start statistics")
        if (statisticsTimer == null) {
            val task: TimerTask = object : TimerTask() {
                override fun run() {
                    binding.root.post {
                        updateStatistics()
                    }
                }
            }
            statisticsTimer = Timer("${TAG}::Statistics").apply {
                schedule(task, 0, 1000)
            }
        }
    }

    private fun stopStatistics() {
        if (DEBUG) Log.v(TAG, "stopStatistics()")
        statisticsTimer?.apply {
            Log.i(TAG, "Stop statistics")
            cancel()
        }
        statisticsTimer = null
    }

    companion object {
        private val TAG: String = RawFragment::class.java.simpleName
        private const val DEBUG = true

        private const val DEFAULT_RTSP_PORT = 554
    }

}
