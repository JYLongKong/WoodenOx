package com.lgjy.woodenox.framework

import android.util.ArrayMap
import androidx.collection.ArraySet
import com.lgjy.woodenox.config.Configuration
import com.lgjy.woodenox.db.TaskDatabase
import com.lgjy.woodenox.entity.TaskState
import com.lgjy.woodenox.framework.executor.TaskExecutor
import com.lgjy.woodenox.util.LogP

/**
 * Created by LGJY on 2022/11/9.
 * Email：yujye@sina.com
 *
 * 任务执行器
 * 统一管理着将要执行的任务和被取消的任务
 */

class TaskProcessor internal constructor(
    private val mTaskExecutor: TaskExecutor,
    private val mTaskDatabase: TaskDatabase,
    private val mConfiguration: Configuration
) : ExecutionListener {

    // 存储正在执行的任务集合
    private val mProcessingTasks: ArrayMap<Long, TaskWrapper> = ArrayMap()

    // 存储被标记取消的任务集合
    private val mCancelledIds: ArraySet<Long> by lazy { ArraySet() }

    // 存放外部的任务执行结果监听器
    private val mExecutionListeners: MutableList<ExecutionListener> = arrayListOf()
    private val mLock = Any()

    /**
     * 当任务执行结束后的回调
     * 从正在执行集合中移除，让外部执行监听决定是否重新调度执行
     */
    override fun onExecuted(taskId: Long, endedState: TaskState) {
        // LogP.d(TAG, "onExecuted: taskId->$taskId")
        synchronized(mLock) {
            mProcessingTasks.remove(taskId)
            mExecutionListeners.forEach {
                it.onExecuted(taskId, endedState)
            }
        }
    }

    /**
     * 开启执行任务
     *
     * @return 是否成功开启任务执行
     */
    fun startTask(taskId: Long): Boolean {
        val taskWrapper: TaskWrapper
        synchronized(mLock) {
            // 判断是否正在处理
            if (isProcessing(taskId)) {
                LogP.e(TAG, "startTask: taskId->$taskId is Processing")
                return false
            }

            // 初始化任务包装器
            taskWrapper = TaskWrapper(taskId, mTaskDatabase, mTaskExecutor, mConfiguration)
            // 设置重新调度监听
            taskWrapper.executionListener = this
            // 记录入队任务
            mProcessingTasks[taskId] = taskWrapper
        }

        // 任务进入线程池执行
        mTaskExecutor.executeInBackground(taskWrapper)
        return true
    }

    /**
     * 停止任务
     *
     * @return 返回是否成功中断任务
     */
    fun stopTask(taskId: Long): Boolean {
        synchronized(mLock) {
            val taskWrapper = mProcessingTasks.remove(taskId)
            return interrupt(taskId, taskWrapper)
        }
    }

    /**
     * 停止并取消任务
     */
    fun stopAndCancelTask(taskId: Long): Boolean {
        synchronized(mLock) {
            mCancelledIds.add(taskId)
            val taskWrapper = mProcessingTasks.remove(taskId)
            return interrupt(taskId, taskWrapper)
        }
    }

    /**
     * @return 该任务是否正在处理
     */
    fun isProcessing(taskId: Long): Boolean {
        synchronized(mLock) {
            return mProcessingTasks.containsKey(taskId)
        }
    }

    /**
     * @return 该任务是否被标记取消
     */
    fun isCancelled(taskId: Long): Boolean {
        synchronized(mLock) {
            return mCancelledIds.contains(taskId)
        }
    }

    /**
     * @return 任务执行器中是否还有任务
     */
    fun hasTask(): Boolean {
        synchronized(mLock) {
            return mProcessingTasks.isNotEmpty()
        }
    }

    /**
     * 向任务执行器中添加任务执行结果监听器
     */
    fun addExecutionListener(listener: ExecutionListener) {
        synchronized(mLock) {
            if (!mExecutionListeners.contains(listener)) {
                mExecutionListeners.add(listener)
            }
        }
    }

    /**
     * 在任务执行器中移除任务执行结果监听器
     */
    fun removeExecutionListener(listener: ExecutionListener) {
        synchronized(mLock) {
            mExecutionListeners.remove(listener)
        }
    }

    /**
     * 中断任务
     */
    private fun interrupt(taskId: Long, taskWrapper: TaskWrapper?): Boolean {
        return if (taskWrapper == null) {
            LogP.e(TAG, "interrupt: taskId->$taskId doesn't exsit for interrupting")
            false
        } else {
            taskWrapper.interrupt()
            true
        }
    }

    companion object {
        private const val TAG = "===TaskProcessor"
    }
}
