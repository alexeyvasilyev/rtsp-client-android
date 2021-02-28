package com.alexvas.utils;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public final class VideoCodecUtils {

    private static final String TAG = VideoCodecUtils.class.getSimpleName();
    private static final boolean DEBUG = false;

    public static final byte NAL_SLICE           = 1;
    public static final byte NAL_DPA             = 2;
    public static final byte NAL_DPB             = 3;
    public static final byte NAL_DPC             = 4;
    public static final byte NAL_IDR_SLICE       = 5;
    public static final byte NAL_SEI             = 6;
    public static final byte NAL_SPS             = 7;
    public static final byte NAL_PPS             = 8;
    public static final byte NAL_AUD             = 9;
    public static final byte NAL_END_SEQUENCE    = 10;
    public static final byte NAL_END_STREAM      = 11;
    public static final byte NAL_FILLER_DATA     = 12;
    public static final byte NAL_SPS_EXT         = 13;
    public static final byte NAL_AUXILIARY_SLICE = 19;
    public static final byte NAL_STAP_A          = 24; // https://tools.ietf.org/html/rfc3984 5.7.1
    public static final byte NAL_STAP_B          = 25; // 5.7.1
    public static final byte NAL_MTAP16          = 26; // 5.7.2
    public static final byte NAL_MTAP24          = 27; // 5.7.2
    public static final byte NAL_FU_A            = 28; // 5.8 fragmented unit
    public static final byte NAL_FU_B            = 29; // 5.8
//  public static final int NAL_FF_IGNORE       = 0xff0f001;

    // Table 7-3: NAL unit type codes
    public static final byte H265_NAL_TRAIL_N    = 0;
    public static final byte H265_NAL_TRAIL_R    = 1;
    public static final byte H265_NAL_TSA_N      = 2;
    public static final byte H265_NAL_TSA_R      = 3;
    public static final byte H265_NAL_STSA_N     = 4;
    public static final byte H265_NAL_STSA_R     = 5;
    public static final byte H265_NAL_RADL_N     = 6;
    public static final byte H265_NAL_RADL_R     = 7;
    public static final byte H265_NAL_RASL_N     = 8;
    public static final byte H265_NAL_RASL_R     = 9;
    public static final byte H265_NAL_BLA_W_LP   = 16;
    public static final byte H265_NAL_BLA_W_RADL = 17;
    public static final byte H265_NAL_BLA_N_LP   = 18;
    public static final byte H265_NAL_IDR_W_RADL = 19;
    public static final byte H265_NAL_IDR_N_LP   = 20;
    public static final byte H265_NAL_CRA_NUT    = 21;
    public static final byte H265_NAL_VPS        = 32;
    public static final byte H265_NAL_SPS        = 33;
    public static final byte H265_NAL_PPS        = 34;
    public static final byte H265_NAL_AUD        = 35;
    public static final byte H265_NAL_EOS_NUT    = 36;
    public static final byte H265_NAL_EOB_NUT    = 37;
    public static final byte H265_NAL_FD_NUT     = 38;
    public static final byte H265_NAL_SEI_PREFIX = 39;
    public static final byte H265_NAL_SEI_SUFFIX = 40;

    private static final byte[] NAL_PREFIX1 = { 0x00, 0x00, 0x00, 0x01 };
    private static final byte[] NAL_PREFIX2 = { 0x00, 0x00, 0x01 };

    public static boolean isValidH264NalUnit(@Nullable byte[] data, int offset, int length) {
        boolean ret = false;

        if (data == null || length <= NAL_PREFIX1.length)
            return false;

        if (data[offset] == 0) {
            // Check first 4 bytes maximum
            for (int cpos = 1; cpos < NAL_PREFIX1.length; cpos++) {
                if (data[cpos + offset] != 0) {
                    ret = data[cpos + offset] == 1;
                    break;
                }
            }
        }
        return ret;
    }

    public static byte getH264NalUnitType(@Nullable byte[] data, int offset, int length) {
        if (data == null || length <= NAL_PREFIX1.length)
            return (byte)-1;

        int nalUnitTypeOctetOffset = -1;
        if (data[offset + NAL_PREFIX2.length - 1] == 1)
            nalUnitTypeOctetOffset = offset + NAL_PREFIX2.length - 1;
        else if (data[offset + NAL_PREFIX1.length - 1] == 1)
            nalUnitTypeOctetOffset = offset + NAL_PREFIX1.length - 1;

        if (nalUnitTypeOctetOffset != -1) {
            byte nalUnitTypeOctet = data[nalUnitTypeOctetOffset + 1];
            return (byte) (nalUnitTypeOctet & 0x1f);
        } else {
            return (byte)-1;
        }
    }

    /**
     * Search for 00 00 01 or 00 00 00 01 in byte stream.
     * @return offset to the start of NAL unit if found, otherwise -1
     */
    public static int searchForH264NalUnitStart(
            @NonNull byte[] data,
            int offset,
            int length,
            @NonNull AtomicInteger prefixSize) {
        if (offset >= data.length - 3)
            return -1;
        for (int pos = 0; pos < length; pos++) {
            int prefix = getNalUnitStartCodePrefixSize(data, pos + offset, length);
            if (prefix >= 0) {
                prefixSize.set(prefix);
                return pos + offset;
            }
        }
        return -1;
    }

//    /**
//     * Search for 00 00 01 or 00 00 00 01 in byte stream.
//     * @param data
//     * @param offset
//     * @param length
//     * @return offset to the NAL unit type if found, otherwise -1
//     */
//    private static int searchForNalUnitType(byte[] data, int offset, int length) {
//        if (data[offset] == 0) {
//            for (int pos = 1; pos < length; pos++) {
//                int prefixSize = getNalUnitStartCodePrefixSize(data, pos + offset, length);
//                if (prefixSize >= 0)
//                    return pos + prefixSize;
//            }
//        }
//        return -1;
//    }

//    public static boolean isAnyH264KeyFrame(@Nullable byte[] data, int offset, int length) {
//        if (data == null || length <= 0)
//            return false;
//
//        AtomicInteger nalUnitPrefixSize = new AtomicInteger(-1);
//        long timestamp = System.currentTimeMillis();
//        boolean nalUnitsFound = false;
//        while (true) {
//            int nalUnitIndex = searchForH264NalUnitStart(
//                    data,
//                    offset,
//                    length,
//                    nalUnitPrefixSize);
//
//            if (nalUnitIndex >= 0) {
//                nalUnitsFound = true;
//                int nalUnitOffset = nalUnitIndex + nalUnitPrefixSize.get();
//                byte nalUnitTypeOctet = data[nalUnitOffset];
//                byte nalUnitType = (byte)(nalUnitTypeOctet & 0x1f);
//
//                if (DEBUG)
//                    Log.d(TAG, "NAL unit type: " + getH264NalUnitTypeString(nalUnitType));
//
//                switch (nalUnitType) {
//                    case NAL_IDR_SLICE:
//                        return true;
//                    case NAL_SLICE:
//                        return false;
//                }
//
//                // Continue searching
//                offset = nalUnitOffset;
//
//                // Check that we are not too long here
//                if (System.currentTimeMillis() - timestamp > 100) {
//                    Log.w(TAG, "Cannot process data within 100 msec in " + length +
//                            " bytes (index=" + nalUnitIndex + ", length=" + length + ")");
//                    break;
//                }
//            } else {
//                break;
//            }
//        }
//
//        // Maybe this is FLV H264 I-frame
//        // http://195.154.182.204:8083/camera1/h264
//        // 0, 0, 0, 23, 103
////        final byte[] FLV_H264_IFRAME = { 0, 0, 0, 0x17 };
////        return ByteUtils.memcmp(FLV_H264_IFRAME, 0, data, 0, FLV_H264_IFRAME.length);
//
//        //noinspection RedundantIfStatement
//        if (nalUnitsFound) {
//            // NAL units found, but no key frame
//            return false;
//        } else {
//            // FLV? Force it to decode.
//            // FLV decoding will work on software H.264 decoder only.
//            return true;
//        }
//
////      Log.d(TAG, "Non keyframe");
////      return false;
//    }

//    public static boolean isH264KeyFrame(@Nullable byte[] data, int offset, int length) {
//        boolean isKeyFrame = false;
//
//        if (data == null || length <= 0)
//            return false;
//
//        boolean isNalUnit = false;
//        int nalUnitTypeIndex = -1;
//
//        if (data[offset] == 0) {
//            for (int cpos = 1; cpos < length; cpos++) {
//                if (data[cpos + offset] != 0) {
//                    if (data[cpos + offset] == 1) {
//                        isNalUnit = true;
//                        nalUnitTypeIndex = cpos + 1;
//                    }
//                    break;
//                }
//            }
//        }
//
//        // https://www.cmlab.csie.ntu.edu.tw/~cathyp/eBooks/14496_MPEG4/iso14496-10.pdf, p48
//        // Type Name
//        //    0 [unspecified]
//        //    1 Coded slice
//        //    2 Data Partition A
//        //    3 Data Partition B
//        //    4 Data Partition C
//        //    5 IDR (Instantaneous Decoding Refresh) Picture
//        //    6 SEI (Supplemental Enhancement Information)
//        //    7 SPS (Sequence Parameter Set)
//        //    8 PPS (Picture Parameter Set)
//        //    9 Access Unit Delimiter
//        //   10 EoS (End of Sequence)
//        //   11 EoS (End of Stream)
//        //   12 Filter Data
//        //13-23 [extended]
//        //24-31 [unspecified]
//        if (isNalUnit) {
//            byte nalUnitTypeOctet = data [offset + nalUnitTypeIndex];
//            byte nalUnitType = (byte)(nalUnitTypeOctet & 0x1f);
////          if (DEBUG)
////              Log.d(TAG, "NAL unit type: " + getNalUnitTypeString(nalUnitType) + " (" + nalUnitType + ")");
//            if (   nalUnitType == NAL_IDR_SLICE // IDR (key frame)
////                || nalUnitType == NAL_SPS       // SPS (Sequence Parameter Set)
////                || nalUnitType == NAL_PPS       // PPS (Picture Parameter Set)
////                || nalUnitType == NAL_AUD // HACK For TRENDnet TV-IP310PI. The camera sends only NAL unit 9.
////                                          // We do not know how to handle it. Just say that this is keyframe
////                                          // to force decoding it.
//               ) {
//                isKeyFrame = true;
//            }
//        } else {
//            // Maybe this is FLV H264 I-frame
//            // http://195.154.182.204:8083/camera1/h264
//            // 0, 0, 0, 23, 103
//            final byte[] FLV_H264_IFRAME = { 0, 0, 0, 0x17 };
//            isKeyFrame = ByteUtils.memcmp(FLV_H264_IFRAME, 0, data, 0, FLV_H264_IFRAME.length);
//        }
//        return isKeyFrame;
//    }

//    private static boolean isH265IRAP(int nalUnitType) {
//        return nalUnitType >= 16 && nalUnitType <= 23;
//    }

//    public static boolean isH265KeyFrame(@Nullable byte[] data, int offset, int length) {
//        boolean isKeyFrame = false;
//
//        if (data == null || length <= 0)
//            return false;
//
//        boolean isNalUnit = false;
//        int nalUnitTypeIndex = -1;
//
//        if (data[offset] == 0) {
//            for (int cpos = 1; cpos < length; cpos++) {
//                if (data[cpos + offset] != 0) {
//                    if (data[cpos + offset] == 1) {
//                        isNalUnit = true;
//                        nalUnitTypeIndex = cpos + 1;
//                    }
//                    break;
//                }
//            }
//        }
//
//        if (isNalUnit) {
//            byte nalUnitTypeOctet = data [offset + nalUnitTypeIndex];
//            byte nalUnitType = (byte)((nalUnitTypeOctet & 0x7E) >> 1);
////          if (DEBUG)
////              Log.d(TAG, "NAL unit type: " + getNalUnitTypeString(nalUnitType) + " (" + nalUnitType + ")");
//            isKeyFrame = isH265IRAP(nalUnitType)
//                    || nalUnitType == H265_NAL_VPS
//                    || nalUnitType == H265_NAL_SPS
//                    || nalUnitType == H265_NAL_PPS
//                    // GW Security GW4536IP sends 1 NAL_SEI_PREFIX and 20 NAL_TRAIL_R (no SPS or VPS).
//                    // Treat SEI_PREFIX as key frame.
//                    || nalUnitType == H265_NAL_SEI_PREFIX
//                    || nalUnitType == H265_NAL_SEI_SUFFIX;
//        }
//        return isKeyFrame;
//    }

    public static class NalUnit {
        public final byte type;
        public final int offset;
        public final int length;
        private NalUnit(byte type, int offset, int length) {
            this.type = type;
            this.offset = offset;
            this.length = length;
        }
    }

   public static int getH264NalUnitsNumber(
           @NonNull byte[] data,
           int    dataOffset,
           int    length) {
       return getH264NalUnits(data, dataOffset, length, new ArrayList<NalUnit>());
   }

    public static int getH264NalUnits(
            @NonNull byte[] data,
            int    dataOffset,
            int    length,
            @NonNull ArrayList<NalUnit> foundNals) {

        foundNals.clear();

        int nalUnits = 0;
        int nextNalOffset = 0;
        AtomicInteger nalUnitPrefixSize = new AtomicInteger(-1);
        long timestamp = System.currentTimeMillis();

        int offset = dataOffset;
        boolean stopped = false;
        while (!stopped) {

            // Search for first NAL unit
            int nalUnitIndex = searchForH264NalUnitStart(
                    data,
                    offset + nextNalOffset,
                    length - nextNalOffset,
                    nalUnitPrefixSize);

            // NAL unit found
            if (nalUnitIndex >= 0) {
                nalUnits++;

                int nalUnitOffset = offset + nextNalOffset + nalUnitPrefixSize.get();
                byte nalUnitTypeOctet = data[nalUnitOffset];
                byte nalUnitType = (byte)(nalUnitTypeOctet & 0x1f);

                // Search for second NAL unit (optional)
                int nextNalUnitStartIndex = searchForH264NalUnitStart(
                        data,
                        nalUnitOffset,
                        length - nalUnitOffset,
                        nalUnitPrefixSize);

                // Second NAL unit not found. Use till the end.
                if (nextNalUnitStartIndex < 0) {
                    // Not found next NAL unit. Use till the end.
//                  nextNalUnitStartIndex = length - nextNalOffset + dataOffset;
                    nextNalUnitStartIndex = length + dataOffset;
                    stopped = true;
                }

                int l = nextNalUnitStartIndex - offset;
                if (DEBUG)
                    Log.d(TAG, "NAL unit type: " + getH264NalUnitTypeString(nalUnitType) +
                            " (" + nalUnitType + ") - " + l + " bytes, offset " + offset);
                foundNals.add(new NalUnit(nalUnitType, offset, l));
                offset = nextNalUnitStartIndex;

                // Check that we are not too long here
                if (System.currentTimeMillis() - timestamp > 100) {
                    Log.w(TAG, "Cannot process data within 100 msec in " + length + " bytes");
                    break;
                }
            } else {
                stopped = true;
            }
        }
        return nalUnits;
    }

   // TODO: Code has a BUG! Sometimes it goes to infinite loop!
   public static int searchForH264NalUnitByType(
           @NonNull byte[] data,
           int    offset,
           int    length,
           int    byUnitType) {

       AtomicInteger nalUnitPrefixSize = new AtomicInteger(-1);
       long timestamp = System.currentTimeMillis();

       while (true) {
           int nalUnitIndex = searchForH264NalUnitStart(data, offset, length, nalUnitPrefixSize);
           if (nalUnitIndex >= 0) {
               int nalUnitOffset = nalUnitIndex + nalUnitPrefixSize.get();
               byte nalUnitTypeOctet = data[nalUnitOffset];
               byte nalUnitType = (byte)(nalUnitTypeOctet & 0x1f);
               if (nalUnitType == byUnitType) {
                   return nalUnitIndex;
               }
               offset = nalUnitOffset;

               // Check that we are not too long here
               if (System.currentTimeMillis() - timestamp > 100) {
                   Log.w(TAG, "Cannot process data within 100 msec in " + length + " bytes");
                   break;
               }
           } else {
               break;
           }
       }
       return -1;
   }

//    // 00 00 00 01 .. .. .. .. .. .. 00 00 00 01 .. .. .. ..
//    // 0 0 0 1 103 66 -128 30 -38 2 -128 -10 -108 -126 -127 1 3 104 80 -102 -128 0 0 0 1 104 -50 6 -30
//    public static boolean updateSpsPpsFromH264NalUnits(
//            @NonNull VideoCodecContext context,
//            @NonNull byte[] data,
//            int dataOffset,
//            int length) {
//
//        boolean updatedSps = false;
//        boolean updatedPps = false;
//        boolean stopped    = false;
//        AtomicInteger nalUnitPrefixSize = new AtomicInteger(-1);
//        int nextNalOffset = 0;
//        long timestamp = System.currentTimeMillis();
//
//        int offset = dataOffset;
//        while (!stopped) {
//
//            // Search for first NAL unit
//            int nalUnitIndex = searchForH264NalUnitStart(
//                    data,
//                    offset + nextNalOffset,
//                    length - nextNalOffset,
//                    nalUnitPrefixSize);
//
//            // NAL unit found
//            if (nalUnitIndex >= 0) {
//
//                int nalUnitOffset = offset + nextNalOffset + nalUnitPrefixSize.get();
//                byte nalUnitTypeOctet = data[nalUnitOffset];
//                byte nalUnitType = (byte)(nalUnitTypeOctet & 0x1f);
//
//                // Search for second NAL unit (optional)
//                int nextNalUnitStartIndex = searchForH264NalUnitStart(
//                        data,
//                        nalUnitOffset,
//                        length - nalUnitOffset,
//                        nalUnitPrefixSize);
//
//                // Second NAL unit not found. Use till the end.
//                if (nextNalUnitStartIndex < 0) {
////                  nextNalUnitStartIndex = length - nextNalOffset + dataOffset;
//                    nextNalUnitStartIndex = length + dataOffset;
//                    // Prepare to leave the loop
//                    stopped = true;
//                }
//
//                if (DEBUG)
//                    Log.d(TAG, "NAL unit type: " + getH264NalUnitTypeString(nalUnitType) + " (" + nalUnitType + ")");
//
//                // Found SPS or PPS NAL unit
//                if (nalUnitType == NAL_SPS || nalUnitType == NAL_PPS) {
//                    int nalUnitSize = nextNalUnitStartIndex - nalUnitIndex;
//                    Assert.assertTrue("Negative NAL unit size " + nalUnitSize + " (" + nextNalUnitStartIndex + ", " + nalUnitIndex + ")", nalUnitSize >= 0);
//                    byte[] nalUnit = new byte[nalUnitSize];
//                    System.arraycopy(data, offset + nextNalOffset, nalUnit, 0, nalUnitSize);
//
//                    // Update SPS
//                    if (nalUnitType == NAL_SPS) {
//                        if (DEBUG)
//                            Log.d(TAG, "Updated SPS");
//                        context.h264HeaderSps = nalUnit;
//                        updatedSps = true;
//                        // Update PPS
//                    } else { // nalUnitType == NAL_PPS
//                        if (DEBUG)
//                            Log.d(TAG, "Updated PPS");
//                        context.h264HeaderPps = nalUnit;
//                        updatedPps = true;
//                    }
//                    // Updated both SPS and PPS
//                    if (updatedPps && updatedSps)
//                        break;
//                }
//                nextNalOffset = nextNalUnitStartIndex;
//            } else {
//                break;
//            }
//
//            // Check that we are not too long here
//            if (System.currentTimeMillis() - timestamp > 100) {
//                Log.w(TAG, "Cannot find SPS or PPS within 100 msec in " + length + " bytes");
//                break;
//            }
//        } // while
//        return updatedPps || updatedSps;
//    }

//    @Nullable
//    public static Size getImageSizeFromH264SpsNalUnit(@NonNull byte[] data, int offset, int length) {
//        int nalUnitTypeIndex = getNalUnitStartCodePrefixSize(data, offset, length);
//        if (nalUnitTypeIndex > 0) {
//            byte nalUnitTypeOctet = data [offset + nalUnitTypeIndex];
//            byte nalUnitType = (byte)(nalUnitTypeOctet & 0x1f);
//            if (DEBUG)
//                Log.d(TAG, "NAL unit type: " + getH264NalUnitTypeString(nalUnitType) + " (" + nalUnitType + ")");
//            // SPS NAL unit found
//            if (nalUnitType == NAL_SPS) {
//                int offsetSps = nalUnitTypeIndex + 1;
//                ByteBuffer buffer = ByteBuffer.wrap(data, offsetSps, length - offsetSps);
//                SeqParameterSet sps = SeqParameterSet.read(buffer);
//                Size size = H264Utils.getPicSize(sps);
//                if (DEBUG)
//                    Log.d(TAG, "NAL SPS size " + size.getWidth() + "x" + size.getHeight());
//                return size;
//            }
//        }
//        // No SPS NAL unit found
//        return null;
//    }

//    @Nullable
//    public static Size getImageSizeFromH265SpsNalUnit(@NonNull byte[] data, int offset, int length) {
//        int nalUnitTypeIndex = getNalUnitStartCodePrefixSize(data, offset, length);
//        if (nalUnitTypeIndex > 0) {
//            byte nalUnitTypeOctet = data [offset + nalUnitTypeIndex];
//            byte nalUnitType = (byte)((nalUnitTypeOctet & 0x7E) >> 1);
//            if (DEBUG)
//                Log.d(TAG, "NAL unit type: " + getH264NalUnitTypeString(nalUnitType) + " (" + nalUnitType + ")");
//            // SPS NAL unit found
//            if (nalUnitType == H265_NAL_SPS) {
//                int offsetSps = nalUnitTypeIndex + 1;
//                ByteBuffer buffer = ByteBuffer.wrap(data, offsetSps, length - offsetSps);
//                SeqParameterSet sps = SeqParameterSetH265.read(buffer);
//                Size size = H265Utils.getPicSize(sps);
//                if (DEBUG)
//                    Log.d(TAG, "NAL SPS size " + size.getWidth() + "x" + size.getHeight());
//                return size;
//            }
//        }
//        // No SPS NAL unit found
//        return null;
//    }

    @NonNull
    public static String getH264NalUnitTypeString(byte nalUnitType) {
        switch (nalUnitType) {
            case NAL_SLICE:           return "NAL_SLICE";
            case NAL_DPA:             return "NAL_DPA";
            case NAL_DPB:             return "NAL_DPB";
            case NAL_DPC:             return "NAL_DPC";
            case NAL_IDR_SLICE:       return "NAL_IDR_SLICE";
            case NAL_SEI:             return "NAL_SEI";
            case NAL_SPS:             return "NAL_SPS";
            case NAL_PPS:             return "NAL_PPS";
            case NAL_AUD:             return "NAL_AUD";
            case NAL_END_SEQUENCE:    return "NAL_END_SEQUENCE";
            case NAL_END_STREAM:      return "NAL_END_STREAM";
            case NAL_FILLER_DATA:     return "NAL_FILLER_DATA";
            case NAL_SPS_EXT:         return "NAL_SPS_EXT";
            case NAL_AUXILIARY_SLICE: return "NAL_AUXILIARY_SLICE";
            case NAL_STAP_A:          return "NAL_STAP_A";
            case NAL_STAP_B:          return "NAL_STAP_B";
            case NAL_MTAP16:          return "NAL_MTAP16";
            case NAL_MTAP24:          return "NAL_MTAP24";
            case NAL_FU_A:            return "NAL_FU_A";
            case NAL_FU_B:            return "NAL_FU_B";
            default:                  return "unknown - " + nalUnitType;
        }
    }

//    @NonNull
//    public static String getH265NalUnitTypeString(byte nalUnitType) {
//        switch (nalUnitType) {
//            case H265_NAL_TRAIL_N:    return "NAL_TRAIL_N";
//            case H265_NAL_TRAIL_R:    return "NAL_TRAIL_R";
//            case H265_NAL_TSA_N:      return "NAL_TSA_N";
//            case H265_NAL_TSA_R:      return "NAL_TSA_R";
//            case H265_NAL_STSA_N:     return "NAL_STSA_N";
//            case H265_NAL_STSA_R:     return "NAL_STSA_R";
//            case H265_NAL_RADL_N:     return "NAL_RADL_N";
//            case H265_NAL_RADL_R:     return "NAL_RADL_R";
//            case H265_NAL_RASL_N:     return "NAL_RASL_N";
//            case H265_NAL_RASL_R:     return "NAL_RASL_R";
//            case H265_NAL_BLA_W_LP:   return "NAL_BLA_W_LP";
//            case H265_NAL_BLA_W_RADL: return "NAL_BLA_W_RADL";
//            case H265_NAL_BLA_N_LP:   return "NAL_BLA_N_LP";
//            case H265_NAL_IDR_W_RADL: return "NAL_IDR_W_RADL";
//            case H265_NAL_IDR_N_LP:   return "NAL_IDR_N_LP";
//            case H265_NAL_CRA_NUT:    return "NAL_CRA_NUT";
//            case H265_NAL_VPS:        return "NAL_VPS";
//            case H265_NAL_SPS:        return "NAL_SPS";
//            case H265_NAL_PPS:        return "NAL_PPS";
//            case H265_NAL_AUD:        return "NAL_AUD";
//            case H265_NAL_EOS_NUT:    return "NAL_EOS_NUT";
//            case H265_NAL_EOB_NUT:    return "NAL_EOB_NUT";
//            case H265_NAL_FD_NUT:     return "NAL_FD_NUT";
//            case H265_NAL_SEI_PREFIX: return "NAL_SEI_PREFIX";
//            case H265_NAL_SEI_SUFFIX: return "NAL_SEI_SUFFIX";
//            default:                  return "unknown - " + nalUnitType;
//        }
//    }
    private static int getNalUnitStartCodePrefixSize(@NonNull byte[] data, int offset, int length) {
        if (length < 4)
            return -1;

        if (ByteUtils.memcmp(data, offset, NAL_PREFIX1, 0, NAL_PREFIX1.length))
            return NAL_PREFIX1.length;
        else if (ByteUtils.memcmp(data, offset, NAL_PREFIX2, 0, NAL_PREFIX2.length))
            return NAL_PREFIX2.length;
        else
            return -1;
    }

}
