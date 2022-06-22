package com.lgjy.woodenox.scheduler

import com.lgjy.woodenox.db.Task
import com.lgjy.woodenox.entity.TaskState
import com.lgjy.woodenox.util.LogP
import org.json.JSONObject

/**
 * Created by LGJY on 2022/6/22.
 * Email：yujye@sina.com
 */

class RunningScheduler internal constructor(
    private val mTaskManagerImpl: TaskManagerImpl,
    taskDatabase: TaskDatabase
) : TaskScheduler, ExecutionListener, EnvConditionListener {

    private val mTaskDao = taskDatabase.TaskDao()

    // 任务条件管理器
    private val mConditionManager = mTaskManagerImpl.configuration.conditionManager
        .also { it.outerEnvConditionListener = this }

    // 受限任务存储和对其相关操作
    private val mConstrainedTask = ConstrainedTask(mConditionManager)

    override suspend fun reload() {
        LogP.d(TAG, "reload()")
        val blockedTasks = mTaskDao.getTaskWithState(TaskState.BLOCKED)
        mConstrainedTask.clear()
        schedule(*blockedTasks.toTypedArray())
    }

    override fun schedule(vararg tasks: Task) {
        mTaskManagerImpl.getProcessor().addExecutionListener(this)

        val temp = arrayListOf<Task>()
        for (task in tasks) {
            if (task.state == TaskState.ENQUEUED || task.state == TaskState.BLOCKED) {
                if (mConditionManager.isMetCondition(task)) {   // 当前条件满足
                    if (task.state == TaskState.BLOCKED) {
                        mTaskDao.setTaskState(TaskState.ENQUEUED, task.taskId)
                    }
                    mTaskManagerImpl.startTask(task.taskId)
                } else {    // 当前条件不满足
                    temp.add(task)
                }
            } else {
                LogP.e(TAG, "schedule: taskId->${task.taskId} state is ${task.state}")
            }
        }

        // 筛选出受限任务添加到集合中
        mConstrainedTask.putAll(temp)
        // 同步阻塞态到DB
        val blockIds = temp.filter { it.state != TaskState.BLOCKED }.map { it.taskId }
        mTaskDao.setTaskState(TaskState.BLOCKED, *blockIds.toLongArray())
    }

    override fun cancel(taskId: Long) {
        mTaskDao.setTaskState(TaskState.CANCELLED, taskId)
        mTaskManagerImpl.getProcessor().addExecutionListener(this)
        mConstrainedTask.remove(taskId)
        mTaskManagerImpl.stopTask(taskId)
    }

    override fun onExecuted(taskId: Long, endedState: TaskState) {
        mConstrainedTask.remove(taskId)

        if (endedState == TaskState.SUCCEEDED) {
            mTaskDao.getTaskWithId(taskId)?.let { task ->
                if (task.nextTaskId == 0L) {    // 没有子任务
                    // 同步整条任务链为成功
//                    mTaskDao.setChainStateWithChildId(TaskState.SUCCEEDED, taskId)
                } else {    // 仍有子任务
                    val nextTask = mTaskDao.getTaskWithId(task.nextTaskId)
                    if (nextTask == null) {
                        LogP.e(TAG, "onExecuted: ")
                        return
                    }
                    if (nextTask.state != TaskState.ENQUEUED) {
                        mTaskDao.setTaskState(TaskState.ENQUEUED, nextTask.taskId)
                    }

                    // 将任务的结果传递到子任务的参数中
                    if (nextTask.expectations.isNotBlank()) {
                        passParams(task, nextTask)
                    }

                    // 调度子任务
                    schedule(nextTask)
                }
            }
        } else if (mTaskDao.getAttemptTimes(taskId) > THRESHOLD_RECYCLED_TIMES) { // 失败次数超过阈值
            // 同步整条任务链为回收
            mTaskDao.setChainStateWithChildId(TaskState.RECYCLED, taskId)
        } else {    // 执行失败
            // 同步整条任务链为入队
            mTaskDao.setChainStateWithChildId(TaskState.ENQUEUED, taskId)
            // 重新调度任务入队执行
            mTaskManagerImpl.enqueueTask(taskId)
        }
    }

    override fun onEnvConditionChanged(envCond: Int) {
        when {
            envCond and mConstrainedTask.getCondtionRequired() == mConstrainedTask.getCondtionRequired() -> { // 条件全部满足
                mConstrainedTask.getConstrainedTasks()
                    .forEach { mTaskManagerImpl.startTask(it.taskId) }
                mConstrainedTask.clear()
            }
            envCond and mConstrainedTask.getCondtionRequired() == 0 -> {   // 条件全都不满足
                LogP.d(TAG, "onConditionChanged: no condition met, do nothing")
                // TODO: 2021/3/18  阻塞"全部"任务
            }
            else -> {    // 部分条件满足
                val metConditionTasks = mConstrainedTask.getConstrainedTasks()
                    .filter { mConditionManager.isMetCondition(it) }
                    .onEach { mTaskManagerImpl.startTask(it.taskId) }

                mConstrainedTask.removeAll(metConditionTasks)
            }
        }
    }

    /**
     * 根据toTask.expectation参数
     * 将从fromTask.output中提取参数
     * 传递到toTask.data中
     *
     * 例
     * expectations: {
     *  "oss$accessUrl": "data$uploadUrl"
     * }
     */
    private fun passParams(fromTask: Task, toTask: Task) {
        kotlin.runCatching {
            LogP.i(TAG, "before passParams() toTask.data->${toTask.data}")
            val expectations = JSONObject(toTask.expectations)
            val newKV = arrayListOf<Pair<String, String>>()

            // 从output取数据
            for (key in expectations.keys()) {
                val valueHierarchy = expectations.getString(key).split('$')
                var temp = JSONObject(fromTask.output)
                for (i in 1 until valueHierarchy.size) {
                    temp = temp.getJSONObject(valueHierarchy[i])
                }
                val targetValue = temp.getString(valueHierarchy.last())
                newKV.add(key.orEmpty() to targetValue)     // "oss$accessUrl": "www.oss-hangzhou.com/2103312.jpg"
            }

            val data = JSONObject(toTask.data)
            // 向data放数据
            for ((k, v) in newKV) {
                val keyHierarchy = k.split('$')
                var temp = data
                for (i in 1 until keyHierarchy.size) {
                    temp = temp.getJSONObject(keyHierarchy[i])
                }
                temp.put(keyHierarchy.last(), v)    // "accessUrl": "www.oss-hangzhou.com/2103312.jpg"
            }
            toTask.data = data.toString()
            // TODO: 2021/4/16 由于就算被中断了也会从父任务开始进行 所有这里没有同步到DB中
            LogP.i(TAG, "after passParams() toTask.data->${toTask.data}")
        }.onFailure { LogP.e(TAG, "fromTask.output->${fromTask.output} toTask.expectations->${toTask.expectations}", it) }
    }

    private fun get(fromTask: Task,valueHierarchy: List<String>) {
        kotlin.runCatching {
            var temp = JSONObject(fromTask.output)
            for (i in 1 until valueHierarchy.size) {
                temp = temp.getJSONObject(valueHierarchy[i])
            }
        }
    }

    companion object {
        private val TAG = "===".intern() + RunningScheduler::class.java.simpleName
    }
}
