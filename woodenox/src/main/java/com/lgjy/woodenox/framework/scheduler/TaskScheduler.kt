package com.lgjy.woodenox.framework.scheduler

import com.lgjy.woodenox.db.Task

/**
 * Created by LGJY on 2022/6/22.
 * Email：yujye@sina.com
 *
 * 任务调度器接口
 * The interface of task scheduler
 */

interface TaskScheduler {

    /**
     * 重新加载初始化
     * Reload task
     */
    suspend fun reload()

    /**
     * 调度任务
     * Schedule task
     */
    fun schedule(vararg tasks: Task)

    /**
     * 取消任务
     * Cancel task
     */
    fun cancel(taskId: Long)
}
