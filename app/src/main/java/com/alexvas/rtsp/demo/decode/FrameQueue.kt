package com.alexvas.rtsp.demo.decode

import android.util.Log
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue
import java.util.concurrent.TimeUnit

class FrameQueue {

    class Frame(val data: ByteArray, val offset: Int, val length: Int, val timestamp: Long)

    val TAG: String = FrameQueue::class.java.simpleName
    val queue: BlockingQueue<Frame> = ArrayBlockingQueue(60)

    @Throws(InterruptedException::class)
    fun push(frame: Frame): Boolean {
        if (queue.offer(frame, 5, TimeUnit.MILLISECONDS)) {
            return true
        }
        Log.w(TAG, "Cannot add frame, queue is full")
        return false
    }

    @Throws(InterruptedException::class)
    fun pop(): Frame? {
        try {
            val frame: Frame? = queue.poll(1000, TimeUnit.MILLISECONDS)
            if (frame == null) {
                Log.w(TAG, "Cannot get frame, queue is empty")
            }
            return frame
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
        }
        return null
    }

    fun clear() {
        queue.clear()
    }
}
