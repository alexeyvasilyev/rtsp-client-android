package com.alexvas.rtsp.parser;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.exoplayer2.util.ParsableBitArray;
import com.google.android.exoplayer2.util.ParsableByteArray;

// https://tools.ietf.org/html/rfc3640
//          +---------+-----------+-----------+---------------+
//         | RTP     | AU Header | Auxiliary | Access Unit   |
//         | Header  | Section   | Section   | Data Section  |
//         +---------+-----------+-----------+---------------+
//
//                   <----------RTP Packet Payload----------->
public class AacParser {

    private static final String TAG = AacParser.class.getSimpleName();
    private static final boolean DEBUG = false;

    private final ParsableBitArray headerScratchBits;
    private final ParsableByteArray headerScratchBytes;

    private static final int MODE_LBR = 0;
    private static final int MODE_HBR = 1;

    // Number of bits for AAC AU sizes, indexed by mode (LBR and HBR)
    private static final int[] NUM_BITS_AU_SIZES = {6, 13};

    // Number of bits for AAC AU index(-delta), indexed by mode (LBR and HBR)
    private static final int[] NUM_BITS_AU_INDEX = {2, 3};

    // Frame Sizes for AAC AU fragments, indexed by mode (LBR and HBR)
    private static final int[] FRAME_SIZES = {63, 8191};

    private final int _aacMode;
    private boolean completeFrameIndicator = true;

    public AacParser(@NonNull String aacMode) {
        _aacMode = aacMode.equalsIgnoreCase("AAC-lbr") ? MODE_LBR : MODE_HBR;

        headerScratchBits = new ParsableBitArray();
        headerScratchBytes = new ParsableByteArray();
    }

    @Nullable
    public byte[] processRtpPacketAndGetSample(@NonNull byte[] data, int length) {
        if (DEBUG)
            Log.v(TAG, "processRtpPacketAndGetSample(length=" + length + ")");
        int auHeadersCount = 1;
        int numBitsAuSize = NUM_BITS_AU_SIZES[_aacMode];
        int numBitsAuIndex = NUM_BITS_AU_INDEX[_aacMode];

        ParsableByteArray packet = new ParsableByteArray(data, length);

//      +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+- .. -+-+-+-+-+-+-+-+-+-+
//      |AU-headers-length|AU-header|AU-header|      |AU-header|padding|
//      |                 |   (1)   |   (2)   |      |   (n)   | bits  |
//      +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+- .. -+-+-+-+-+-+-+-+-+-+
        int auHeadersLength = packet.readShort();//((data[0] & 0xFF) << 8) | (data[1] & 0xFF);
        int auHeadersLengthBytes = (auHeadersLength + 7) / 8;

        headerScratchBytes.reset(auHeadersLengthBytes);
        packet.readBytes(headerScratchBytes.getData(), 0, auHeadersLengthBytes);
        headerScratchBits.reset(headerScratchBytes.getData());

        int bitsAvailable = auHeadersLength - (numBitsAuSize + numBitsAuIndex);

        if (bitsAvailable > 0) {// && (numBitsAuSize + numBitsAuSize) > 0) {
            auHeadersCount +=  bitsAvailable / (numBitsAuSize + numBitsAuIndex);
        }

        if (auHeadersCount == 1) {
            int auSize = headerScratchBits.readBits(numBitsAuSize);
            int auIndex = headerScratchBits.readBits(numBitsAuIndex);

            if (completeFrameIndicator) {
                if (auIndex == 0) {
                    if (packet.bytesLeft() == auSize) {
                        return handleSingleAacFrame(packet);

                    } else {
//                        handleFragmentationAacFrame(packet, auSize);
                    }
                }
            } else {
//                handleFragmentationAacFrame(packet, auSize);
            }

        } else {
            if (completeFrameIndicator) {
//                handleMultipleAacFrames(packet, auHeadersLength);
            }
        }
//        byte[] auHeader = new byte[length-2-auHeadersLengthBytes];
//        System.arraycopy(data,2-auHeadersLengthBytes, auHeader,0, auHeader.length);
//        if (DEBUG)
//            Log.d(TAG, "AU headers size: " + auHeadersLengthBytes + ", AU headers: " + auHeadersCount + ", sample length: " + auHeader.length);
//        return auHeader;
        return new byte[0];
    }

    private byte[] handleSingleAacFrame(ParsableByteArray packet) {
        int length = packet.bytesLeft();
        byte[] data = new byte[length];
        System.arraycopy(packet.getData(), packet.getPosition(), data,0, data.length);
        return data;
    }

//    private static final class AUHeader {
//        private int size;
//        private int index;
//
//        public AUHeader(int size, int index) {
//            this.size = size;
//            this.index = index;
//        }
//
//        public int size() { return size; }
//
//        public int index() { return index; }
//    }

//    /**
//     * Stores the consecutive fragment AU to reconstruct an AAC-Frame
//     */
//    private static final class FragmentedAacFrame {
//        public byte[] auData;
//        public int auLength;
//        public int auSize;
//
//        private int sequence;
//
//        public FragmentedAacFrame(int frameSize) {
//            // Initialize data
//            auData = new byte[frameSize];
//            sequence = -1;
//        }
//
//        /**
//         * Resets the buffer, clearing any data that it holds.
//         */
//        public void reset() {
//            auLength = 0;
//            auSize = 0;
//            sequence = -1;
//        }
//
//        public void sequence(int sequence) {
//            this.sequence = sequence;
//        }
//
//        public int sequence() {
//            return sequence;
//        }
//
//        /**
//         * Called to add a fragment unit to fragmented AU.
//         *
//         * @param fragment Holds the data of fragment unit being passed.
//         * @param offset The offset of the data in {@code fragment}.
//         * @param limit The limit (exclusive) of the data in {@code fragment}.
//         */
//        public void appendFragment(byte[] fragment, int offset, int limit) {
//            if (auSize == 0) {
//                auSize = limit;
//            } else if (auSize != limit) {
//                reset();
//            }
//
//            if (auData.length < auLength + limit) {
//                auData = Arrays.copyOf(auData, (auLength + limit) * 2);
//            }
//
//            System.arraycopy(fragment, offset, auData, auLength, limit);
//            auLength += limit;
//        }
//
//        public boolean isCompleted() {
//            return auSize == auLength;
//        }
//    }

}
