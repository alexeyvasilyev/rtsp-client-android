# rtsp-client-android
<b>Lightweight RTSP client library for Android</b> with almost zero lag video display. Designed for lag criticial applications (e.g. video surveillance from drones).

Unlike [ExoPlayer](https://github.com/google/ExoPlayer) which also supports RTSP, this library does not make any video buffering. Video frames are shown immidiately when they arrive.

[![Release](https://jitpack.io/v/alexeyvasilyev/rtsp-client-android.svg)](https://jitpack.io/#alexeyvasilyev/rtsp-client-android)

![Screenshot](docs/images/rtsp-demo-app.png?raw=true "Screenshot")

## Features:
- Android min API 24.
- RTSP/RTSPS over TCP.
- Video H.264 only.
- Audio AAC LC only.
- Basic/Digest authentification.
- Supports majority of RTSP IP cameras.


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
  implementation 'com.github.alexeyvasilyev:rtsp-client-android:2.0.8'
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
