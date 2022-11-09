package com.lgjy.woodenox.impl.condition

import android.content.Context
import com.lgjy.woodenox.condition.ConditionEnvListener
import com.lgjy.woodenox.condition.ConditionRegisterListener
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Created by LGJY on 2022/11/9.
 * Email：yujye@sina.com
 *
 * 基于高德地图的GPS管理器
 */

class GPSManager(
    private val context: Context,
    private val conditionEnvListener: ConditionEnvListener
) : ConditionRegisterListener {

    private var isGPSWork: AtomicBoolean = AtomicBoolean(false)
    private val gpsFlag: Int = 1 shl TaskCondition.GPS.ordinal

    override fun onRegisterCondition(registerFlag: Int): Boolean {
        val gpsFlag = 1 shl TaskCondition.GPS.ordinal
        if (registerFlag and gpsFlag != gpsFlag) {
            return false
        }

        startGPS()

        return true
    }

    override fun onUnregisterCondition(unregisterFlag: Int): Boolean {
        val gpsFlag = 1 shl TaskCondition.GPS.ordinal
        if (unregisterFlag and gpsFlag != gpsFlag) {
            return false
        }

        stopGPS()

        return true
    }

    /**
     * 开启GPS
     */
    private fun startGPS() {
        // TODO: startGPS
    }

    /**
     * 关闭GPS
     */
    private fun stopGPS() {
        // TODO: stopGPS
    }

    companion object {
        private const val TAG = "===GPSManager"
    }
}
