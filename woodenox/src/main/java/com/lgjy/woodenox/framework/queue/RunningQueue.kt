package com.lgjy.woodenox.framework.queue

import com.lgjy.woodenox.config.THRESHOLD_DOZE_TIMES
import com.lgjy.woodenox.db.Task
import com.lgjy.woodenox.util.LogP
import java.util.concurrent.PriorityBlockingQueue

/**
 * Created by LGJY on 2022/6/23.
 * Email：yujye@sina.com
 *
 * 运行队列
 */

class RunningQueue : TaskQueue {

    private val lock = Any()
    private var runningHeaderQueue = PriorityBlockingQueue<Task>(11) { t1, t2 -> t1.priority - t2.priority }

    override fun reload(headers: List<Task>) {
        val runningHeaders = headers.filter(::isBelongHere)
        LogP.i(TAG, "reload ${runningHeaders.size} tasks")
        if (runningHeaders.isEmpty()) return
        synchronized(lock) {
            runningHeaderQueue.apply {
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
            return runningHeaderQueue.offer(header)
        }
    }

    override suspend fun dequeue(): Task? {
        return runningHeaderQueue.take()
    }

    override fun isBelongHere(task: Task): Boolean = task.attemptTimes <= THRESHOLD_DOZE_TIMES

    companion object {
        private val TAG = "===".intern() + DozeQueue::class.java.simpleName
    }
}
