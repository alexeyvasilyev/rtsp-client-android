package com.alexvas.rtsp;

import android.os.Build;
import android.util.Log;

import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.StandardProtocolFamily;
import java.nio.channels.DatagramChannel;

/** Negotiated multicast destination. SETUP parameters take precedence over SDP. */
final class MulticastTransport {
    private static final String TAG = MulticastTransport.class.getSimpleName();

    final InetAddress group;
    final int rtpPort;
    final int rtcpPort;

    private MulticastTransport(InetAddress group, int rtpPort, int rtcpPort) {
        this.group = group;
        this.rtpPort = rtpPort;
        this.rtcpPort = rtcpPort;
    }

    InetSocketAddress bindAddress(int port) {
        // A wildcard socket would also receive unicast datagrams and, on Linux, every group
        // joined on this host for that port, mixing streams of cameras sharing the port.
        return new InetSocketAddress(group, port);
    }

    /** Caller owns the returned channel; it stays open for the whole RTSP session. */
    DatagramChannel openRtpChannel(NetworkInterface networkInterface, int receiveBufferSize) throws IOException {
        DatagramChannel channel = DatagramChannel.open(group instanceof Inet6Address
                ? StandardProtocolFamily.INET6 : StandardProtocolFamily.INET);
        try {
            channel.configureBlocking(false);
            channel.socket().setReuseAddress(true);
            try {
                channel.socket().setReceiveBufferSize(receiveBufferSize);
            } catch (SocketException e) {
                Log.w(TAG, "Failed to enlarge UDP RTP receive buffer", e);
            }
            channel.socket().bind(bindAddress(rtpPort));
            // MulticastChannel is API 26+. Membership is per host and interface, not per socket, so
            // on older versions the group joined by openRtcpSocket() also feeds this channel.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                channel.join(group, networkInterface);
            return channel;
        } catch (IOException e) {
            channel.close();
            throw e;
        }
    }

    /** Caller owns the returned socket; it stays open for the whole RTSP session. */
    MulticastSocket openRtcpSocket(NetworkInterface networkInterface) throws IOException {
        MulticastSocket socket = new MulticastSocket(null);
        try {
            socket.setReuseAddress(true);
            socket.bind(bindAddress(rtcpPort));
            socket.joinGroup(new InetSocketAddress(group, rtcpPort), networkInterface);
            return socket;
        } catch (IOException e) {
            socket.close();
            throw e;
        }
    }

    boolean sameEndpoint(MulticastTransport other) {
        return group.equals(other.group) && rtpPort == other.rtpPort;
    }

    static MulticastTransport parse(String transport, String sdpAddress, int sdpPort) throws IOException {
        String destination = sdpAddress;
        String ports = null;
        if (transport != null) {
            String[] tokens = transport.split(";");
            String profile = tokens[0].trim();
            if (!profile.equalsIgnoreCase("RTP/AVP") && !profile.equalsIgnoreCase("RTP/AVP/UDP"))
                throw new IOException("Unsupported multicast transport: " + profile);
            for (int i = 1; i < tokens.length; i++) {
                String token = tokens[i].trim();
                if (token.equalsIgnoreCase("unicast"))
                    throw new IOException("RTSP server selected unicast for multicast SETUP");
                String[] pair = token.split("=", 2);
                if (pair.length != 2) continue;
                if (pair[0].trim().equalsIgnoreCase("destination")) destination = pair[1].trim();
                if (pair[0].trim().equalsIgnoreCase("port")) ports = pair[1].trim();
            }
        }
        if (destination == null || destination.isEmpty())
            throw new IOException("Missing multicast destination in SETUP and SDP");
        // SDP IPv4 addresses may include /ttl[/number-of-addresses].
        InetAddress group = InetAddress.getByName(destination.split("/", 2)[0]);
        if (!group.isMulticastAddress())
            throw new IOException("RTSP destination is not a multicast address: " + destination);
        int rtp = sdpPort;
        int rtcp;
        try {
            if (ports != null) {
                String[] values = ports.split("-", -1);
                if (values.length > 2) throw new NumberFormatException();
                rtp = Integer.parseInt(values[0]);
                rtcp = values.length == 2 ? Integer.parseInt(values[1]) : rtp + 1;
            } else {
                rtcp = rtp + 1;
            }
        } catch (NumberFormatException e) {
            throw new IOException("Invalid multicast port: " + ports, e);
        }
        if (rtp < 1 || rtp > 65535 || rtcp < 1 || rtcp > 65535 || rtp == rtcp)
            throw new IOException("Invalid multicast RTP/RTCP ports: " + rtp + "-" + rtcp);
        return new MulticastTransport(group, rtp, rtcp);
    }
}
