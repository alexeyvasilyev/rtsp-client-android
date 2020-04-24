package com.alexvas.rtsp;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.alexvas.utils.NetUtils;

import java.io.IOException;
import java.io.InputStream;

class RtpParser {

    private static final String TAG = RtpParser.class.getSimpleName();
    private static final boolean DEBUG = false;

    private static int RTP_HEADER_SIZE = 12;

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

        @NonNull
        private static RtpHeader parseData(@NonNull byte[] header, int packetSize) {
            RtpHeader rtpHeader = new RtpHeader();
            rtpHeader.version = (header[0] & 0xFF) >> 6;
            if (rtpHeader.version != 2) {
                Log.e("RTP","This is not a RTP packet (" + rtpHeader.version + ")");
            }

            // 80 60 40 91 fd ab d4 2a
            // 80 c8 00 06
            rtpHeader.padding = (header[0] & 0x20) >> 5; // 0b00100100
            rtpHeader.extension = (header[0] & 0x10) >> 4;
            rtpHeader.marker = (header[1] & 0x80) >> 7;
            rtpHeader.payloadType = header[1] & 0x7F;
            rtpHeader.sequenceNumber = (header[3] & 0xFF) + ((header[2] & 0xFF) << 8);
            rtpHeader.timeStamp = (header[7] & 0xFF) + ((header[6] & 0xFF) << 8) + ((header[5] & 0xFF) << 16) + ((header[4] & 0xFF) << 24) & 0xffffffffL;
            rtpHeader.ssrc = (header[7] & 0xFF) + ((header[6] & 0xFF) << 8) + ((header[5] & 0xFF) << 16) + ((header[4] & 0xFF) << 24) & 0xffffffffL;
            rtpHeader.payloadSize = packetSize - RTP_HEADER_SIZE;
            return rtpHeader;
        }

        public void dumpHeader() {
            Log.d("RTP","RTP header version: " + version
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

    @Nullable
    static RtpHeader readHeader(@NonNull InputStream inputStream) throws IOException {
        // 24 01 00 1c 80 c8 00 06  7f 1d d2 c4
        // 24 01 00 1c 80 c8 00 06  13 9b cf 60
        byte[] header = new byte[RTP_HEADER_SIZE];
        // Skip 4 bytes (TCP only). No those bytes in UDP.
        NetUtils.readData(inputStream, header, 0, 4);
        if (DEBUG && header[0] == 0x24)
            Log.d(TAG, header[1] == 0 ? "RTP packet" : "RTCP packet");

        int packetSize = ((header[2] & 0xFF) << 8) | (header[3] & 0xFF);
        if (DEBUG)
            Log.d(TAG, "Packet size: " + packetSize);

        if (NetUtils.readData(inputStream, header, 0, header.length) == header.length) {
            return RtpHeader.parseData(header, packetSize);
        } else {
            return null;
        }
    }
}
