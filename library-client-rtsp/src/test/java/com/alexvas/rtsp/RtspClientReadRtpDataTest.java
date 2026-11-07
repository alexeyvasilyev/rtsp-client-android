package com.alexvas.rtsp;

import com.alexvas.rtsp.parser.RtpHeaderParser;
import org.junit.Test;
import java.io.IOException;
import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.Assert.*;

public class RtspClientReadRtpDataTest {
    private final RtspClient.RtspClientListener listener = (RtspClient.RtspClientListener) Proxy.newProxyInstance(
            getClass().getClassLoader(),
            new Class<?>[]{RtspClient.RtspClientListener.class},
            (proxy, method, args) -> null);

    /** Behaves like an UDP reader with no incoming packets. */
    private static class IdleReader implements RtpPacketReader {
        @Override public RtpHeaderParser.RtpHeader readHeader() {
            try {
                Thread.sleep(2);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return null;
        }

        @Override public void readPayload(byte[] data, int offset, int length) {
        }
    }

    @Test(timeout = 5000) public void failsWhenNoPacketsArrive() {
        IOException e = assertThrows(IOException.class, () -> RtspClient.readRtpData(
                new IdleReader(), new RtspClient.SdpInfo(), new AtomicBoolean(), listener, 0, 20, () -> {}));
        assertTrue(e.getMessage(), e.getMessage().contains("No RTP packets"));
    }

    @Test(timeout = 5000) public void sendsKeepAliveWithoutPackets() throws Exception {
        AtomicBoolean exit = new AtomicBoolean();
        AtomicInteger keepAlives = new AtomicInteger();
        RtspClient.readRtpData(new IdleReader(), new RtspClient.SdpInfo(), exit, listener, 5, 0, () -> {
            if (keepAlives.incrementAndGet() == 3) exit.set(true);
        });
        assertEquals(3, keepAlives.get());
    }

    @Test(timeout = 5000) public void stopDuringOutageIsNotFailure() throws Exception {
        AtomicBoolean exit = new AtomicBoolean();
        RtspClient.readRtpData(new IdleReader() {
            @Override public RtpHeaderParser.RtpHeader readHeader() {
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                exit.set(true);
                return null;
            }
        }, new RtspClient.SdpInfo(), exit, listener, 0, 5, () -> {});
    }
}
