package com.alexvas.rtsp

import org.junit.Assert.*
import org.junit.Test
import java.net.InetAddress
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.DatagramChannel
import java.util.concurrent.atomic.AtomicBoolean

class UdpRtpPacketReaderTest {
    @Test(timeout = 5000) fun servicesBothReadyTracksAndPreservesDatagrams() = readTracks(true)

    @Test(timeout = 5000) fun readsUnconnectedChannelsForMulticast() = readTracks(false)

    @Test(timeout = 5000) fun idleChannelReturnsControlToCaller() {
        DatagramChannel.open().use { channel ->
            channel.bind(InetSocketAddress(InetAddress.getLoopbackAddress(), 0))
            channel.configureBlocking(false)
            UdpRtpPacketReader(arrayOf(channel), AtomicBoolean()).use { reader ->
                assertNull(reader.readHeader())
            }
        }
    }

    private fun readTracks(connected: Boolean) {
        val exit = AtomicBoolean()
        val senders = List(2) { DatagramChannel.open() }
        val receivers = List(2) { DatagramChannel.open() }
        try {
            for (i in 0..1) {
                senders[i].bind(InetSocketAddress(InetAddress.getLoopbackAddress(), 0))
                receivers[i].bind(InetSocketAddress(InetAddress.getLoopbackAddress(), 0))
                if (connected) receivers[i].connect(senders[i].localAddress)
                receivers[i].configureBlocking(false)
                // Queue both tracks before the first select; a busy track must not starve
                // the second key left in the selected set.
                repeat(8) { seq ->
                    val payload = ByteArray(if (seq % 2 == 0) 7 else 1400) { (seq + i).toByte() }
                    val packet = ByteBuffer.allocate(12 + payload.size)
                        .put(0x80.toByte()).put((96 + i).toByte()).putShort(seq.toShort())
                        .putInt(100).putInt(i).put(payload)
                    packet.flip()
                    senders[i].send(packet, receivers[i].localAddress)
                }
            }
            UdpRtpPacketReader(arrayOf(receivers[0], receivers[1]), exit).use { reader ->
                val counts = IntArray(2)
                val firstTracks = mutableSetOf<Int>()
                repeat(16) { index ->
                    val header = requireNotNull(reader.readHeader())
                    val track = header.payloadType - 96
                    if (index < 2) firstTracks.add(track)
                    assertEquals(counts[track]++, header.sequenceNumber)
                    val payload = ByteArray(header.payloadSize)
                    reader.readPayload(payload, 0, payload.size)
                    assertArrayEquals(ByteArray(if (header.sequenceNumber % 2 == 0) 7 else 1400) {
                        (header.sequenceNumber + track).toByte()
                    }, payload)
                }
                assertEquals(setOf(0, 1), firstTracks)
                assertArrayEquals(intArrayOf(8, 8), counts)
            }
        } finally {
            exit.set(true)
            receivers.forEach { it.close() }
            senders.forEach { it.close() }
        }
    }
}
