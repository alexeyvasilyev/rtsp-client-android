package com.alexvas.rtsp.demo.live

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.alexvas.rtsp.RtspClient
import com.alexvas.rtsp.demo.R
import com.alexvas.rtsp.widget.toHexString
import com.alexvas.utils.NetUtils
import kotlinx.coroutines.delay
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.min

@Composable
fun RawScreen(liveViewModel: LiveViewModel) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val request by liveViewModel.rtspRequest.observeAsState("")
    val username by liveViewModel.rtspUsername.observeAsState("")
    val password by liveViewModel.rtspPassword.observeAsState("")
    var requestVideo by remember { mutableStateOf(true) }
    var requestAudio by remember { mutableStateOf(false) }
    var requestApplication by remember { mutableStateOf(false) }
    var debug by remember { mutableStateOf(false) }
    var transport by remember { mutableStateOf(RtspClient.Transport.TCP) }
    var passwordVisible by remember { mutableStateOf(false) }
    val player = remember { RawPlayerState(context.applicationContext) }
    var pendingStart by remember { mutableStateOf<(() -> Unit)?>(null) }
    val localNetworkPermissionDenied =
        stringResource(R.string.local_network_permission_denied)
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        val start = pendingStart
        pendingStart = null
        if (granted) {
            if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                start?.invoke()
            }
        } else {
            player.showError(localNetworkPermissionDenied)
        }
    }

    DisposableEffect(lifecycleOwner, player) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) player.stop()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            player.dispose()
        }
    }
    LaunchedEffect(player.isStarted) {
        while (player.isStarted) {
            player.updateStatistics()
            delay(1000)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(rememberNestedScrollInteropConnection())
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 10.dp, vertical = 30.dp),
    ) {
        RtspParamsSection(
            rtspRequest = request,
            onRtspRequestChange = { liveViewModel.rtspRequest.value = it },
            rtspUsername = username,
            onRtspUsernameChange = { liveViewModel.rtspUsername.value = it },
            rtspPassword = password,
            onRtspPasswordChange = { liveViewModel.rtspPassword.value = it },
            passwordVisible = passwordVisible,
            onPasswordVisibleChange = { passwordVisible = it },
            requestVideo = requestVideo,
            onRequestVideoChange = { requestVideo = it },
            requestAudio = requestAudio,
            onRequestAudioChange = { requestAudio = it },
            requestApplication = requestApplication,
            onRequestApplicationChange = { requestApplication = it },
            debugEnabled = debug,
            onDebugEnabledChange = { debug = it },
            transport = transport,
            onTransportChange = { transport = it },
            enabled = !player.isStarted && pendingStart == null,
        )
        Button(
            onClick = {
                if (player.isStarted) {
                    player.stop()
                } else {
                    val start = {
                        player.start(request, username, password, transport, debug,
                            requestVideo, requestAudio, requestApplication)
                    }
                    if (Build.VERSION.SDK_INT >= 37 && ContextCompat.checkSelfPermission(
                            context, Manifest.permission.ACCESS_LOCAL_NETWORK,
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        pendingStart = start
                        permissionLauncher.launch(Manifest.permission.ACCESS_LOCAL_NETWORK)
                    } else {
                        start()
                    }
                }
            },
            enabled = !player.isStopping && pendingStart == null,
            modifier = Modifier.align(Alignment.CenterHorizontally).padding(vertical = 10.dp),
        ) {
            Text(
                text = if (player.isStarted) "Stop RTSP" else "Start",
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 5.dp),
            )
        }
        Text(
            text = player.errorMessage ?: player.statusText,
            color = if (player.errorMessage != null) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.End,
            modifier = Modifier.fillMaxWidth(),
        )
        Column(modifier = Modifier.padding(top = 30.dp)) {
            listOf(player.videoStatistics, player.audioStatistics, player.applicationStatistics).forEach {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(5.dp),
                )
            }
        }
    }
}

/** Owns one raw RTSP session; all UI state updates run on the main thread. */
@SuppressLint("LogNotTimber")
private class RawPlayerState(private val context: Context) {
    var isStarted by mutableStateOf(false)
        private set
    var isStopping by mutableStateOf(false)
        private set
    var statusText by mutableStateOf("")
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set
    var videoStatistics by mutableStateOf("Video: 0 bytes, 0 frames")
        private set
    var audioStatistics by mutableStateOf("Audio: 0 bytes, 0 samples")
        private set
    var applicationStatistics by mutableStateOf("Application: 0 bytes, 0 samples")
        private set

    private class Session {
        val stopped = AtomicBoolean(false)
        val videoBytes = AtomicLong()
        val videoFrames = AtomicLong()
        val audioBytes = AtomicLong()
        val audioSamples = AtomicLong()
        val applicationBytes = AtomicLong()
        val applicationSamples = AtomicLong()
        @Volatile var socket: Socket? = null
    }

    private val handler = Handler(Looper.getMainLooper())
    private var session: Session? = null

    fun showError(message: String) {
        errorMessage = message
    }

    fun start(
        request: String,
        username: String,
        password: String,
        transport: RtspClient.Transport,
        debug: Boolean,
        requestVideo: Boolean,
        requestAudio: Boolean,
        requestApplication: Boolean,
    ) {
        if (session != null) return
        val current = Session()
        session = current
        errorMessage = null
        statusText = "RTSP connecting"
        isStarted = true
        isStopping = false
        updateStatistics()

        fun postStatus(status: String) {
            handler.post {
                if (session === current && !current.stopped.get()) statusText = status
            }
        }
        fun postError(message: String) {
            handler.post {
                if (session === current && !current.stopped.get()) showError(message)
            }
        }
        val listener = object : RtspClient.RtspClientListener {
            override fun onRtspConnecting() = postStatus("RTSP connecting")
            override fun onRtspConnected(sdpInfo: RtspClient.SdpInfo) = postStatus("RTSP connected")
            override fun onRtspDisconnecting() = postStatus("RTSP disconnecting")
            override fun onRtspDisconnected() = postStatus("RTSP disconnected")
            override fun onRtspFailedUnauthorized() = postError("RTSP username or password invalid")
            override fun onRtspFailed(message: String?) =
                postError("Error: ${message?.takeIf { it.isNotBlank() } ?: "Unable to receive RTSP stream"}")

            override fun onRtspVideoNalUnitReceived(data: ByteArray, offset: Int, length: Int, timestamp: Long) {
                logData("video", data, offset, length)
                current.videoBytes.addAndGet(length.toLong())
                current.videoFrames.incrementAndGet()
            }

            override fun onRtspAudioSampleReceived(data: ByteArray, offset: Int, length: Int, timestamp: Long) {
                logData("audio", data, offset, length)
                current.audioBytes.addAndGet(length.toLong())
                current.audioSamples.incrementAndGet()
            }

            override fun onRtspApplicationDataReceived(data: ByteArray, offset: Int, length: Int, timestamp: Long) {
                logData("app", data, offset, length)
                current.applicationBytes.addAndGet(length.toLong())
                current.applicationSamples.incrementAndGet()
            }
        }
        Thread({
            try {
                val uri = request.toUri()
                val host = requireNotNull(uri.host?.takeIf { it.isNotBlank() }) { "Invalid RTSP URL" }
                val port = if (uri.port == -1) 554 else uri.port
                val socket = NetUtils.createSocket(5000)
                current.socket = socket
                if (!current.stopped.get()) {
                    socket.connect(InetSocketAddress(host, port), 5000)
                }
                if (!current.stopped.get()) {
                    RtspClient.Builder(socket, request, current.stopped, listener)
                        .requestVideo(requestVideo)
                        .requestAudio(requestAudio)
                        .requestApplication(requestApplication)
                        .withDebug(debug)
                        .withUserAgent("rtsp-client-android")
                        .withCredentials(username, password)
                        .withTransport(transport)
                        .withContext(context)
                        .build()
                        .execute()
                }
            } catch (e: Exception) {
                if (!current.stopped.get()) {
                    Log.e("RawScreen", "RTSP failed", e)
                    listener.onRtspFailed(e.message)
                }
            } finally {
                closeSocket(current)
                handler.post {
                    if (session === current) {
                        updateStatistics()
                        session = null
                        isStarted = false
                        isStopping = false
                        statusText = "RTSP disconnected"
                    }
                }
            }
        }, "RTSP raw thread").start()
    }

    fun stop() {
        val current = session ?: return
        if (current.stopped.getAndSet(true)) return
        isStopping = true
        statusText = "RTSP disconnecting"
        closeSocket(current)
    }

    fun dispose() {
        stop()
        session = null
        handler.removeCallbacksAndMessages(null)
    }

    fun updateStatistics() {
        val current = session ?: return
        videoStatistics = "Video: ${current.videoBytes.get()} bytes, ${current.videoFrames.get()} frames"
        audioStatistics = "Audio: ${current.audioBytes.get()} bytes, ${current.audioSamples.get()} samples"
        applicationStatistics = "Application: ${current.applicationBytes.get()} bytes, ${current.applicationSamples.get()} samples"
    }

    private fun closeSocket(current: Session) {
        try {
            NetUtils.closeSocket(current.socket)
        } catch (e: IOException) {
            Log.w("RawScreen", "Failed to close RTSP socket", e)
        }
    }

    private fun logData(type: String, data: ByteArray, offset: Int, length: Int) {
        Log.i("RawScreen", "RTSP $type data ($length bytes): ${data.toHexString(offset, offset + min(length, 25))}")
    }
}
