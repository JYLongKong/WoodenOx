package com.lgjy.woodenox.framework

import android.util.ArrayMap
import com.lgjy.woodenox.db.Task
import com.lgjy.woodenox.db.TaskDatabase
import com.lgjy.woodenox.entity.TaskState
import com.lgjy.woodenox.util.LogP
import java.util.*

/**
 * Created by LGJY on 2022/11/9.
 * Email：yujye@sina.com
 *
 * 任务缓冲区
 *
 * 有前/后置依赖关系的任务进入缓冲区
 * 等待组装任务链完成调用任务分发器进入对应任务队列
 * 同步任务链中所有任务的头节点、优先级和入队状态，完善各节点的nextTaskId
 */

class TaskBuffer internal constructor(
    private val taskDatabase: TaskDatabase,
    private val taskDispatcher: TaskDispatcher
) {
    // 缓冲区任务集合
    private val bufferingTasks = ArrayMap<Long, Task>()

    // 尾结点任务链中缺失的任务id
    private val lackedIds = ArrayMap<Long, Task>()
    private val taskDao = taskDatabase.TaskDao()
    private val lock = Any()

    /**
     * 重新载入缓冲区
     */
    suspend fun reload() {
        val bufferedTasks = taskDao.getTaskWithState(TaskState.BUFFERED)
        LogP.i(TAG, "reload ${bufferedTasks.size} tasks")
        synchronized(lock) {
            // 先重建缓冲区任务集合
            lackedIds.clear()
            bufferingTasks.clear()
            bufferingTasks.ensureCapacity(bufferedTasks.size)
            bufferedTasks.forEach { bufferingTasks[it.taskId] = it }

            // 再进行进入缓冲区后的操作
            bufferedTasks.forEach { onTaskBuffering(it) }
        }
    }

    /**
     * 任务进入缓冲区
     */
    fun taskIn(task: Task) {
        LogP.d(TAG, "taskIn(taskId->${task.taskId})")
        // 筛除非BUFFERED态任务
        if (task.state != TaskState.BUFFERED) {
            LogP.e(TAG, "taskInBuffer failed: taskId->${task.taskId}")
            return
        }
        synchronized(lock) {
            // 加入缓存区集合
            bufferingTasks[task.taskId] = task
            onTaskBuffering(task)
        }
    }

    /**
     * 任务进入缓冲区后相关操作，区分尾节点/非尾节点
     */
    private fun onTaskBuffering(task: Task) {
        if (task.nextTaskId == 0L) {    // 尾节点进入缓冲区，则开始组装任务链弹出
            tryMakeChain(task)?.let { chainOut(it) }
        } else {    // 非尾节点进入缓冲区
            if (lackedIds.contains(task.taskId)) {  // 如果是缺失任务id
                // 向前检查缺失
                val lackId = checkLack(task)
                lackedIds.remove(task.taskId)?.let { footer ->
                    if (lackId != null) {   // 更新缺失任务id
                        lackedIds[lackId] = footer
                    } else {    // 尝试组装任务链
                        tryMakeChain(footer)?.let { chainOut(it) }
                    }
                }
            }
        }
    }

    /**
     * 给定尾节点，通过prevId查询并尝试组装任务链
     * 若组装中途没找到对应的任务，则向缺失任务id集合注册所需的任务id，以备等到对应任务进入再次启动组装流程
     *
     * @return 组装成功的任务链，如果组装失败则返回null
     */
    private fun tryMakeChain(footer: Task): List<Task>? {
        var chain: LinkedList<Task>? = null
        var temp = footer
        while (temp.prevTaskId > 0) {
            val preTemp = bufferingTasks[temp.prevTaskId]
            if (preTemp == null) {
                lackedIds[temp.prevTaskId] = footer // 注册任务链缺失的任务id
                return null
            }
            if (chain == null) {
                chain = LinkedList<Task>()
                chain.addFirst(footer)
            }
            chain.addFirst(preTemp)
            temp = preTemp
        }
        return chain
    }

    /**
     * 统一设置任务链中所有任务的头节点、优先级和入队状态，完善各节点的nextTaskId
     * 最后交给任务分发器分发
     */
    private fun chainOut(chain: List<Task>) {
        if (chain.isEmpty()) return
        val headerTaskId = chain.first().taskId
        val maxPriority = chain.maxOf { it.priority }
        taskDatabase.runInTransaction {
            chain.forEachIndexed { index, task ->
                task.headerTaskId = headerTaskId
                task.priority = maxPriority
                task.state = TaskState.ENQUEUED
                if (task.nextTaskId == -1L) {
                    chain.getOrNull(index + 1)?.let {
                        task.nextTaskId = it.taskId
                    }
                }
                taskDao.insertTask(task)
            }
        }

        bufferingTasks.removeAll(chain)
        taskDispatcher.dispatchTaskChain(chain)
    }

    /**
     * 通过传入节点向前寻找缺失的任务id
     *
     * @return 缺失的任务id 如果顺利抵达头节点则返回null
     */
    private fun checkLack(task: Task): Long? {
        var temp = task
        while (temp.prevTaskId > 0) {
            val preTemp = bufferingTasks[temp.prevTaskId] ?: return temp.prevTaskId
            temp = preTemp
        }
        return null
    }

    companion object {
        private const val TAG = "===TaskBuffer"
    }
}
