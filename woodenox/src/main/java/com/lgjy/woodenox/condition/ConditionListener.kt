package com.lgjy.woodenox.condition

/**
 * Created by LGJY on 2021/3/1.
 * Email：yujye@sina.com
 *
 * 外部条件变化监听器
 */

interface EnvConditionListener {

    /**
     * 外部环境条件变化时的回调
     *
     * @param envCond 新的环境条件
     */
    fun onEnvConditionChanged(envCond: Int)
}

/**
 * 内部需要条件变化监听器
 */
interface RequiredConditionListener{

    /**
     * 内部需要执行条件变化时的回调
     *
     * @param oldRequiredCond 老的需要条件
     * @param newRequiredCond 新的需要条件
     */
    fun onRequiredConditionChanged(oldRequiredCond: Int, newRequiredCond: Int)
}

/**
 * 环境中单个任务条件可用/不可用变化回调
 */
interface ConditionEnvListener {

    /**
     * 当前环境该条件可用了的回调
     */
    fun onConditionAvailable(availableFlag: Int)

    /**
     * 当前环境该条件不可用了的回调
     */
    fun onConditionUnavailable(unavailableFlag: Int)
}

/**
 * 主动注册/注销某个环境条件监听
 */
interface ConditionRegisterListener {

    /**
     * 单次注册某个环境条件的变化监听器
     * @param registerFlag 注册条件的标志位
     * @return 是否注册条件成功
     */
    fun onRegisterCondition(registerFlag: Int): Boolean

    /**
     * 单次注销某个环境条件的变化监听器
     * @param unregisterFlag 注销条件的标志位
     * @return 是否注册条件成功
     */
    fun onUnregisterCondition(unregisterFlag: Int): Boolean
}
