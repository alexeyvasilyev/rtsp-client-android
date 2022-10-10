package com.alexvas.rtsp.parser;

import androidx.annotation.NonNull;

public interface AudioParser {
    public byte[] processRtpPacketAndGetSample(@NonNull byte[] data, int length);
}
