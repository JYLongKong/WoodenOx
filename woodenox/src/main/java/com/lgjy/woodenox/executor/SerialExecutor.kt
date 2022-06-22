package com.lgjy.woodenox.executor

import java.util.ArrayDeque
import java.util.concurrent.Executor

/**
 * Created by LGJY on 2022/6/22.
 * Email：yujye@sina.com
 *
 * 依次执行的Executor
 * Execute in sequence
 */

class SerialExecutor(private val executor: Executor) : Executor {

    @Volatile
    private var activeRunnable: Runnable? = null
    private val serialRunnables = ArrayDeque<SerialRunnable>()
    private val lock = Any()

    override fun execute(command: Runnable) {
        synchronized(lock) {
            serialRunnables.add(SerialRunnable(this, command))
            if (activeRunnable == null) {
                scheduleNext()
            }
        }
    }

    /**
     * 调度下一个任务
     * Schedule the next task
     */
    fun scheduleNext() {
        synchronized(lock) {
            if (serialRunnables.poll().also { activeRunnable = it } != null) {
                executor.execute(activeRunnable)
            }
        }
    }

    /**
     * 执行完毕后调度下一个任务
     * Schedule the next task when execute completed
     */
    inner class SerialRunnable(
        private val serialExecutor: SerialExecutor,
        private val runnable: Runnable
    ) : Runnable {

        override fun run() {
            try {
                runnable.run()
            } finally {
                serialExecutor.scheduleNext()
            }
        }
    }
}
