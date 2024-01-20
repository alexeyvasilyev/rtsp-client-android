package com.alexvas.rtsp.codec

import android.util.Log
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue
import java.util.concurrent.TimeUnit

class FrameQueue(private val frameQueueSize: Int) {

    data class Frame (
        val data: ByteArray,
        val offset: Int,
        val length: Int,
        val timestamp: Long
    )

    private val queue: BlockingQueue<Frame> = ArrayBlockingQueue(frameQueueSize)

    @Throws(InterruptedException::class)
    fun push(frame: Frame): Boolean {
        if (queue.offer(frame, 5, TimeUnit.MILLISECONDS)) {
            return true
        }
        if (queue.size >= frameQueueSize) {
            Log.w(TAG, "Cannot add frame, queue is full. Queue size: ${queue.size}")
            Thread.currentThread().interrupt()
            queue.poll()
            queue.clear()
        }
        Log.w(TAG, "Cannot add frame, queue is full")
        return false
    }

    @Throws(InterruptedException::class)
    fun pop(): Frame? {
        try {
            if (queue.size > 0) {
                val frame: Frame? = queue.poll(1000, TimeUnit.MILLISECONDS)

                if (frame == null) {
                    Log.w(TAG, "Cannot get frame, queue is empty. Queue size: ${queue.size}")
                }
                return frame
            }
        } catch (e: InterruptedException) {
            Log.w(TAG, "InterruptedException caught. Queue size: ${queue.size}")
            queue.clear()
            Thread.currentThread().interrupt()
        }
        return null
    }

    fun clear() {
        queue.clear()
    }

    companion object {
        private val TAG: String = FrameQueue::class.java.simpleName
    }

}
