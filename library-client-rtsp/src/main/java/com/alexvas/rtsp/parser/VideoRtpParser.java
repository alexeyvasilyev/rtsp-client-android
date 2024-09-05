package com.alexvas.rtsp.parser;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.alexvas.utils.VideoCodecUtils;

public class VideoRtpParser {

    private static final String TAG = VideoRtpParser.class.getSimpleName();
    private static final boolean DEBUG = false;

    // TODO Use already allocated buffer with RtpPacket.MAX_SIZE = 65507
    // Used only for NAL_FU_A fragmented packets
    private final byte[][] _fragmentedBuffer = new byte[1024][];
    private int _fragmentedBufferLength;
    private int _fragmentedPackets = 0;

    @Nullable
    public byte[] processRtpPacketAndGetNalUnit(@NonNull byte[] data, int length) {
        if (DEBUG)
            Log.v(TAG, "processRtpPacketAndGetNalUnit(length=" + length + ")");

        int tmpLen;
        byte nalType = (byte) (data[0] & 0x1F);
        int packFlag = data[1] & 0xC0;
        byte[] nalUnit = null;

        if (DEBUG)
            Log.d(TAG, "NAL type: " + VideoCodecUtils.INSTANCE.getH264NalUnitTypeString(nalType) + ", pack flag: " + packFlag);
        switch (nalType) {

            // Single-time aggregation packet
            case VideoCodecUtils.NAL_STAP_A, VideoCodecUtils.NAL_STAP_B:
                // Not supported
                break;

            // Multi-time aggregation packet
            case VideoCodecUtils.NAL_MTAP16, VideoCodecUtils.NAL_MTAP24:
                // Not supported
                break;

            // Fragmentation unit
            // See https://github.com/FFmpeg/FFmpeg/blob/master/libavformat/rtpdec_h264.c for more info.
            case VideoCodecUtils.NAL_FU_A:
                switch (packFlag) {
                    // NAL Unit start packet
                    case 0x80 -> {
                        _fragmentedPackets = 0;
                        _fragmentedBufferLength = length - 1;
                        _fragmentedBuffer[0] = new byte[_fragmentedBufferLength];
                        _fragmentedBuffer[0][0] = (byte) ((data[0] & 0xE0) | (data[1] & 0x1F));
                        System.arraycopy(data, 2, _fragmentedBuffer[0], 1, length - 2);
                    }

                    // NAL Unit middle packet
                    case 0x00 -> {
                        _fragmentedPackets++;
                        if (_fragmentedPackets > _fragmentedBuffer.length) {
                            Log.e(TAG, "Too many middle packets. No NAL FU_A end packet received. Skipped RTP packet.");
                            _fragmentedBuffer[0] = null;
                        } else {
                            _fragmentedBufferLength += length - 2;
                            _fragmentedBuffer[_fragmentedPackets] = new byte[length - 2];
                            System.arraycopy(data, 2, _fragmentedBuffer[_fragmentedPackets], 0, length - 2);
                        }
                    }

                    // NAL Unit end packet. Combine all packets.
                    case 0x40 -> {
                        if (_fragmentedBuffer[0] == null) {
                            Log.e(TAG, "No NAL FU_A start packet received. Skipped RTP packet.");
                        } else {
                            nalUnit = new byte[_fragmentedBufferLength + length + 2];
                            writeNalPrefix0001(nalUnit);
                            tmpLen = 4;
                            // Write start and middle packets
                            for (int i = 0; i < _fragmentedPackets + 1; ++i) {
                                System.arraycopy(_fragmentedBuffer[i], 0, nalUnit, tmpLen, _fragmentedBuffer[i].length);
                                tmpLen += _fragmentedBuffer[i].length;
                            }
                            // Write end packet
                            System.arraycopy(data, 2, nalUnit, tmpLen, length - 2);
                            clearFragmentedBuffer();
                            if (DEBUG)
                                Log.d(TAG, "Fragmented NAL (" + (nalUnit.length) + ")");
                        }
                    }
                }
                break;

            // Fragmentation unit
            case VideoCodecUtils.NAL_FU_B:
                // Not supported
                break;

            // Single NAL unit per packet
            default:
                nalUnit = new byte[4 + length];
                writeNalPrefix0001(nalUnit);
                System.arraycopy(data,0, nalUnit,4, length);
                clearFragmentedBuffer();
                if (DEBUG)
                    Log.d(TAG,"Single NAL (" + nalUnit.length + ")");
                break;
        }
        return nalUnit;
    }

    private void clearFragmentedBuffer() {
        for (int i = 0; i < _fragmentedPackets + 1; ++i) {
            _fragmentedBuffer[i] = null;
        }
    }

    private void writeNalPrefix0001(byte[] buffer) {
        buffer[0] = 0x00;
        buffer[1] = 0x00;
        buffer[2] = 0x00;
        buffer[3] = 0x01;
    }

}
