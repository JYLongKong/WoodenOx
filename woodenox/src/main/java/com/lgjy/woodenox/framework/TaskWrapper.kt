package com.lgjy.woodenox.framework

import com.lgjy.woodenox.config.Configuration
import com.lgjy.woodenox.db.Task
import com.lgjy.woodenox.db.TaskDatabase
import com.lgjy.woodenox.entity.TaskParameters
import com.lgjy.woodenox.entity.TaskResult
import com.lgjy.woodenox.entity.TaskState
import com.lgjy.woodenox.framework.executor.TaskExecutor
import com.lgjy.woodenox.framework.futures.ListenableFuture
import com.lgjy.woodenox.framework.futures.SettableFuture
import com.lgjy.woodenox.framework.task.ListenableTask
import com.lgjy.woodenox.util.LogP

/**
 * Created by LGJY on 2022/11/9.
 * Email：yujye@sina.com
 *
 * 任务包装器
 *
 * 提供对任务的运行、中断操作
 * 统一处理执行结果并回调
 */

class TaskWrapper internal constructor(
    private val mTaskId: Long,
    private val mTaskDatabase: TaskDatabase,
    private val mTaskExecutor: TaskExecutor,
    private val mConfiguration: Configuration,
    private var mTask: ListenableTask? = null
) : Runnable {

    var executionListener: ExecutionListener? = null

    @Volatile
    private var mInterrupted: Boolean = false
    private val mTaskDao = mTaskDatabase.TaskDao()
    private var mFuture: ListenableFuture<TaskResult>? = null
    private var mResult: TaskResult? = null

    override fun run() {
        runInternal()
    }

    /**
     * 中断任务
     */
    internal fun interrupt() {
        mInterrupted = true
        checkInterruption()
        var isDone = false
        mFuture?.let {
            isDone = it.isDone
            it.cancel(true)
        }
        if (!isDone) {
            mTask?.stop()
        }
    }

    /**
     * 运行任务
     */
    private fun runInternal() {
        // 检查是否中断
        if (checkInterruption()) return

        // 通过任务id查询任务
        val task: Task? = mTaskDao.getTaskWithId(mTaskId)

        // 检查任务是否存在
        if (task == null) {
            LogP.e(TAG, "runInternal: Didn't find taskId->$mTaskId")
            onExecuteEnded(TaskState.NONE)
            return
        }

        // 检查任务状态，只允许ENQUEUED通过
        if (task.state != TaskState.ENQUEUED) {
            LogP.e(TAG, "runInternal: taskId->$mTaskId is ${task.state}")
            onExecuteEnded(task.state)
            return
        }

        // 检查是否成功创建自定义任务
        if (mTask == null) {
            mTask = mConfiguration.taskFactory.createWorkerWithDefaultFallback(TaskParameters(task, mTaskExecutor))
            if (mTask == null) {
                LogP.e(TAG, "runInternal: TaskFactory create Task failed!")
                onExecuteEnded(task.state)
                return
            }
        }

        // 检查任务是否正在被使用
        if (mTask?.isUsed() == true) {
            LogP.e(TAG, "runInternal: Received an already-used Task")
            onExecuteEnded(task.state)
            return
        }

        mTask?.setUsed()

        // 设置任务状态为Running
        if (!trySetRunning()) {
            LogP.e(TAG, "runInternal: trySetRunning() failed")
            onExecuteEnded(task.state)
            return
        }

        // 再次检查是否中断
        if (checkInterruption()) return

        // 创建并执行具体任务 将执行结果存入resultFuture
        val resultFuture = SettableFuture.create<TaskResult>()
        mTaskExecutor.executeInMainThread(Runnable {
            kotlin.runCatching {
                // LogP.d(TAG, "runInternal: taskId->$mTaskId start Working")
                mFuture = mTask?.startTask()
                resultFuture.setFuture(mFuture)
            }.onFailure { resultFuture.setException(it) }
        })

        // 添加任务执行结果监听
        resultFuture.addListener(Runnable {
            try {
                val result = resultFuture.get()
                if (result == null) {
                    LogP.e(TAG, "runInternal: taskId->$mTaskId result is NULL")
                } else {
                    mResult = result
                }
            } catch (e: Exception) {
                LogP.e(TAG, "runInternal: taskId->$mTaskId occurred Exception", e)
            } finally {
                onTaskFinished()
            }
        }, mTaskExecutor.backgroundExecutor)
    }

    /**
     * 检查若被中断条件下的处理
     *
     * @return 是否被中断
     */
    private fun checkInterruption(): Boolean {
        if (mInterrupted) {
            LogP.d(TAG, "checkInterruption: taskId->${mTaskId} interrupted")
            onExecuteEnded(mTaskDao.getState(mTaskId))
            return true
        }
        return false
    }

    /**
     * 任务结束后的操作
     */
    private fun onTaskFinished() {
        if (!checkInterruption()) {
            val state = mTaskDao.getState(mTaskId)
            if (state == TaskState.RUNNING && mResult is TaskResult.Success) {
                onExecuteEnded(TaskState.SUCCEEDED)
            } else {
                onExecuteEnded(state)
            }
        }
    }

    /**
     * 在任务执行结束(中断、失败、成功)后调用
     *
     * @param endedState 以何种任务状态结束
     */
    private fun onExecuteEnded(endedState: TaskState) {
        LogP.d(TAG, "onExecuteEnded: taskId->$mTaskId endedState->${endedState.name}")
        mTaskDatabase.runInTransaction {
            when (endedState) {
                TaskState.SUCCEEDED -> mTaskDao.setTaskResult(mTaskId, TaskState.SUCCEEDED, mResult?.output.orEmpty())
                TaskState.RUNNING -> mTaskDao.setTaskResult(mTaskId, TaskState.ENQUEUED, mResult?.output.orEmpty())
                else -> LogP.e(TAG, "onExecuteEnded: unHandled TaskState->$endedState")
            }
            executionListener?.onExecuted(mTaskId, endedState)
        }
    }

    /**
     * 尝试设置任务状态为Running
     *
     * @return 是否设置Running状态成功
     */
    private fun trySetRunning(): Boolean {
        var setToRunning = false
        mTaskDatabase.runInTransaction {
            mTaskDao.setTaskState(TaskState.RUNNING, mTaskId)
            setToRunning = true
        }
        return setToRunning
    }

    companion object {
        private const val TAG = "===TaskWrapper"
    }
}
