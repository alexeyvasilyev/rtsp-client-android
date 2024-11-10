package com.alexvas.rtsp.demo.live

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.alexvas.rtsp.codec.VideoDecodeThread
import com.alexvas.rtsp.demo.databinding.FragmentLiveBinding
import com.alexvas.rtsp.widget.RtspDataListener
import com.alexvas.rtsp.widget.RtspImageView
import com.alexvas.rtsp.widget.RtspStatusListener
import com.alexvas.rtsp.widget.toHexString
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.min

@SuppressLint("LogNotTimber")
class LiveFragment : Fragment() {

    private lateinit var binding: FragmentLiveBinding
    private lateinit var liveViewModel: LiveViewModel

    private var statisticsTimer: Timer? = null

    private val rtspStatusSurfaceListener = object: RtspStatusListener {
        override fun onRtspStatusConnecting() {
            if (DEBUG) Log.v(TAG, "onRtspStatusConnecting()")
            binding.apply {
                tvStatusSurface.text = "RTSP connecting"
                pbLoadingSurface.visibility = View.VISIBLE
                vShutterSurface.visibility = View.VISIBLE
                llRtspParams.etRtspRequest.isEnabled = false
                llRtspParams.etRtspUsername.isEnabled = false
                llRtspParams.etRtspPassword.isEnabled = false
                llRtspParams.cbVideo.isEnabled = false
                llRtspParams.cbAudio.isEnabled = false
                llRtspParams.cbApplication.isEnabled = false
                llRtspParams.cbDebug.isEnabled = false
                tgRotation.isEnabled = false
            }
        }

        override fun onRtspStatusConnected() {
            if (DEBUG) Log.v(TAG, "onRtspStatusConnected()")
            binding.apply {
                tvStatusSurface.text = "RTSP connected"
                bnStartStopSurface.text = "Stop RTSP"
                pbLoadingSurface.visibility = View.GONE
            }
            setKeepScreenOn(true)
        }

        override fun onRtspStatusDisconnecting() {
            if (DEBUG) Log.v(TAG, "onRtspStatusDisconnecting()")
            binding.apply {
                tvStatusSurface.text = "RTSP disconnecting"
            }
        }

        override fun onRtspStatusDisconnected() {
            if (DEBUG) Log.v(TAG, "onRtspStatusDisconnected()")
            binding.apply {
                tvStatusSurface.text = "RTSP disconnected"
                bnStartStopSurface.text = "Start RTSP"
                pbLoadingSurface.visibility = View.GONE
                vShutterSurface.visibility = View.VISIBLE
                pbLoadingSurface.isEnabled = false
                llRtspParams.cbVideo.isEnabled = true
                llRtspParams.cbAudio.isEnabled = true
                llRtspParams.cbApplication.isEnabled = true
                llRtspParams.cbDebug.isEnabled = true
                llRtspParams.etRtspRequest.isEnabled = true
                llRtspParams.etRtspUsername.isEnabled = true
                llRtspParams.etRtspPassword.isEnabled = true
                tgRotation.isEnabled = true
            }
            setKeepScreenOn(false)
        }

        override fun onRtspStatusFailedUnauthorized() {
            if (DEBUG) Log.e(TAG, "onRtspStatusFailedUnauthorized()")
            if (context == null) return
            onRtspStatusDisconnected()
            binding.apply {
                tvStatusSurface.text = "RTSP username or password invalid"
                pbLoadingSurface.visibility = View.GONE
            }
        }

        override fun onRtspStatusFailed(message: String?) {
            if (DEBUG) Log.e(TAG, "onRtspStatusFailed(message='$message')")
            if (context == null) return
            onRtspStatusDisconnected()
            binding.apply {
                tvStatusSurface.text = "Error: $message"
                pbLoadingSurface.visibility = View.GONE
            }
        }

        override fun onRtspFirstFrameRendered() {
            if (DEBUG) Log.v(TAG, "onRtspFirstFrameRendered()")
            binding.apply {
                vShutterSurface.visibility = View.GONE
                bnSnapshotSurface.isEnabled = true
            }
        }
    }

    private val rtspDataListener = object: RtspDataListener {
        override fun onRtspDataApplicationDataReceived(data: ByteArray, offset: Int, length: Int, timestamp: Long) {
            val numBytesDump = min(length, 25) // dump max 25 bytes
            Log.i(TAG, "RTSP app data ($length bytes): ${data.toHexString(offset, offset + numBytesDump)}")
        }
    }

    private val rtspStatusImageListener = object: RtspStatusListener {
        override fun onRtspStatusConnecting() {
            if (DEBUG) Log.v(TAG, "onRtspStatusConnecting()")
            binding.apply {
                tvStatusImage.text = "RTSP connecting"
                pbLoadingImage.visibility = View.VISIBLE
                vShutterImage.visibility = View.VISIBLE
            }
        }

        override fun onRtspStatusConnected() {
            if (DEBUG) Log.v(TAG, "onRtspStatusConnected()")
            binding.apply {
                tvStatusImage.text = "RTSP connected"
                bnStartStopImage.text = "Stop RTSP"
                pbLoadingImage.visibility = View.GONE
            }
            setKeepScreenOn(true)
        }

        override fun onRtspStatusDisconnecting() {
            if (DEBUG) Log.v(TAG, "onRtspStatusDisconnecting()")
            binding.apply {
                tvStatusImage.text = "RTSP disconnecting"
            }
        }

        override fun onRtspStatusDisconnected() {
            if (DEBUG) Log.v(TAG, "onRtspStatusDisconnected()")
            binding.apply {
                tvStatusImage.text = "RTSP disconnected"
                bnStartStopImage.text = "Start RTSP"
                pbLoadingImage.visibility = View.GONE
                vShutterImage.visibility = View.VISIBLE
                pbLoadingImage.isEnabled = false
            }
            setKeepScreenOn(false)
        }

        override fun onRtspStatusFailedUnauthorized() {
            if (DEBUG) Log.e(TAG, "onRtspStatusFailedUnauthorized()")
            if (context == null) return
            onRtspStatusDisconnected()
            binding.apply {
                tvStatusImage.text = "RTSP username or password invalid"
                pbLoadingImage.visibility = View.GONE
            }
        }

        override fun onRtspStatusFailed(message: String?) {
            if (DEBUG) Log.e(TAG, "onRtspStatusFailed(message='$message')")
            if (context == null) return
            onRtspStatusDisconnected()
            binding.apply {
                tvStatusImage.text = "Error: $message"
                pbLoadingImage.visibility = View.GONE
            }
        }

        override fun onRtspFirstFrameRendered() {
            if (DEBUG) Log.v(TAG, "onRtspFirstFrameRendered()")
            binding.apply {
                vShutterImage.visibility = View.GONE
            }
        }
    }

    private fun getSnapshot(): Bitmap? {
        if (DEBUG) Log.v(TAG, "getSnapshot()")
        val surfaceBitmap = Bitmap.createBitmap(1920, 1080, Bitmap.Config.ARGB_8888)
        val lock = Object()
        val success = AtomicBoolean(false)
        val thread = HandlerThread("PixelCopyHelper")
        thread.start()
        val sHandler = Handler(thread.looper)
        val listener = PixelCopy.OnPixelCopyFinishedListener { copyResult ->
            success.set(copyResult == PixelCopy.SUCCESS)
            synchronized (lock) {
                lock.notify()
            }
        }
        synchronized (lock) {
            PixelCopy.request(binding.svVideoSurface.holder.surface, surfaceBitmap, listener, sHandler)
            lock.wait()
        }
        thread.quitSafely()
        return if (success.get()) surfaceBitmap else null
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        if (DEBUG) Log.v(TAG, "onCreateView()")

        liveViewModel = ViewModelProvider(this)[LiveViewModel::class.java]
        binding = FragmentLiveBinding.inflate(inflater, container, false)

        binding.bnVideoDecoderGroup.check(binding.bnVideoDecoderHardware.id)

        binding.svVideoSurface.setStatusListener(rtspStatusSurfaceListener)
        binding.svVideoSurface.setDataListener(rtspDataListener)
        binding.ivVideoImage.setStatusListener(rtspStatusImageListener)
        binding.ivVideoImage.setDataListener(rtspDataListener)

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

        binding.cbExperimentalRewriteSps.setOnCheckedChangeListener { _, isChecked ->
            binding.svVideoSurface.experimentalUpdateSpsFrameWithLowLatencyParams = isChecked
        }

        binding.bnRotate0.setOnClickListener {
            binding.svVideoSurface.videoRotation = 0
            binding.ivVideoImage.videoRotation = 0
        }

        binding.bnRotate90.setOnClickListener {
            binding.svVideoSurface.videoRotation = 90
            binding.ivVideoImage.videoRotation = 90
        }

        binding.bnRotate180.setOnClickListener {
            binding.svVideoSurface.videoRotation = 180
            binding.ivVideoImage.videoRotation = 180
        }

        binding.bnRotate270.setOnClickListener {
            binding.svVideoSurface.videoRotation = 270
            binding.ivVideoImage.videoRotation = 270
        }

        binding.bnRotate0.performClick()

        binding.bnVideoDecoderHardware.setOnClickListener {
            binding.svVideoSurface.videoDecoderType = VideoDecodeThread.DecoderType.HARDWARE
            binding.ivVideoImage.videoDecoderType = VideoDecodeThread.DecoderType.HARDWARE
        }

        binding.bnVideoDecoderSoftware.setOnClickListener {
            binding.svVideoSurface.videoDecoderType = VideoDecodeThread.DecoderType.SOFTWARE
            binding.ivVideoImage.videoDecoderType = VideoDecodeThread.DecoderType.SOFTWARE
        }

        binding.bnStartStopSurface.setOnClickListener {
            if (binding.svVideoSurface.isStarted()) {
                binding.svVideoSurface.stop()
                stopStatistics()
            } else {
                val uri = Uri.parse(liveViewModel.rtspRequest.value)
                binding.svVideoSurface.apply {
                    init(
                        uri,
                        username = liveViewModel.rtspUsername.value,
                        password = liveViewModel.rtspPassword.value,
                        userAgent = "rtsp-client-android")
                    debug = binding.llRtspParams.cbDebug.isChecked
                    start(
                        requestVideo = binding.llRtspParams.cbVideo.isChecked,
                        requestAudio = binding.llRtspParams.cbAudio.isChecked,
                        requestApplication = binding.llRtspParams.cbApplication.isChecked
                    )
                }
                startStatistics()
            }
        }

        binding.bnStartStopImage.setOnClickListener {
            if (binding.ivVideoImage.isStarted()) {
                binding.ivVideoImage.stop()
                stopStatistics()
            } else {
                val uri = Uri.parse(liveViewModel.rtspRequest.value)
                binding.ivVideoImage.apply {
                    init(uri, liveViewModel.rtspUsername.value, liveViewModel.rtspPassword.value, "rtsp-client-android")
                    debug = binding.llRtspParams.cbDebug.isChecked
                    onRtspImageBitmapListener = object : RtspImageView.RtspImageBitmapListener {
                        override fun onRtspImageBitmapObtained(bitmap: Bitmap) {
                            // TODO: You can send bitmap for processing
                        }
                    }
                    start(
                        requestVideo = binding.llRtspParams.cbVideo.isChecked,
                        requestAudio = binding.llRtspParams.cbAudio.isChecked,
                        requestApplication = binding.llRtspParams.cbApplication.isChecked
                    )
                }
                startStatistics()
            }
        }

        binding.pbLoadingSurface.setOnClickListener {
            val bitmap = getSnapshot()
            // TODO Save snapshot to DCIM folder
            if (bitmap != null) {
                Toast.makeText(requireContext(), "Snapshot succeeded", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(requireContext(), "Snapshot failed", Toast.LENGTH_LONG).show()
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
        val started = binding.svVideoSurface.isStarted()
        if (DEBUG) Log.v(TAG, "onPause(), started:$started")
        super.onPause()
        liveViewModel.saveParams(requireContext())

        if (started) {
            binding.svVideoSurface.stop()
            stopStatistics()
        }
    }

    private fun startStatistics() {
        if (DEBUG) Log.v(TAG, "startStatistics()")
        Log.i(TAG, "Start statistics")
        if (statisticsTimer == null) {
            val task: TimerTask = object : TimerTask() {
                override fun run() {
                    val statistics = binding.svVideoSurface.statistics
                    val text =
                        "Video decoder: ${statistics.videoDecoderType.toString().lowercase()} ${if (statistics.videoDecoderName.isNullOrEmpty()) "" else "(${statistics.videoDecoderName})"}" +
                        "\nVideo decoder latency: ${statistics.videoDecoderLatencyMsec} ms"
//                        "\nNetwork latency: "

//                    // Assume that difference between current Android time and camera time cannot be more than 5 sec.
//                    // Otherwise time need to be synchronized on both devices.
//                    text += if (statistics.networkLatencyMsec == -1) {
//                        "-"
//                    } else if (statistics.networkLatencyMsec < 0 || statistics.networkLatencyMsec > TimeUnit.SECONDS.toMillis(5)) {
//                        "[time out of sync]"
//                    } else {
//                        "${statistics.networkLatencyMsec} ms"
//                    }

                    binding.tvStatistics.post {
                        binding.tvStatistics.text = text
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

    private fun setKeepScreenOn(enable: Boolean) {
        if (DEBUG) Log.v(TAG, "setKeepScreenOn(enable=$enable)")
        if (enable) {
            activity?.apply {
                window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                Log.i(TAG, "Enabled keep screen on")
            }
        } else {
            activity?.apply {
                window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                Log.i(TAG, "Disabled keep screen on")
            }
        }
    }
    companion object {
        private val TAG: String = LiveFragment::class.java.simpleName
        private const val DEBUG = true
    }

}
