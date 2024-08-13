# rtsp-client-android
<b>Lightweight RTSP client library for Android</b> with almost zero lag video decoding (achieved 20 msec video decoding latency on some RTSP streams). Designed for lag criticial applications (e.g. video surveillance from drones).

Unlike [AndroidX Media ExoPlayer](https://github.com/androidx/media) which also supports RTSP, this library does not make any video buffering. Video frames are shown immidiately when they arrive.

[![Release](https://jitpack.io/v/alexeyvasilyev/rtsp-client-android.svg)](https://jitpack.io/#alexeyvasilyev/rtsp-client-android)

![Screenshot](docs/images/rtsp-demo-app.png?raw=true "Screenshot")

## Features:
- Android min API 24.
- RTSP/RTSPS over TCP.
- Video H.264 only.
- Audio AAC LC only.
- Basic/Digest authentication.
- Supports majority of RTSP IP cameras.
- Uses Android's [Low-Latency MediaCodec](https://source.android.com/docs/core/media/low-latency-media) by default if available.


## Permissions:

```xml
<uses-permission android:name="android.permission.INTERNET" />
```

## Compile

To use this library in your project add this to your build.gradle:
```gradle
allprojects {
  repositories {
    maven { url 'https://jitpack.io' }
  }
}
dependencies {
  implementation 'com.github.alexeyvasilyev:rtsp-client-android:x.x.x'
}
```

## How to use:
Easiest way is just to use `RtspSurfaceView` class for showing video stream in UI.
```xml
<com.alexvas.rtsp.widget.RtspSurfaceView
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:id="@+id/svVideo" />
```

Then in code use:
```kotlin
val uri = Uri.parse("rtsps://10.0.1.3/test.sdp")
val username = "admin"
val password = "secret"
svVideo.init(uri, username, password)
svVideo.start(requestVideo = true, requestAudio = true)
// ...
svVideo.stop()
```

You can still use library without any decoding (just for obtaining raw frames), e.g. for writing video stream into MP4 via muxer.

```kotlin
val rtspClientListener = object: RtspClient.RtspClientListener {
    override fun onRtspConnecting() {}
    override fun onRtspConnected(sdpInfo: SdpInfo) {}
    override fun onRtspVideoNalUnitReceived(data: ByteArray, offset: Int, length: Int, timestamp: Long) {
        // Send raw H264/H265 NAL unit to decoder
    }
    override fun onRtspAudioSampleReceived(data: ByteArray, offset: Int, length: Int, timestamp: Long) {
        // Send raw audio to decoder
    }
    override fun onRtspDisconnected() {}
    override fun onRtspFailedUnauthorized() {
        Log.e(TAG, "RTSP failed unauthorized");
    }
    override fun onRtspFailed(message: String?) {
        Log.e(TAG, "RTSP failed with message '$message'")
    }
}

val uri = Uri.parse("rtsps://10.0.1.3/test.sdp")
val username = "admin"
val password = "secret"
val stopped = new AtomicBoolean(false)
val sslSocket = NetUtils.createSslSocketAndConnect(uri.getHost(), uri.getPort(), 10000)

val rtspClient = RtspClient.Builder(sslSocket, uri.toString(), stopped, rtspClientListener)
    .requestVideo(true)
    .requestAudio(true)
    .withDebug(false)
    .withUserAgent("RTSP client")
    .withCredentials(username, password)
    .build()
// Blocking call until stopped variable is true or connection failed
rtspClient.execute()

NetUtils.closeSocket(sslSocket)
```

## How to get lowest possible latency:
There are two types of latencies in the library:

### Network latency caused by network
If you want lowest possible network latency, be sure that both Android device and RTSP camera are connected to the same network by the Ethernet cable (not WiFi).

### Video decoder latency
Video decoder latency can vary significantly on different Android devices and on different RTSP camera streams.

For the same profile/level and resolution (but different cameras) the latency in best cases can can be 20 msec, in worst cases 1200 msec.

To decrease latency be sure you use the lowest possible H.264 video stream profile and level (enable `debug` in the library and check SPS frame params `profile_idc` and `level_idc` in the log).
Check `max_num_reorder_frames` param as well. For best latency value should be 0.
