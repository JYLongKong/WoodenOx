package com.lgjy.woodenox.framework.queue

import com.lgjy.woodenox.config.THRESHOLD_DOZE_TIMES
import com.lgjy.woodenox.db.Task
import com.lgjy.woodenox.util.LogP
import kotlinx.coroutines.delay
import java.util.concurrent.PriorityBlockingQueue
import kotlin.math.pow

/**
 * Created by LGJY on 2022/6/23.
 * Email：yujye@sina.com
 *
 * 打盹队列--按重试次数指数级增长delay时间
 */

class DozeQueue : TaskQueue {

    private val lock = Any()
    private var dozeHeaderQueue = PriorityBlockingQueue<Task>(11) { t1, t2 -> t1.priority - t2.priority }

    override fun reload(headers: List<Task>) {
        val dozeHeaders = headers.filter(::isBelongHere)
        LogP.i(TAG, "reload ${headers.size} tasks")
        if (dozeHeaders.isEmpty()) return
        synchronized(lock) {
            dozeHeaderQueue.apply {
                clear()
                headers.forEach { offer(it) }
            }
        }
    }

    override fun tryEnqueue(header: Task): Boolean {
        if (!isBelongHere(header)) {
            return false
        }
        synchronized(lock) {
            LogP.d(TAG, "enqueue: taskId->${header.taskId}")
            return dozeHeaderQueue.offer(header)
        }
    }

    /**
     * 每失败一次则推迟一倍时间去执行
     */
    override suspend fun dequeue(): Task? {
        val headerTask = dozeHeaderQueue.take()

        headerTask?.run {
            val delayTime: Long = ((2.0.pow(attemptTimes)) * 1000).toLong()
            LogP.d(TAG, "dequeue: delay $delayTime ms")
            delay(delayTime)
        }

        return headerTask
    }

    override fun isBelongHere(task: Task): Boolean = task.attemptTimes > THRESHOLD_DOZE_TIMES

    companion object {
        private val TAG = "===".intern() + DozeQueue::class.java.simpleName
    }
}
