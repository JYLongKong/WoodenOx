package com.lgjy.woodenox.executor

import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.max
import kotlin.math.min

/**
 * Created by LGJY on 2022/6/22.
 * Email：yujye@sina.com
 *
 * 全局执行任务的Executor
 * Globally unique task executor
 */

class TaskExecutor {

    private val mainThreadHandler = Handler(Looper.getMainLooper())
    private val mainThreadExecutor = Executor { command -> mainThreadHandler.post(command) }

    // This value is the same as the core pool size for AsyncTask#THREAD_POOL_EXECUTOR.
    val backgroundExecutor: ExecutorService =
        Executors.newFixedThreadPool(max(2, min(Runtime.getRuntime().availableProcessors() - 1, 4)))
    private val coroutineScope = CoroutineScope(backgroundExecutor.asCoroutineDispatcher())

    /**
     * 协程运行
     * Running by coroutine
     */
    fun launch(block: suspend CoroutineScope.() -> Unit) {
        coroutineScope.launch(block = block)
    }

    /**
     * 运行在后台线程
     * Execute in background threads
     */
    fun executeInBackground(command: Runnable) {
        backgroundExecutor.execute(command)
    }

    fun executeInMainThread(command: Runnable) {
        mainThreadExecutor.execute(command)
    }
}
