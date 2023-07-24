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
import com.alexvas.rtsp.demo.databinding.FragmentLiveBinding
import com.alexvas.rtsp.widget.RtspSurfaceView
import java.util.concurrent.atomic.AtomicBoolean


@SuppressLint("LogNotTimber")
class LiveFragment : Fragment() {

    private lateinit var binding: FragmentLiveBinding
    private lateinit var liveViewModel: LiveViewModel

    private val rtspStatusListener = object: RtspSurfaceView.RtspStatusListener {
        override fun onRtspStatusConnecting() {
            binding.apply {
                tvStatus.text = "RTSP connecting"
                pbLoading.visibility = View.VISIBLE
                vShutter.visibility = View.VISIBLE
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
                tvStatus.text = "RTSP connected"
                bnStartStop.text = "Stop RTSP"
                pbLoading.visibility = View.GONE
            }
        }

        override fun onRtspStatusDisconnecting() {
            binding.apply {
                tvStatus.text = "RTSP disconnecting"
            }
        }

        override fun onRtspStatusDisconnected() {
            binding.apply {
                tvStatus.text = "RTSP disconnected"
                bnStartStop.text = "Start RTSP"
                pbLoading.visibility = View.GONE
                vShutter.visibility = View.VISIBLE
                bnSnapshot.isEnabled = false
                cbVideo.isEnabled = true
                cbAudio.isEnabled = true
                cbDebug.isEnabled = true
                etRtspRequest.isEnabled = true
                etRtspUsername.isEnabled = true
                etRtspPassword.isEnabled = true
                tgRotation.isEnabled = true
            }
        }

        override fun onRtspStatusFailedUnauthorized() {
            if (context == null) return
            binding.apply {
                tvStatus.text = "RTSP username or password invalid"
                pbLoading.visibility = View.GONE
            }
            Toast.makeText(context, binding.tvStatus.text , Toast.LENGTH_LONG).show()
        }

        override fun onRtspStatusFailed(message: String?) {
            if (context == null) return
            binding.apply {
                tvStatus.text = "Error: $message"
                Toast.makeText(context, tvStatus.text, Toast.LENGTH_LONG).show()
                pbLoading.visibility = View.GONE
            }
        }

        override fun onRtspFirstFrameRendered() {
            binding.apply {
                vShutter.visibility = View.GONE
                bnSnapshot.isEnabled = true
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
            PixelCopy.request(binding.svVideo.holder.surface, surfaceBitmap, listener, sHandler)
            lock.wait()
        }
        thread.quitSafely()
        return if (success.get()) surfaceBitmap else null
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        if (DEBUG) Log.v(TAG, "onCreateView()")

        liveViewModel = ViewModelProvider(this).get(LiveViewModel::class.java)
        binding = FragmentLiveBinding.inflate(inflater, container, false)

        binding.svVideo.setStatusListener(rtspStatusListener)
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
            binding.svVideo.videoRotation = 0
        }

        binding.bnRotate90.setOnClickListener {
            binding.svVideo.videoRotation = 90
        }

        binding.bnRotate180.setOnClickListener {
            binding.svVideo.videoRotation = 180
        }

        binding.bnRotate270.setOnClickListener {
            binding.svVideo.videoRotation = 270
        }

        binding.bnRotate0.performClick()

        binding.bnStartStop.setOnClickListener {
            if (binding.svVideo.isStarted()) {
                binding.svVideo.stop()
            } else {
                val uri = Uri.parse(liveViewModel.rtspRequest.value)
                binding.svVideo.init(uri, liveViewModel.rtspUsername.value, liveViewModel.rtspPassword.value, "rtsp-client-android")
                binding.svVideo.debug = binding.cbDebug.isChecked
                binding.svVideo.start(binding.cbVideo.isChecked, binding.cbAudio.isChecked)
            }
        }

        binding.bnSnapshot.setOnClickListener {
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
        val started = binding.svVideo.isStarted()
        if (DEBUG) Log.v(TAG, "onPause(), started:$started")
        super.onPause()
        liveViewModel.saveParams(requireContext())

        if (started) {
            binding.svVideo.stop()
        }
    }

    companion object {
        private val TAG: String = LiveFragment::class.java.simpleName
        private const val DEBUG = true
    }

}
