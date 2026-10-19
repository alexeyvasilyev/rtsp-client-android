package com.alexvas.rtsp

import com.alexvas.rtsp.parser.RtpHeaderParser
import com.alexvas.utils.NetUtils
import java.io.Closeable
import java.io.IOException
import java.io.InputStream
import java.io.PushbackInputStream
import java.nio.ByteBuffer
import java.nio.channels.DatagramChannel
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.util.concurrent.atomic.AtomicBoolean

// Max UDP datagram size.
private const val MAX_DATAGRAM_SIZE = 65536

/**
 * Abstracts where RTP packets come from, so the media-processing loop in
 * [RtspClient]'s readRtpData() works the same whether media arrives interleaved on the RTSP
 * TCP connection or as separate UDP datagrams.
 */
interface RtpPacketReader {
    /**
     * Blocks until the next RTP packet (of any track) is available.
     * @return the parsed header, or null if reading should be retried (e.g. a
     * malformed/foreign packet was skipped, or a keep-alive response was found instead).
     */
    @Throws(IOException::class)
    fun readHeader(): RtpHeaderParser.RtpHeader?

    /** Copies the payload of the packet last returned by [readHeader] into `data`. */
    @Throws(IOException::class)
    fun readPayload(data: ByteArray, offset: Int, length: Int)
}

class TcpRtpPacketReader(inputStream: InputStream) : RtpPacketReader {

    // Lets RtpHeaderParser.readHeader() rescan bytes of a candidate header which turned out invalid.
    private val inputStream = PushbackInputStream(inputStream, RtpHeaderParser.PUSHBACK_SIZE)

    @Throws(IOException::class)
    override fun readHeader(): RtpHeaderParser.RtpHeader? =
        RtpHeaderParser.readHeader(inputStream)

    @Throws(IOException::class)
    override fun readPayload(data: ByteArray, offset: Int, length: Int) {
        NetUtils.readData(inputStream, data, offset, length)
    }
}

/**
 * Reads RTP packets off up to 3 UDP [DatagramChannel]s (video/audio/application), multiplexed
 * with a [Selector] so a single thread can service them all. Each channel is expected to already
 * be connect()-ed to its track's server RTP address, so the kernel filters out anything not sent
 * by that peer.
 */
class UdpRtpPacketReader @Throws(IOException::class) constructor(
    rtpChannels: Array<DatagramChannel?>,
    private val exitFlag: AtomicBoolean,
) : RtpPacketReader, Closeable {

    private val selector: Selector = Selector.open()
    private val buffer: ByteBuffer = ByteBuffer.allocate(MAX_DATAGRAM_SIZE)

    init {
        for (channel in rtpChannels) {
            channel?.register(selector, SelectionKey.OP_READ)
        }
    }

    @Throws(IOException::class)
    override fun readHeader(): RtpHeaderParser.RtpHeader? {
        while (!exitFlag.get()) {
            // Drain the selected set before selecting again. Already-selected keys do not
            // necessarily contribute to select()'s return value, and repeatedly selecting
            // can starve another track while the first track stays readable.
            if (selector.selectedKeys().isEmpty()) {
                selector.select(SELECT_TIMEOUT_MSEC)
            }
            val it = selector.selectedKeys().iterator()
            if (!it.hasNext())
                continue
            val key = it.next()
            it.remove()
            if (!key.isReadable)
                continue
            val channel = key.channel() as DatagramChannel
            buffer.clear()
            channel.read(buffer)
            val length = buffer.position()
            // Too short, or not a RTP packet (e.g. stray non-RTP traffic). Skip it.
            val header = RtpHeaderParser.parsePacket(buffer.array(), length) ?: continue
            return header
        }
        return null
    }

    override fun readPayload(data: ByteArray, offset: Int, length: Int) {
        System.arraycopy(buffer.array(), RtpHeaderParser.RTP_HEADER_SIZE, data, offset, length)
    }

    override fun close() {
        try {
            selector.close()
        } catch (_: IOException) {
        }
    }

    companion object {
        // Selector poll interval, in milliseconds. Bounds how long shutdown (exitFlag) takes to notice.
        private const val SELECT_TIMEOUT_MSEC = 500L
    }
}
