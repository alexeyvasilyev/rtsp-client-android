package com.alexvas.rtsp.parser;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class VideoRtpParser {

    private static final String TAG = VideoRtpParser.class.getSimpleName();
    private static final boolean DEBUG = false;

    private final static int NAL_UNIT_TYPE_STAP_A = 24;
    private final static int NAL_UNIT_TYPE_STAP_B = 25;
    private final static int NAL_UNIT_TYPE_MTAP16 = 26;
    private final static int NAL_UNIT_TYPE_MTAP24 = 27;
    private final static int NAL_UNIT_TYPE_FU_A = 28;
    private final static int NAL_UNIT_TYPE_FU_B = 29;

    private final byte[][] _buffer = new byte[1024][];
    private byte[] _nalUnit;
    private boolean _nalEndFlag;
    private int _bufferLength;
    private int _packetNum = 0;

    @Nullable
    public byte[] processRtpPacketAndGetNalUnit(@NonNull byte[] data, int length) {
        if (DEBUG)
            Log.v(TAG, "processRtpPacketAndGetNalUnit(length=" + length + ")");

        int tmpLen;
        int nalType = data[0] & 0x1F;
        int packFlag = data[1] & 0xC0;

        if (DEBUG)
            Log.d(TAG, "NAL type: " + nalType + ", pack flag: " + packFlag);
        switch (nalType) {

            //Single-timeaggregation packet
            case NAL_UNIT_TYPE_STAP_A:
                break;

            //Single-timeaggregation packet
            case NAL_UNIT_TYPE_STAP_B:
                break;

            //Multi-time aggregationpacket
            case NAL_UNIT_TYPE_MTAP16:
                break;

            //Multi-time aggregationpacket
            case NAL_UNIT_TYPE_MTAP24:
                break;

            //Fragmentationunit
            case NAL_UNIT_TYPE_FU_A:
                switch (packFlag) {
                    //NAL Unit start packet
                    case 0x80:
                        _nalEndFlag = false;
                        _packetNum = 1;
                        _bufferLength = length - 1 ;
                        _buffer[1] = new byte[_bufferLength];
                        _buffer[1][0] = (byte)((data[0] & 0xE0) | (data[1] & 0x1F));
                        System.arraycopy(data,2, _buffer[1],1,length - 2);
                        break;
                    //NAL Unit middle packet
                    case 0x00:
                        _nalEndFlag = false;
                        _packetNum++;
                        _bufferLength += length - 2;
                        _buffer[_packetNum] = new byte[length - 2];
                        System.arraycopy(data,2, _buffer[_packetNum],0,length - 2);
                        break;
                    //NAL Unit end packet
                    case 0x40:
                        _nalEndFlag = true;
                        _nalUnit = new byte[_bufferLength + length + 2];
                        _nalUnit[0] = 0x00;
                        _nalUnit[1] = 0x00;
                        _nalUnit[2] = 0x00;
                        _nalUnit[3] = 0x01;
                        tmpLen = 4;
                        System.arraycopy(_buffer[1], 0, _nalUnit, tmpLen, _buffer[1].length);
                        tmpLen += _buffer[1].length;
                        for(int i = 2; i < _packetNum +1; ++i) {
                            System.arraycopy(_buffer[i],0, _nalUnit, tmpLen, _buffer[i].length);
                            tmpLen += _buffer[i].length;
                        }
                        System.arraycopy(data,2, _nalUnit, tmpLen,length-2);
                        break;
                }
                break;

            //Fragmentationunit
            case NAL_UNIT_TYPE_FU_B:
                break;

            //Single NAL unit per packet
            default:
                if (DEBUG)
                    Log.d(TAG,"Single NAL");
                _nalUnit = new byte[4 + length];
                _nalUnit[0] = 0x00;
                _nalUnit[1] = 0x00;
                _nalUnit[2] = 0x00;
                _nalUnit[3] = 0x01;
                System.arraycopy(data,0, _nalUnit,4, length);
                _nalEndFlag = true;
                break;
        }
        if (_nalEndFlag) {
//            int type = VideoCodecUtils.getH264NalUnitType(_nalUnit, 0, _nalUnit.length);
//            switch (type) {
//                case VideoCodecUtils.NAL_SPS
//            }
//            if (DEBUG)
//                Log.i(TAG, "NAL unit: " + VideoCodecUtils.getH264NalUnitTypeString(type) + " (" + _nalUnit.length + " bytes)");
//            ArrayList<VideoCodecUtils.NalUnit> nals = new ArrayList<>();
//            int numNals = VideoCodecUtils.getH264NalUnits(_nalUnit, 0, _nalUnit.length - 1, nals);
//            Log.i(TAG, "numNals: " + numNals);
            return _nalUnit;
        } else {
            return null;
        }
    }

}
