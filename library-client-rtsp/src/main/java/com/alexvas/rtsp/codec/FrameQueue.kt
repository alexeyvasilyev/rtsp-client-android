package com.alexvas.rtsp.codec

import android.content.Context
import android.util.Log
import android.widget.Toast
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue
import java.util.concurrent.TimeUnit

class FrameQueue(frameQueueSize: Int) {

    data class Frame (
        val data: ByteArray,
        val offset: Int,
        val length: Int,
        val timestamp: Long,
        val applicationContext: Context
    )

    private val queue: BlockingQueue<Frame> = ArrayBlockingQueue(frameQueueSize)
    lateinit var mapplicationContext:Context


    @Throws(InterruptedException::class)
    fun push(frame: Frame): Boolean {
        mapplicationContext = frame.applicationContext
        if (queue.offer(frame, 5, TimeUnit.MILLISECONDS)) {
            return true
        }

        if (queue.size>=200) {
            Thread.currentThread().interrupt()
            queue.poll()
            queue.clear()
            Log.i(TAG, "frame, queue is equal "+queue.size)


        } else {
            Log.i(TAG, "frame, queue is "+queue.size)
        }



//            Toast.makeText(
//                mapplicationContext,
//                "Cannot add frame, queue is full " + queue.size,
//                Toast.LENGTH_SHORT
//            ).show()
//
        return false
    }

    @Throws(InterruptedException::class)
    fun pop(): Frame? {
        try {
            if (queue.size>0) {
                val frame: Frame? = queue.poll(100, TimeUnit.MILLISECONDS)

                if (frame == null) {
                    Log.i(TAG, "frame, queue size is " + queue.size)
                    // Toast.makeText(mapplicationContext,"Cannot add frame, queue is empty "+queue.size,Toast.LENGTH_SHORT).show()

                }
                return frame
            }
            } catch (e: InterruptedException) {
            Log.i(TAG, "Cannot add frame, queue is full "+queue.size, e)
           // Toast.makeText(mapplicationContext,"Cannot add frame, queue is full "+queue.size,Toast.LENGTH_SHORT).show()
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
