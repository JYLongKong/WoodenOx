package com.lgjy.woodenox.framework.task

import com.lgjy.woodenox.condition.RequiredConditionListener
import com.lgjy.woodenox.db.Task
import android.util.ArrayMap

/**
 * Created by LGJY on 2021/3/16.
 * Email：yujye@sina.com
 *
 * 用于储存受条件限制任务和其相关操作
 */

class ConstrainedTask(private val requiredConditionListener: RequiredConditionListener) {

    // 存放受限条件的任务
    private val mConstrainedTasks: ArrayMap<Long, Task> = ArrayMap()

    // 满足所有受限任务所需的条件
    private var mConditionRequired: Int = 0
        set(value) {
            if (field != value) {
                requiredConditionListener.onRequiredConditionChanged(field, value)
                field = value
            }
        }

    @Synchronized
    fun putAll(tasks: Iterable<Task>) {
        tasks.forEach { mConstrainedTasks[it.taskId] = it }
        mConditionRequired = mConditionRequired or computeRequiredCondition(tasks)
    }

    @Synchronized
    fun removeAll(tasks: Iterable<Task>) {
        tasks.forEach { mConstrainedTasks.remove(it.taskId) }
        mConditionRequired = computeRequiredCondition(mConstrainedTasks.values)
    }

    @Synchronized
    fun clear() {
        mConstrainedTasks.clear()
        mConditionRequired = 0
    }

    @Synchronized
    fun put(task: Task) {
        mConstrainedTasks[task.taskId] = task
        mConditionRequired = mConditionRequired or task.condition
    }

    @Synchronized
    fun remove(taskId: Long) {
        mConstrainedTasks.remove(taskId)?.let {
            var temp = 0
            for (constrainedTask in mConstrainedTasks.values) {
                temp = temp or constrainedTask.condition
                if (it.condition and temp == it.condition) {    // 一旦满足 说明删除该任务不对任务需求有影响
                    return@let
                }
            }
            mConditionRequired = temp
        }
    }

    fun getCondtionRequired(): Int = mConditionRequired

    fun getConstrainedTasks(): Collection<Task> = mConstrainedTasks.values

    /**
     * 统计任务集合所需的所有条件(没必要频繁调用，建议在集合有重大变化时调用)
     */
    private fun computeRequiredCondition(tasks: Iterable<Task>): Int {
        var requiredCondition = 0
        for (task in tasks) {
            requiredCondition = requiredCondition or task.condition
        }
        return requiredCondition
    }

    companion object {
        private const val TAG = "===ConstrainedTask"
    }
}
