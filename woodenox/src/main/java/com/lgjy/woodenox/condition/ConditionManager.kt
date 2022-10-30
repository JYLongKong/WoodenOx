package com.lgjy.woodenox.condition

import com.lgjy.woodenox.db.Task
import com.lgjy.woodenox.util.LogP

/**
 * Created by LGJY on 2021/3/22.
 * Email：yujye@sina.com
 *
 * 任务条件管理器
 * 1.实现需求条件改变和环境条件改变接口
 * 2.设置环境条件改变的监听器
 * 3.实现任务条件满足的判断
 */

class ConditionManager private constructor() : RequiredConditionListener, ConditionEnvListener {

    /**
     * 外部设置的环境条件改变的监听器
     */
    var outerEnvConditionListener: EnvConditionListener? = null

    // 当前环境的任务条件
    private var mAvailbleCondition: Int = 0
    private val mLock = Any()

    // 注册的控制/监听环境变化影响任务条件的管理器
    private val mConditionRegisterListeners = arrayListOf<ConditionRegisterListener>()

    override fun onRequiredConditionChanged(oldRequiredCond: Int, newRequiredCond: Int) {
        val diff = oldRequiredCond xor newRequiredCond
        if (diff > 0) {
            val registerBits = newRequiredCond and diff
            val diffCount = diff.countOneBits()    // 统计有几位发生变化
            var diffTemp = 0
            for (index in 0 until Int.SIZE_BITS) {  // 避免无限循环
                val conditionFlag = 1 shl index
                if (diff and conditionFlag == conditionFlag) {  // 该条件发生变化
                    if (registerBits and conditionFlag == conditionFlag) {  // 注册
                        registerCondition(conditionFlag)
                    } else {    // 注销
                        unregisterCondition(conditionFlag)
                    }
                    diffTemp++
                    if (diffTemp >= diffCount) {
                        return
                    }
                }
            }
        }
    }

    override fun onConditionAvailable(availableFlag: Int) {
        LogP.d(TAG, "onConditionAvailable($availableFlag)")
        synchronized(mLock) {
            mAvailbleCondition = mAvailbleCondition or availableFlag
        }
        outerEnvConditionListener?.onEnvConditionChanged(mAvailbleCondition)
    }

    override fun onConditionUnavailable(unavailableFlag: Int) {
        LogP.d(TAG, "onConditionUnavailable($unavailableFlag)")
        synchronized(mLock) {
            mAvailbleCondition = mAvailbleCondition and unavailableFlag.inv()
        }
        outerEnvConditionListener?.onEnvConditionChanged(mAvailbleCondition)
    }

    /**
     * 当前环境是否满足该任务条件
     */
    fun isMetCondition(task: Task): Boolean = task.condition.let { mAvailbleCondition and it == it }

    /**
     * 添加环境控制条件管理器
     */
    fun addConditionManager(listener: ConditionRegisterListener) {
        mConditionRegisterListeners.add(listener)
    }

    /**
     * 注册某任务条件监听
     */
    private fun registerCondition(registerFlag: Int) {
        mConditionRegisterListeners.firstOrNull { it.onRegisterCondition(registerFlag) }
            ?: LogP.e(TAG, "registerCondition: register $registerFlag failed!")
    }

    /**
     * 注销某任务条件监听
     */
    private fun unregisterCondition(unregisterFlag: Int) {
        mConditionRegisterListeners.firstOrNull { it.onUnregisterCondition(unregisterFlag) }
            ?: LogP.e(TAG, "unregisterCondition: unregister $unregisterFlag failed!")
    }

    companion object {
        private const val TAG = "===ConditionManager"

        /**
         * 创建默认的条件管理器
         */
        fun createDefaultConditionManager(): ConditionManager {
            return ConditionManager()
        }
    }
}
