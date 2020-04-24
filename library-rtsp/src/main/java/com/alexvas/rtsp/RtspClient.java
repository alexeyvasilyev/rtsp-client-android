package com.alexvas.rtsp;

import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.alexvas.utils.NetUtils;
import com.alexvas.utils.VideoCodecUtils;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

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
    private static final boolean DEBUG = false;

    public interface RtspClientListener {
        void onRtspConnecting();
        void onRtspConnected(@Nullable byte[] sps, @Nullable byte[] pps);
        void onRtspNalUnitReceived(@NonNull byte[] data, int offset, int length, long timestamp);
        void onRtspDisconnected();
        void onRtspFailedUnauthorized();
        void onRtspFailed(@Nullable String message);
    }

    private static final String CRLF = "\r\n";
    private static final String USER_AGENT = "Lavf58.29.100";

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

//    private static final int RTP_RCV_PORT = 25000; //port where the client will receive the RTP packets

    public void process(
            @NonNull Socket rtspSocket,
            @NonNull String uriRtsp,
            @Nullable String username,
            @Nullable String password,
            @NonNull AtomicBoolean exitFlag,
            @NonNull RtspClientListener listener) {
        if (DEBUG)
            Log.v(TAG, "process(uriRtsp=\"" + uriRtsp + "\", username=\"" + username + "\", password=\"" + password + "\")");
        listener.onRtspConnecting();
        try {
            InputStream inputStream = rtspSocket.getInputStream();
            OutputStream outputStream = new BufferedOutputStream(rtspSocket.getOutputStream());

            byte[] sps = null;
            byte[] pps = null;
            int cSeq = 0;
            ArrayList<Pair<String, String>> headers;
            int status;

// OPTIONS rtsp://10.0.1.78:8080/video/h264 RTSP/1.0
// CSeq: 1
// User-Agent: Lavf58.29.100

// RTSP/1.0 200 OK
// CSeq: 1
// Public: OPTIONS, DESCRIBE, SETUP, PLAY, GET_PARAMETER, SET_PARAMETER, TEARDOWN
//            checkExitFlag(exitFlag);
//            sendOptionsCommand(outputStream, uriRtsp, ++cSeq);
//            status = readResponseStatusCode(inputStream);
//            Log.i(TAG, "OPTIONS status: " + status);
//            checkStatusCode(status);
//            headers = readResponseHeaders(inputStream);
//            dumpHeaders(headers);


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
            String authToken = null;
            Pair<String, String> digestRealmNonce = null;
            String basicRealm;

            sendDescribeCommand(outputStream, uriRtsp, ++cSeq, authToken);
            status = readResponseStatusCode(inputStream);
            headers = readResponseHeaders(inputStream);
            dumpHeaders(headers);
            // Try once again with credentials
            if (status == 401) {
                digestRealmNonce = getHeaderWwwAuthenticateDigestRealmAndNonce(headers);
                if (digestRealmNonce == null) {
                    basicRealm = getHeaderWwwAuthenticateBasicRealm(headers);
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
                sendDescribeCommand(outputStream, uriRtsp, ++cSeq, authToken);
                status = readResponseStatusCode(inputStream);
                headers = readResponseHeaders(inputStream);
                dumpHeaders(headers);
            }
            if (DEBUG)
                Log.i(TAG, "DESCRIBE status: " + status);
            checkStatusCode(status);
            int contentLength = getHeaderContentLength(headers);
            String uriRtspSetup = uriRtsp;
            if (contentLength > 0) {
                String content = readContentAsText(inputStream, contentLength);
                if (DEBUG)
                    Log.i(TAG, "" + content);
                String videoRequest = getVideoRequestFromDescribeParams(content);
                if (!TextUtils.isEmpty(videoRequest)) {
                    if (videoRequest.startsWith("rtsp://")) {
                        // Absolute URL
                        uriRtspSetup = videoRequest;
                    } else {
                        // Relative URL
                        if (!videoRequest.startsWith("/")) {
                            videoRequest = "/" + videoRequest;
                        }
                        uriRtspSetup += videoRequest;
                    }
                }
                try {
                    Pair<byte[], byte[]> spsPps = getSpsPpsFromDescribeParams(content);
                    if (spsPps != null) {
                        sps = spsPps.first;
                        pps = spsPps.second;
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
            checkExitFlag(exitFlag);
            if (digestRealmNonce != null)
                authToken = getDigestAuthHeader(username, password, "SETUP", uriRtspSetup, digestRealmNonce.first, digestRealmNonce.second);
            sendSetupCommand(outputStream, uriRtspSetup, ++cSeq, authToken);
            status = readResponseStatusCode(inputStream);
            if (DEBUG)
                Log.i(TAG, "SETUP status: " + status);
            checkStatusCode(status);
            headers = readResponseHeaders(inputStream);
            dumpHeaders(headers);
            String session = getHeader(headers, "Session");
            if (!TextUtils.isEmpty(session)) {
                // ODgyODg3MjQ1MDczODk3NDk4Nw;timeout=30
                String[] params = TextUtils.split(session, ";");
                session = params[0];
            }
            if (DEBUG)
                Log.d(TAG, "SETUP session: " + session);
            if (TextUtils.isEmpty(session))
                throw new IOException("Failed to get RTSP session");


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
                authToken = getDigestAuthHeader(username, password, "PLAY", uriRtspSetup, digestRealmNonce.first, digestRealmNonce.second);
            sendPlayCommand(outputStream, uriRtsp, ++cSeq, authToken, session);
            status = readResponseStatusCode(inputStream);
            if (DEBUG)
                Log.i(TAG, "PLAY status: " + status);
            checkStatusCode(status);
            headers = readResponseHeaders(inputStream);
            dumpHeaders(headers);

            listener.onRtspConnected(sps, pps);
            readRtpData(inputStream, sps, pps, exitFlag, listener);

            listener.onRtspDisconnected();
        } catch (UnauthorizedException e) {
            e.printStackTrace();
            listener.onRtspFailedUnauthorized();
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
            @Nullable byte[] sps,
            @Nullable byte[] pps,
            @NonNull AtomicBoolean exitFlag,
            @NonNull RtspClientListener listener)
    throws IOException {
        byte[] data = new byte[15000]; // Usually not bigger than MTU

        // Read 1000 RTP packets
        VideoRtpParser parser = new VideoRtpParser();
        byte[] nalUnitSps = sps;
        byte[] nalUnitPps = pps;
        while (!exitFlag.get()) {
//        for (int i = 0; i < numRtpFrames; i++) {
//          Log.i(TAG, "RTP packet #" + i);
            RtpParser.RtpHeader header = RtpParser.readHeader(inputStream);
            if (header == null)
                return;
//          header.dumpHeader();
            NetUtils.readData(inputStream, data, 0, header.payloadSize);
            if (header.payloadType >= 96 && header.payloadType <= 99) {
                byte[] nalUnit = parser.processRtpPacketAndGetNalUnit(data, header.payloadSize);
                if (nalUnit != null) {
                    int type = VideoCodecUtils.getH264NalUnitType(nalUnit, 0, nalUnit.length);
//                  Log.i(TAG, "NAL u: " + VideoCodecUtils.getH264NalUnitTypeString(type));
                    switch (type) {
                        case VideoCodecUtils.NAL_SPS:
                            nalUnitSps = nalUnit;
                            break;
                        case VideoCodecUtils.NAL_PPS:
                            nalUnitPps = nalUnit;
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
                                listener.onRtspNalUnitReceived(nalUnitSppPps, 0, nalUnitSppPps.length, (long)(header.timeStamp * 11.111111));
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
                            listener.onRtspNalUnitReceived(nalUnit, 0, nalUnit.length, (long)(header.timeStamp * 11.111111));
//                            listener.onRtspNalUnitReceived(nalUnit, 0, nalUnit.length, System.currentTimeMillis() / 10);
                    }
                }
            } else {
                if (DEBUG)
                    Log.w(TAG, "Invalid RTP payload type " + header.payloadType);
            }
        }
    }

    private static void sendOptionsCommand(@NonNull OutputStream outputStream, @NonNull String request, int cSeq)
    throws IOException {
        if (DEBUG)
            Log.v(TAG, "sendOptionsCommand(request=\"" + request + "\", cSeq=" + cSeq + ")");
        outputStream.write(("OPTIONS " + request + " RTSP/1.0" + CRLF).getBytes());
        outputStream.write(("CSeq: " + cSeq + CRLF).getBytes());
        outputStream.write(("User-Agent: " + USER_AGENT + CRLF).getBytes());
        outputStream.write(CRLF.getBytes());
        outputStream.flush();
    }

    private static void sendDescribeCommand(
            @NonNull OutputStream outputStream,
            @NonNull String request,
            int cSeq,
            @Nullable String authToken)
    throws IOException {
        if (DEBUG)
            Log.v(TAG, "sendDescribeCommand(request=\"" + request + "\", cSeq=" + cSeq + ")");
        outputStream.write(("DESCRIBE " + request + " RTSP/1.0" + CRLF).getBytes());
        outputStream.write(("Accept: application/sdp" + CRLF).getBytes());
        if (authToken != null)
            outputStream.write(("Authorization: " + authToken + CRLF).getBytes());
        outputStream.write(("CSeq: " + cSeq + CRLF).getBytes());
        outputStream.write(("User-Agent: " + USER_AGENT + CRLF).getBytes());
        outputStream.write(CRLF.getBytes());
        outputStream.flush();
    }

    private static void sendSetupCommand(
            @NonNull OutputStream outputStream,
            @NonNull String request,
            int cSeq,
            @Nullable String authToken)
    throws IOException {
        if (DEBUG)
            Log.v(TAG, "sendSetupCommand(request=\"" + request + "\", cSeq=" + cSeq + ")");
        outputStream.write(("SETUP " + request + " RTSP/1.0" + CRLF).getBytes());
        outputStream.write(("Transport: RTP/AVP/TCP;unicast;interleaved=0-1" + CRLF).getBytes());
        if (authToken != null)
            outputStream.write(("Authorization:" + authToken + CRLF).getBytes());
        outputStream.write(("CSeq: " + cSeq + CRLF).getBytes());
        outputStream.write(("User-Agent: " + USER_AGENT + CRLF).getBytes());
        outputStream.write(CRLF.getBytes());
        outputStream.flush();
    }

    private static void sendPlayCommand(
            @NonNull OutputStream outputStream,
            @NonNull String request,
            int cSeq,
            @Nullable String authToken,
            @NonNull String session)
    throws IOException {
        if (DEBUG)
            Log.v(TAG, "sendPlayCommand(request=\"" + request + "\", cSeq=" + cSeq + ")");
        outputStream.write(("PLAY " + request + " RTSP/1.0" + CRLF).getBytes());
        outputStream.write(("Range: npt=0.000-" + CRLF).getBytes());
        if (authToken != null)
            outputStream.write(("Authorization:" + authToken + CRLF).getBytes());
        outputStream.write(("CSeq: " + cSeq + CRLF).getBytes());
        outputStream.write(("User-Agent: " + USER_AGENT + CRLF).getBytes());
        outputStream.write(("Session: " + session + CRLF).getBytes());
        outputStream.write(CRLF.getBytes());
        outputStream.flush();
    }

    private static int readResponseStatusCode(@NonNull InputStream inputStream) throws IOException {
        String line = readLine(inputStream);
        if (DEBUG)
            Log.d(TAG, "" + line);
        if (!TextUtils.isEmpty(line)) {
            int indexRtsp = line.indexOf("RTSP/1.0 "); // 9 characters
            if (indexRtsp >= 0) {
                int indexCode = line.indexOf(' ', indexRtsp + 9);
                String code = line.substring(indexRtsp + 9, indexCode);
                try {
                    return Integer.parseInt(code);
                } catch (NumberFormatException e) {
                    // Does not fulfill standard "RTSP/1.1 200 Ok" token
                    // Continue search for
                }
            }
        }
        return -1;
    }

    @NonNull
    private static ArrayList<Pair<String, String>> readResponseHeaders(@NonNull InputStream inputStream) throws IOException {
        ArrayList<Pair<String, String>> headers = new ArrayList<>();
        String line;
        while (true) {
            line = readLine(inputStream);
            if (!TextUtils.isEmpty(line)) {
//                if (DEBUG)
//                    Log.d(TAG, "" + line);
                if (line.equals(CRLF)) {
                    return headers;
                } else {
                    String[] pairs = TextUtils.split(line, ":");
                    if (pairs.length == 2) {
                        headers.add(Pair.create(pairs[0].trim(), pairs[1].trim()));
                    }
                }
            } else {
                break;
            }
        }
        return headers;
    }

    @Nullable
    private static String getVideoRequestFromDescribeParams(@NonNull String text) {
        String[] params = TextUtils.split(text, "\r\n");
        boolean videoFound = false;
        for (String param: params) {
            //  a=control:trackID=1
            if (videoFound && param.startsWith("a=control:")) {
                return param.substring(10).trim(); // trackID=1
            // m=video 0 RTP/AVP 96
            } else if (param.startsWith("m=video")) {
                videoFound = true;
            }
        }
        return null;
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
    }

    @Nullable
    private static Pair<byte[], byte[]> getSpsPpsFromDescribeParams(@NonNull String text) {
        String[] params = TextUtils.split(text, "\r\n");
        for (String param : params) {
            // a=fmtp:96 packetization-mode=1;profile-level-id=42C028;sprop-parameter-sets=Z0LAKIyNQDwBEvLAPCIRqA==,aM48gA==;
            // a=fmtp:96 packetization-mode=1; profile-level-id=4D4029; sprop-parameter-sets=Z01AKZpmBkCb8uAtQEBAQXpw,aO48gA==
            // a=fmtp:99 sprop-parameter-sets=Z0LgKdoBQBbpuAgIMBA=,aM4ySA==;packetization-mode=1;profile-level-id=42e029
            if (param.startsWith("a=fmtp:9")) { //
                param = param.substring(10).trim(); // packetization-mode=1;profile-level-id=42C028;sprop-parameter-sets=Z0LAKIyNQDwBEvLAPCIRqA==,aM48gA==;
                String[] paramsA = TextUtils.split(param, ";");
                for (String paramA: paramsA) {
                    paramA = paramA.trim();
                    // sprop-parameter-sets=Z0LAKIyNQDwBEvLAPCIRqA==,aM48gA==
                    if (paramA.startsWith("sprop-parameter-sets=")) {
                        // Z0LAKIyNQDwBEvLAPCIRqA==,aM48gA==
                        paramA = paramA.substring(21);
                        String[] paramsSpsPps = TextUtils.split(paramA, ",");
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
                            return new Pair<>(nalSps, nalPps);
                        }
                    }
                }
            }
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

    @Nullable
    private static Pair<String, String> getHeaderWwwAuthenticateDigestRealmAndNonce(@NonNull ArrayList<Pair<String, String>> headers) {
        for (Pair<String, String> head: headers) {
            String h = head.first.toLowerCase();
            // WWW-Authenticate: Digest realm="AXIS_00408CEF081C", nonce="00054cecY7165349339ae05f7017797d6b0aaad38f6ff45", stale=FALSE
            // WWW-Authenticate: Basic realm="AXIS_00408CEF081C"
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

    // Basic
    @NonNull
    private static String getBasicAuthHeader(@Nullable String username, @Nullable String password) {
        String auth = (username == null ? "" : username) + ":" + (password == null ? "" : password);
        return "Basic " + new String(Base64.encode(auth.getBytes(StandardCharsets.ISO_8859_1), Base64.NO_WRAP));
    }

    @Nullable
    private String getDigestAuthHeader(
            @NonNull String username,
            @NonNull String password,
            @NonNull String method,
            @NonNull String digestUri,
            @NonNull String realm,
            @NonNull String nonce) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] ha1;

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

    @Nullable
    private static String readLine(@NonNull InputStream inputStream) throws IOException {
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
        } while (readBytes > 0);
        return null;
    }

    private static int readData(@NonNull InputStream inputStream, @NonNull byte[] buffer, int offset, int length) throws IOException {
        if (DEBUG)
            Log.v(TAG, "readData(offset=" + offset + ", length=" + length + ")");
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

}
