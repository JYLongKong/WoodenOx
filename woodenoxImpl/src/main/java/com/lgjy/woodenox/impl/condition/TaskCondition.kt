package com.lgjy.woodenox.impl.condition

/**
 * Created by LGJY on 2022/11/9.
 * Email：yujye@sina.com
 *
 * 任务执行条件
 *
 * 具体条件的次序对应所需条件的int位置
 * 例如WIFI在第三位，所以所需的条件int表示为
 * 1 shl TaskCondition.WIFI.ordinal
 * 即
 * ..00100
 */

enum class TaskCondition {
    NET,
    CELLULAR,
    WIFI,
    ETHERNET,
    GPS,
    BLUETOOTH,
    BLE,
    LOGIN,
}
