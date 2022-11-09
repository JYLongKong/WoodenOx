package com.lgjy.woodenox.impl.init

import android.content.Context
import com.lgjy.woodenox.condition.ConditionManager
import com.lgjy.woodenox.impl.condition.GPSManager
import com.lgjy.woodenox.impl.condition.NetworkManager

/**
 * Created by LGJY on 2022/11/9.
 * Email：yujye@sina.com
 *
 * taskImpl主要的初始化器
 * 注：由于父模块通过反射调用初始化过程，该类名和位置不可修改
 * 保持com.lgjy.woodenox.impl.init.Initializer
 */

object Initializer {

    /**
     * Configuration.initChildModule()
     * 初始化配置时通过反射调用该方法将具体的条件管理器添加进监听条件集合中
     */
    @JvmStatic
    fun initConditionManager(context: Context, conditionManager: ConditionManager) {
        conditionManager.apply {
            addConditionManager(NetworkManager(context, this))
            addConditionManager(GPSManager(context, this))
        }
    }
}
