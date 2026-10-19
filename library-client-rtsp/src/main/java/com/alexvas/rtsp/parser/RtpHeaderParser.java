package com.alexvas.rtsp.parser;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.alexvas.utils.NetUtils;

import java.io.IOException;
import java.io.EOFException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.util.Arrays;

public class RtpHeaderParser {

    private static final String TAG = RtpHeaderParser.class.getSimpleName();
    private static final boolean DEBUG = false;

    /** Size of the fixed RTP header (RFC 3550), not counting any CSRC list. */
    public final static int RTP_HEADER_SIZE = 12;

    public static class RtpHeader {
        public int version;
        public int padding;
        public int extension;
        public int cc;
        public int marker;
        public int payloadType;
        public int sequenceNumber;
        public long timeStamp;
        public long ssrc;
        public int payloadSize;
        /** The 12-byte fixed RTP header exactly as received, needed for SRTP authentication. */
        public byte[] rawHeader;

        public long getTimestampMsec() {
            return (long)(timeStamp * 11.111111);
        }

        @Nullable
        private static RtpHeader parseData(@NonNull byte[] header, int packetSize) {
            RtpHeader rtpHeader = new RtpHeader();
            rtpHeader.version = (header[0] & 0xFF) >> 6;
            if (rtpHeader.version != 2) {
                if (DEBUG)
                    Log.e(TAG,"Not a RTP packet (" + rtpHeader.version + ")");
                return null;
            }

            // 80 60 40 91 fd ab d4 2a
            // 80 c8 00 06
            rtpHeader.padding = (header[0] & 0x20) >> 5; // 0b00100100
            rtpHeader.extension = (header[0] & 0x10) >> 4;
            rtpHeader.marker = (header[1] & 0x80) >> 7;
            rtpHeader.payloadType = header[1] & 0x7F;
            rtpHeader.sequenceNumber = (header[3] & 0xFF) + ((header[2] & 0xFF) << 8);
            rtpHeader.timeStamp = (header[7] & 0xFF) + ((header[6] & 0xFF) << 8) + ((header[5] & 0xFF) << 16) + ((header[4] & 0xFF) << 24) & 0xffffffffL;
            rtpHeader.ssrc = (header[11] & 0xFFL) | ((header[10] & 0xFFL) << 8)
                    | ((header[9] & 0xFFL) << 16) | ((header[8] & 0xFFL) << 24);
            rtpHeader.payloadSize = packetSize - RTP_HEADER_SIZE;
            rtpHeader.rawHeader = header.clone();
            return rtpHeader;
        }

        private static int getPacketSize(@NonNull byte[] header) {
            int packetSize = ((header[2] & 0xFF) << 8) | (header[3] & 0xFF);
            if (DEBUG)
                Log.d(TAG, "Packet size: " + packetSize);
            return packetSize;
        }

        public void dumpHeader() {
            Log.d("RTP","\t\tRTP header version: " + version
                    + ", padding: " + padding
                    + ", ext: " + extension
                    + ", cc: " + cc
                    + ", marker: " + marker
                    + ", payload type: " + payloadType
                    + ", seq num: " + sequenceNumber
                    + ", ts: " + timeStamp
                    + ", ssrc: " + ssrc
                    + ", payload size: " + payloadSize);
        }
    }

    /**
     * Parses one complete RTP packet (header + payload) as received in a single UDP datagram.
     * Unlike {@link #readHeader}, there is no "$"-prefixed interleaved framing to strip, and the
     * whole packet is already available in {@code packet}.
     */
    @Nullable
    public static RtpHeader parsePacket(@NonNull byte[] packet, int length) {
        if (length < RTP_HEADER_SIZE)
            return null;
        byte[] header = Arrays.copyOfRange(packet, 0, RTP_HEADER_SIZE);
        return RtpHeader.parseData(header, length);
    }

    /** Max interleaved channel used by this library's SETUP requests ("0-1" video, "2-3" audio). */
    private final static int MAX_INTERLEAVED_CHANNEL = 3;
    /** Min RTCP packet size (RR without report blocks). */
    private final static int RTCP_MIN_PACKET_SIZE = 8;
    /** Max bytes skipped while resyncing before returning, so the caller can check its exit flag. */
    private final static int MAX_RESYNC_BYTES = 65536;
    /** Max interleaved RTSP response body size skipped. Keep-alive responses have tiny or no body. */
    private final static int MAX_RTSP_RESPONSE_BODY_SIZE = 65536;
    /** Max interleaved RTSP response header lines. */
    private final static int MAX_RTSP_RESPONSE_HEADERS = 64;
    /** Max interleaved RTSP response body bytes logged. */
    private final static int MAX_RTSP_RESPONSE_BODY_LOGGED = 1024;
    /** Pushback buffer size {@link #readHeader} needs: 4 bytes "$" prefix + RTP header. */
    public final static int PUSHBACK_SIZE = 4 + RTP_HEADER_SIZE;

    /**
     * Reads the rest of an RTSP response (after "RTSP") interleaved with RTP data:
     * status line, headers and body (if Content-Length present).
     * <p>
     * Content-Length comes from the network, so the body is skipped via a small fixed buffer
     * instead of being allocated whole. An out of range value is ignored, and whatever follows
     * is handled by {@link #readHeader} resync.
     */
    private static void readRtspResponse(@NonNull InputStream inputStream) throws IOException {
        StringBuilder response = new StringBuilder("RTSP");
        int contentLength = 0;
        String line = readLine(inputStream);
        response.append(line);
        int headers = 0;
        while (!(line = readLine(inputStream)).isEmpty()) {
            if (++headers > MAX_RTSP_RESPONSE_HEADERS)
                throw new IOException("Too many RTSP response headers");
            response.append("\n").append(line);
            int colon = line.indexOf(':');
            if (colon > 0 && line.substring(0, colon).trim().equalsIgnoreCase("Content-Length")) {
                try {
                    contentLength = Integer.parseInt(line.substring(colon + 1).trim());
                } catch (NumberFormatException ignored) {
                }
            }
        }
        if (contentLength > MAX_RTSP_RESPONSE_BODY_SIZE) {
            Log.w(TAG, "Ignoring invalid RTSP response Content-Length " + contentLength);
        } else if (contentLength > 0) {
            byte[] buffer = new byte[Math.min(contentLength, 4096)];
            int bytesLeft = contentLength;
            boolean firstChunk = true;
            while (bytesLeft > 0) {
                int len = Math.min(bytesLeft, buffer.length);
                NetUtils.readData(inputStream, buffer, 0, len);
                if (firstChunk) {
                    response.append("\n\n").append(new String(buffer, 0, Math.min(len, MAX_RTSP_RESPONSE_BODY_LOGGED)));
                    firstChunk = false;
                }
                bytesLeft -= len;
            }
        }
        Log.i(TAG, "Interleaved RTSP response:\n" + response);
    }

    /** Reads a line terminated by "\r\n" or "\n", without the terminator. */
    @NonNull
    private static String readLine(@NonNull InputStream inputStream) throws IOException {
        StringBuilder line = new StringBuilder();
        int b;
        while ((b = inputStream.read()) != -1 && b != '\n') {
            if (b != '\r')
                line.append((char) b);
            if (line.length() > 4096)
                throw new IOException("RTSP response line too long");
        }
        if (b == -1)
            throw new EOFException("Stream closed while reading RTSP response");
        return line.toString();
    }

    /**
     * Reads the next interleaved RTP packet header (RFC 2326 §10.12) from the RTSP TCP connection.
     * <p>
     * The stream is not always made of "$"-prefixed packets only: e.g. unread RTSP keep-alive
     * responses land in it, and some cameras (Hikvision) write such a response right in the
     * middle of an interleaved RTP packet. Every candidate header is validated, and on mismatch
     * the stream is rescanned starting from the next byte (bytes are pushed back), so alignment
     * is always restored on the very next real packet regardless of the channel it belongs to.
     *
     * @param inputStream stream with pushback capacity of at least {@link #PUSHBACK_SIZE} bytes
     * @return RTP header (payload not read yet), or null if no valid header found within
     * {@link #MAX_RESYNC_BYTES} bytes
     */
    @Nullable
    public static RtpHeader readHeader(@NonNull PushbackInputStream inputStream) throws IOException {
        // 24 01 00 1c 80 c8 00 06  7f 1d d2 c4
        // 24 01 00 1c 80 c8 00 06  13 9b cf 60
        // 24 02 01 12 80 e1 01 d2  00 07 43 f0
        byte[] prefix = new byte[4];
        byte[] header = new byte[RTP_HEADER_SIZE];
        int skippedBytes = 0;
        while (skippedBytes < MAX_RESYNC_BYTES) {
            NetUtils.readData(inputStream, prefix, 0, prefix.length);
            if (prefix[0] == 'R' && prefix[1] == 'T' && prefix[2] == 'S' && prefix[3] == 'P') {
                // RTSP response (e.g. to keep-alive) interleaved with RTP data. Consume it whole.
                readRtspResponse(inputStream);
                continue;
            }
            int channel = prefix[1] & 0xFF;
            int packetSize = RtpHeader.getPacketSize(prefix);
            if (prefix[0] == 0x24 && channel <= MAX_INTERLEAVED_CHANNEL) {
                // ODD channels carry RTCP sender/receiver reports - a different packet layout,
                // skip them entirely. EVEN channels carry RTP media.
                boolean isRtcpChannel = (channel % 2) != 0;
                if (isRtcpChannel) {
                    if (packetSize >= RTCP_MIN_PACKET_SIZE) {
                        NetUtils.readData(inputStream, header, 0, 2);
                        int payloadType = header[1] & 0xFF;
                        if (((header[0] & 0xFF) >> 6) == 2 && payloadType >= 200 && payloadType <= 207) {
                            NetUtils.readData(inputStream, new byte[packetSize - 2], 0, packetSize - 2);
                            continue;
                        }
                        inputStream.unread(header, 0, 2);
                    }
                } else if (packetSize >= RTP_HEADER_SIZE) {
                    NetUtils.readData(inputStream, header, 0, header.length);
                    RtpHeader rtpHeader = RtpHeader.parseData(header, packetSize);
                    if (rtpHeader != null) {
                        if (skippedBytes > 0)
                            Log.w(TAG, "Resynced RTP stream, skipped " + skippedBytes + " bytes");
                        return rtpHeader;
                    }
                    inputStream.unread(header, 0, header.length);
                }
            }
            // Not a valid header. Possible keep-alive response. Rescan from the next byte.
            inputStream.unread(prefix, 1, prefix.length - 1);
            skippedBytes++;
        }
        Log.w(TAG, "No RTP header found within " + skippedBytes + " bytes");
        return null;
    }
}
