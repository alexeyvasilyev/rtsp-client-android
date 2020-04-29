package com.alexvas.rtsp.demo.ui.live

import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.alexvas.rtsp.RtspClient
import com.alexvas.rtsp.demo.R
import com.alexvas.utils.NetUtils
import java.net.Socket
import java.util.concurrent.atomic.AtomicBoolean


class LiveFragment : Fragment() {

    private val TAG: String = LiveFragment::class.java.getSimpleName()
    private val DEBUG = true

    private lateinit var liveViewModel: LiveViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        liveViewModel =
                ViewModelProvider(this).get(LiveViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_live, container, false)
//        val textView: TextView = root.findViewById(R.id.text_live)
        val textRtspRequest: EditText = root.findViewById(R.id.edit_rtsp_request)
        val textRtspUsername: EditText = root.findViewById(R.id.edit_rtsp_username)
        val textRtspPassword: EditText = root.findViewById(R.id.edit_rtsp_password)

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

        class SimpleRunnable: Runnable {
            override fun run() {
                val stopped = AtomicBoolean(false)
                val listener = object: RtspClient.RtspClientListener {
                    override fun onRtspDisconnected() {
                        Log.i(TAG, "onRtspDisconnected")
                        stopped.set(true)
                    }

                    override fun onRtspFailed(message: String?) {
                        Log.e(TAG, "onRtspFailed")
                        stopped.set(true)
                    }

                    override fun onRtspConnected(sps: ByteArray?, pps: ByteArray?) {
                        Log.i(TAG, "onRtspConnected");
                        stopped.set(true)
                    }

                    override fun onRtspFailedUnauthorized() {
                        Log.e(TAG, "onRtspFailedUnauthorized")
                        stopped.set(true)
                    }

                    override fun onRtspNalUnitReceived(data: ByteArray, offset: Int, length: Int, timestamp: Long) {
                        Log.i(TAG, "onRtspNalUnitReceived")
                        stopped.set(true)
                    }

                    override fun onRtspConnecting() {
                        Log.i(TAG, "onRtspConnecting")
                    }
                }
                val uri: Uri = Uri.parse(liveViewModel.rtspRequest.value)
                val username = liveViewModel.rtspUsername.value
                val password = liveViewModel.rtspPassword.value
                try {
                    val socket: Socket = NetUtils.createSocketAndConnect(uri.getHost().toString(), uri.getPort(), 10000)

                    // Blocking call until stopped variable is true or connection failed
                    RtspClient().process(socket, uri.toString(), username, password, stopped, listener)

                    NetUtils.closeSocket(socket)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        val btnStartStop: Button = root.findViewById(R.id.button_start_stop_rtsp)
        btnStartStop.setOnClickListener {
            val threadWithRunnable = Thread(SimpleRunnable())
            threadWithRunnable.start()
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
    }

}
