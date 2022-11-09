package com.lgjy.woodenox.framework

import com.lgjy.woodenox.db.Task
import com.lgjy.woodenox.framework.queue.TaskQueue
import com.lgjy.woodenox.util.LogP

/**
 * Created by LGJY on 2022/11/9.
 * Email：yujye@sina.com
 *
 * 任务分发器
 */

class TaskDispatcher(private val taskQueues: List<TaskQueue>) {

    /**
     * 将已经组装好的任务链头节点入队
     */
    fun dispatchTaskChain(tasks: List<Task>) {
        tasks.getOrNull(0)?.let { taskEnqueue(it) }
    }

    /**
     * 遍历任务队列集合，尝试入队
     * @return 入队是否成功
     */
    fun taskEnqueue(task: Task): Boolean {
        for (taskQueue in taskQueues) {
            if (taskQueue.tryEnqueue(task)) {
                return true
            }
        }

        LogP.e(TAG, "taskEnqueue failed: taskId->${task.taskId}")
        return false
    }

    companion object {
        private const val TAG = "===TaskDispatcher"
    }
}
