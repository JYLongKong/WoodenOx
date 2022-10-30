package com.lgjy.woodenox.framework.task

import com.lgjy.woodenox.entity.TaskParameters
import com.lgjy.woodenox.entity.TaskResult
import com.lgjy.woodenox.framework.futures.ListenableFuture
import com.lgjy.woodenox.framework.futures.SettableFuture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import java.util.concurrent.Executor

/**
 * Created by LGJY on 2022/6/22.
 * Email：yujye@sina.com
 *
 * 赋予ListenableTask的Kt协程属性
 * Coroutine attribute given to ListenableTask
 */

abstract class CoroutineTask(taskParameters: TaskParameters) : ListenableTask(taskParameters) {

    private val job = Job()
    private val future: SettableFuture<TaskResult> = SettableFuture.create()
    private val taskExecutor: Executor = taskParameters.taskExecutor.backgroundExecutor

    init {
        future.addListener(
            Runnable {
                if (future.isCancelled) {
                    job.cancel()
                }
            }, taskExecutor
        )
    }

    abstract suspend fun doTask(): TaskResult

    override fun startTask(): ListenableFuture<TaskResult> {
        CoroutineScope(taskExecutor.asCoroutineDispatcher() + job).launch {
            try {
                val result = doTask()
                future.set(result)
            } catch (e: Exception) {
                future.setException(e)
            }
        }

        return future
    }

    override fun onStopped() {
        super.onStopped()
        future.cancel(false)
    }
}
