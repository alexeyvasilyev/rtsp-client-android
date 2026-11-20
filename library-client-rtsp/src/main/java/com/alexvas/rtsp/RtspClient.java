package com.alexvas.rtsp;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import com.alexvas.rtsp.parser.AacParser;
import com.alexvas.rtsp.parser.G711Parser;
import com.alexvas.rtsp.parser.OpusParser;
import com.alexvas.rtsp.parser.AudioParser;
import com.alexvas.rtsp.parser.RtpAv1Parser;
import com.alexvas.rtsp.parser.RtpH264Parser;
import com.alexvas.rtsp.parser.RtpH265Parser;
import com.alexvas.rtsp.parser.RtpHeaderParser;
import com.alexvas.rtsp.parser.RtpParser;
import com.alexvas.rtsp.parser.RtpVideoPacketAssembler;
import com.alexvas.rtsp.srtp.SrtpCryptoContext;
import com.alexvas.utils.NetUtils;
import com.alexvas.utils.VideoCodecUtils;

import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serial;
import java.math.BigInteger;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.DatagramChannel;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
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
    private static final byte[] EMPTY_ARRAY = new byte[0];

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

    /** RTP/RTCP data transport used for SETUP/media delivery. */
    public enum Transport {
        /** RTP interleaved over the RTSP TCP connection itself (RFC 2326 §10.12). */
        TCP,
        /** RTP/RTCP delivered over dedicated UDP ports (RFC 2326 §C.1.1). */
        UDP,
        /** RTP/RTCP delivered to a server-selected UDP multicast group. */
        MULTICAST,
    }

    public interface RtspClientListener {
        void onRtspConnecting();
        void onRtspConnected(@NonNull SdpInfo sdpInfo);
        void onRtspVideoNalUnitReceived(@NonNull byte[] data, int offset, int length, long timestamp);
        void onRtspAudioSampleReceived(@NonNull byte[] data, int offset, int length, long timestamp);
        void onRtspApplicationDataReceived(@NonNull byte[] data, int offset, int length, long timestamp);
        void onRtspDisconnecting();
        void onRtspDisconnected();
        void onRtspFailedUnauthorized();
        void onRtspFailed(@Nullable String message);
    }

    @VisibleForTesting
    interface RtspClientKeepAliveListener {
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
        public @Nullable ApplicationTrack applicationTrack;
    }

    public abstract static class Track {
        public String request;
        public int payloadType;
        /** SDP connection address (media-level c= overrides session-level c=). */
        public @Nullable String connectionAddress;
        /** SDP media port; zero when the server assigns it during SETUP. */
        public int port;

        /**
         * SRTP crypto suite from "a=crypto:" SDP line (RFC 4568), e.g. "AES_CM_128_HMAC_SHA1_80".
         * Null if the track is not SRTP-encrypted, or uses an unsupported crypto suite.
         */
        public @Nullable String cryptoSuite;
        /** SRTP master key decoded from the "a=crypto:" SDP line, 16 bytes. */
        public @Nullable byte[] cryptoMasterKey;
        /** SRTP master salt decoded from the "a=crypto:" SDP line, 14 bytes. */
        public @Nullable byte[] cryptoMasterSalt;

        @NonNull
        @Override
        public String toString() {
            return "Track{request='" + request + "', payloadType=" + payloadType + '}';
        }
    }

    public static final int VIDEO_CODEC_H264 = 0;
    public static final int VIDEO_CODEC_H265 = 1;
    public static final int VIDEO_CODEC_AV1 = 2;

    public static class VideoTrack extends Track {
        public int videoCodec = VIDEO_CODEC_H264;
        public @Nullable byte[] sps; // Both H.264 and H.265
        public @Nullable byte[] pps; // Both H.264 and H.265
        public @Nullable byte[] vps; // H.265 only
    }

    public static final int AUDIO_CODEC_UNKNOWN = -1;
    public static final int AUDIO_CODEC_AAC = 0;
    public static final int AUDIO_CODEC_OPUS = 1;
    public static final int AUDIO_CODEC_G711_ULAW = 2;
    public static final int AUDIO_CODEC_G711_ALAW = 3;

    @NonNull
    private static String getAudioCodecName(int codec) {
        return switch (codec) {
            case AUDIO_CODEC_AAC -> "AAC";
            case AUDIO_CODEC_OPUS -> "Opus";
            case AUDIO_CODEC_G711_ULAW -> "G.711 uLaw";
            case AUDIO_CODEC_G711_ALAW -> "G.711 aLaw";
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

    public static class ApplicationTrack extends Track {
    }

    private static final String CRLF = "\r\n";

    // Size of buffer for reading from the connection
    private final static int MAX_LINE_SIZE = 4098;

    // Requested SO_RCVBUF for UDP RTP sockets. Generous on purpose: it only reserves kernel
    // memory, and a too-small buffer is what turns a brief processing stall into dropped (and
    // therefore visibly corrupted) video packets.
    private final static int UDP_RTP_RECEIVE_BUFFER_SIZE = 1024 * 1024;

    private static class UnauthorizedException extends IOException {
        UnauthorizedException() {
            super("Unauthorized");
        }
    }

    private final static class NoResponseHeadersException extends IOException {
        @Serial
        private static final long serialVersionUID = 1L;
    }

    private final @NonNull Socket rtspSocket;
    private @NonNull String uriRtsp;
    private final @NonNull AtomicBoolean exitFlag;
    private final @NonNull RtspClientListener listener;

//  private boolean sendOptionsCommand;
    private final boolean requestVideo;
    private final boolean requestAudio;
    private final boolean requestApplication;
    private final boolean debug;
    private final @Nullable String username;
    private final @Nullable String password;
    private final @Nullable String userAgent;
    private final @NonNull Transport transport;
    private final @Nullable Context context;
    private final @NonNull List<Pair<String, String>> playHeaders;

    private RtspClient(@NonNull RtspClient.Builder builder) {
        rtspSocket = builder.rtspSocket;
        // Credentials are passed via withCredentials(), never in the request line.
        uriRtsp = NetUtils.stripUriUserInfo(builder.uriRtsp);
        exitFlag = builder.exitFlag;
        listener = builder.listener;
//      sendOptionsCommand = builder.sendOptionsCommand;
        requestVideo = builder.requestVideo;
        requestAudio = builder.requestAudio;
        requestApplication = builder.requestApplication;
        username = builder.username;
        password = builder.password;
        debug = builder.debug;
        userAgent = builder.userAgent;
        transport = builder.transport;
        context = builder.context;
        playHeaders = builder.playHeaders;
    }

    // UDP sockets outlive the blocks opening them and are closed in the finally section below.
    @SuppressWarnings("resource")
    public void execute() {
        if (DEBUG) Log.v(TAG, "execute()");
        listener.onRtspConnecting();
        // UDP tracks: indices 0=video, 1=audio, 2=application.
        final DatagramChannel[] udpRtpChannels = new DatagramChannel[3];
        final DatagramSocket[] udpRtcpSockets = new DatagramSocket[3];
        WifiManager.MulticastLock multicastLock = null;
        try {
            if (transport == Transport.MULTICAST && context != null) {
                WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                if (wifi != null) {
                    multicastLock = wifi.createMulticastLock("rtsp-multicast");
                    try {
                        multicastLock.acquire();
                    } catch (SecurityException e) {
                        Log.e(TAG, "Add android.permission.CHANGE_WIFI_MULTICAST_STATE to your app's AndroidManifest.xml to use RTSP multicast");
                        throw e;
                    }
                }
            }
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

            if (digestRealmNonce != null) {
                authToken = getDigestAuthHeader(username, password, "DESCRIBE", uriRtsp, digestRealmNonce.first, digestRealmNonce.second);
            }
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

            String contentBaseUri = getHeaderContentBase(headers);
            if (contentBaseUri != null) {
                uriRtsp = getUriFromContentBase(uriRtsp, contentBaseUri);
                if (debug)
                    Log.i(TAG_DEBUG, "RTSP URI changed to '" + uriRtsp + "'");
            }

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
                    if (!requestApplication)
                        sdpInfo.applicationTrack = null;
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
            final MulticastTransport[] multicastTransports = new MulticastTransport[3];
            for (int i = 0; i < 3; i++) {
                // 0 - video track, 1 - audio track, 2 - application track
                checkExitFlag(exitFlag);
                Track track;
                switch (i) {
                    case 0 -> track = requestVideo ? sdpInfo.videoTrack : null;
                    case 1 -> track = requestAudio ? sdpInfo.audioTrack : null;
                    default -> track = requestApplication ? sdpInfo.applicationTrack : null;
                }
                if (track != null) {
                    String uriRtspSetup = getUriForSetup(uriRtsp, track);
                    if (uriRtspSetup == null) {
                        Log.e(TAG, "Failed to get RTSP URI for SETUP");
                        continue;
                    }

                    String transportHeader;
                    if (transport == Transport.UDP) {
                        udpRtcpSockets[i] = new DatagramSocket(null);
                        udpRtcpSockets[i].setReuseAddress(true);
                        udpRtcpSockets[i].bind(new InetSocketAddress(0));

                        udpRtpChannels[i] = DatagramChannel.open();
                        udpRtpChannels[i].configureBlocking(false);
                        DatagramSocket rtpSocket = udpRtpChannels[i].socket();
                        rtpSocket.setReuseAddress(true);
                        // Bursty video can otherwise overflow the OS's default receive buffer
                        // between two select() calls, causing self-inflicted packet loss (and
                        // therefore corrupted frames) even on an otherwise clean network. Must
                        // be set before bind() to take effect.
                        try {
                            rtpSocket.setReceiveBufferSize(UDP_RTP_RECEIVE_BUFFER_SIZE);
                        } catch (SocketException e) {
                            Log.w(TAG, "Failed to enlarge UDP RTP receive buffer", e);
                        }
                        rtpSocket.bind(new InetSocketAddress(0));

                        transportHeader = "RTP/AVP/UDP;unicast;client_port="
                                + udpRtpChannels[i].socket().getLocalPort() + "-"
                                + udpRtcpSockets[i].getLocalPort();
                    } else if (transport == Transport.MULTICAST) {
                        transportHeader = "RTP/AVP;multicast";
                    } else {
                        transportHeader = "RTP/AVP/TCP;unicast;interleaved=" + (i == 0 ? "0-1" /*video*/ : "2-3" /*audio*/);
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
                            transportHeader);
                    status = readResponseStatusCode(inputStream);
                    if (DEBUG)
                        Log.i(TAG, "SETUP status: " + status);
                    checkStatusCode(status);
                    headers = readResponseHeaders(inputStream);
                    dumpHeaders(headers);

                    if (transport == Transport.UDP) {
                        Pair<Integer, Integer> serverPorts = getHeaderTransportServerPort(headers);
                        if (serverPorts == null)
                            throw new IOException("RTSP server did not return an UDP server_port for SETUP");
                        String source = getHeaderTransportSource(headers);
                        InetAddress serverAddress = null;
                        if (source != null) {
                            try {
                                serverAddress = InetAddress.getByName(source);
                            } catch (Exception e) {
                                Log.w(TAG, "Failed to resolve Transport 'source' address \"" + source + "\", falling back to RTSP server address");
                            }
                        }
                        if (serverAddress == null)
                            serverAddress = rtspSocket.getInetAddress();
                        // Locking the channel to the server address makes the kernel drop any
                        // packet not sent by it (basic protection against off-path UDP spoofing),
                        // and lets us use read()/write() instead of receive()/send().
                        udpRtpChannels[i].connect(new InetSocketAddress(serverAddress, serverPorts.first));
                        // Send a single byte to punch a hole through any NAT/firewall between us
                        // and the server, and to give the server our actual reachable address in
                        // case it differs from the client_port we asked for above.
                        try {
                            udpRtpChannels[i].write(ByteBuffer.wrap(new byte[]{0}));
                        } catch (IOException e) {
                            Log.w(TAG, "Failed to send UDP NAT punch packet", e);
                        }
                    }

                    if (transport == Transport.MULTICAST) {
                        MulticastTransport multicast = MulticastTransport.parse(
                                getHeader(headers, "Transport"), track.connectionAddress, track.port);
                        NetworkInterface networkInterface = NetworkInterface.getByInetAddress(rtspSocket.getLocalAddress());
                        if (networkInterface == null)
                            throw new IOException("Cannot determine multicast network interface from RTSP connection");
                        multicastTransports[i] = multicast;
                        // Tracks sharing an endpoint share a socket, otherwise each of them would
                        // get its own copy of every packet. readRtpData() splits by payload type.
                        boolean endpointOpen = false;
                        for (int j = 0; j < i; j++) {
                            if (multicastTransports[j] != null && multicast.sameEndpoint(multicastTransports[j]))
                                endpointOpen = true;
                        }
                        if (!endpointOpen) {
                            udpRtpChannels[i] = multicast.openRtpChannel(networkInterface, UDP_RTP_RECEIVE_BUFFER_SIZE);
                            udpRtcpSockets[i] = multicast.openRtcpSocket(networkInterface);
                        }
                    }

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
            sendPlayCommand(outputStream, uriRtsp, cSeq.addAndGet(1), userAgent, authToken, session, playHeaders);
            status = readResponseStatusCode(inputStream);
            if (DEBUG)
                Log.i(TAG, "PLAY status: " + status);
            checkStatusCode(status);
            headers = readResponseHeaders(inputStream);
            dumpHeaders(headers);

            Log.i(TAG, "RTSP connected over " + transport);
            listener.onRtspConnected(sdpInfo);

            if (sdpInfo.videoTrack != null ||  sdpInfo.audioTrack != null || sdpInfo.applicationTrack != null) {
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

                        if (transport != Transport.TCP) {
                            // Unlike TCP-interleaved mode, RTP data never shares this connection,
                            // so the response is safe (and necessary) to read right away -
                            // otherwise it just piles up unread in the socket's receive buffer
                            // for the life of the stream.
                            readResponseStatusCode(inputStream);
                            readResponseHeaders(inputStream);
                        }
                        // Else do not read response right now, since it may contain unread RTP
                        // frames. RtpHeader.searchForNextRtpHeader will handle that.
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                };

                RtpPacketReader reader = (transport != Transport.TCP)
                        ? new UdpRtpPacketReader(udpRtpChannels, exitFlag)
                        : new TcpRtpPacketReader(inputStream);
                // Blocking call unless exitFlag set to true, thread.interrupt() called or connection closed.
                try {
                    readRtpData(
                            reader,
                            sdpInfo,
                            exitFlag,
                            listener,
                            sessionTimeout / 2 * 1000,
                            // Over TCP the blocking read is already bound by this socket timeout.
                            transport != Transport.TCP ? rtspSocket.getSoTimeout() : 0,
                            keepAliveListener);
                } finally {
                    if (reader instanceof Closeable closeable) {
                        try {
                            closeable.close();
                        } catch (IOException ignored) {
                        }
                    }
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
        } catch (InterruptedException | ClosedByInterruptException e) {
            // Thread interrupted. Expected behavior.
            listener.onRtspDisconnecting();
            listener.onRtspDisconnected();
        } catch (Exception e) {
            e.printStackTrace();
            listener.onRtspFailed(e.getMessage());
        } finally {
            try {
                rtspSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            for (DatagramChannel channel : udpRtpChannels) {
                if (channel != null) {
                    try {
                        channel.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            for (DatagramSocket socket : udpRtcpSockets) {
                if (socket != null)
                    socket.close();
            }
            if (multicastLock != null && multicastLock.isHeld())
                multicastLock.release();
        }
    }

    /**
     * Content-Base may contain "user:password@" which should never get into the request line.
     */
    @VisibleForTesting
    @NonNull
    static String getUriFromContentBase(@NonNull String uriRtsp, @Nullable String contentBase) {
        if (contentBase == null || contentBase.trim().isEmpty())
            return uriRtsp;
        return NetUtils.stripUriUserInfo(contentBase.trim());
    }

    @VisibleForTesting
    @Nullable
    static String getUriForSetup(@NonNull String uriRtsp, @Nullable Track track) {
        if (DEBUG) Log.v(TAG, "getUriForSetup(uriRtsp='" + uriRtsp + "', track=" + track + ")");
        if (track == null)
            return null;
        if (track.request == null) {
            // a=control:trackID=1 is missed
            Log.w(TAG, "Track request is empty. Skipping it.");
            track.request = uriRtsp;
        }
        String uriRtspSetup = uriRtsp;
        // Absolute URL
        if (track.request.startsWith("rtsp://") || track.request.startsWith("rtsps://")) {
            uriRtspSetup = track.request;
        // Relative URL
        } else {
            if (!track.request.startsWith("/") && !uriRtspSetup.endsWith("/")) {
                track.request = "/" + track.request;
            }
            uriRtspSetup += track.request;
        }
        // Absolute a=control URL may contain "user:password@"
        return NetUtils.stripUriUserInfo(uriRtspSetup.trim());
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

    @VisibleForTesting
    static void readRtpData(
            @NonNull RtpPacketReader reader,
            @NonNull SdpInfo sdpInfo,
            @NonNull AtomicBoolean exitFlag,
            @NonNull RtspClientListener listener,
            int keepAliveTimeout,
            int noDataTimeout,
            @NonNull RtspClientKeepAliveListener keepAliveListener)
    throws IOException {
        byte[] data = EMPTY_ARRAY; // Usually not bigger than MTU = 15KB

        final RtpParser videoParser = (sdpInfo.videoTrack == null ? new RtpH264Parser() :
                switch (sdpInfo.videoTrack.videoCodec) {
                    case VIDEO_CODEC_H265 -> new RtpH265Parser();
                    case VIDEO_CODEC_AV1 -> new RtpAv1Parser();
                    default -> new RtpH264Parser();
                });
        final AudioParser audioParser = sdpInfo.audioTrack != null
                ? switch (sdpInfo.audioTrack.audioCodec) {
                    case AUDIO_CODEC_AAC -> new AacParser(sdpInfo.audioTrack.mode);
                    case AUDIO_CODEC_OPUS -> new OpusParser();
                    case AUDIO_CODEC_G711_ULAW,
                         AUDIO_CODEC_G711_ALAW -> new G711Parser();
                    default -> null;
                }
                : null;

        byte[] nalUnitSps = (sdpInfo.videoTrack != null ? sdpInfo.videoTrack.sps : null);
        byte[] nalUnitPps = (sdpInfo.videoTrack != null ? sdpInfo.videoTrack.pps : null);
        byte[] nalUnitSei = EMPTY_ARRAY;
        byte[] nalUnitAud = EMPTY_ARRAY;
        final RtpVideoPacketAssembler videoAssembler = new RtpVideoPacketAssembler(videoParser);

        final SrtpCryptoContext videoSrtpContext = createSrtpContext(sdpInfo.videoTrack);
        final SrtpCryptoContext audioSrtpContext = createSrtpContext(sdpInfo.audioTrack);
        final SrtpCryptoContext applicationSrtpContext = createSrtpContext(sdpInfo.applicationTrack);

        long keepAliveSent = System.currentTimeMillis();
        long lastPacketReceived = keepAliveSent;

        while (!exitFlag.get()) {
            RtpHeaderParser.RtpHeader header = reader.readHeader();

            // Check if keep-alive should be sent
            long l = System.currentTimeMillis();
            if (keepAliveTimeout > 0 && l - keepAliveSent > keepAliveTimeout) {
                keepAliveSent = l;
                keepAliveListener.onRtspKeepAliveRequested();
            }

            if (header == null) {
                if (noDataTimeout > 0 && l - lastPacketReceived > noDataTimeout && !exitFlag.get())
                    throw new IOException("No RTP packets received for " + noDataTimeout + " msec");
                continue;
            }
            lastPacketReceived = l;
//          header.dumpHeader();
            if (header.payloadSize > data.length)
                data = new byte[header.payloadSize];

            reader.readPayload(data, 0, header.payloadSize);

            // Decrypt SRTP payload in place, if this track is encrypted.
            // Per RFC 3711 sec.3.1, an RTP header extension (X bit set) is authenticated but
            // NOT encrypted, so it must be skipped when decrypting even though it is covered
            // by the auth tag. The extension header itself is never encrypted either, so it's
            // safe to read its length from `data` before any decryption happens.
            SrtpCryptoContext srtpContext = null;
            if (sdpInfo.videoTrack != null && header.payloadType == sdpInfo.videoTrack.payloadType)
                srtpContext = videoSrtpContext;
            else if (sdpInfo.audioTrack != null && header.payloadType == sdpInfo.audioTrack.payloadType)
                srtpContext = audioSrtpContext;
            else if (sdpInfo.applicationTrack != null && header.payloadType == sdpInfo.applicationTrack.payloadType)
                srtpContext = applicationSrtpContext;
            if (srtpContext != null) {
                int headerExtensionLength = 0;
                if (header.extension == 1 && header.payloadSize >= 4)
                    headerExtensionLength = ((data[2] & 0xFF) << 8 | (data[3] & 0xFF)) * 4 + 4;
                header.payloadSize = srtpContext.decryptAndVerify(header.rawHeader, data, header.payloadSize, header.ssrc, header.sequenceNumber, headerExtensionLength);
                if (header.payloadSize < 0)
                    continue; // Dropped: failed SRTP authentication.
            }

            // Audio and application payloads start right after the header extension, if any
            // (e.g. ONVIF replay adds a 0xABAC NTP timestamp extension to every RTP packet).
            // Video skips it on its own below.
            boolean isVideo = sdpInfo.videoTrack != null && header.payloadType == sdpInfo.videoTrack.payloadType;
            if (!isVideo && header.extension == 1) {
                if (header.payloadSize < 4) continue;
                int skipBytes = ((data[2] & 0xFF) << 8 | (data[3] & 0xFF)) * 4 + 4;
                if (skipBytes >= header.payloadSize) continue;
                header.payloadSize -= skipBytes;
                System.arraycopy(data, skipBytes, data, 0, header.payloadSize);
            }

            // Video
            if (isVideo) {
                byte[] nalUnit;
                // Skip extension bytes, validating against the received packet, not buffer capacity.
                if (header.extension == 1) {
                    if (header.payloadSize < 4) continue;
                    int skipBytes = ((data[2] & 0xFF) << 8 | (data[3] & 0xFF)) * 4 + 4;
                    if (skipBytes >= header.payloadSize) continue;
                    nalUnit = videoAssembler.process(header,
                            Arrays.copyOfRange(data, skipBytes, header.payloadSize),
                            header.payloadSize - skipBytes);
                } else {
                    nalUnit = videoAssembler.process(header, data, header.payloadSize);
                }

                if (nalUnit != null && sdpInfo.videoTrack.videoCodec == VIDEO_CODEC_AV1) {
                    // AV1 has no NAL units / SPS-PPS-IDR concept. RtpAv1Parser already
                    // hands back one fully reassembled temporal unit (concatenated OBUs,
                    // including the sequence header OBU on keyframes), so just forward it.
                    listener.onRtspVideoNalUnitReceived(nalUnit, 0, nalUnit.length, header.getTimestampMsec());
                } else if (nalUnit != null) {
                    boolean isH265 = sdpInfo.videoTrack.videoCodec == VIDEO_CODEC_H265;
                    byte type = VideoCodecUtils.INSTANCE.getNalUnitType(nalUnit, 0, nalUnit.length, isH265);
//                  Log.i(TAG, "NAL u: " + VideoCodecUtils.INSTANCE.getH265NalUnitTypeString(type));
                    switch (type) {
                        case VideoCodecUtils.NAL_SPS:
                            nalUnitSps = nalUnit;
                            // Looks like there is NAL_IDR_SLICE as well. Send it now.
                            if (nalUnit.length > VideoCodecUtils.MAX_NAL_SPS_SIZE)
                                listener.onRtspVideoNalUnitReceived(nalUnit, 0, nalUnit.length, header.getTimestampMsec());
                            break;

                        case VideoCodecUtils.NAL_PPS:
                            nalUnitPps = nalUnit;
                            // Looks like there is NAL_IDR_SLICE as well. Send it now.
                            if (nalUnit.length > VideoCodecUtils.MAX_NAL_SPS_SIZE)
                                listener.onRtspVideoNalUnitReceived(nalUnit, 0, nalUnit.length, header.getTimestampMsec());
                            break;

                        case VideoCodecUtils.NAL_AUD:
                            nalUnitAud = nalUnit;
                            break;

                        case VideoCodecUtils.NAL_SEI:
                            nalUnitSei = nalUnit;
                            break;

                        case VideoCodecUtils.NAL_IDR_SLICE:
                            // Combine IDR with SPS/PPS
                            if (nalUnitSps != null && nalUnitPps != null) {
                                byte[] nalUnitSpsPpsIdr = new byte[nalUnitAud.length + nalUnitSps.length + nalUnitPps.length + nalUnitSei.length + nalUnit.length];
                                int offset = 0;
                                System.arraycopy(nalUnitSps, 0, nalUnitSpsPpsIdr, offset, nalUnitSps.length);
                                offset += nalUnitSps.length;
                                System.arraycopy(nalUnitPps, 0, nalUnitSpsPpsIdr, offset, nalUnitPps.length);
                                offset += nalUnitPps.length;
                                System.arraycopy(nalUnitAud, 0, nalUnitSpsPpsIdr, offset, nalUnitAud.length);
                                offset += nalUnitAud.length;
                                System.arraycopy(nalUnitSei, 0, nalUnitSpsPpsIdr, offset, nalUnitSei.length);
                                offset += nalUnitSei.length;
                                System.arraycopy(nalUnit, 0, nalUnitSpsPpsIdr, offset, nalUnit.length);
                                listener.onRtspVideoNalUnitReceived(nalUnitSpsPpsIdr, 0, nalUnitSpsPpsIdr.length, header.getTimestampMsec());
//                              listener.onRtspVideoNalUnitReceived(nalUnitSppPpsIdr, 0, nalUnitSppPpsIdr.length, System.currentTimeMillis());
                                // Send it only once
                                nalUnitSps = null;
                                nalUnitPps = null;
                                nalUnitSei = EMPTY_ARRAY;
                                nalUnitAud = EMPTY_ARRAY;
                                break;
                            }

                        default:
                            if (nalUnitSei.length == 0 && nalUnitAud.length == 0) {
                                listener.onRtspVideoNalUnitReceived(nalUnit, 0, nalUnit.length, header.getTimestampMsec());
                            } else {
                                byte[] nalUnitAudSeiSlice = new byte[nalUnitAud.length + nalUnitSei.length + nalUnit.length];
                                int offset = 0;
                                System.arraycopy(nalUnitAud, 0, nalUnitAudSeiSlice, offset, nalUnitAud.length);
                                offset += nalUnitAud.length;
                                System.arraycopy(nalUnitSei, 0, nalUnitAudSeiSlice, offset, nalUnitSei.length);
                                offset += nalUnitSei.length;
                                System.arraycopy(nalUnit, 0, nalUnitAudSeiSlice, offset, nalUnit.length);
                                listener.onRtspVideoNalUnitReceived(nalUnitAudSeiSlice, 0, nalUnitAudSeiSlice.length, header.getTimestampMsec());
                                nalUnitSei = EMPTY_ARRAY;
                                nalUnitAud = EMPTY_ARRAY;
                            }
                    }
                }

            // Audio
            } else if (sdpInfo.audioTrack != null && header.payloadType == sdpInfo.audioTrack.payloadType) {
                if (audioParser != null) {
                    // One RTP packet can hold several complete AAC access
                    // units back to back (see AacParser's own doc) — emit
                    // each as its own sample, in order. All share this
                    // packet's single RTP timestamp rather than each AU's
                    // own index-delta-derived timing: the decoder's sample-
                    // rate-driven playback pacing is what actually matters
                    // for smooth audio, not RTP-timestamp precision per AU.
                    for (byte[] sample : audioParser.processRtpPacketAndGetSamples(data, header.payloadSize)) {
                        listener.onRtspAudioSampleReceived(sample, 0, sample.length, header.getTimestampMsec());
                    }
                }

            // Application
            } else if (sdpInfo.applicationTrack != null && header.payloadType == sdpInfo.applicationTrack.payloadType) {
                listener.onRtspApplicationDataReceived(data, 0, header.payloadSize, header.getTimestampMsec());

            // Unknown
            } else {
                // https://www.iana.org/assignments/rtp-parameters/rtp-parameters.xhtml
                if (DEBUG && header.payloadType >= 96 && header.payloadType <= 127)
                    Log.w(TAG, "Invalid RTP payload type " + header.payloadType);
            }
        }
    }

    @Nullable
    private static SrtpCryptoContext createSrtpContext(@Nullable Track track) {
        if (track == null || track.cryptoSuite == null || track.cryptoMasterKey == null || track.cryptoMasterSalt == null)
            return null;
        try {
            return new SrtpCryptoContext(track.cryptoSuite, track.cryptoMasterKey, track.cryptoMasterSalt);
        } catch (GeneralSecurityException e) {
            Log.e(TAG, "Failed to initialize SRTP decryption for track " + track, e);
            return null;
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
            @NonNull String transportHeader)
    throws IOException {
        if (DEBUG) Log.v(TAG, "sendSetupCommand(request=\"" + request + "\", cSeq=" + cSeq + ")");
        outputStream.write(("SETUP " + request + " RTSP/1.0" + CRLF).getBytes());
        outputStream.write(("Transport: " + transportHeader + CRLF).getBytes());
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
            @NonNull String session,
            @NonNull List<Pair<String, String>> extraHeaders)
    throws IOException {
        if (DEBUG) Log.v(TAG, "sendPlayCommand(request=\"" + request + "\", cSeq=" + cSeq + ")");
        outputStream.write(("PLAY " + request + " RTSP/1.0" + CRLF).getBytes());
        boolean hasRange = false;
        for (Pair<String, String> header : extraHeaders) {
            outputStream.write((header.first + ": " + header.second + CRLF).getBytes());
            if ("Range".equalsIgnoreCase(header.first))
                hasRange = true;
        }
        if (!hasRange)
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
//                if (debug)
//                    Log.d(TAG_DEBUG, "Status code: " + statusCode);
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
                String[] pairs = line.split(":", 2);
                if (pairs.length == 2) {
                    headers.add(Pair.create(pairs[0].trim(), pairs[1].trim()));
                }
            }
        }
        return headers;
    }

    /**
     * Get a list of tracks from SDP. Usually contains video and audio track only.
     * @return array of 3 tracks. First is video track, second audio track, third application track.
     */
    @NonNull
    private static Track[] getTracksFromDescribeParams(@NonNull List<Pair<String, String>> params) {
        Track[] tracks = new Track[3];
        Track currentTrack = null;
        String sessionAddress = null;
        boolean mediaStarted = false;
        for (Pair<String, String> param: params) {
            switch (param.first) {
                case "c":
                    String[] connection = param.second.trim().split("\\s+");
                    if (connection.length >= 3 && connection[0].equals("IN")) {
                        if (!mediaStarted) sessionAddress = connection[2];
                        else if (currentTrack != null) currentTrack.connectionAddress = connection[2];
                    }
                    break;
                case "m":
                    mediaStarted = true;
                    currentTrack = null;
                    // m=video 0 RTP/AVP 96
                    if (param.second.startsWith("video")) {
                        currentTrack = new VideoTrack();
                        tracks[0] = currentTrack;

                    // m=audio 0 RTP/AVP 97
                    // m=audio 0 RTP/AVP 0 8
                    } else if (param.second.startsWith("audio")) {
                        currentTrack = new AudioTrack();
                        tracks[1] = currentTrack;

                    // m=application 0 RTP/AVP 99
                    // a=rtpmap:99 com.my/90000
                    } else if (param.second.startsWith("application")) {
                        currentTrack = new ApplicationTrack();
                        tracks[2] = currentTrack;

                    } else if (param.second.startsWith("text")) {
                        Log.w(TAG, "Media track 'text' is not supported");

                    } else if (param.second.startsWith("message")) {
                        Log.w(TAG, "Media track 'message' is not supported");

                    } else {
                        currentTrack = null;
                    }

                    if (currentTrack != null) {
                        currentTrack.connectionAddress = sessionAddress;
                        // m=<media> <port>/<number of ports> <proto> <fmt> ...
                        String[] values = TextUtils.split(param.second, " ");
                        try {
                            currentTrack.port = Integer.parseInt(values[1].split("/", 2)[0]);
                        } catch (Exception ignored) {
                            currentTrack.port = 0;
                        }
                        try {
                            currentTrack.payloadType = (values.length > 3 ? Integer.parseInt(values[3]) : -1);
                            // Handle static PT that comes with no rtpmap
                            if (currentTrack instanceof AudioTrack track) {
                                switch (currentTrack.payloadType) {
                                    case 0 -> { // uLaw
                                        track.audioCodec = AUDIO_CODEC_G711_ULAW;
                                        track.sampleRateHz = 8000;
                                        track.channels = 1;
                                    }
                                    case 8 -> { // aLaw
                                        track.audioCodec = AUDIO_CODEC_G711_ALAW;
                                        track.sampleRateHz = 8000;
                                        track.channels = 1;
                                    }
                                }
                            }
                        } catch (Exception e) {
                            currentTrack.payloadType = -1;
                        }
                        if (currentTrack.payloadType == -1)
                            Log.e(TAG, "Failed to get payload type from \"m=" + param.second + "\"");
                    }
                    break;

                case "a":
                    // a=control:trackID=1
                    if (currentTrack != null) {
                        if (param.second.startsWith("control:")) {
                            currentTrack.request = param.second.substring(8);

                        // a=crypto:1 AES_CM_128_HMAC_SHA1_80 inline:ov0eMuXIdmbT0h1Q4eUoUiHKUUWlxNdIZOHIP44b
                        } else if (param.second.startsWith("crypto:")) {
                            updateTrackCryptoFromDescribeParam(currentTrack, param);

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
                            } else if (currentTrack instanceof AudioTrack) {
                                updateAudioTrackFromDescribeParam((AudioTrack)tracks[1], param);
                            }

                        // a=rtpmap:96 H264/90000
                        // a=rtpmap:97 mpeg4-generic/16000/1
                        // a=rtpmap:97 MPEG4-GENERIC/16000
                        // a=rtpmap:97 G726-32/8000
                        // a=rtpmap:96 mpeg4-generic/44100/2
                        } else if (param.second.startsWith("rtpmap:")) {
                            // Video
                            String[] values = TextUtils.split(param.second, " ");
                            if (currentTrack instanceof VideoTrack) {
                                if (values.length > 1) {
                                    values = TextUtils.split(values[1], "/");
                                    if (values.length > 0) {
                                        switch (values[0].toLowerCase()) {
                                            case "h264" -> ((VideoTrack) tracks[0]).videoCodec = VIDEO_CODEC_H264;
                                            case "h265" -> ((VideoTrack) tracks[0]).videoCodec = VIDEO_CODEC_H265;
                                            case "av1" -> ((VideoTrack) tracks[0]).videoCodec = VIDEO_CODEC_AV1;
                                            default -> Log.w(TAG, "Unknown video codec \"" + values[0] + "\"");
                                        }
                                        Log.i(TAG, "Video: " + values[0]);
                                    }
                                }

                            // Audio
                            } else if (currentTrack instanceof AudioTrack) {
                                if (values.length > 1) {
                                    AudioTrack track = ((AudioTrack) tracks[1]);
                                    values = TextUtils.split(values[1], "/");
                                    if (values.length > 1) {
                                        switch (values[0].toLowerCase()) {
                                            case "mpeg4-generic" -> track.audioCodec = AUDIO_CODEC_AAC;
                                            case "opus" -> track.audioCodec = AUDIO_CODEC_OPUS;
                                            case "pcmu" -> track.audioCodec = AUDIO_CODEC_G711_ULAW;
                                            case "pcma" -> track.audioCodec = AUDIO_CODEC_G711_ALAW;
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

                            // Application
                            } else {
                                // Do nothing
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
        sdpInfo.applicationTrack = ((ApplicationTrack)tracks[2]);

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

    @NonNull
    private static byte[] getNalUnitFromSprop(String nalBase64) {
        byte[] nal = Base64.decode(nalBase64, Base64.NO_WRAP);
        byte[] nalWithStart = new byte[nal.length + 4];
        // Add 00 00 00 01 NAL unit header
        nalWithStart[0] = 0;
        nalWithStart[1] = 0;
        nalWithStart[2] = 0;
        nalWithStart[3] = 1;
        System.arraycopy(nal, 0, nalWithStart, 4, nal.length);
        return nalWithStart;
    }

    private static void updateVideoTrackFromDescribeParam(@NonNull VideoTrack videoTrack, @NonNull Pair<String, String> param) {
        // a=fmtp:96 packetization-mode=1;profile-level-id=42C028;sprop-parameter-sets=Z0LAKIyNQDwBEvLAPCIRqA==,aM48gA==;
        // a=fmtp:96 packetization-mode=1; profile-level-id=4D4029; sprop-parameter-sets=Z01AKZpmBkCb8uAtQEBAQXpw,aO48gA==
        // a=fmtp:99 sprop-parameter-sets=Z0LgKdoBQBbpuAgIMBA=,aM4ySA==;packetization-mode=1;profile-level-id=42e029
        // a=fmtp:98 profile-id=1;sprop-sps=QgEBAWAAAAMAgAAAAwAAAwB4oAWCAJB/ja7tTd3Jdf+ACAAFtwUFBQQAAA+gAAGGoch3uUQD6AARlAB9AAIygg==;sprop-pps=RAHBcrAiQA==;sprop-vps=QAEMAf//AWAAAAMAgAAAAwAAAwB4rAk=
        List<Pair<String, String>> params = getSdpAParams(param);
        if (params != null) {
            for (Pair<String, String> pair: params) {
                switch (pair.first.toLowerCase()) {
                    case "sprop-sps" -> {
                        videoTrack.sps = getNalUnitFromSprop(pair.second);
                    }
                    case "sprop-pps" -> {
                        videoTrack.pps = getNalUnitFromSprop(pair.second);
                    }
                    case "sprop-vps" -> {
                        videoTrack.vps = getNalUnitFromSprop(pair.second);
                    }
                    case "sprop-parameter-sets" -> {
                        String[] paramsSpsPps = TextUtils.split(pair.second, ",");
                        if (paramsSpsPps.length > 1) {
                            videoTrack.sps = getNalUnitFromSprop(paramsSpsPps[0]);
                            videoTrack.pps = getNalUnitFromSprop(paramsSpsPps[1]);
//                            Base64.decode(paramsSpsPps[0], Base64.NO_WRAP);
//                            byte[] pps = Base64.decode(paramsSpsPps[1], Base64.NO_WRAP);
//                            byte[] nalSps = new byte[sps.length + 4];
//                            byte[] nalPps = new byte[pps.length + 4];
//                            // Add 00 00 00 01 NAL unit header
//                            nalSps[0] = 0;
//                            nalSps[1] = 0;
//                            nalSps[2] = 0;
//                            nalSps[3] = 1;
//                            System.arraycopy(sps, 0, nalSps, 4, sps.length);
//                            nalPps[0] = 0;
//                            nalPps[1] = 0;
//                            nalPps[2] = 0;
//                            nalPps[3] = 1;
//                            System.arraycopy(pps, 0, nalPps, 4, pps.length);
//                            videoTrack.sps = nalSps;
//                            videoTrack.pps = nalPps;
                        }
                    }
                    // packetization-mode=1
                    case "packetization-mode" -> {
                        // 0 - single NAL unit (default)
                        // 1 - non-interleaved mode (STAP-A and FU-A NAL units)
                        // 2 - interleaved mode
                        try {
                            int mode = Integer.parseInt(pair.second);
                            if (mode == 2)
                                Log.e(TAG, "Interleaved packetization mode is not supported");
                        } catch (NumberFormatException ignored) {
                        }
                    }
                }
            }
        }
    }

    // a=crypto:1 AES_CM_128_HMAC_SHA1_80 inline:ov0eMuXIdmbT0h1Q4eUoUiHKUUWlxNdIZOHIP44b
    // a=crypto:1 AES_CM_128_HMAC_SHA1_32 inline:PS1uQCVeeCFCanVmcjkpPlwsdTlv|2^20|1:32
    private static void updateTrackCryptoFromDescribeParam(@NonNull Track track, @NonNull Pair<String, String> param) {
        // Keep the first (preferred) crypto line if a track offers several.
        if (track.cryptoMasterKey != null)
            return;
        String value = param.second.substring(7).trim(); // strip "crypto:"
        String[] tokens = TextUtils.split(value, " ");
        if (tokens.length < 3) {
            Log.w(TAG, "Invalid a=crypto line \"" + param.second + "\"");
            return;
        }
        String cryptoSuite = tokens[1];
        if (!SrtpCryptoContext.isSuiteSupported(cryptoSuite)) {
            Log.w(TAG, "Unsupported SRTP crypto suite \"" + cryptoSuite + "\"");
            return;
        }
        String keyParams = tokens[2];
        if (!keyParams.startsWith("inline:")) {
            Log.w(TAG, "Unsupported SRTP key method in \"" + param.second + "\"");
            return;
        }
        // inline:<base64 key||salt>[|<lifetime>][|<MKI>:<MKI length>]
        String[] keyParts = keyParams.substring(7).split("\\|");
        if (keyParts.length > 1) {
            Log.w(TAG, "SRTP MKI/key lifetime params are not supported, ignoring: \"" + param.second + "\"");
        }
        byte[] keyAndSalt;
        try {
            keyAndSalt = Base64.decode(keyParts[0], Base64.NO_WRAP);
        } catch (IllegalArgumentException e) {
            Log.w(TAG, "Failed to decode SRTP inline key from \"" + param.second + "\"");
            return;
        }
        int expectedLength = SrtpCryptoContext.MASTER_KEY_LEN + SrtpCryptoContext.MASTER_SALT_LEN;
        if (keyAndSalt.length != expectedLength) {
            Log.w(TAG, "Unexpected SRTP inline key length " + keyAndSalt.length + " (expected " + expectedLength + ")");
            return;
        }
        track.cryptoSuite = cryptoSuite;
        track.cryptoMasterKey = Arrays.copyOfRange(keyAndSalt, 0, SrtpCryptoContext.MASTER_KEY_LEN);
        track.cryptoMasterSalt = Arrays.copyOfRange(keyAndSalt, SrtpCryptoContext.MASTER_KEY_LEN, expectedLength);
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

    /**
     * Search for header "Content-Base: rtsp://example.com/stream/"
     * and return "rtsp://example.com/stream/"
     */
    @Nullable
    private static String getHeaderContentBase(@NonNull ArrayList<Pair<String, String>> headers) {
        String contentBase = getHeader(headers, "content-base");
        if (!TextUtils.isEmpty(contentBase)) {
            return contentBase;
        }
        return null;
    }

    /**
     * Parses the "server_port=&lt;rtp&gt;-&lt;rtcp&gt;" parameter out of a SETUP response's
     * Transport header, e.g. "RTP/AVP;unicast;destination=10.0.1.53;source=10.0.1.145;
     * client_port=27452-27453;server_port=6972-6973".
     */
    @Nullable
    private static Pair<Integer, Integer> getHeaderTransportServerPort(@NonNull ArrayList<Pair<String, String>> headers) {
        String transportValue = getHeader(headers, "Transport");
        if (TextUtils.isEmpty(transportValue))
            return null;
        for (String token : TextUtils.split(transportValue, ";")) {
            token = token.trim();
            if (token.startsWith("server_port=")) {
                String[] ports = TextUtils.split(token.substring("server_port=".length()), "-");
                if (ports.length == 2) {
                    try {
                        return Pair.create(Integer.parseInt(ports[0]), Integer.parseInt(ports[1]));
                    } catch (NumberFormatException e) {
                        Log.e(TAG, "Failed to parse Transport server_port \"" + token + "\"");
                    }
                }
            }
        }
        return null;
    }

    /** Parses the "source=&lt;ip&gt;" parameter out of a SETUP response's Transport header, if present. */
    @Nullable
    private static String getHeaderTransportSource(@NonNull ArrayList<Pair<String, String>> headers) {
        String transportValue = getHeader(headers, "Transport");
        if (TextUtils.isEmpty(transportValue))
            return null;
        for (String token : TextUtils.split(transportValue, ";")) {
            token = token.trim();
            if (token.startsWith("source="))
                return token.substring("source=".length());
        }
        return null;
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
        private boolean requestApplication = true;
        private boolean debug = false;
        private @Nullable String username = null;
        private @Nullable String password = null;
        private @Nullable String userAgent = DEFAULT_USER_AGENT;
        private @NonNull Transport transport = Transport.TCP;
        private @Nullable Context context;
        private @NonNull List<Pair<String, String>> playHeaders = new ArrayList<>();

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

        /** RTP/RTCP transport to request in SETUP. Defaults to {@link Transport#TCP} (interleaved). */
        @NonNull
        public Builder withTransport(@NonNull Transport transport) {
            this.transport = transport;
            return this;
        }

        /** Enables Wi-Fi multicast reception for MULTICAST; only the application context is retained.
         * Without a context the caller is responsible for holding a WifiManager.MulticastLock.
         */
        @NonNull
        public Builder withContext(@NonNull Context context) {
            this.context = context.getApplicationContext();
            return this;
        }

        @NonNull
        public Builder withUserAgent(@Nullable String userAgent) {
            this.userAgent = userAgent;
            return this;
        }

        /**
         * Additional headers of PLAY, e.g. "Require: onvif-replay", "Scale: 1.0".
         * "Range" header replaces default "npt=0.000-".
         */
        @NonNull
        public Builder withPlayHeaders(@NonNull List<Pair<String, String>> headers) {
            this.playHeaders = new ArrayList<>(headers);
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
        public Builder requestApplication(boolean requestApplication) {
            this.requestApplication = requestApplication;
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
