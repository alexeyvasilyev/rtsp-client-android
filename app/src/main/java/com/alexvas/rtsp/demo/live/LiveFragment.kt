package com.alexvas.rtsp.demo.live

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.alexvas.rtsp.codec.VideoDecodeThread
import com.alexvas.rtsp.demo.databinding.FragmentLiveBinding
import com.alexvas.rtsp.widget.RtspImageView
import com.alexvas.rtsp.widget.RtspStatusListener
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.atomic.AtomicBoolean

@SuppressLint("LogNotTimber")
class LiveFragment : Fragment() {

    private lateinit var binding: FragmentLiveBinding
    private lateinit var liveViewModel: LiveViewModel

    private val rtspStatusSurfaceListener = object: RtspStatusListener {
        override fun onRtspStatusConnecting() {
            binding.apply {
                tvStatusSurface.text = "RTSP connecting"
                pbLoadingSurface.visibility = View.VISIBLE
                vShutterSurface.visibility = View.VISIBLE
                etRtspRequest.isEnabled = false
                etRtspUsername.isEnabled = false
                etRtspPassword.isEnabled = false
                cbVideo.isEnabled = false
                cbAudio.isEnabled = false
                cbDebug.isEnabled = false
                tgRotation.isEnabled = false
            }
        }

        override fun onRtspStatusConnected() {
            binding.apply {
                tvStatusSurface.text = "RTSP connected"
                bnStartStopSurface.text = "Stop RTSP"
                pbLoadingSurface.visibility = View.GONE
            }
            setKeepScreenOn(true)
        }

        override fun onRtspStatusDisconnecting() {
            binding.apply {
                tvStatusSurface.text = "RTSP disconnecting"
            }
        }

        override fun onRtspStatusDisconnected() {
            binding.apply {
                tvStatusSurface.text = "RTSP disconnected"
                bnStartStopSurface.text = "Start RTSP"
                pbLoadingSurface.visibility = View.GONE
                vShutterSurface.visibility = View.VISIBLE
                pbLoadingSurface.isEnabled = false
                cbVideo.isEnabled = true
                cbAudio.isEnabled = true
                cbDebug.isEnabled = true
                etRtspRequest.isEnabled = true
                etRtspUsername.isEnabled = true
                etRtspPassword.isEnabled = true
                tgRotation.isEnabled = true
            }
            setKeepScreenOn(false)
        }

        override fun onRtspStatusFailedUnauthorized() {
            if (context == null) return
            binding.apply {
                tvStatusSurface.text = "RTSP username or password invalid"
                pbLoadingSurface.visibility = View.GONE
            }
            Toast.makeText(context, binding.tvStatusSurface.text , Toast.LENGTH_LONG).show()
        }

        override fun onRtspStatusFailed(message: String?) {
            if (context == null) return
            binding.apply {
                tvStatusSurface.text = "Error: $message"
                Toast.makeText(context, tvStatusSurface.text, Toast.LENGTH_LONG).show()
                pbLoadingSurface.visibility = View.GONE
            }
        }

        override fun onRtspFirstFrameRendered() {
            binding.apply {
                vShutterSurface.visibility = View.GONE
                bnSnapshotSurface.isEnabled = true
            }
        }
    }

    private val rtspStatusImageListener = object: RtspStatusListener {
        override fun onRtspStatusConnecting() {
            binding.apply {
                tvStatusImage.text = "RTSP connecting"
                pbLoadingImage.visibility = View.VISIBLE
                vShutterImage.visibility = View.VISIBLE
            }
        }

        override fun onRtspStatusConnected() {
            binding.apply {
                tvStatusImage.text = "RTSP connected"
                bnStartStopImage.text = "Stop RTSP"
                pbLoadingImage.visibility = View.GONE
            }
            setKeepScreenOn(true)
        }

        override fun onRtspStatusDisconnecting() {
            binding.apply {
                tvStatusImage.text = "RTSP disconnecting"
            }
        }

        override fun onRtspStatusDisconnected() {
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
            if (context == null) return
            binding.apply {
                tvStatusImage.text = "RTSP username or password invalid"
                pbLoadingImage.visibility = View.GONE
            }
            Toast.makeText(context, binding.tvStatusImage.text , Toast.LENGTH_LONG).show()
        }

        override fun onRtspStatusFailed(message: String?) {
            if (context == null) return
            binding.apply {
                tvStatusImage.text = "Error: $message"
                Toast.makeText(context, tvStatusImage.text, Toast.LENGTH_LONG).show()
                pbLoadingImage.visibility = View.GONE
            }
        }

        override fun onRtspFirstFrameRendered() {
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
        binding.ivVideoImage.setStatusListener(rtspStatusImageListener)
        binding.etRtspRequest.addTextChangedListener(object : TextWatcher {
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
        binding.etRtspUsername.addTextChangedListener(object : TextWatcher {
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
        binding.etRtspPassword.addTextChangedListener(object : TextWatcher {
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

        liveViewModel.rtspRequest.observe(viewLifecycleOwner) {
            if (binding.etRtspRequest.text.toString() != it)
                binding.etRtspRequest.setText(it)
        }
        liveViewModel.rtspUsername.observe(viewLifecycleOwner) {
            if (binding.etRtspUsername.text.toString() != it)
                binding.etRtspUsername.setText(it)
        }
        liveViewModel.rtspPassword.observe(viewLifecycleOwner) {
            if (binding.etRtspPassword.text.toString() != it)
                binding.etRtspPassword.setText(it)
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
                    init(uri, liveViewModel.rtspUsername.value, liveViewModel.rtspPassword.value, "rtsp-client-android")
                    debug = binding.cbDebug.isChecked
                    start(binding.cbVideo.isChecked, binding.cbAudio.isChecked)
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
                    debug = binding.cbDebug.isChecked
                    onRtspImageBitmapListener = object : RtspImageView.RtspImageBitmapListener {
                        override fun onRtspImageBitmapObtained(bitmap: Bitmap) {
                            // TODO: You can send bitmap for processing
                        }
                    }
                    start(binding.cbVideo.isChecked, binding.cbAudio.isChecked)
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

    private var statisticsTimer: Timer? = null

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
