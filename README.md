# rtsp-client-android
Lightweight RTSP client library for Android written in Java.

[![Release](https://jitpack.io/v/alexeyvasilyev/rtsp-client-android.svg)](https://jitpack.io/#alexeyvasilyev/rtsp-client-android)

![Screenshot](docs/images/rtsp-demo-app.png?raw=true "Screenshot")

## Features:
- Android min API 21.
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

To use this library in your project with gradle add this to your build.gradle:

```gradle
allprojects {
  repositories {
    maven { url 'https://jitpack.io' }
  }
}
dependencies {
  implementation 'com.github.alexeyvasilyev:rtsp-client-android:1.4.1'
}
```

## How to use:
```java
RtspClient.RtspClientListener rtspClientListener = new RtspClient.RtspClientListener() {
    @Override
    public void onRtspConnecting() {
    }

    @Override
    public void onRtspConnected(@NonNull RtspClient.SdpInfo sdpInfo) {
    }

    @Override
    public void onRtspVideoNalUnitReceived(@NonNull byte[] data, int offset, int length, long timestamp) {
        // Send raw H264/H265 NAL unit to decoder
    }

    @Override
    public void onRtspAudioSampleReceived(@NonNull byte[] data, int offset, int length, long timestamp) {
        // Send raw audio to decoder
    }

    @Override
    public void onRtspDisconnected() {
    }

    @Override
    public void onRtspFailedUnauthorized() {
        Log.e(TAG, "RTSP failed unauthorized");
    }

    @Override
    public void onRtspFailed(@Nullable String message) {
        Log.e(TAG, "RTSP failed with message \"" + message + "\"");
    }
};

Uri uri = Uri.parse("rtsps://10.0.1.3/test.sdp");
String username = "admin";
String password = "secret";
AtomicBoolean stopped = new AtomicBoolean(false);
SSLSocket sslSocket = NetUtils.createSslSocketAndConnect(uri.getHost(), uri.getPort(), 10000);

RtspClient rtspClient = new RtspClient.Builder(sslSocket, uri.toString(), stopped, rtspClientListener)
    .requestVideo(true)
    .requestAudio(true)
    .withDebug(false)
    .withUserAgent("RTSP client")
    .withCredentials(username, password)
    .build();
// Blocking call until stopped variable is true or connection failed
rtspClient.execute();

NetUtils.closeSocket(sslSocket);
```
