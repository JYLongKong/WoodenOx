package com.lgjy.woodenox.api

import android.content.Context
import com.lgjy.woodenox.db.Task
import com.lgjy.woodenox.entity.TaskState
import com.lgjy.woodenox.framework.executor.TaskExecutor
import com.lgjy.woodenox.framework.task.ListenableTask

/**
 * Created by LGJY on 2022/10/30.
 * Email：yujye@sina.com
 *
 * 木牛流马
 * 内部依赖WoodenOxImpl实现
 * 凭WoodenOxInitializer初始化或者主动调用initialize初始化
 */

abstract class WoodenOx {

    /**
     * 提交任务
     */
    abstract fun submit(listenableTask: ListenableTask): Long

    /**
     * 取消任务
     */
    abstract fun cancel(taskId: Long)

    /**
     * 更新任务
     */
    abstract fun updateTask(task: Task)

    /**
     * 获取任务列表
     * 可通过任务状态、标签进行筛选某应用下的任务
     */
    abstract suspend fun getTaskList(appId: String, state: Int, tag: String): List<Task>

    /**
     * 获取任务状态
     */
    abstract fun getTaskState(taskId: Long): TaskState

    /**
     * 获取任务执行器
     */
    abstract fun getTaskExecutor(): TaskExecutor

    companion object {

        fun getInstance(): WoodenOx = WoodenOxImpl.getInstance() ?: throw IllegalStateException(
            "WoodenOx is not initialized properly. The most likely cause is that you disabled " +
                    "WoodenOxInitializer in your manifest but forgot to call WoodenOx#initialize " +
                    "in your Application#onCreate or a ContentProvider."
        )

        fun initialize(context: Context) {
            WoodenOxImpl.initialize(context)
        }
    }
}
