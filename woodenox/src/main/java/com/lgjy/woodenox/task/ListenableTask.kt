package com.lgjy.woodenox.task

import com.lgjy.woodenox.entity.TaskParameters
import com.lgjy.woodenox.entity.TaskResult
import com.lgjy.woodenox.futures.ListenableFuture

/**
 * Created by LGJY on 2022/6/22.
 * Email：yujye@sina.com
 *
 * 任务基类--可监听执行结果
 * Abstract class of task, make result of task listenable
 */

abstract class ListenableTask(val taskParamters: TaskParameters) {

    @Volatile
    private var stopped: Boolean = false
    private var used: Boolean = false

    abstract fun startTask(): ListenableFuture<TaskResult>

    open fun onStopped() {
    }

    internal fun stop() {
        stopped = true
        onStopped()
    }

    internal fun isUsed():Boolean = used

    internal fun setUsed() {
        used = true
    }
}
