package com.alexvas.rtsp.demo.live

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.PixelCopy
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.alexvas.rtsp.demo.R
import com.alexvas.rtsp.RtspClient
import com.alexvas.rtsp.codec.VideoDecodeThread
import com.alexvas.rtsp.widget.RtspDataListener
import com.alexvas.rtsp.widget.RtspImageView
import com.alexvas.rtsp.widget.RtspStatusListener
import com.alexvas.rtsp.widget.RtspSurfaceView
import com.alexvas.rtsp.widget.toHexString
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.min

private const val TAG = "LiveScreen"
private const val DEBUG = true
private const val USER_AGENT = "rtsp-client-android"

@SuppressLint("LogNotTimber")
@Composable
fun LiveScreen(liveViewModel: LiveViewModel) {
    val context = LocalContext.current
    val activity = context as? Activity

    val rtspRequest by liveViewModel.rtspRequest.observeAsState("")
    val rtspUsername by liveViewModel.rtspUsername.observeAsState("")
    val rtspPassword by liveViewModel.rtspPassword.observeAsState("")

    var requestVideo by remember { mutableStateOf(true) }
    var requestAudio by remember { mutableStateOf(false) }
    var requestApplication by remember { mutableStateOf(false) }
    var debugEnabled by remember { mutableStateOf(false) }
    var transport by remember { mutableStateOf(RtspClient.Transport.TCP) }
    var passwordVisible by remember { mutableStateOf(false) }

    var fpsStabilization by remember { mutableStateOf(false) }
    var experimentalRewriteSps by remember { mutableStateOf(false) }
    var volume by remember { mutableFloatStateOf(1f) }

    var decoderType by remember { mutableStateOf(VideoDecodeThread.DecoderType.HARDWARE) }
    var rotation by remember { mutableIntStateOf(0) }

    val setKeepScreenOn = remember(activity) {
        { enable: Boolean ->
            if (DEBUG) Log.v(TAG, "setKeepScreenOn(enable=$enable)")
            activity?.window?.apply {
                if (enable) addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                else clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
            Unit
        }
    }

    val surfacePlayer = remember { RtspSurfacePlayerState(setKeepScreenOn) }
    val imagePlayer = remember { RtspImagePlayerState(setKeepScreenOn) }

    var pendingStart by remember { mutableStateOf<(() -> Unit)?>(null) }
    val localNetworkPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        val start = pendingStart
        pendingStart = null
        if (granted) {
            start?.invoke()
        } else {
            Toast.makeText(context, R.string.local_network_permission_denied, Toast.LENGTH_LONG).show()
        }
    }

    fun startWithLocalNetworkPermission(start: () -> Unit) {
        if (DEBUG) Log.v(TAG, "startWithLocalNetworkPermission()")
        if (Build.VERSION.SDK_INT >= 37 && ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_LOCAL_NETWORK,
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (pendingStart == null) {
                pendingStart = start
                localNetworkPermissionLauncher.launch(Manifest.permission.ACCESS_LOCAL_NETWORK)
            }
        } else {
            start()
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                surfacePlayer.stopIfStarted()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(rememberNestedScrollInteropConnection())
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 10.dp, vertical = 30.dp),
    ) {
        RtspParamsSection(
            rtspRequest = rtspRequest,
            onRtspRequestChange = { liveViewModel.rtspRequest.value = it },
            rtspUsername = rtspUsername,
            onRtspUsernameChange = { liveViewModel.rtspUsername.value = it },
            rtspPassword = rtspPassword,
            onRtspPasswordChange = { liveViewModel.rtspPassword.value = it },
            passwordVisible = passwordVisible,
            onPasswordVisibleChange = { passwordVisible = it },
            requestVideo = requestVideo,
            onRequestVideoChange = { requestVideo = it },
            requestAudio = requestAudio,
            onRequestAudioChange = { requestAudio = it },
            requestApplication = requestApplication,
            onRequestApplicationChange = { requestApplication = it },
            debugEnabled = debugEnabled,
            onDebugEnabledChange = { debugEnabled = it },
            transport = transport,
            onTransportChange = { transport = it },
            enabled = !surfacePlayer.isConnecting,
        )

        LabeledCheckbox(
            checked = fpsStabilization,
            onCheckedChange = { fpsStabilization = it },
            text = "Video frame rate stabilization.\nAdd delay up to 100ms for smoother playback. RtspSurfaceView only.",
        )

        LabeledCheckbox(
            checked = experimentalRewriteSps,
            onCheckedChange = { experimentalRewriteSps = it },
            text = "Rewrite SPS frames w/ low-latency params (EXPERIMENTAL)",
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, start = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Volume:")
            Slider(
                value = volume,
                onValueChange = { volume = it },
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp),
            )
            Text(
                text = "${(volume * 100).toInt()}%",
                modifier = Modifier.width(48.dp),
                textAlign = TextAlign.End,
            )
        }

        Button(
            onClick = {
                if (surfacePlayer.isStarted) {
                    surfacePlayer.stop()
                } else {
                    startWithLocalNetworkPermission {
                        surfacePlayer.start(
                            uri = rtspRequest.toUri(),
                            username = rtspUsername,
                            password = rtspPassword,
                            transport = transport,
                            debug = debugEnabled,
                            fpsStabilization = fpsStabilization,
                            requestVideo = requestVideo,
                            requestAudio = requestAudio,
                            requestApplication = requestApplication,
                        )
                    }
                }
            },
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 40.dp),
        ) {
            Text(
                text = if (surfacePlayer.isStarted) "Stop RTSP" else "Start",
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 5.dp),
            )
        }

        Text(
            text = "RtspSurfaceView:",
            modifier = Modifier.padding(bottom = 5.dp),
        )

        val surfaceResolution = surfacePlayer.resolution
        val surfaceAspectRatio =
            if (surfaceResolution.first > 0 && surfaceResolution.second > 0)
                surfaceResolution.first.toFloat() / surfaceResolution.second
            else
                16f / 9f

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(surfaceAspectRatio),
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx -> RtspSurfaceView(ctx).also { surfacePlayer.attach(it) } },
                update = { view ->
                    view.videoRotation = rotation
                    view.videoDecoderType = decoderType
                    view.volume = volume
                    view.videoFrameRateStabilization = fpsStabilization
                    view.experimentalUpdateSpsFrameWithLowLatencyParams = experimentalRewriteSps
                },
                onRelease = { view ->
                    view.setStatusListener(null)
                    view.setDataListener(null)
                },
            )
            VideoShutter(visible = surfacePlayer.showShutter)
            if (surfacePlayer.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            VideoErrorOverlay(message = surfacePlayer.errorMessage)
        }

        Text(
            text = surfacePlayer.statisticsText,
            modifier = Modifier.padding(top = 2.dp),
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 5.dp, end = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(
                onClick = {
                    val bitmap = surfacePlayer.getSnapshot()
                    if (bitmap != null) {
                        Toast.makeText(context, "Snapshot succeeded ${bitmap.width}x${bitmap.height}", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(context, "Snapshot failed", Toast.LENGTH_LONG).show()
                    }
                },
                enabled = surfacePlayer.snapshotEnabled,
            ) {
                Icon(painterResource(R.drawable.ic_camera_black_24dp), contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Snapshot")
            }
            Text(
                text = surfacePlayer.statusText,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End,
            )
        }

        Button(
            onClick = {
                if (imagePlayer.isStarted) {
                    imagePlayer.stop()
                } else {
                    startWithLocalNetworkPermission {
                        imagePlayer.start(
                            uri = rtspRequest.toUri(),
                            username = rtspUsername,
                            password = rtspPassword,
                            transport = transport,
                            debug = debugEnabled,
                            requestVideo = requestVideo,
                            requestAudio = requestAudio,
                            requestApplication = requestApplication,
                        )
                    }
                }
            },
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 30.dp),
        ) {
            Text(
                text = if (imagePlayer.isStarted) "Stop RTSP" else "Start",
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 5.dp),
            )
        }

        Text(
            text = "RtspImageView:",
            modifier = Modifier.padding(bottom = 5.dp),
        )

        val imageResolution = imagePlayer.resolution
        val imageAspectRatio =
            if (imageResolution.first > 0 && imageResolution.second > 0)
                imageResolution.first.toFloat() / imageResolution.second
            else
                16f / 9f

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(imageAspectRatio),
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx -> RtspImageView(ctx).also { imagePlayer.attach(it) } },
                update = { view ->
                    view.videoRotation = rotation
                    view.videoDecoderType = decoderType
                    view.volume = volume
                },
                onRelease = { view ->
                    view.setStatusListener(null)
                    view.setDataListener(null)
                    view.onRtspImageBitmapListener = null
                },
            )
            VideoShutter(visible = imagePlayer.showShutter)
            if (imagePlayer.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            VideoErrorOverlay(message = imagePlayer.errorMessage)
        }

        Text(
            text = imagePlayer.statusText,
            modifier = Modifier
                .fillMaxWidth()
                .padding(5.dp),
            textAlign = TextAlign.End,
        )

        DecoderAndRotationSection(
            decoderType = decoderType,
            onDecoderTypeChange = { decoderType = it },
            rotation = rotation,
            onRotationChange = { rotation = it },
        )
    }
}

@Composable
private fun VideoErrorOverlay(message: String?) {
    if (message == null) return

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = message,
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.verticalScroll(rememberScrollState()),
        )
    }
}

@Composable
private fun VideoShutter(visible: Boolean) {
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "Video shutter",
    )
    // Animate an overlay: SurfaceView renders video in a separate surface.
    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind { drawRect(Color.Black, alpha = alpha) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RtspParamsSection(
    rtspRequest: String,
    onRtspRequestChange: (String) -> Unit,
    rtspUsername: String,
    onRtspUsernameChange: (String) -> Unit,
    rtspPassword: String,
    onRtspPasswordChange: (String) -> Unit,
    passwordVisible: Boolean,
    onPasswordVisibleChange: (Boolean) -> Unit,
    requestVideo: Boolean,
    onRequestVideoChange: (Boolean) -> Unit,
    requestAudio: Boolean,
    onRequestAudioChange: (Boolean) -> Unit,
    requestApplication: Boolean,
    onRequestApplicationChange: (Boolean) -> Unit,
    debugEnabled: Boolean,
    onDebugEnabledChange: (Boolean) -> Unit,
    transport: RtspClient.Transport,
    onTransportChange: (RtspClient.Transport) -> Unit,
    enabled: Boolean,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = rtspRequest,
            onValueChange = onRtspRequestChange,
            label = { Text("RTSP request", maxLines = 1, overflow = TextOverflow.Ellipsis) },
            enabled = enabled,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp),
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp),
        ) {
            OutlinedTextField(
                value = rtspUsername,
                onValueChange = onRtspUsernameChange,
                label = { Text("RTSP username", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                enabled = enabled,
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 5.dp),
            )
            OutlinedTextField(
                value = rtspPassword,
                onValueChange = onRtspPasswordChange,
                label = { Text("RTSP password", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                enabled = enabled,
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { onPasswordVisibleChange(!passwordVisible) }) {
                        Text(
                            text = if (passwordVisible) "Hide" else "Show",
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                },
                modifier = Modifier.weight(1f),
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.Center,
        ) {
            LabeledCheckbox(checked = requestVideo, onCheckedChange = onRequestVideoChange, text = "Video", enabled = enabled)
            LabeledCheckbox(checked = requestAudio, onCheckedChange = onRequestAudioChange, text = "Audio", enabled = enabled)
            LabeledCheckbox(checked = requestApplication, onCheckedChange = onRequestApplicationChange, text = "Application", enabled = enabled)
            LabeledCheckbox(checked = debugEnabled, onCheckedChange = onDebugEnabledChange, text = "Debug", enabled = enabled)
        }

        Row(
            modifier = Modifier.fillMaxWidth().wrapContentWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = transport == RtspClient.Transport.TCP,
                    onClick = { onTransportChange(RtspClient.Transport.TCP) },
                    enabled = enabled,
                )
                Text("TCP")
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = transport == RtspClient.Transport.UDP,
                    onClick = { onTransportChange(RtspClient.Transport.UDP) },
                    enabled = enabled,
                )
                Text("UDP")
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = transport == RtspClient.Transport.MULTICAST,
                    onClick = { onTransportChange(RtspClient.Transport.MULTICAST) },
                    enabled = enabled,
                )
                Text("UDP Multicast")
            }
        }
    }
}

@Composable
private fun LabeledCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    text: String,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.padding(end = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange, enabled = enabled)
        Text(text)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DecoderAndRotationSection(
    decoderType: VideoDecodeThread.DecoderType,
    onDecoderTypeChange: (VideoDecodeThread.DecoderType) -> Unit,
    rotation: Int,
    onRotationChange: (Int) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Video decoder")
        SingleChoiceSegmentedButtonRow(modifier = Modifier.padding(top = 4.dp)) {
            val decoders = listOf(VideoDecodeThread.DecoderType.HARDWARE to "Hardware", VideoDecodeThread.DecoderType.SOFTWARE to "Software")
            decoders.forEachIndexed { index, (type, label) ->
                SegmentedButton(
                    selected = decoderType == type,
                    onClick = { onDecoderTypeChange(type) },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = decoders.size),
                ) {
                    Text(label)
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Rotation")
        SingleChoiceSegmentedButtonRow(modifier = Modifier.padding(top = 4.dp)) {
            val rotations = listOf(0, 90, 180, 270)
            rotations.forEachIndexed { index, value ->
                SegmentedButton(
                    selected = rotation == value,
                    onClick = { onRotationChange(value) },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = rotations.size),
                ) {
                    Text(value.toString())
                }
            }
        }
    }
}

/** Holds RtspSurfaceView playback state and mirrors it into Compose state. */
@SuppressLint("LogNotTimber")
private class RtspSurfacePlayerState(
    private val setKeepScreenOn: (Boolean) -> Unit,
) : RtspStatusListener, RtspDataListener {

    var statusText by mutableStateOf("")
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set
    var isConnecting by mutableStateOf(false)
        private set
    var isLoading by mutableStateOf(false)
        private set
    var showShutter by mutableStateOf(true)
        private set
    var isStarted by mutableStateOf(false)
        private set
    var snapshotEnabled by mutableStateOf(false)
        private set
    var statisticsText by mutableStateOf("")
        private set
    var resolution by mutableStateOf(0 to 0)
        private set

    private var view: RtspSurfaceView? = null
    private var statisticsTimer: Timer? = null

    fun attach(view: RtspSurfaceView) {
        this.view = view
        view.setStatusListener(this)
        view.setDataListener(this)
    }

    fun start(
        uri: Uri,
        username: String?,
        password: String?,
        transport: RtspClient.Transport,
        debug: Boolean,
        fpsStabilization: Boolean,
        requestVideo: Boolean,
        requestAudio: Boolean,
        requestApplication: Boolean,
    ) {
        if (DEBUG) Log.v(TAG, "start(uri='$uri', username='$username', password='$password', transport=$transport)")
        val view = view ?: return
        errorMessage = null
        view.init(uri, username = username, password = password, userAgent = USER_AGENT, transport = transport)
        view.debug = debug
        view.videoFrameRateStabilization = fpsStabilization
        view.start(requestVideo = requestVideo, requestAudio = requestAudio, requestApplication = requestApplication)
        startStatistics()
    }

    fun stop() {
        if (DEBUG) Log.v(TAG, "stop()")
        showShutter = true
        view?.stop()
        stopStatistics()
    }

    fun stopIfStarted() {
        if (DEBUG) Log.v(TAG, "stopIfStarted()")
        if (isStarted) stop()
    }

    fun getSnapshot(): Bitmap? {
        if (DEBUG) Log.v(TAG, "getSnapshot()")
        val view = view ?: return null
        val (width, height) = resolution
        if (width <= 0 || height <= 0) return null
        val surfaceBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val lock = Object()
        val success = AtomicBoolean(false)
        val thread = HandlerThread("PixelCopyHelper")
        thread.start()
        val sHandler = Handler(thread.looper)
        val listener = PixelCopy.OnPixelCopyFinishedListener { copyResult ->
            success.set(copyResult == PixelCopy.SUCCESS)
            synchronized(lock) {
                lock.notify()
            }
        }
        synchronized(lock) {
            PixelCopy.request(view.holder.surface, surfaceBitmap, listener, sHandler)
            lock.wait()
        }
        thread.quitSafely()
        return if (success.get()) surfaceBitmap else null
    }

    private fun startStatistics() {
        if (DEBUG) Log.v(TAG, "startStatistics()")
        if (statisticsTimer == null) {
            val task = object : TimerTask() {
                override fun run() {
                    val view = view ?: return
                    val statistics = view.statistics
                    val audioParams = if (statistics.audioCodec != null) {
                        " (${if (statistics.audioSampleRate > 0) "${statistics.audioSampleRate} Hz" else "-"}, " +
                            "${if (statistics.audioChannels > 0) statistics.audioChannels.toString() else "-"} channels)"
                    } else {
                        ""
                    }
                    statisticsText =
                        "Video codec: ${statistics.videoCodec ?: "-"}" +
                            "\nVideo decoder: ${statistics.videoDecoderType.toString().lowercase()} ${if (statistics.videoDecoderName.isNullOrEmpty()) "" else "(${statistics.videoDecoderName})"}" +
                            "\nVideo decoder latency: ${statistics.videoDecoderLatencyMsec} ms" +
                            "\nVideo resolution: ${resolution.first}x${resolution.second}" +
                            "\n\nAudio codec: ${statistics.audioCodec ?: "-"}" + audioParams +
                            "\nAudio decoder: ${statistics.audioDecoderName ?: "-"}"
                }
            }
            statisticsTimer = Timer("${TAG}::Statistics").apply {
                schedule(task, 0, 1000)
            }
        }
    }

    private fun stopStatistics() {
        if (DEBUG) Log.v(TAG, "stopStatistics()")
        statisticsTimer?.cancel()
        statisticsTimer = null
    }

    override fun onRtspStatusConnecting() {
        if (DEBUG) Log.v(TAG, "onRtspStatusConnecting()")
        errorMessage = null
        statusText = "RTSP connecting"
        isLoading = true
        showShutter = true
        isConnecting = true
    }

    override fun onRtspStatusConnected() {
        if (DEBUG) Log.v(TAG, "onRtspStatusConnected()")
        statusText = "RTSP connected"
        isStarted = true
        setKeepScreenOn(true)
    }

    override fun onRtspStatusDisconnecting() {
        if (DEBUG) Log.v(TAG, "onRtspStatusDisconnecting()")
        statusText = "RTSP disconnecting"
    }

    override fun onRtspStatusDisconnected() {
        if (DEBUG) Log.v(TAG, "onRtspStatusDisconnected()")
        statusText = "RTSP disconnected"
        isStarted = false
        isLoading = false
        showShutter = true
        isConnecting = false
        setKeepScreenOn(false)
    }

    override fun onRtspStatusFailedUnauthorized() {
        if (DEBUG) Log.e(TAG, "onRtspStatusFailedUnauthorized()")
        onRtspStatusDisconnected()
        statusText = "RTSP username or password invalid"
        errorMessage = statusText
        isLoading = false
    }

    override fun onRtspStatusFailed(message: String?) {
        if (DEBUG) Log.e(TAG, "onRtspStatusFailed(message='$message')")
        onRtspStatusDisconnected()
        statusText = "Error: ${message?.takeIf { it.isNotBlank() } ?: "Unable to play RTSP stream"}"
        errorMessage = statusText
        isLoading = false
    }

    override fun onRtspFirstFrameRendered() {
        if (DEBUG) Log.v(TAG, "onRtspFirstFrameRendered()")
        Log.i(TAG, "First frame rendered")
        isLoading = false
        showShutter = false
        snapshotEnabled = true
    }

    override fun onRtspFrameSizeChanged(width: Int, height: Int) {
        if (DEBUG) Log.v(TAG, "onRtspFrameSizeChanged(width=$width, height=$height)")
        Log.i(TAG, "Video resolution changed to ${width}x${height}")
        resolution = width to height
    }

    override fun onRtspDataApplicationDataReceived(data: ByteArray, offset: Int, length: Int, timestamp: Long) {
        val numBytesDump = min(length, 25) // dump max 25 bytes
        Log.i(TAG, "RTSP app data ($length bytes): ${data.toHexString(offset, offset + numBytesDump)}")
    }
}

/** Holds RtspImageView playback state and mirrors it into Compose state. */
@SuppressLint("LogNotTimber")
private class RtspImagePlayerState(
    private val setKeepScreenOn: (Boolean) -> Unit,
) : RtspStatusListener, RtspDataListener {

    var statusText by mutableStateOf("")
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set
    var isLoading by mutableStateOf(false)
        private set
    var showShutter by mutableStateOf(true)
        private set
    var isStarted by mutableStateOf(false)
        private set
    var resolution by mutableStateOf(0 to 0)
        private set

    private var view: RtspImageView? = null

    fun attach(view: RtspImageView) {
        this.view = view
        view.setStatusListener(this)
        view.setDataListener(this)
        view.onRtspImageBitmapListener = object : RtspImageView.RtspImageBitmapListener {
            override fun onRtspImageBitmapObtained(bitmap: Bitmap) {
                // TODO: You can send bitmap for processing
            }
        }
    }

    fun start(
        uri: Uri,
        username: String?,
        password: String?,
        transport: RtspClient.Transport,
        debug: Boolean,
        requestVideo: Boolean,
        requestAudio: Boolean,
        requestApplication: Boolean,
    ) {
        val view = view ?: return
        errorMessage = null
        view.init(uri, username = username, password = password, userAgent = USER_AGENT, transport = transport)
        view.debug = debug
        view.start(requestVideo = requestVideo, requestAudio = requestAudio, requestApplication = requestApplication)
    }

    fun stop() {
        showShutter = true
        view?.stop()
    }

    override fun onRtspStatusConnecting() {
        if (DEBUG) Log.v(TAG, "onRtspStatusConnecting()")
        errorMessage = null
        statusText = "RTSP connecting"
        isLoading = true
        showShutter = true
    }

    override fun onRtspStatusConnected() {
        if (DEBUG) Log.v(TAG, "onRtspStatusConnected()")
        statusText = "RTSP connected"
        isStarted = true
        setKeepScreenOn(true)
    }

    override fun onRtspStatusDisconnecting() {
        if (DEBUG) Log.v(TAG, "onRtspStatusDisconnecting()")
        statusText = "RTSP disconnecting"
    }

    override fun onRtspStatusDisconnected() {
        if (DEBUG) Log.v(TAG, "onRtspStatusDisconnected()")
        statusText = "RTSP disconnected"
        isStarted = false
        isLoading = false
        showShutter = true
        setKeepScreenOn(false)
    }

    override fun onRtspStatusFailedUnauthorized() {
        if (DEBUG) Log.e(TAG, "onRtspStatusFailedUnauthorized()")
        onRtspStatusDisconnected()
        statusText = "RTSP username or password invalid"
        errorMessage = statusText
        isLoading = false
    }

    override fun onRtspStatusFailed(message: String?) {
        if (DEBUG) Log.e(TAG, "onRtspStatusFailed(message='$message')")
        onRtspStatusDisconnected()
        statusText = "Error: ${message?.takeIf { it.isNotBlank() } ?: "Unable to play RTSP stream"}"
        errorMessage = statusText
        isLoading = false
    }

    override fun onRtspFirstFrameRendered() {
        if (DEBUG) Log.v(TAG, "onRtspFirstFrameRendered()")
        Log.i(TAG, "First frame rendered")
        showShutter = false
        isLoading = false
    }

    override fun onRtspFrameSizeChanged(width: Int, height: Int) {
        if (DEBUG) Log.v(TAG, "onRtspFrameSizeChanged(width=$width, height=$height)")
        Log.i(TAG, "Video resolution changed to ${width}x${height}")
        resolution = width to height
    }

    override fun onRtspDataApplicationDataReceived(data: ByteArray, offset: Int, length: Int, timestamp: Long) {
        val numBytesDump = min(length, 25) // dump max 25 bytes
        Log.i(TAG, "RTSP app data ($length bytes): ${data.toHexString(offset, offset + numBytesDump)}")
    }
}
