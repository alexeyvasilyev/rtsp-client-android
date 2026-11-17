package com.alexvas.rtsp;

import com.alexvas.rtsp.parser.RtpHeaderParser;
import org.junit.Test;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.Assert.*;

public class RtspClientOnvifReplayTest {

    /** ONVIF replay RTP packets carry 0xABAC header extension (NTP timestamp + flags, 3 words). */
    @Test(timeout = 5000) public void skipsHeaderExtensionOfApplicationTrack() throws Exception {
        byte[] payload = {0x11, 0x22, 0x33};
        byte[] packet = new byte[4 + 12 + payload.length];
        packet[0] = (byte) 0xAB; packet[1] = (byte) 0xAC; packet[3] = 3; // 3 words
        System.arraycopy(payload, 0, packet, 16, payload.length);

        RtspClient.SdpInfo sdpInfo = new RtspClient.SdpInfo();
        sdpInfo.applicationTrack = new RtspClient.ApplicationTrack();
        sdpInfo.applicationTrack.payloadType = 107;

        AtomicBoolean exit = new AtomicBoolean();
        AtomicReference<byte[]> received = new AtomicReference<>();
        RtspClient.RtspClientListener listener = (RtspClient.RtspClientListener) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class<?>[]{RtspClient.RtspClientListener.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("onRtspApplicationDataReceived")) {
                        byte[] data = (byte[]) args[0];
                        int offset = (int) args[1];
                        int length = (int) args[2];
                        received.set(Arrays.copyOfRange(data, offset, offset + length));
                        exit.set(true);
                    }
                    return null;
                });

        RtpPacketReader reader = new RtpPacketReader() {
            private boolean sent = false;
            @Override public RtpHeaderParser.RtpHeader readHeader() {
                if (sent) return null;
                sent = true;
                RtpHeaderParser.RtpHeader header = new RtpHeaderParser.RtpHeader();
                header.version = 2;
                header.extension = 1;
                header.payloadType = 107;
                header.payloadSize = packet.length;
                return header;
            }
            @Override public void readPayload(byte[] data, int offset, int length) {
                System.arraycopy(packet, 0, data, offset, length);
            }
        };
        RtspClient.readRtpData(reader, sdpInfo, exit, listener, 0, 0, () -> {});
        assertArrayEquals(payload, received.get());
    }
}
