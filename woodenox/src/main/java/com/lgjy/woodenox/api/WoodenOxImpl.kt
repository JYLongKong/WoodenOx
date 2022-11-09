package com.lgjy.woodenox.api

import android.content.Context
import com.lgjy.woodenox.config.Configuration
import com.lgjy.woodenox.db.Task
import com.lgjy.woodenox.db.TaskConvertor
import com.lgjy.woodenox.db.TaskDao
import com.lgjy.woodenox.db.TaskDatabase
import com.lgjy.woodenox.entity.TaskState
import com.lgjy.woodenox.framework.TaskBuffer
import com.lgjy.woodenox.framework.TaskDispatcher
import com.lgjy.woodenox.framework.TaskProcessor
import com.lgjy.woodenox.framework.executor.TaskExecutor
import com.lgjy.woodenox.framework.queue.DozeQueue
import com.lgjy.woodenox.framework.queue.RunningQueue
import com.lgjy.woodenox.framework.queue.TaskQueue
import com.lgjy.woodenox.framework.scheduler.RunningScheduler
import com.lgjy.woodenox.framework.scheduler.TaskScheduler
import com.lgjy.woodenox.framework.task.ListenableTask
import com.lgjy.woodenox.util.LogP
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlin.system.measureTimeMillis

/**
 * Created by LGJY on 2022/10/30.
 * Email：yujye@sina.com
 *
 * 木牛流马实现类
 */

class WoodenOxImpl private constructor(context: Context, val configuration: Configuration) : WoodenOx() {

    // 任务DB
    private val mDatabase: TaskDatabase = TaskDatabase.getInstance(context.applicationContext)
    private val mTaskDao: TaskDao = mDatabase.TaskDao()

    // 任务执行器
    private val mExecutor: TaskExecutor = TaskExecutor()

    // 任务处理器
    private val mProcessor: TaskProcessor = TaskProcessor(mExecutor, mDatabase, configuration)

    // 任务队列
    private val mQueues: List<TaskQueue> = arrayListOf(RunningQueue(), DozeQueue())

    // 任务分发器
    private val mDispatcher: TaskDispatcher = TaskDispatcher(mQueues)

    // 任务调度器
    private val mScheduler: TaskScheduler = RunningScheduler(this, mDatabase)

    // 任务缓冲区
    private val mBuffer: TaskBuffer = TaskBuffer(mDatabase, mDispatcher)

    init {
        init()
    }

    private fun init() {
        mExecutor.launch {
            measureTimeMillis {
                // 加载调度器
                mScheduler.reload()

                // 加载运行队列、打盹队列
                mTaskDao.setUnfinishedTaskEnqueued()
                val enqueuedHeaders = mTaskDao.getEnqueuedHeaders()
                mQueues.forEach { it.reload(enqueuedHeaders) }

                // 加载任务缓冲区
                mBuffer.reload()
            }.let { LogP.i(TAG, "init(): take $it ms") }

            // 开启队列出队轮询 收到最新的出队任务进入调度器
            for (queue in mQueues) {
                produce<Task> {
                    while (true) {
                        queue.dequeue()?.let { send(it) }
                    }
                }.consumeEach { mScheduler.schedule(it) }
            }
        }
    }

    override fun submit(listenableTask: ListenableTask): Long {
        LogP.d(TAG, "submit()")
//        CheckData.checkTask(taskData)?.let { return IBResponse(it.first, it.second, "") }

        val task = listenableTask.taskParamters.task
        task.className = listenableTask.javaClass.name
        task.state = if (task.prevTaskId == 0L && task.nextTaskId == 0L) {  // 单任务
            TaskState.ENQUEUED
        } else {    // 任务链
            TaskState.BUFFERED
        }

        // 任务入库
        val taskId = runBlocking {
            withContext(mExecutor.backgroundExecutor.asCoroutineDispatcher()) {
                mTaskDao.insertTask(task)
            }
        }
        LogP.i(TAG, "submit: taskId->$taskId insertTask succeeded")
        task.taskId = taskId

        // 区分任务状态分发到相应任务区域
        mExecutor.launch {
            when (task.state) {
                TaskState.BUFFERED -> mBuffer.taskIn(task)
                TaskState.ENQUEUED -> mDispatcher.taskEnqueue(task)
                else -> LogP.e(
                    TAG, "dispatchTask failed: taskId->${task.taskId} state is ${task.state}"
                )
            }
        }

        return taskId
    }

    override fun cancel(taskId: Long) {
        mScheduler.cancel(taskId)
    }

    override fun updateTask(task: Task) {
        TODO("not implemented")
    }

    override suspend fun getTaskList(appId: String, state: Int, tag: String): List<Task> {
        return mTaskDao.getTaskWithTagAndState(appId, TaskConvertor.deserializeTaskState(state), tag)
    }

    override fun getTaskState(taskId: Long): TaskState {
        return mTaskDao.getState(taskId)
    }

    /**
     * 通过任务执行器开启任务
     */
    internal fun startTask(taskId: Long) {
        mExecutor.executeInBackground { mProcessor.startTask(taskId) }
    }

    /**
     * 通过任务执行器停止任务
     */
    internal fun stopTask(taskId: Long) {
        mExecutor.executeInBackground { mProcessor.stopTask(taskId) }
    }

    /**
     * 交给分发器分发进入任务队列
     */
    internal fun enqueueTask(taskId: Long) {
        mTaskDao.getTaskWithId(taskId)?.let { mDispatcher.taskEnqueue(it) }
    }

    /**
     * 获取任务执行器
     */
    internal fun getProcessor(): TaskProcessor = mProcessor

    override fun getTaskExecutor(): TaskExecutor = mExecutor

    companion object {
        private const val TAG = "===WoodenOxImpl"

        // 通过App生命周期初始化的默认实例
        private var sDefaultInstance: WoodenOxImpl? = null

        // 手动初始化的实例
        private var sDelegatedInstance: WoodenOxImpl? = null

        /**
         * 获取任务管理器实例，优先获取测试用例
         */
        fun getInstance(): WoodenOxImpl? = synchronized(this) {
            sDelegatedInstance ?: sDefaultInstance
        }

        /**
         * 初始化任务管理器
         */
        fun initialize(context: Context) {
            LogP.d(TAG, "initialize()")
            synchronized(this) {
                if (sDelegatedInstance != null && sDefaultInstance != null) {
                    throw IllegalStateException("WoodenOx is already initialized")
                }
                if (sDelegatedInstance == null) {
                    if (sDefaultInstance == null) {
                        sDefaultInstance = WoodenOxImpl(context, Configuration.Builder(context).build())
                    }
                    sDelegatedInstance = sDefaultInstance
                }
            }
        }

        /**
         * 设置WoodenOxImpl测试实例
         */
        fun setDelegate(delegate: WoodenOxImpl?) {
            synchronized(this) {
                sDelegatedInstance = delegate
            }
        }
    }
}
