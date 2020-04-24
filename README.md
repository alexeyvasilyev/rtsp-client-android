# rtsp-client-android
Lightweight RTSP client library for Android written on Java.

## Features:
- Android min API 21.
- RTSP/RTSPS over TCP.
- Basic/Digest authentification.

## How to use:
```
RtspClient.RtspClientListener rtspClientListener = new RtspClient.RtspClientListener() {
    @Override
    public void onRtspConnecting() {
    }

    @Override
    public void onRtspConnected(@Nullable byte[] sps, @Nullable byte[] pps) {
    }

    @Override
    public void onRtspNalUnitReceived(@NonNull byte[] data, int offset, int length, long timestamp) {
        // Send raw H264/H265 NAL unit to decoder
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
RtspClient rtspClient = new RtspClient();
// Blocking call untill stopped is true or connection failed
rtspClient.process(sslSocket, streamUrl, username, password, stopped, rtspClientListener);
NetUtils.closeSocket(sslSocket);
```
