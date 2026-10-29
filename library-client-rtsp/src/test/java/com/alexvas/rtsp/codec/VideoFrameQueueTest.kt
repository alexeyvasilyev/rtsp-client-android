package com.alexvas.rtsp.codec

import org.junit.Assert.*
import org.junit.Test

class VideoFrameQueueTest {
    private fun frame(ts: Long, keyframe: Boolean = false) =
        FrameQueue.VideoFrame(VideoCodecType.H264, keyframe, ByteArray(1), 0, 1, ts)

    private fun drain(q: VideoFrameQueue): List<Long> =
        generateSequence { q.pop(0) }.map { it.timestampMs }.toList()

    @Test fun overflowSkipsFramesTillNextKeyframe() {
        val q = VideoFrameQueue(2)
        assertTrue(q.push(frame(1, keyframe = true)))
        assertTrue(q.push(frame(2)))
        // Queue is full. Cleared, frame dropped.
        assertFalse(q.push(frame(3)))
        assertEquals(0, q.size)
        // Non-keyframes skipped till the next keyframe.
        assertFalse(q.push(frame(4)))
        assertTrue(q.push(frame(5, keyframe = true)))
        assertTrue(q.push(frame(6)))
        assertEquals(listOf(5L, 6L), drain(q))
    }

    @Test fun overflowOnKeyframeKeepsKeyframe() {
        val q = VideoFrameQueue(2)
        q.push(frame(1, keyframe = true))
        q.push(frame(2))
        assertTrue(q.push(frame(3, keyframe = true)))
        assertTrue(q.push(frame(4)))
        assertEquals(listOf(3L, 4L), drain(q))
    }
}
