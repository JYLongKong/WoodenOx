package com.lgjy.woodenox.config

import android.content.Context
import com.lgjy.woodenox.condition.ConditionManager
import com.lgjy.woodenox.framework.TaskFactory
import com.lgjy.woodenox.util.LogP

/**
 * Created by LGJY on 2022/10/30.
 * Email：yujye@sina.com
 *
 * 木牛流马配置中心
 */

// 重试次数超过该值任务状态则设为RECYCLED
const val THRESHOLD_RECYCLED_TIMES = 10
const val THRESHOLD_DOZE_TIMES = 3

const val TIMEOUT_CONNECT: Long = 8_000L
const val TIMEOUT_CALL: Long = 10_000L

class Configuration private constructor(builder: Builder) {

    val taskFactory: TaskFactory = builder.mTaskFactory ?: TaskFactory.getDefaultTaskFactory()
    val conditionManager: ConditionManager =
        builder.mConditionManager ?: ConditionManager.createDefaultConditionManager()

    init {
        initChildModule(builder.context)
    }

    /**
     * 通过反射调用初始化子模块Initializer的初始化过程
     */
    private fun initChildModule(context: Context) {
        kotlin.runCatching {
            val initializer = Class.forName("com.alibaba.ib.task.taskimpl.init.Initializer")
            val initConditionManager = initializer.getDeclaredMethod(
                "initConditionManager", Context::class.java, ConditionManager::class.java
            )
            initConditionManager.isAccessible = true
            initConditionManager.invoke(null, context, conditionManager)
        }.onFailure {
            LogP.e(
                TAG, "initChildModule error, check com.alibaba.ib.task.taskimpl.Initializer", it
            )
        }
    }

    companion object {
        private const val TAG = "===Configuration"
    }

    class Builder(val context: Context) {

        internal var mTaskFactory: TaskFactory? = null
        internal var mConditionManager: ConditionManager? = null

        fun taskFactory(taskFactory: TaskFactory): Builder {
            mTaskFactory = taskFactory
            return this
        }

        fun conditionManager(conditionManager: ConditionManager): Builder {
            mConditionManager = conditionManager
            return this
        }

        fun build(): Configuration {
            return Configuration(this)
        }
    }
}
