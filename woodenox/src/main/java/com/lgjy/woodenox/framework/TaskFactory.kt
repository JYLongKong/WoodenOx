package com.lgjy.woodenox.framework

import com.lgjy.woodenox.entity.TaskParameters
import com.lgjy.woodenox.framework.task.ListenableTask
import com.lgjy.woodenox.util.LogP

/**
 * Created by LGJY on 2022/6/24.
 * Email：yujye@sina.com
 * 
 * 任务工厂抽象类
 * 可以通过实行该抽象类自定义创建可执行任务的方式
 */

abstract class TaskFactory {

    /**
     * 实现它创建自定义任务
     */
    abstract fun createTask(parameters: TaskParameters): ListenableTask?

    /**
     * 默认初始化(没有主动设置自定义TaskFactory到TaskManagerImpl)后调用该函数去创建具体的可执行任务
     * 默认初始化的createTask创建为null，然后通过反射创建具体任务，达到在子模块通过继承实现自定义任务
     *
     * 由于具体的任务在子模块中继承ListenableTask或CoroutineTask，在本模块无法知晓具体的任务实现，
     * 所以通过task.className字段来创建具体任务类
     */
    fun createWorkerWithDefaultFallback(parameters: TaskParameters): ListenableTask? {
        var listenableTask = createTask(parameters)
        if (listenableTask == null) {
            kotlin.runCatching {
                val clz = Class.forName(parameters.task.className).asSubclass(ListenableTask::class.java)
                val constructor = clz.getDeclaredConstructor(TaskParameters::class.java)
                listenableTask = constructor.newInstance(parameters)
            }.onFailure { LogP.e(TAG, "createWorkerWithDefaultFallback(${parameters.task.className}) error", it) }
        }
        return listenableTask
    }

    companion object {
        private const val TAG = "===TaskFactory"

        /**
         * 默认初始化TaskManager时创建的任务工厂
         * 默认创建失败, 靠task中的className字段反射创建具体的任务，详情见
         * @see createWorkerWithDefaultFallback
         */
        fun getDefaultTaskFactory(): TaskFactory = object : TaskFactory() {
            override fun createTask(parameters: TaskParameters): ListenableTask? {
                return null
            }
        }
    }
}
