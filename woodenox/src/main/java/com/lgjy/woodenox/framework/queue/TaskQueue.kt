package com.lgjy.woodenox.framework.queue

import com.lgjy.woodenox.db.Task

/**
 * Created by LGJY on 2022/6/23.
 * Email：yujye@sina.com
 *
 * 任务队列
 * The interface of task queue which needs to be implemented
 */

interface TaskQueue {

    /**
     * 重新加载头部节点到队列
     * Reload header node into queue
     */
    fun reload(headers: List<Task>)

    /**
     * 按具体任务队列规则尝试头任务入队
     * @return 是否入队成功
     * Try to enqueue according to specific task queue rules
     * @return whether to enqueue successfully
     */
    fun tryEnqueue(header: Task): Boolean

    /**
     * 从任务队列取出任务
     */
    suspend fun dequeue(): Task?

    /**
     * 定义筛选条件区分是否属于该队列
     */
    fun isBelongHere(task: Task): Boolean
}
