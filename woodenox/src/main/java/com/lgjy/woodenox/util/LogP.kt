package com.lgjy.woodenox.util

import android.util.Log

/**
 * Created by LGJY on 2022/6/22.
 * Email：yujye@sina.com
 *
 * 日志打印代理类
 * Proxy of logging
 */

object LogP {

    fun d(tag: String, msg: String) {
        Log.d(tag, msg)
    }

    fun i(tag: String, msg: String) {
        Log.i(tag, msg)
    }

    fun e(tag: String, msg: String, throwable: Throwable? = null) {
        Log.e(tag, msg, throwable)
    }
}
