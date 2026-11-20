package com.alexvas.rtsp.parser;

import android.annotation.SuppressLint;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.media3.common.util.ParsableBitArray;
import androidx.media3.common.util.ParsableByteArray;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// https://tools.ietf.org/html/rfc3640
//          +---------+-----------+-----------+---------------+
//         | RTP     | AU Header | Auxiliary | Access Unit   |
//         | Header  | Section   | Section   | Data Section  |
//         +---------+-----------+-----------+---------------+
//
//                   <----------RTP Packet Payload----------->
@SuppressLint("UnsafeOptInUsageError")
public class AacParser extends AudioParser {

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

    private final int _aacMode;

    public AacParser(@Nullable String aacMode) {
        // "mode" may be missing in SDP fmtp
        _aacMode = "AAC-lbr".equalsIgnoreCase(aacMode) ? MODE_LBR : MODE_HBR;

        headerScratchBits = new ParsableBitArray();
        headerScratchBytes = new ParsableByteArray();
    }

    /**
     * One RTP packet can carry several complete AAC access units back to
     * back ("multiple AU per packet" mode, RFC 3640 §3.3.6): the AU Header
     * Section up front holds one (size, index) header per AU, followed by
     * that many concatenated access units in the Access Unit Data Section.
     * This is the common case for a low-bitrate stream (small AAC frames,
     * several packed per RTP packet to cut overhead) — the original
     * implementation only ever handled exactly one AU per packet and
     * silently returned an empty array for everything else, which is why
     * audio-only playback produced a steady stream of real RTP packets but
     * no actual sound.
     * <p>
     * Fragmentation (one AU split across multiple RTP packets, RFC 3640
     * §3.3.5) is deliberately NOT handled — that needs cross-packet
     * reassembly state this parser doesn't keep. A low-bitrate/small-frame
     * stream's AAC frames are far smaller than one RTP packet's payload
     * capacity, so fragmentation isn't expected in practice; if a
     * particular AU's declared size runs past what's left in this packet,
     * that's treated as a sign of fragmentation and the remainder of this
     * packet is dropped (any AUs already extracted from earlier in the same
     * packet are still returned).
     */
    @Override
    @NonNull
    public List<byte[]> processRtpPacketAndGetSamples(@NonNull byte[] data, int length) {
        if (DEBUG)
            Log.v(TAG, "processRtpPacketAndGetSamples(length=" + length + ")");
        int numBitsAuSize = NUM_BITS_AU_SIZES[_aacMode];
        int numBitsAuIndex = NUM_BITS_AU_INDEX[_aacMode];
        int auHeaderBits = numBitsAuSize + numBitsAuIndex;

        ParsableByteArray packet = new ParsableByteArray(data, length);

//      +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+- .. -+-+-+-+-+-+-+-+-+-+
//      |AU-headers-length|AU-header|AU-header|      |AU-header|padding|
//      |                 |   (1)   |   (2)   |      |   (n)   | bits  |
//      +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+- .. -+-+-+-+-+-+-+-+-+-+
        int auHeadersLength = packet.readShort();
        int auHeadersLengthBytes = (auHeadersLength + 7) / 8;

        if (auHeadersLength < auHeaderBits || auHeadersLengthBytes > packet.bytesLeft()) {
            if (DEBUG)
                Log.w(TAG, "Malformed AU header section (auHeadersLength=" + auHeadersLength + ")");
            return Collections.emptyList();
        }

        int auHeadersCount = auHeadersLength / auHeaderBits;

        headerScratchBytes.reset(auHeadersLengthBytes);
        packet.readBytes(headerScratchBytes.getData(), 0, auHeadersLengthBytes);
        headerScratchBits.reset(headerScratchBytes.getData());

        int[] auSizes = new int[auHeadersCount];
        for (int i = 0; i < auHeadersCount; i++) {
            auSizes[i] = headerScratchBits.readBits(numBitsAuSize);
            // AU-index (first header) / AU-index-delta (subsequent headers)
            // — not used for reordering/gap-filling here, only consumed to
            // keep the bit reader aligned to the next AU-header.
            headerScratchBits.readBits(numBitsAuIndex);
        }

        List<byte[]> samples = new ArrayList<>(auHeadersCount);
        for (int i = 0; i < auHeadersCount; i++) {
            int auSize = auSizes[i];
            if (auSize <= 0 || auSize > packet.bytesLeft()) {
                if (DEBUG)
                    Log.w(TAG, "AU " + i + "/" + auHeadersCount + " size " + auSize
                            + " exceeds remaining payload " + packet.bytesLeft() + " — likely fragmented, not supported");
                break;
            }
            byte[] sample = new byte[auSize];
            System.arraycopy(packet.getData(), packet.getPosition(), sample, 0, auSize);
            packet.skipBytes(auSize);
            samples.add(sample);
        }
        return samples;
    }
}
