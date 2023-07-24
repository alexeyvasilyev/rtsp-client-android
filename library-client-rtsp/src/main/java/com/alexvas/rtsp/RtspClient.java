package com.alexvas.rtsp;

import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.alexvas.rtsp.parser.AacParser;
import com.alexvas.rtsp.parser.RtpParser;
import com.alexvas.rtsp.parser.VideoRtpParser;
import com.alexvas.utils.NetUtils;
import com.alexvas.utils.VideoCodecUtils;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

//OPTIONS rtsp://10.0.1.145:88/videoSub RTSP/1.0
//CSeq: 1
//User-Agent: Lavf58.29.100
//
//RTSP/1.0 200 OK
//CSeq: 1
//Date: Fri, Jan 03 2020 22:03:07 GMT
//Public: OPTIONS, DESCRIBE, SETUP, TEARDOWN, PLAY, PAUSE, GET_PARAMETER, SET_PARAMETER

//DESCRIBE rtsp://10.0.1.145:88/videoSub RTSP/1.0
//Accept: application/sdp
//CSeq: 2
//User-Agent: Lavf58.29.100
//
//RTSP/1.0 401 Unauthorized
//CSeq: 2
//Date: Fri, Jan 03 2020 22:03:07 GMT
//WWW-Authenticate: Digest realm="Foscam IPCam Living Video", nonce="3c889dbf8371d3660aa2496789a5d130"

//DESCRIBE rtsp://10.0.1.145:88/videoSub RTSP/1.0
//Accept: application/sdp
//CSeq: 3
//User-Agent: Lavf58.29.100
//Authorization: Digest username="admin", realm="Foscam IPCam Living Video", nonce="3c889dbf8371d3660aa2496789a5d130", uri="rtsp://10.0.1.145:88/videoSub", response="4f062baec1c813ae3db15e3a14111d3d"
//
//RTSP/1.0 200 OK
//CSeq: 3
//Date: Fri, Jan 03 2020 22:03:07 GMT
//Content-Base: rtsp://10.0.1.145:65534/videoSub/
//Content-Type: application/sdp
//Content-Length: 495
//
//v=0
//o=- 1578088972261172 1 IN IP4 10.0.1.145
//s=IP Camera Video
//i=videoSub
//t=0 0
//a=tool:LIVE555 Streaming Media v2014.02.10
//a=type:broadcast
//a=control:*
//a=range:npt=0-
//a=x-qt-text-nam:IP Camera Video
//a=x-qt-text-inf:videoSub
//m=video 0 RTP/AVP 96
//c=IN IP4 0.0.0.0
//b=AS:96
//a=rtpmap:96 H264/90000
//a=fmtp:96 packetization-mode=1;profile-level-id=420020;sprop-parameter-sets=Z0IAIJWoFAHmQA==,aM48gA==
//a=control:track1
//m=audio 0 RTP/AVP 0
//c=IN IP4 0.0.0.0
//b=AS:64
//a=control:track2
//SETUP rtsp://10.0.1.145:65534/videoSub/track1 RTSP/1.0
//Transport: RTP/AVP/UDP;unicast;client_port=27452-27453
//CSeq: 4
//User-Agent: Lavf58.29.100
//Authorization: Digest username="admin", realm="Foscam IPCam Living Video", nonce="3c889dbf8371d3660aa2496789a5d130", uri="rtsp://10.0.1.145:65534/videoSub/track1", response="1fbc50b24d582c9331dd5e89f3102a06"
//
//RTSP/1.0 200 OK
//CSeq: 4
//Date: Fri, Jan 03 2020 22:03:07 GMT
//Transport: RTP/AVP;unicast;destination=10.0.1.53;source=10.0.1.145;client_port=27452-27453;server_port=6972-6973
//Session: 1F91B1B6;timeout=65

//SETUP rtsp://10.0.1.145:65534/videoSub/track2 RTSP/1.0
//Transport: RTP/AVP/UDP;unicast;client_port=27454-27455
//CSeq: 5
//User-Agent: Lavf58.29.100
//Session: 1F91B1B6
//Authorization: Digest username="admin", realm="Foscam IPCam Living Video", nonce="3c889dbf8371d3660aa2496789a5d130", uri="rtsp://10.0.1.145:65534/videoSub/track2", response="ad779abe070c096eff1012e7c70c986a"
//
//RTSP/1.0 200 OK
//CSeq: 5
//Date: Fri, Jan 03 2020 22:03:07 GMT
//Transport: RTP/AVP;unicast;destination=10.0.1.53;source=10.0.1.145;client_port=27454-27455;server_port=6974-6975
//Session: 1F91B1B6;timeout=65

//PLAY rtsp://10.0.1.145:65534/videoSub/ RTSP/1.0
//Range: npt=0.000-
//CSeq: 6
//User-Agent: Lavf58.29.100
//Session: 1F91B1B6
//Authorization: Digest username="admin", realm="Foscam IPCam Living Video", nonce="3c889dbf8371d3660aa2496789a5d130", uri="rtsp://10.0.1.145:65534/videoSub/", response="bb52eb6938dd4e50c4fac50363ffded0"
//
//RTSP/1.0 200 OK
//CSeq: 6
//Date: Fri, Jan 03 2020 22:03:07 GMT
//Range: npt=0.000-
//Session: 1F91B1B6
//RTP-Info: url=rtsp://10.0.1.145:65534/videoSub/track1;seq=42731;rtptime=2690581590,url=rtsp://10.0.1.145:65534/videoSub/track2;seq=34051;rtptime=3328043318

// https://www.ietf.org/rfc/rfc2326.txt
public class RtspClient {

    private static final String TAG = RtspClient.class.getSimpleName();
            static final String TAG_DEBUG = TAG + " DBG";
    private static final boolean DEBUG = false;

    public final static int RTSP_CAPABILITY_NONE          = 0;
    public final static int RTSP_CAPABILITY_OPTIONS       = 1 << 1;
    public final static int RTSP_CAPABILITY_DESCRIBE      = 1 << 2;
    public final static int RTSP_CAPABILITY_ANNOUNCE      = 1 << 3;
    public final static int RTSP_CAPABILITY_SETUP         = 1 << 4;
    public final static int RTSP_CAPABILITY_PLAY          = 1 << 5;
    public final static int RTSP_CAPABILITY_RECORD        = 1 << 6;
    public final static int RTSP_CAPABILITY_PAUSE         = 1 << 7;
    public final static int RTSP_CAPABILITY_TEARDOWN      = 1 << 8;
    public final static int RTSP_CAPABILITY_SET_PARAMETER = 1 << 9;
    public final static int RTSP_CAPABILITY_GET_PARAMETER = 1 << 10;
    public final static int RTSP_CAPABILITY_REDIRECT      = 1 << 11;

    public static boolean hasCapability(int capability, int capabilitiesMask) {
        return (capabilitiesMask & capability) != 0;
    }

    public interface RtspClientListener {
        void onRtspConnecting();
        void onRtspConnected(@NonNull SdpInfo sdpInfo);
        void onRtspVideoNalUnitReceived(@NonNull byte[] data, int offset, int length, long timestamp);
        void onRtspAudioSampleReceived(@NonNull byte[] data, int offset, int length, long timestamp);
        void onRtspDisconnecting();
        void onRtspDisconnected();
        void onRtspFailedUnauthorized();
        void onRtspFailed(@Nullable String message);
    }

    private interface RtspClientKeepAliveListener {
        void onRtspKeepAliveRequested();
    }

    public static class SdpInfo {
        /**
         * Session name (RFC 2327). In most cases RTSP server name.
         */
        public @Nullable String sessionName;

        /**
         * Session description (RFC 2327).
         */
        public @Nullable String sessionDescription;

        public @Nullable VideoTrack videoTrack;
        public @Nullable AudioTrack audioTrack;
    }

    public abstract static class Track {
        public String request;
        public int payloadType;
    }

    public static final int VIDEO_CODEC_H264 = 0;
    public static final int VIDEO_CODEC_H265 = 1;

    public static class VideoTrack extends Track {
        public int videoCodec = VIDEO_CODEC_H264;
        public @Nullable byte[] sps; // Both H.264 and H.265
        public @Nullable byte[] pps; // Both H.264 and H.265
//      public @Nullable byte[] vps; // H.265 only
//      public @Nullable byte[] sei; // H.265 only
    }

    public static final int AUDIO_CODEC_UNKNOWN = -1;
    public static final int AUDIO_CODEC_AAC = 0;
    public static final int AUDIO_CODEC_OPUS = 1;

    @NonNull
    private static String getAudioCodecName(int codec) {
        return switch (codec) {
            case AUDIO_CODEC_AAC -> "AAC";
            case AUDIO_CODEC_OPUS -> "Opus";
            default -> "Unknown";
        };
    }

    public static class AudioTrack extends Track {
        public int audioCodec = AUDIO_CODEC_UNKNOWN;
        public int sampleRateHz; // 16000, 8000
        public int channels; // 1 - mono, 2 - stereo
        public String mode; // AAC-lbr, AAC-hbr
        public @Nullable byte[] config; // config=1210fff15081ffdffc
    }

    private static final String CRLF = "\r\n";

    // Size of buffer for reading from the connection
    private final static int MAX_LINE_SIZE = 4098;

    private static class UnauthorizedException extends IOException {
        UnauthorizedException() {
            super("Unauthorized");
        }
    }

    private final static class NoResponseHeadersException extends IOException {
        private static final long serialVersionUID = 1L;
    }

    private final @NonNull Socket rtspSocket;
    private final @NonNull String uriRtsp;
    private final @NonNull AtomicBoolean exitFlag;
    private final @NonNull RtspClientListener listener;

//  private boolean sendOptionsCommand;
    private final boolean requestVideo;
    private final boolean requestAudio;
    private final boolean debug;
    private final @Nullable String username;
    private final @Nullable String password;
    private final @Nullable String userAgent;

    private RtspClient(@NonNull RtspClient.Builder builder) {
        rtspSocket = builder.rtspSocket;
        uriRtsp = builder.uriRtsp;
        exitFlag = builder.exitFlag;
        listener = builder.listener;
//      sendOptionsCommand = builder.sendOptionsCommand;
        requestVideo = builder.requestVideo;
        requestAudio = builder.requestAudio;
        username = builder.username;
        password = builder.password;
        debug = builder.debug;
        userAgent = builder.userAgent;
    }

    public void execute() {
        if (DEBUG) Log.v(TAG, "execute()");
        listener.onRtspConnecting();
        try {
            final InputStream inputStream = rtspSocket.getInputStream();
            final OutputStream outputStream = debug ?
                    new LoggerOutputStream(rtspSocket.getOutputStream()) :
                    new BufferedOutputStream(rtspSocket.getOutputStream());

            SdpInfo sdpInfo = new SdpInfo();
            final AtomicInteger cSeq = new AtomicInteger(0);
            ArrayList<Pair<String, String>> headers;
            int status;

            String authToken = null;
            Pair<String, String> digestRealmNonce = null;

// OPTIONS rtsp://10.0.1.78:8080/video/h264 RTSP/1.0
// CSeq: 1
// User-Agent: Lavf58.29.100

// RTSP/1.0 200 OK
// CSeq: 1
// Public: OPTIONS, DESCRIBE, SETUP, PLAY, GET_PARAMETER, SET_PARAMETER, TEARDOWN
//          if (sendOptionsCommand) {
            checkExitFlag(exitFlag);
            sendOptionsCommand(outputStream, uriRtsp, cSeq.addAndGet(1), userAgent, null);
            status = readResponseStatusCode(inputStream);
            headers = readResponseHeaders(inputStream);
            dumpHeaders(headers);
            // Try once again with credentials
            if (status == 401) {
                digestRealmNonce = getHeaderWwwAuthenticateDigestRealmAndNonce(headers);
                if (digestRealmNonce == null) {
                    String basicRealm = getHeaderWwwAuthenticateBasicRealm(headers);
                    if (TextUtils.isEmpty(basicRealm)) {
                        throw new IOException("Unknown authentication type");
                    }
                    // Basic auth
                    authToken = getBasicAuthHeader(username, password);
                } else {
                    // Digest auth
                    authToken = getDigestAuthHeader(username, password, "OPTIONS", uriRtsp, digestRealmNonce.first, digestRealmNonce.second);
                }
                checkExitFlag(exitFlag);
                sendOptionsCommand(outputStream, uriRtsp, cSeq.addAndGet(1), userAgent, authToken);
                status = readResponseStatusCode(inputStream);
                headers = readResponseHeaders(inputStream);
                dumpHeaders(headers);
            }
            if (DEBUG)
                Log.i(TAG, "OPTIONS status: " + status);
            checkStatusCode(status);
            final int capabilities = getSupportedCapabilities(headers);


// DESCRIBE rtsp://10.0.1.78:8080/video/h264 RTSP/1.0
// Accept: application/sdp
// CSeq: 2
// User-Agent: Lavf58.29.100

// RTSP/1.0 200 OK
// CSeq: 2
// Content-Type: application/sdp
// Content-Length: 364
//
// v=0
// t=0 0
// a=range:npt=now-
// m=video 0 RTP/AVP 96
// a=rtpmap:96 H264/90000
// a=fmtp:96 packetization-mode=1;sprop-parameter-sets=Z0KAH9oBABhpSCgwMDaFCag=,aM4G4g==
// a=control:trackID=1
// m=audio 0 RTP/AVP 96
// a=rtpmap:96 mpeg4-generic/48000/1
// a=fmtp:96 profile-level-id=1;mode=AAC-hbr;sizelength=13;indexlength=3;indexdeltalength=3;config=1188
// a=control:trackID=2
            checkExitFlag(exitFlag);

            sendDescribeCommand(outputStream, uriRtsp, cSeq.addAndGet(1), userAgent, authToken);
            status = readResponseStatusCode(inputStream);
            headers = readResponseHeaders(inputStream);
            dumpHeaders(headers);
            // Try once again with credentials. OPTIONS command can be accepted without authentication.
            if (status == 401) {
                digestRealmNonce = getHeaderWwwAuthenticateDigestRealmAndNonce(headers);
                if (digestRealmNonce == null) {
                    String basicRealm = getHeaderWwwAuthenticateBasicRealm(headers);
                    if (TextUtils.isEmpty(basicRealm)) {
                        throw new IOException("Unknown authentication type");
                    }
                    // Basic auth
                    authToken = getBasicAuthHeader(username, password);
                } else {
                    // Digest auth
                    authToken = getDigestAuthHeader(username, password, "DESCRIBE", uriRtsp, digestRealmNonce.first, digestRealmNonce.second);
                }
                checkExitFlag(exitFlag);
                sendDescribeCommand(outputStream, uriRtsp, cSeq.addAndGet(1), userAgent, authToken);
                status = readResponseStatusCode(inputStream);
                headers = readResponseHeaders(inputStream);
                dumpHeaders(headers);
            }
            if (DEBUG)
                Log.i(TAG, "DESCRIBE status: " + status);
            checkStatusCode(status);
            int contentLength = getHeaderContentLength(headers);
            if (contentLength > 0) {
                String content = readContentAsText(inputStream, contentLength);
                if (debug)
                    Log.i(TAG_DEBUG, "" + content);
                try {
                    List<Pair<String, String>> params = getDescribeParams(content);
                    sdpInfo = getSdpInfoFromDescribeParams(params);
                    if (!requestVideo)
                        sdpInfo.videoTrack = null;
                    if (!requestAudio)
                        sdpInfo.audioTrack = null;
                    // Only AAC supported
                    if (sdpInfo.audioTrack != null && sdpInfo.audioTrack.audioCodec == AUDIO_CODEC_UNKNOWN) {
                        Log.e(TAG_DEBUG, "Unknown RTSP audio codec (" + sdpInfo.audioTrack.audioCodec + ") specified in SDP");
                        sdpInfo.audioTrack = null;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }


// SETUP rtsp://10.0.1.78:8080/video/h264/trackID=1 RTSP/1.0
// Transport: RTP/AVP/TCP;unicast;interleaved=0-1
// CSeq: 3
// User-Agent: Lavf58.29.100

// RTSP/1.0 200 OK
// CSeq: 3
// Transport: RTP/AVP/TCP;unicast;interleaved=0-1
// Session: Mzk5MzY2MzUwMTg3NTc2Mzc5NQ;timeout=30
            String session = null;
            int sessionTimeout = 0;
            for (int i = 0; i < 2; i++) {
                // i=0 - video track, i=1 - audio track
                checkExitFlag(exitFlag);
                Track track = (i == 0 ?
                        (requestVideo ? sdpInfo.videoTrack : null) :
                        (requestAudio ? sdpInfo.audioTrack : null));
                if (track != null) {
                    String uriRtspSetup = getUriForSetup(uriRtsp, track);
                    if (uriRtspSetup == null) {
                        Log.e(TAG, "Failed to get RTSP URI for SETUP");
                        continue;
                    }
                    if (digestRealmNonce != null)
                        authToken = getDigestAuthHeader(
                                username,
                                password,
                                "SETUP",
                                uriRtspSetup,
                                digestRealmNonce.first,
                                digestRealmNonce.second);
                    sendSetupCommand(
                            outputStream,
                            uriRtspSetup,
                            cSeq.addAndGet(1),
                            userAgent,
                            authToken,
                            session,
                            (i == 0 ? "0-1" /*video*/ : "2-3" /*audio*/));
                    status = readResponseStatusCode(inputStream);
                    if (DEBUG)
                        Log.i(TAG, "SETUP status: " + status);
                    checkStatusCode(status);
                    headers = readResponseHeaders(inputStream);
                    dumpHeaders(headers);
                    session = getHeader(headers, "Session");
                    if (!TextUtils.isEmpty(session)) {
                        // ODgyODg3MjQ1MDczODk3NDk4Nw;timeout=30
                        String[] params = TextUtils.split(session, ";");
                        session = params[0];
                        // Getting session timeout
                        if (params.length > 1) {
                            params = TextUtils.split(params[1], "=");
                            if (params.length > 1) {
                                try {
                                    sessionTimeout = Integer.parseInt(params[1]);
                                } catch (NumberFormatException e) {
                                    Log.e(TAG, "Failed to parse RTSP session timeout");
                                }
                            }
                        }
                    }
                    if (DEBUG)
                        Log.d(TAG, "SETUP session: " + session + ", timeout: " + sessionTimeout);
                    if (TextUtils.isEmpty(session))
                        throw new IOException("Failed to get RTSP session");
                }
            }

            if (TextUtils.isEmpty(session))
                throw new IOException("Failed to get any media track");

// PLAY rtsp://10.0.1.78:8080/video/h264 RTSP/1.0
// Range: npt=0.000-
// CSeq: 5
// User-Agent: Lavf58.29.100
// Session: Mzk5MzY2MzUwMTg3NTc2Mzc5NQ

// RTSP/1.0 200 OK
// CSeq: 5
// RTP-Info: url=/video/h264;seq=56
// Session: Mzk5MzY2MzUwMTg3NTc2Mzc5NQ;timeout=30
            checkExitFlag(exitFlag);
            if (digestRealmNonce != null)
                authToken = getDigestAuthHeader(username, password, "PLAY", uriRtsp /*?*/, digestRealmNonce.first, digestRealmNonce.second);
            sendPlayCommand(outputStream, uriRtsp, cSeq.addAndGet(1), userAgent, authToken, session);
            status = readResponseStatusCode(inputStream);
            if (DEBUG)
                Log.i(TAG, "PLAY status: " + status);
            checkStatusCode(status);
            headers = readResponseHeaders(inputStream);
            dumpHeaders(headers);

            listener.onRtspConnected(sdpInfo);

            if (sdpInfo.videoTrack != null ||  sdpInfo.audioTrack != null) {
                if (digestRealmNonce != null)
                    authToken = getDigestAuthHeader(username, password, hasCapability(RTSP_CAPABILITY_GET_PARAMETER, capabilities) ? "GET_PARAMETER" : "OPTIONS", uriRtsp, digestRealmNonce.first, digestRealmNonce.second);
                final String authTokenFinal = authToken;
                final String sessionFinal = session;
                RtspClientKeepAliveListener keepAliveListener = () -> {
                    try {
                        //GET_PARAMETER rtsp://10.0.1.155:554/cam/realmonitor?channel=1&subtype=1/ RTSP/1.0
                        //CSeq: 6
                        //User-Agent: Lavf58.45.100
                        //Session: 4066342621205
                        //Authorization: Digest username="admin", realm="Login to cam", nonce="8fb58500489d60f99a40b43f3c8574ef", uri="rtsp://10.0.1.155:554/cam/realmonitor?channel=1&subtype=1/", response="692a26124a1ee9562135785ace33a23b"

                        //RTSP/1.0 200 OK
                        //CSeq: 6
                        //Session: 4066342621205
                        if (debug)
                            Log.d(TAG_DEBUG, "Sending keep-alive");
                        if (hasCapability(RTSP_CAPABILITY_GET_PARAMETER, capabilities))
                            sendGetParameterCommand(outputStream, uriRtsp, cSeq.addAndGet(1), userAgent, sessionFinal, authTokenFinal);
                        else
                            sendOptionsCommand(outputStream, uriRtsp, cSeq.addAndGet(1), userAgent, authTokenFinal);

                        // Do not read response right now, since it may contain unread RTP frames.
                        // RtpHeader.searchForNextRtpHeader will handle that.
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                };

                // Blocking call unless exitFlag set to true, thread.interrupt() called or connection closed.
                try {
                    readRtpData(
                            inputStream,
                            sdpInfo,
                            exitFlag,
                            listener,
                            sessionTimeout / 2 * 1000,
                            keepAliveListener);
                } finally {
                    // Cleanup resources on server side
                    if (hasCapability(RTSP_CAPABILITY_TEARDOWN, capabilities)) {
                        if (digestRealmNonce != null)
                            authToken = getDigestAuthHeader(username, password, "TEARDOWN", uriRtsp, digestRealmNonce.first, digestRealmNonce.second);
                        sendTeardownCommand(outputStream, uriRtsp, cSeq.addAndGet(1), userAgent, authToken, sessionFinal);
                    }
                }

            } else {
                listener.onRtspFailed("No tracks found. RTSP server issue.");
            }

            listener.onRtspDisconnecting();
            listener.onRtspDisconnected();
        } catch (UnauthorizedException e) {
            e.printStackTrace();
            listener.onRtspFailedUnauthorized();
        } catch (InterruptedException e) {
            // Thread interrupted. Expected behavior.
            listener.onRtspDisconnecting();
            listener.onRtspDisconnected();
        } catch (Exception e) {
            e.printStackTrace();
            listener.onRtspFailed(e.getMessage());
        }
        try {
            rtspSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Nullable
    private static String getUriForSetup(@NonNull String uriRtsp, @Nullable Track track) {
        if (track == null || TextUtils.isEmpty(track.request))
            return null;

        String uriRtspSetup = uriRtsp;
        if (track.request.startsWith("rtsp://") || track.request.startsWith("rtsps://")) {
            // Absolute URL
            uriRtspSetup = track.request;
        } else {
            // Relative URL
            if (!track.request.startsWith("/")) {
                track.request = "/" + track.request;
            }
            uriRtspSetup += track.request;
        }
        return uriRtspSetup;
    }

    private static void checkExitFlag(@NonNull AtomicBoolean exitFlag) throws InterruptedException {
        if (exitFlag.get())
            throw new InterruptedException();
    }

    private static void checkStatusCode(int code) throws IOException {
        switch (code) {
            case 200:
                break;
            case 401:
                throw new UnauthorizedException();
            default:
                throw new IOException("Invalid status code " + code);
        }
    }

    private static void readRtpData(
            @NonNull InputStream inputStream,
            @NonNull SdpInfo sdpInfo,
            @NonNull AtomicBoolean exitFlag,
            @NonNull RtspClientListener listener,
            int keepAliveTimeout,
            @NonNull RtspClientKeepAliveListener keepAliveListener)
    throws IOException {
        byte[] data = new byte[0]; // Usually not bigger than MTU = 15KB

        final VideoRtpParser videoParser = new VideoRtpParser();
        final AacParser audioParser = (sdpInfo.audioTrack != null && sdpInfo.audioTrack.audioCodec == AUDIO_CODEC_AAC ?
                new AacParser(sdpInfo.audioTrack.mode) :
                null);

        byte[] nalUnitSps = (sdpInfo.videoTrack != null ? sdpInfo.videoTrack.sps : null);
        byte[] nalUnitPps = (sdpInfo.videoTrack != null ? sdpInfo.videoTrack.pps : null);

        long keepAliveSent = System.currentTimeMillis();

        while (!exitFlag.get()) {
            RtpParser.RtpHeader header = RtpParser.readHeader(inputStream);
            if (header == null) {
                continue;
//                throw new IOException("No RTP frames found");
            }
//          header.dumpHeader();
            if (header.payloadSize > data.length)
                data = new byte[header.payloadSize];

            NetUtils.readData(inputStream, data, 0, header.payloadSize);

            // Check if keep-alive should be sent
            long l = System.currentTimeMillis();
            if (keepAliveTimeout > 0 && l - keepAliveSent > keepAliveTimeout) {
                keepAliveSent = l;
                keepAliveListener.onRtspKeepAliveRequested();
            }

            // Video
            if (sdpInfo.videoTrack != null && header.payloadType == sdpInfo.videoTrack.payloadType) {
                byte[] nalUnit = videoParser.processRtpPacketAndGetNalUnit(data, header.payloadSize);
                if (nalUnit != null) {
                    byte type = VideoCodecUtils.getH264NalUnitType(nalUnit, 0, nalUnit.length);
//                  Log.i(TAG, "NAL u: " + VideoCodecUtils.getH264NalUnitTypeString(type));
                    switch (type) {
                        case VideoCodecUtils.NAL_SPS:
                            nalUnitSps = nalUnit;
                            // Looks like there is NAL_IDR_SLICE as well. Send it now.
                            if (nalUnit.length > 100)
                                listener.onRtspVideoNalUnitReceived(nalUnit, 0, nalUnit.length, (long)(header.timeStamp * 11.111111));
                            break;
                        case VideoCodecUtils.NAL_PPS:
                            nalUnitPps = nalUnit;
                            // Looks like there is NAL_IDR_SLICE as well. Send it now.
                            if (nalUnit.length > 100)
                                listener.onRtspVideoNalUnitReceived(nalUnit, 0, nalUnit.length, (long)(header.timeStamp * 11.111111));
                            break;
                        case VideoCodecUtils.NAL_IDR_SLICE:
                            // Combine IDR with SPS/PPS
                            if (nalUnitSps != null && nalUnitPps != null) {
//                                byte[] nalUnitSppPpsIdr = new byte[nalUnitSps.length + nalUnitPps.length + nalUnit.length];
//                                System.arraycopy(nalUnitSps, 0, nalUnitSppPpsIdr, 0, nalUnitSps.length);
//                                System.arraycopy(nalUnitPps, 0, nalUnitSppPpsIdr, nalUnitSps.length, nalUnitPps.length);
//                                System.arraycopy(nalUnit, 0, nalUnitSppPpsIdr, nalUnitSps.length + nalUnitPps.length, nalUnit.length);
//                                listener.onRtspNalUnitReceived(nalUnitSppPpsIdr, 0, nalUnitSppPpsIdr.length, System.currentTimeMillis());
                                byte[] nalUnitSppPps = new byte[nalUnitSps.length + nalUnitPps.length];
                                System.arraycopy(nalUnitSps, 0, nalUnitSppPps, 0, nalUnitSps.length);
                                System.arraycopy(nalUnitPps, 0, nalUnitSppPps, nalUnitSps.length, nalUnitPps.length);
                                listener.onRtspVideoNalUnitReceived(nalUnitSppPps, 0, nalUnitSppPps.length, (long)(header.timeStamp * 11.111111));
//                                listener.onRtspNalUnitReceived(nalUnitSppPps, 0, nalUnitSppPps.length, System.currentTimeMillis() / 10);
                                // Send it only once
                                nalUnitSps = null;
                                nalUnitPps = null;
                            }
//                            listener.onRtspNalUnitReceived(nalUnitSps, 0, nalUnitSps.length, System.currentTimeMillis());
//                            listener.onRtspNalUnitReceived(nalUnitPps, 0, nalUnitPps.length, System.currentTimeMillis());
//                            listener.onRtspNalUnitReceived(nalUnit, 0, nalUnit.length, System.currentTimeMillis());
//                            break;
                        default:
                            listener.onRtspVideoNalUnitReceived(nalUnit, 0, nalUnit.length, (long)(header.timeStamp * 11.111111));
//                            listener.onRtspNalUnitReceived(nalUnit, 0, nalUnit.length, System.currentTimeMillis() / 10);
                    }
                }

            // Audio
            } else if (sdpInfo.audioTrack != null && header.payloadType == sdpInfo.audioTrack.payloadType) {
                if (audioParser != null) {
                    byte[] sample = audioParser.processRtpPacketAndGetSample(data, header.payloadSize);
                    if (sample != null)
                        listener.onRtspAudioSampleReceived(sample, 0, sample.length, (long) (header.timeStamp * 11.111111));
                }

            // Unknown
            } else {
                // https://www.iana.org/assignments/rtp-parameters/rtp-parameters.xhtml
                if (DEBUG && header.payloadType >= 96 && header.payloadType <= 127)
                    Log.w(TAG, "Invalid RTP payload type " + header.payloadType);
            }
        }
    }

    private static void sendSimpleCommand(
            @NonNull String command,
            @NonNull OutputStream outputStream,
            @NonNull String request,
            int cSeq,
            @Nullable String userAgent,
            @Nullable String session,
            @Nullable String authToken)
    throws IOException {
        outputStream.write((command + " " + request + " RTSP/1.0" + CRLF).getBytes());
        if (authToken != null)
            outputStream.write(("Authorization: " + authToken + CRLF).getBytes());
        outputStream.write(("CSeq: " + cSeq + CRLF).getBytes());
        if (userAgent != null)
            outputStream.write(("User-Agent: " + userAgent + CRLF).getBytes());
        if (session != null)
            outputStream.write(("Session: " + session + CRLF).getBytes());
        outputStream.write(CRLF.getBytes());
        outputStream.flush();
    }

    private static void sendOptionsCommand(
            @NonNull OutputStream outputStream,
            @NonNull String request,
            int cSeq,
            @Nullable String userAgent,
            @Nullable String authToken)
    throws IOException {
        if (DEBUG) Log.v(TAG, "sendOptionsCommand(request=\"" + request + "\", cSeq=" + cSeq + ")");
        sendSimpleCommand("OPTIONS", outputStream, request, cSeq, userAgent, null, authToken);
    }

    private static void sendGetParameterCommand(
            @NonNull OutputStream outputStream,
            @NonNull String request,
            int cSeq,
            @Nullable String userAgent,
            @Nullable String session,
            @Nullable String authToken)
    throws IOException {
        if (DEBUG) Log.v(TAG, "sendGetParameterCommand(request=\"" + request + "\", cSeq=" + cSeq + ")");
        sendSimpleCommand("GET_PARAMETER", outputStream, request, cSeq, userAgent, session, authToken);
    }

    private static void sendDescribeCommand(
            @NonNull OutputStream outputStream,
            @NonNull String request,
            int cSeq,
            @Nullable String userAgent,
            @Nullable String authToken)
    throws IOException {
        if (DEBUG) Log.v(TAG, "sendDescribeCommand(request=\"" + request + "\", cSeq=" + cSeq + ")");
        outputStream.write(("DESCRIBE " + request + " RTSP/1.0" + CRLF).getBytes());
        outputStream.write(("Accept: application/sdp" + CRLF).getBytes());
        if (authToken != null)
            outputStream.write(("Authorization: " + authToken + CRLF).getBytes());
        outputStream.write(("CSeq: " + cSeq + CRLF).getBytes());
        if (userAgent != null)
            outputStream.write(("User-Agent: " + userAgent + CRLF).getBytes());
        outputStream.write(CRLF.getBytes());
        outputStream.flush();
    }

    private static void sendTeardownCommand(
            @NonNull OutputStream outputStream,
            @NonNull String request,
            int cSeq,
            @Nullable String userAgent,
            @Nullable String authToken,
            @Nullable String session)
    throws IOException {
        if (DEBUG) Log.v(TAG, "sendTeardownCommand(request=\"" + request + "\", cSeq=" + cSeq + ")");
        outputStream.write(("TEARDOWN " + request + " RTSP/1.0" + CRLF).getBytes());
        if (authToken != null)
            outputStream.write(("Authorization: " + authToken + CRLF).getBytes());
        outputStream.write(("CSeq: " + cSeq + CRLF).getBytes());
        if (userAgent != null)
            outputStream.write(("User-Agent: " + userAgent + CRLF).getBytes());
        if (session != null)
            outputStream.write(("Session: " + session + CRLF).getBytes());
        outputStream.write(CRLF.getBytes());
        outputStream.flush();
    }

    private static void sendSetupCommand(
            @NonNull OutputStream outputStream,
            @NonNull String request,
            int cSeq,
            @Nullable String userAgent,
            @Nullable String authToken,
            @Nullable String session,
            @NonNull String interleaved)
    throws IOException {
        if (DEBUG) Log.v(TAG, "sendSetupCommand(request=\"" + request + "\", cSeq=" + cSeq + ")");
        outputStream.write(("SETUP " + request + " RTSP/1.0" + CRLF).getBytes());
        outputStream.write(("Transport: RTP/AVP/TCP;unicast;interleaved=" + interleaved + CRLF).getBytes());
        if (authToken != null)
            outputStream.write(("Authorization: " + authToken + CRLF).getBytes());
        outputStream.write(("CSeq: " + cSeq + CRLF).getBytes());
        if (userAgent != null)
            outputStream.write(("User-Agent: " + userAgent + CRLF).getBytes());
        if (session != null)
            outputStream.write(("Session: " + session + CRLF).getBytes());
        outputStream.write(CRLF.getBytes());
        outputStream.flush();
    }

    private static void sendPlayCommand(
            @NonNull OutputStream outputStream,
            @NonNull String request,
            int cSeq,
            @Nullable String userAgent,
            @Nullable String authToken,
            @NonNull String session)
    throws IOException {
        if (DEBUG) Log.v(TAG, "sendPlayCommand(request=\"" + request + "\", cSeq=" + cSeq + ")");
        outputStream.write(("PLAY " + request + " RTSP/1.0" + CRLF).getBytes());
        outputStream.write(("Range: npt=0.000-" + CRLF).getBytes());
        if (authToken != null)
            outputStream.write(("Authorization: " + authToken + CRLF).getBytes());
        outputStream.write(("CSeq: " + cSeq + CRLF).getBytes());
        if (userAgent != null)
            outputStream.write(("User-Agent: " + userAgent + CRLF).getBytes());
        outputStream.write(("Session: " + session + CRLF).getBytes());
        outputStream.write(CRLF.getBytes());
        outputStream.flush();
    }

    private int readResponseStatusCode(@NonNull InputStream inputStream) throws IOException {
//        String line = readLine(inputStream);
//        if (debug)
//            Log.d(TAG_DEBUG, "" + line);
        String line;
        byte[] rtspHeader = "RTSP/1.0 ".getBytes();
        // Search fpr "RTSP/1.0 "
        while (!exitFlag.get() && readUntilBytesFound(inputStream, rtspHeader) && (line = readLine(inputStream)) != null) {
            if (debug)
                Log.d(TAG_DEBUG, "" + line);
//            int indexRtsp = line.indexOf("TSP/1.0 "); // 8 characters, 'R' already found
//            if (indexRtsp >= 0) {
            int indexCode = line.indexOf(' ');
            String code = line.substring(0, indexCode);
            try {
                int statusCode = Integer.parseInt(code);
                if (debug)
                    Log.d(TAG_DEBUG, "Status code: " + statusCode);
                return statusCode;
            } catch (NumberFormatException e) {
                // Does not fulfill standard "RTSP/1.1 200 OK" token
                // Continue search for
            }
//            }
        }
        if (debug)
            Log.w(TAG_DEBUG, "Could not obtain status code");
        return -1;
    }

    @NonNull
    private ArrayList<Pair<String, String>> readResponseHeaders(@NonNull InputStream inputStream) throws IOException {
        ArrayList<Pair<String, String>> headers = new ArrayList<>();
        String line;
        while (!exitFlag.get() && !TextUtils.isEmpty(line = readLine(inputStream))) {
            if (debug)
                Log.d(TAG_DEBUG, "" + line);
            if (CRLF.equals(line)) {
                return headers;
            } else {
                String[] pairs = TextUtils.split(line, ":");
                if (pairs.length == 2) {
                    headers.add(Pair.create(pairs[0].trim(), pairs[1].trim()));
                }
            }
        }
        return headers;
    }

    /**
     * Get a list of tracks from SDP. Usually contains video and audio track only.
     * @return array of 2 tracks. First is video track, second audio track.
     */
    @NonNull
    private static Track[] getTracksFromDescribeParams(@NonNull List<Pair<String, String>> params) {
        Track[] tracks = new Track[2];
        Track currentTrack = null;
        for (Pair<String, String> param: params) {
            switch (param.first) {
                case "m":
                    // m=video 0 RTP/AVP 96
                    if (param.second.startsWith("video")) {
                        currentTrack = new VideoTrack();
                        tracks[0] = currentTrack;

                    // m=audio 0 RTP/AVP 97
                    } else if (param.second.startsWith("audio")) {
                        currentTrack = new AudioTrack();
                        tracks[1] = currentTrack;

                    } else {
                        currentTrack = null;
                    }
                    if (currentTrack != null) {
                        // m=<media> <port>/<number of ports> <proto> <fmt> ...
                        String[] values = TextUtils.split(param.second, " ");
                        currentTrack.payloadType = (values.length > 3 ? Integer.parseInt(values[3]) : -1);
                        if (currentTrack.payloadType == -1)
                            Log.e(TAG, "Failed to get payload type from \"m=" + param.second + "\"");
                    }
                    break;

                case "a":
                    // a=control:trackID=1
                    if (currentTrack != null) {
                        if (param.second.startsWith("control:")) {
                            currentTrack.request = param.second.substring(8);

                        // a=fmtp:96 packetization-mode=1; profile-level-id=4D4029; sprop-parameter-sets=Z01AKZpmBkCb8uAtQEBAQXpw,aO48gA==
                        // a=fmtp:97 streamtype=5; profile-level-id=15; mode=AAC-hbr; config=1408; sizeLength=13; indexLength=3; indexDeltaLength=3; profile=1; bitrate=32000;
                        // a=fmtp:97 streamtype=5;profile-level-id=1;mode=AAC-hbr;sizelength=13;indexlength=3;indexdeltalength=3;config=1408
                        // a=fmtp:96 streamtype=5; profile-level-id=14; mode=AAC-lbr; config=1388; sizeLength=6; indexLength=2; indexDeltaLength=2; constantDuration=1024; maxDisplacement=5
                        // a=fmtp:96 profile-level-id=1;mode=AAC-hbr;sizelength=13;indexlength=3;indexdeltalength=3;config=1210fff15081ffdffc
                        // a=fmtp:96
                        } else if (param.second.startsWith("fmtp:")) {
                            // Video
                            if (currentTrack instanceof VideoTrack) {
                                updateVideoTrackFromDescribeParam((VideoTrack)tracks[0], param);
                            // Audio
                            } else {
                                updateAudioTrackFromDescribeParam((AudioTrack)tracks[1], param);
                            }

                        // a=rtpmap:96 H264/90000
                        // a=rtpmap:97 mpeg4-generic/16000/1
                        // a=rtpmap:97 MPEG4-GENERIC/16000
                        // a=rtpmap:97 G726-32/8000
                        // a=rtpmap:96 mpeg4-generic/44100/2
                        } else if (param.second.startsWith("rtpmap:")) {
                            // Video
                            if (currentTrack instanceof VideoTrack) {
                                String[] values = TextUtils.split(param.second, " ");
                                if (values.length > 1) {
                                    values = TextUtils.split(values[1], "/");
                                    if (values.length > 0) {
                                        switch (values[0].toLowerCase()) {
                                            case "h264" -> ((VideoTrack) tracks[0]).videoCodec = VIDEO_CODEC_H264;
                                            case "h265" -> ((VideoTrack) tracks[0]).videoCodec = VIDEO_CODEC_H265;
                                            default -> Log.w(TAG, "Unknown video codec \"" + values[0] + "\"");
                                        }
                                        Log.i(TAG, "Video: " + values[0]);
                                    }
                                }
                            // Audio
                            } else {
                                String[] values = TextUtils.split(param.second, " ");
                                if (values.length > 1) {
                                    AudioTrack track = ((AudioTrack) tracks[1]);
                                    values = TextUtils.split(values[1], "/");
                                    if (values.length > 1) {
                                        switch (values[0].toLowerCase()) {
                                            case "mpeg4-generic" -> track.audioCodec = AUDIO_CODEC_AAC;
                                            case "opus" -> track.audioCodec = AUDIO_CODEC_OPUS;
                                            default -> {
                                                Log.w(TAG, "Unknown audio codec \"" + values[0] + "\"");
                                                track.audioCodec = AUDIO_CODEC_UNKNOWN;
                                            }
                                        }
                                        track.sampleRateHz = Integer.parseInt(values[1]);
                                        // If no channels specified, use mono, e.g. "a=rtpmap:97 MPEG4-GENERIC/8000"
                                        track.channels = values.length > 2 ? Integer.parseInt(values[2]) : 1;
                                        Log.i(TAG, "Audio: " + getAudioCodecName(track.audioCodec) + ", sample rate: " + track.sampleRateHz + " Hz, channels: " + track.channels);
                                    }
                                }

                            }
                        }
                    }
                    break;
            }
        }
        return tracks;
    }

//v=0
//o=- 1542237507365806 1542237507365806 IN IP4 10.0.1.111
//s=Media Presentation
//e=NONE
//b=AS:50032
//t=0 0
//a=control:*
//a=range:npt=0.000000-
//m=video 0 RTP/AVP 96
//c=IN IP4 0.0.0.0
//b=AS:50000
//a=framerate:25.0
//a=transform:1.000000,0.000000,0.000000;0.000000,1.000000,0.000000;0.000000,0.000000,1.000000
//a=control:trackID=1
//a=rtpmap:96 H264/90000
//a=fmtp:96 packetization-mode=1; profile-level-id=4D4029; sprop-parameter-sets=Z01AKZpmBkCb8uAtQEBAQXpw,aO48gA==
//m=audio 0 RTP/AVP 97
//c=IN IP4 0.0.0.0
//b=AS:32
//a=control:trackID=2
//a=rtpmap:97 G726-32/8000

// v=0
// o=- 14190294250618174561 14190294250618174561 IN IP4 127.0.0.1
// s=IP Webcam
// c=IN IP4 0.0.0.0
// t=0 0
// a=range:npt=now-
// a=control:*
// m=video 0 RTP/AVP 96
// a=rtpmap:96 H264/90000
// a=control:h264
// a=fmtp:96 packetization-mode=1;profile-level-id=42C028;sprop-parameter-sets=Z0LAKIyNQDwBEvLAPCIRqA==,aM48gA==;
// a=cliprect:0,0,1920,1080
// a=framerate:30.0
// a=framesize:96 1080-1920

    // Pair first - name, e.g. "a"; second - value, e.g "cliprect:0,0,1920,1080"
    @NonNull
    private static List<Pair<String, String>> getDescribeParams(@NonNull String text) {
        ArrayList<Pair<String, String>> list = new ArrayList<>();
        String[] params = TextUtils.split(text, "\r\n");
        for (String param : params) {
            int i = param.indexOf('=');
            if (i > 0) {
                String name = param.substring(0, i).trim();
                String value = param.substring(i + 1);
                list.add(Pair.create(name, value));
            }
        }
        return list;
    }

    @NonNull
    private static SdpInfo getSdpInfoFromDescribeParams(@NonNull List<Pair<String, String>> params) {
        SdpInfo sdpInfo = new SdpInfo();

        Track[] tracks = getTracksFromDescribeParams(params);
        sdpInfo.videoTrack = ((VideoTrack)tracks[0]);
        sdpInfo.audioTrack = ((AudioTrack)tracks[1]);

        for (Pair<String, String> param : params) {
            switch (param.first) {
                case "s" -> sdpInfo.sessionName = param.second;
                case "i" -> sdpInfo.sessionDescription = param.second;
            }
        }
        return sdpInfo;
    }

    // a=fmtp:97 streamtype=5;profile-level-id=1;mode=AAC-hbr;sizelength=13;indexlength=3;indexdeltalength=3;config=1408
    @Nullable
    private static List<Pair<String, String>> getSdpAParams(@NonNull Pair<String, String> param) {
        if (param.first.equals("a") && param.second.startsWith("fmtp:") && param.second.length() > 8) { //
            String value = param.second.substring(8).trim(); // fmtp can be '96' (2 chars) and '127' (3 chars)
            String[] paramsA = TextUtils.split(value, ";");
            // streamtype=5
            // profile-level-id=1
            // mode=AAC-hbr
            ArrayList<Pair<String, String>> retParams = new ArrayList<>();
            for (String paramA: paramsA) {
                paramA = paramA.trim();
                // sprop-parameter-sets=Z0LAKIyNQDwBEvLAPCIRqA==,aM48gA==
                int i = paramA.indexOf("=");
                if (i != -1)
                    retParams.add(
                            Pair.create(
                                    paramA.substring(0, i),
                                    paramA.substring(i + 1)));
            }
            return retParams;
        } else {
            Log.w(TAG, "Not a valid fmtp");
        }
        return null;
    }

    private static void updateVideoTrackFromDescribeParam(@NonNull VideoTrack videoTrack, @NonNull Pair<String, String> param) {
        // a=fmtp:96 packetization-mode=1;profile-level-id=42C028;sprop-parameter-sets=Z0LAKIyNQDwBEvLAPCIRqA==,aM48gA==;
        // a=fmtp:96 packetization-mode=1; profile-level-id=4D4029; sprop-parameter-sets=Z01AKZpmBkCb8uAtQEBAQXpw,aO48gA==
        // a=fmtp:99 sprop-parameter-sets=Z0LgKdoBQBbpuAgIMBA=,aM4ySA==;packetization-mode=1;profile-level-id=42e029
        List<Pair<String, String>> params = getSdpAParams(param);
        if (params != null) {
            for (Pair<String, String> pair: params) {
                switch (pair.first.toLowerCase()) {
                    case "sprop-parameter-sets" -> {
                            String[] paramsSpsPps = TextUtils.split(pair.second, ",");
                            if (paramsSpsPps.length > 1) {
                                byte[] sps = Base64.decode(paramsSpsPps[0], Base64.NO_WRAP);
                                byte[] pps = Base64.decode(paramsSpsPps[1], Base64.NO_WRAP);
                                byte[] nalSps = new byte[sps.length + 4];
                                byte[] nalPps = new byte[pps.length + 4];
                                // Add 00 00 00 01 NAL unit header
                                nalSps[0] = 0;
                                nalSps[1] = 0;
                                nalSps[2] = 0;
                                nalSps[3] = 1;
                                System.arraycopy(sps, 0, nalSps, 4, sps.length);
                                nalPps[0] = 0;
                                nalPps[1] = 0;
                                nalPps[2] = 0;
                                nalPps[3] = 1;
                                System.arraycopy(pps, 0, nalPps, 4, pps.length);
                                videoTrack.sps = nalSps;
                                videoTrack.pps = nalPps;
                            }
                        }
                }
            }
        }
    }

    @NonNull
    private static byte[] getBytesFromHexString(@NonNull String config) {
        // "1210fff1" -> [12, 10, ff, f1]
        return new BigInteger(config ,16).toByteArray();
    }

    private static void updateAudioTrackFromDescribeParam(@NonNull AudioTrack audioTrack, @NonNull Pair<String, String> param) {
        // a=fmtp:96 streamtype=5; profile-level-id=14; mode=AAC-lbr; config=1388; sizeLength=6; indexLength=2; indexDeltaLength=2; constantDuration=1024; maxDisplacement=5
        // a=fmtp:97 streamtype=5;profile-level-id=1;mode=AAC-hbr;sizelength=13;indexlength=3;indexdeltalength=3;config=1408
        // a=fmtp:96 profile-level-id=1;mode=AAC-hbr;sizelength=13;indexlength=3;indexdeltalength=3;config=1210fff15081ffdffc
        List<Pair<String, String>> params = getSdpAParams(param);
        if (params != null) {
            for (Pair<String, String> pair: params) {
                switch (pair.first.toLowerCase()) {
                    case "mode" -> audioTrack.mode = pair.second;
                    case "config" -> audioTrack.config = getBytesFromHexString(pair.second);
                }
            }
        }
    }

    private static int getHeaderContentLength(@NonNull ArrayList<Pair<String, String>> headers) {
        String length = getHeader(headers, "content-length");
        if (!TextUtils.isEmpty(length)) {
            try {
                return Integer.parseInt(length);
            } catch (NumberFormatException ignored) {
            }
        }
        return -1;
    }

    private static int getSupportedCapabilities(@NonNull ArrayList<Pair<String, String>> headers) {
        for (Pair<String, String> head: headers) {
            String h = head.first.toLowerCase();
            // Public: OPTIONS, DESCRIBE, SETUP, PLAY, GET_PARAMETER, SET_PARAMETER, TEARDOWN
            if ("public".equals(h)) {
                int mask = 0;
                String[] tokens = TextUtils.split(head.second.toLowerCase(), ",");
                for (String token: tokens) {
                    switch (token.trim()) {
                        case "options" -> mask |= RTSP_CAPABILITY_OPTIONS;
                        case "describe" -> mask |= RTSP_CAPABILITY_DESCRIBE;
                        case "announce" -> mask |= RTSP_CAPABILITY_ANNOUNCE;
                        case "setup" -> mask |= RTSP_CAPABILITY_SETUP;
                        case "play" -> mask |= RTSP_CAPABILITY_PLAY;
                        case "record" -> mask |= RTSP_CAPABILITY_RECORD;
                        case "pause" -> mask |= RTSP_CAPABILITY_PAUSE;
                        case "teardown" -> mask |= RTSP_CAPABILITY_TEARDOWN;
                        case "set_parameter" -> mask |= RTSP_CAPABILITY_SET_PARAMETER;
                        case "get_parameter" -> mask |= RTSP_CAPABILITY_GET_PARAMETER;
                        case "redirect" -> mask |= RTSP_CAPABILITY_REDIRECT;
                    }
                }
                return mask;
            }
        }
        return RTSP_CAPABILITY_NONE;
    }

    @Nullable
    private static Pair<String, String> getHeaderWwwAuthenticateDigestRealmAndNonce(@NonNull ArrayList<Pair<String, String>> headers) {
        for (Pair<String, String> head: headers) {
            String h = head.first.toLowerCase();
            // WWW-Authenticate: Digest realm="AXIS_00408CEF081C", nonce="00054cecY7165349339ae05f7017797d6b0aaad38f6ff45", stale=FALSE
            // WWW-Authenticate: Basic realm="AXIS_00408CEF081C"
            // WWW-Authenticate: Digest realm="Login to 4K049EBPAG1D7E7", nonce="de4ccb15804565dc8a4fa5b115695f4f"
            if ("www-authenticate".equals(h) && head.second.toLowerCase().startsWith("digest")) {
                String v = head.second.substring(7).trim();
                int begin, end;

                begin = v.indexOf("realm=");
                begin = v.indexOf('"', begin) + 1;
                end = v.indexOf('"', begin);
                String digestRealm = v.substring(begin, end);

                begin = v.indexOf("nonce=");
                begin = v.indexOf('"', begin)+1;
                end = v.indexOf('"', begin);
                String digestNonce = v.substring(begin, end);

                return Pair.create(digestRealm, digestNonce);
            }
        }
        return null;
    }

    @Nullable
    private static String getHeaderWwwAuthenticateBasicRealm(@NonNull ArrayList<Pair<String, String>> headers) {
        for (Pair<String, String> head: headers) {
            // Session: ODgyODg3MjQ1MDczODk3NDk4Nw
            String h = head.first.toLowerCase();
            String v = head.second.toLowerCase();
            // WWW-Authenticate: Digest realm="AXIS_00408CEF081C", nonce="00054cecY7165349339ae05f7017797d6b0aaad38f6ff45", stale=FALSE
            // WWW-Authenticate: Basic realm="AXIS_00408CEF081C"
            if ("www-authenticate".equals(h) && v.startsWith("basic")) {
                v = v.substring(6).trim();
                // realm=
                // AXIS_00408CEF081C
                String[] tokens = TextUtils.split(v, "\"");
                if (tokens.length > 2)
                    return tokens[1];
            }
        }
        return null;
    }

    // Basic authentication
    @NonNull
    private static String getBasicAuthHeader(@Nullable String username, @Nullable String password) {
        String auth = (username == null ? "" : username) + ":" + (password == null ? "" : password);
        return "Basic " + new String(Base64.encode(auth.getBytes(StandardCharsets.ISO_8859_1), Base64.NO_WRAP));
    }

    // Digest authentication
    @Nullable
    private static String getDigestAuthHeader(
            @Nullable String username,
            @Nullable String password,
            @NonNull String method,
            @NonNull String digestUri,
            @NonNull String realm,
            @NonNull String nonce) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] ha1;

            if (username == null)
                username = "";
            if (password == null)
                password = "";

            // calc A1 digest
            md.update(username.getBytes(StandardCharsets.ISO_8859_1));
            md.update((byte) ':');
            md.update(realm.getBytes(StandardCharsets.ISO_8859_1));
            md.update((byte) ':');
            md.update(password.getBytes(StandardCharsets.ISO_8859_1));
            ha1 = md.digest();

            // calc A2 digest
            md.reset();
            md.update(method.getBytes(StandardCharsets.ISO_8859_1));
            md.update((byte) ':');
            md.update(digestUri.getBytes(StandardCharsets.ISO_8859_1));
            byte[] ha2 = md.digest();

            // calc response
            md.update(getHexStringFromBytes(ha1).getBytes(StandardCharsets.ISO_8859_1));
            md.update((byte) ':');
            md.update(nonce.getBytes(StandardCharsets.ISO_8859_1));
            md.update((byte) ':');
            // TODO add support for more secure version of digest auth
            //md.update(nc.getBytes(StandardCharsets.ISO_8859_1));
            //md.update((byte) ':');
            //md.update(cnonce.getBytes(StandardCharsets.ISO_8859_1));
            //md.update((byte) ':');
            //md.update(qop.getBytes(StandardCharsets.ISO_8859_1));
            //md.update((byte) ':');
            md.update(getHexStringFromBytes(ha2).getBytes(StandardCharsets.ISO_8859_1));
            String response = getHexStringFromBytes(md.digest());

//            log.trace("username=\"{}\", realm=\"{}\", nonce=\"{}\", uri=\"{}\", response=\"{}\"",
//                    userName, digestRealm, digestNonce, digestUri, response);

            return "Digest username=\"" + username + "\", realm=\"" + realm + "\", nonce=\"" + nonce + "\", uri=\"" + digestUri + "\", response=\"" + response + "\"";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @NonNull
    private static String getHexStringFromBytes(@NonNull byte[] bytes) {
        StringBuilder buf = new StringBuilder();
        for (byte b : bytes)
            buf.append(String.format("%02x", b));
        return buf.toString();
    }

    @NonNull
    private static String readContentAsText(@NonNull InputStream inputStream, int length) throws IOException {
        if (length <= 0)
            return "";
        byte[] b = new byte[length];
        int read = readData(inputStream, b, 0, length);
        return new String(b, 0, read);
    }

    // int memcmp ( const void * ptr1, const void * ptr2, size_t num );
    public static boolean memcmp(
            @NonNull byte[] source1,
            int offsetSource1,
            @NonNull byte[] source2,
            int offsetSource2,
            int num) {
        if (source1.length - offsetSource1 < num)
            return false;
        if (source2.length - offsetSource2 < num)
            return false;

        for (int i = 0; i < num; i++) {
            if (source1[offsetSource1 + i] != source2[offsetSource2 + i])
                return false;
        }
        return true;
    }

    private static void shiftLeftArray(@NonNull byte[] array, int num) {
        // ABCDEF -> BCDEF
        if (num - 1 >= 0)
            System.arraycopy(array, 1, array, 0, num - 1);
    }

    private boolean readUntilBytesFound(@NonNull InputStream inputStream, @NonNull byte[] array) throws IOException {
        byte[] buffer = new byte[array.length];

        // Fill in buffer
        if (NetUtils.readData(inputStream, buffer, 0, buffer.length) != buffer.length)
            return false; // EOF

        while (!exitFlag.get()) {
            // Check if buffer is the same one
            if (memcmp(buffer, 0, array, 0, buffer.length)) {
                return true;
            }
            // ABCDEF -> BCDEFF
            shiftLeftArray(buffer, buffer.length);
            // Read 1 byte into last buffer item
            if (NetUtils.readData(inputStream, buffer, buffer.length - 1, 1) != 1) {
                return false; // EOF
            }
        }
        return false;
    }

//    private boolean readUntilByteFound(@NonNull InputStream inputStream, byte bt) throws IOException {
//        byte[] buffer = new byte[1];
//        int readBytes;
//        while (!exitFlag.get()) {
//            readBytes = inputStream.read(buffer, 0, 1);
//            if (readBytes == -1) // EOF
//                return false;
//            if (readBytes == 1 && buffer[0] == bt) {
//                return true;
//            }
//        }
//        return false;
//    }

    @Nullable
    private String readLine(@NonNull InputStream inputStream) throws IOException {
        byte[] bufferLine = new byte[MAX_LINE_SIZE];
        int offset = 0;
        int readBytes;
        do {
            // Didn't find "\r\n" within 4K bytes
            if (offset >= MAX_LINE_SIZE) {
                throw new NoResponseHeadersException();
            }

            // Read 1 byte
            readBytes = inputStream.read(bufferLine, offset, 1);
            if (readBytes == 1) {
                // Check for EOL
                // Some cameras like Linksys WVC200 do not send \n instead of \r\n
                if (offset > 0 && /*bufferLine[offset-1] == '\r' &&*/ bufferLine[offset] == '\n') {
                    // Found empty EOL. End of header section
                    if (offset == 1)
                        return "";//break;

                    // Found EOL. Add to array.
                    return new String(bufferLine, 0, offset-1);
                } else {
                    offset++;
                }
            }
        } while (readBytes > 0 && !exitFlag.get());
        return null;
    }

    private static int readData(@NonNull InputStream inputStream, @NonNull byte[] buffer, int offset, int length) throws IOException {
        if (DEBUG) Log.v(TAG, "readData(offset=" + offset + ", length=" + length + ")");
        int readBytes;
        int totalReadBytes = 0;
        do {
            readBytes = inputStream.read(buffer, offset + totalReadBytes, length - totalReadBytes);
            if (readBytes > 0)
                totalReadBytes += readBytes;
        } while (readBytes >= 0 && totalReadBytes < length);
        return totalReadBytes;
    }

    private static void dumpHeaders(@NonNull ArrayList<Pair<String, String>> headers) {
        if (DEBUG) {
            for (Pair<String, String> head : headers) {
                Log.d(TAG, head.first + ": " + head.second);
            }
        }
    }

    @Nullable
    private static String getHeader(@NonNull ArrayList<Pair<String, String>> headers, @NonNull String header) {
        for (Pair<String, String> head: headers) {
            // Session: ODgyODg3MjQ1MDczODk3NDk4Nw
            String h = head.first.toLowerCase();
            if (header.toLowerCase().equals(h)) {
                return head.second;
            }
        }
        // Not found
        return null;
    }

    public static class Builder {

        private static final String DEFAULT_USER_AGENT = "Lavf58.29.100";

        private final @NonNull Socket rtspSocket;
        private final @NonNull String uriRtsp;
        private final @NonNull AtomicBoolean exitFlag;
        private final @NonNull RtspClientListener listener;
//      private boolean sendOptionsCommand = true;
        private boolean requestVideo = true;
        private boolean requestAudio = true;
        private boolean debug = false;
        private @Nullable String username = null;
        private @Nullable String password = null;
        private @Nullable String userAgent = DEFAULT_USER_AGENT;

        public Builder(
                @NonNull Socket rtspSocket,
                @NonNull String uriRtsp,
                @NonNull AtomicBoolean exitFlag,
                @NonNull RtspClientListener listener) {
            this.rtspSocket = rtspSocket;
            this.uriRtsp = uriRtsp;
            this.exitFlag = exitFlag;
            this.listener = listener;
        }

        @NonNull
        public Builder withDebug(boolean debug) {
            this.debug = debug;
            return this;
        }

        @NonNull
        public Builder withCredentials(@Nullable String username, @Nullable String password) {
            this.username = username;
            this.password = password;
            return this;
        }

        @NonNull
        public Builder withUserAgent(@Nullable String userAgent) {
            this.userAgent = userAgent;
            return this;
        }

//        @NonNull
//        public Builder sendOptionsCommand(boolean sendOptionsCommand) {
//            this.sendOptionsCommand = sendOptionsCommand;
//            return this;
//        }

        @NonNull
        public Builder requestVideo(boolean requestVideo) {
            this.requestVideo = requestVideo;
            return this;
        }

        @NonNull
        public Builder requestAudio(boolean requestAudio) {
            this.requestAudio = requestAudio;
            return this;
        }

        @NonNull
        public RtspClient build() {
            return new RtspClient(this);
        }
    }
}

class LoggerOutputStream extends BufferedOutputStream {
    private boolean logging = true;

    public LoggerOutputStream(@NonNull OutputStream out) {
        super(out);
    }

    public synchronized void setLogging(boolean logging) {
        this.logging = logging;
    }

    @Override
    public synchronized void write(byte[] b, int off, int len) throws IOException {
        super.write(b, off, len);
        if (logging)
            Log.i(RtspClient.TAG_DEBUG, new String(b, off, len));
    }
}
