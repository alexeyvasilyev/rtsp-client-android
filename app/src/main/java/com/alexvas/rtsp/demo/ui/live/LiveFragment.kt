package com.alexvas.rtsp.demo.ui.live

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.alexvas.rtsp.demo.databinding.FragmentLiveBinding
import com.alexvas.rtsp.widget.RtspSurfaceView


@SuppressLint("LogNotTimber")
class LiveFragment : Fragment() {

    private lateinit var binding: FragmentLiveBinding
    private lateinit var liveViewModel: LiveViewModel

    private val rtspStatusListener = object: RtspSurfaceView.RtspStatusListener {
        override fun onRtspStatusConnecting() {
            binding.tvStatus.text = "RTSP connecting"
            binding.pbLoading.visibility = View.VISIBLE
        }

        override fun onRtspStatusConnected() {
            binding.tvStatus.text = "RTSP connected"
            binding.bnStartStop.text = "Stop RTSP"
            binding.pbLoading.visibility = View.GONE
        }

        override fun onRtspStatusDisconnected() {
            binding.tvStatus.text = "RTSP disconnected"
            binding.bnStartStop.text = "Start RTSP"
            binding.pbLoading.visibility = View.GONE
        }

        override fun onRtspStatusFailedUnauthorized() {
            binding.tvStatus.text = "RTSP username or password invalid"
            binding.pbLoading.visibility = View.GONE
        }

        override fun onRtspStatusFailed(message: String?) {
            binding.tvStatus.text = "Error: $message"
            binding.pbLoading.visibility = View.GONE
        }
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

        liveViewModel.rtspRequest.observe(viewLifecycleOwner, {
            if (binding.etRtspRequest.text.toString() != it)
                binding.etRtspRequest.setText(it)
        })
        liveViewModel.rtspUsername.observe(viewLifecycleOwner, {
            if (binding.etRtspUsername.text.toString() != it)
                binding.etRtspUsername.setText(it)
        })
        liveViewModel.rtspPassword.observe(viewLifecycleOwner, {
            if (binding.etRtspPassword.text.toString() != it)
                binding.etRtspPassword.setText(it)
        })

        binding.bnStartStop.setOnClickListener {
            if (binding.svVideo.isStarted()) {
                binding.svVideo.stop()
            } else {
                val uri = Uri.parse(liveViewModel.rtspRequest.value)
                binding.svVideo.init(uri, liveViewModel.rtspUsername.value, liveViewModel.rtspPassword.value, "rtsp-client-android")
                binding.svVideo.start(binding.cbVideo.isChecked, binding.cbAudio.isChecked)
            }
        }
        return binding.root
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
//        onRtspClientStopped()
    }

    companion object {
        private val TAG: String = LiveFragment::class.java.simpleName
        private const val DEBUG = true
    }

}
