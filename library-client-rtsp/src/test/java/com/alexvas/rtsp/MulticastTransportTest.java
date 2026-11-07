package com.alexvas.rtsp;

import org.junit.Test;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardProtocolFamily;
import java.nio.channels.DatagramChannel;
import static org.junit.Assert.*;

public class MulticastTransportTest {
    @Test public void cameraIpv4ResponseBindsGroup() throws Exception {
        MulticastTransport result = MulticastTransport.parse(
                "RTP/AVP;multicast;destination=239.1.1.2;port=8860-8861;mode=\"play\"",
                "0.0.0.0", 0);
        for (int port : new int[]{result.rtpPort, result.rtcpPort}) {
            InetSocketAddress address = result.bindAddress(port);
            assertEquals(result.group, address.getAddress());
            assertEquals(port, address.getPort());
        }
        try (DatagramChannel channel = DatagramChannel.open(StandardProtocolFamily.INET)) {
            channel.bind(result.bindAddress(0));
        }
    }

    @Test public void ipv6GroupBindsGroup() throws Exception {
        MulticastTransport result = MulticastTransport.parse(
                "RTP/AVP;multicast;destination=ff15::123;port=5000-5001", null, 0);
        for (int port : new int[]{result.rtpPort, result.rtcpPort}) {
            InetSocketAddress address = result.bindAddress(port);
            assertEquals(result.group, address.getAddress());
            assertEquals(port, address.getPort());
        }
    }

    @Test public void sameEndpointRequiresSameGroupAndPort() throws Exception {
        MulticastTransport video = MulticastTransport.parse(
                "RTP/AVP;multicast;destination=239.1.1.1;port=5000-5001", null, 0);
        assertTrue(video.sameEndpoint(MulticastTransport.parse(
                "RTP/AVP;multicast;destination=239.1.1.1;port=5000-5001", null, 0)));
        assertFalse(video.sameEndpoint(MulticastTransport.parse(
                "RTP/AVP;multicast;destination=239.1.1.2;port=5000-5001", null, 0)));
        assertFalse(video.sameEndpoint(MulticastTransport.parse(
                "RTP/AVP;multicast;destination=239.1.1.1;port=5002-5003", null, 0)));
    }

    @Test public void setupOverridesSdp() throws Exception {
        MulticastTransport result = MulticastTransport.parse(
                "RTP/AVP;multicast;destination=239.10.20.30;port=5000-5001;ttl=16",
                "239.1.1.1/127", 4000);
        assertEquals("239.10.20.30", result.group.getHostAddress());
        assertEquals(5000, result.rtpPort);
        assertEquals(5001, result.rtcpPort);
    }

    @Test public void usesSdpWhenSetupOmitsDestinationAndPorts() throws Exception {
        MulticastTransport result = MulticastTransport.parse("RTP/AVP/UDP;multicast", "239.1.1.1/127/1", 4000);
        assertEquals("239.1.1.1", result.group.getHostAddress());
        assertEquals(4000, result.rtpPort);
        assertEquals(4001, result.rtcpPort);
    }

    @Test public void acceptsSinglePortAndCaseInsensitiveParameters() throws Exception {
        MulticastTransport result = MulticastTransport.parse(
                "RTP/AVP/UDP; Multicast; Destination = 239.1.1.2; Port = 6000", null, 0);
        assertEquals(6000, result.rtpPort);
        assertEquals(6001, result.rtcpPort);
    }

    @Test public void supportsIpv6() throws Exception {
        assertTrue(MulticastTransport.parse("RTP/AVP;multicast;destination=ff15::123;port=5000-5001", null, 0)
                .group.isMulticastAddress());
    }

    @Test public void rejectsInvalidNegotiation() {
        String[] invalid = {
                "RTP/AVP;unicast;destination=239.1.1.1;port=5000-5001",
                "RTP/AVP/TCP;multicast;destination=239.1.1.1;port=5000-5001",
                "RTP/AVP;multicast;destination=127.0.0.1;port=5000-5001",
                "RTP/AVP;multicast;port=5000-5001",
                "RTP/AVP;multicast;destination=239.1.1.1",
                "RTP/AVP;multicast;destination=239.1.1.1;port=0-1",
                "RTP/AVP;multicast;destination=239.1.1.1;port=65535",
                "RTP/AVP;multicast;destination=239.1.1.1;port=5000-5000",
                "RTP/AVP;multicast;destination=239.1.1.1;port=5000-bad",
                "RTP/AVP;multicast;destination=239.1.1.1;port=5000-5001-5002"
        };
        for (String transport : invalid) {
            assertThrows(transport, IOException.class, () -> MulticastTransport.parse(transport, null, 0));
        }
    }
}
