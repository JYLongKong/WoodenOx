package com.lgjy.woodenox.impl.condition

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.ArrayMap
import com.lgjy.woodenox.condition.ConditionEnvListener
import com.lgjy.woodenox.condition.ConditionRegisterListener
import com.lgjy.woodenox.util.LogP

/**
 * Created by LGJY on 2022/11/9.
 * Email：yujye@sina.com
 *
 * 网络状态监听管理器
 * 默认注册蜂窝数据、Wifi、以太网、蓝牙
 * 网络状态改变回调时筛选正在监听的部分状态
 */

class NetworkManager(
    private val context: Context,
    private val conditionEnvListener: ConditionEnvListener
) : ConditionRegisterListener {

    // 网络状态监听系统管理器
    private var connectivityManager: ConnectivityManager? = null

    // 处于观察中的条件(其中可能包括NET)
    private var observeConditions = ArrayMap<Int, TaskCondition>()

    // 当前正在监听的网络状态能力->是否正常
    private val capabilityAvailability = ArrayMap<Int, Boolean>().apply {
        networkCapabilities.forEach { put(it, false) }
    }

    // 是否有网络
    private var hasInternet: Boolean = false

    // 所能检测到的所有网络状态能力
    private val netConditions = (1 shl TaskCondition.NET.ordinal) or
            (1 shl TaskCondition.CELLULAR.ordinal) or
            (1 shl TaskCondition.WIFI.ordinal) or
            (1 shl TaskCondition.ETHERNET.ordinal) or
            (1 shl TaskCondition.BLUETOOTH.ordinal) or
            (1 shl TaskCondition.BLE.ordinal)

    private val networkCallback: ConnectivityManager.NetworkCallback by lazy {
        object : ConnectivityManager.NetworkCallback() {
            override fun onLost(network: Network) {
                super.onLost(network)
                LogP.d(TAG, "onLost()")
            }

            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                LogP.d(TAG, "onAvailable()")
            }

            override fun onUnavailable() {
                super.onUnavailable()
                LogP.d(TAG, "onUnavailable()")
                observeConditions[NetworkCapabilities.NET_CAPABILITY_INTERNET]?.let {
                    if (hasInternet) {
                        hasInternet = false
                        conditionEnvListener.onConditionUnavailable(1 shl TaskCondition.NET.ordinal)
                    }
                }

            }

            /**
             * Called when the network corresponding to this request changes capabilities but still
             * satisfies the requested criteria.
             */
            override fun onCapabilitiesChanged(net: Network, capabilities: NetworkCapabilities) {
                super.onCapabilitiesChanged(net, capabilities)
                LogP.d(TAG, "onCapabilitiesChanged()")
                if (capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
                    // 如果有观察NET条件 如果Internet能力变化 则进行相应条件变化回调
                    observeConditions[NetworkCapabilities.NET_CAPABILITY_INTERNET]?.let {
                        if (capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) != hasInternet) {
                            if (hasInternet) {
                                conditionEnvListener.onConditionUnavailable(1 shl TaskCondition.NET.ordinal)
                            } else {
                                conditionEnvListener.onConditionAvailable(1 shl TaskCondition.NET.ordinal)
                            }
                            hasInternet = !hasInternet
                        }
                    }

                    // 观察具体条件的变化回调具体条件的可用/不可用
                    for ((capability, availability) in capabilityAvailability) {
                        if (capabilities.hasTransport(capability) != availability) {   // 能力发生变化
                            // 更新该能力可用性
                            capabilityAvailability[capability] = !availability
                            // 假如属于观察条件则通知更新
                            observeConditions[capability]?.let {
                                if (availability) {
                                    conditionEnvListener.onConditionUnavailable(1 shl it.ordinal)
                                } else {
                                    conditionEnvListener.onConditionAvailable(1 shl it.ordinal)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onRegisterCondition(registerFlag: Int): Boolean {
        if (netConditions and registerFlag == 0) {
            return false
        }

        flagToCondition(registerFlag)?.let {
            observeConditions.put(it.first, it.second)
        }
        registerNetworkListener()

        return true
    }

    override fun onUnregisterCondition(unregisterFlag: Int): Boolean {
        if (netConditions and unregisterFlag == 0) {
            return false
        }

        flagToCondition(unregisterFlag)?.let {
            observeConditions.remove(it.first)
        }

        return true
    }

    /**
     * 注册蜂窝数据、Wifi、以太网、蓝牙的状态改变监听回调
     */
    private fun registerNetworkListener() {
        if (connectivityManager == null) {
            connectivityManager =
                (context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager)?.apply {
                    val networkRequest = NetworkRequest.Builder()
                        .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                        .apply { networkCapabilities.forEach { addTransportType(it) } }
                        .build()
                    registerNetworkCallback(networkRequest, networkCallback)
                }
        }
    }

    /**
     * 标志位转化成任务执行条件
     */
    private fun flagToCondition(flag: Int): Pair<Int, TaskCondition>? = when {
        flag and (1 shl TaskCondition.NET.ordinal) != 0 -> NetworkCapabilities.NET_CAPABILITY_INTERNET to TaskCondition.NET
        flag and (1 shl TaskCondition.CELLULAR.ordinal) != 0 -> NetworkCapabilities.TRANSPORT_CELLULAR to TaskCondition.CELLULAR
        flag and (1 shl TaskCondition.WIFI.ordinal) != 0 -> NetworkCapabilities.TRANSPORT_WIFI to TaskCondition.WIFI
        flag and (1 shl TaskCondition.BLUETOOTH.ordinal) != 0 -> NetworkCapabilities.TRANSPORT_BLUETOOTH to TaskCondition.BLUETOOTH
        flag and (1 shl TaskCondition.BLE.ordinal) != 0 -> NetworkCapabilities.TRANSPORT_BLUETOOTH to TaskCondition.BLE
        else -> {
            LogP.e(TAG, "flagToConditin error: flag->$flag")
            null
        }
    }

    companion object {
        private const val TAG = "===NetworkManager"
        private val networkCapabilities = arrayOf(
            NetworkCapabilities.TRANSPORT_CELLULAR, // 蜂窝数据
            NetworkCapabilities.TRANSPORT_WIFI,     // Wifi
            NetworkCapabilities.TRANSPORT_ETHERNET, // 以太网
            NetworkCapabilities.TRANSPORT_BLUETOOTH // 蓝牙/BLE
        )
    }
}
