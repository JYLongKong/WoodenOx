package com.lgjy.woodenox.entity

import com.lgjy.woodenox.db.Task
import com.lgjy.woodenox.executor.TaskExecutor

/**
 * Created by LGJY on 2022/6/22.
 * Email：yujye@sina.com
 *
 * 封装执行任务的所有环境参数供
 * @see com.lgjy.woodenox.task.ListenableTask 实现类使用
 * Encapsulates all environment parameters for tasks to apply ListenableTask
 */

data class TaskParameters(
    val task: Task,
    var taskExecutor: TaskExecutor
)
