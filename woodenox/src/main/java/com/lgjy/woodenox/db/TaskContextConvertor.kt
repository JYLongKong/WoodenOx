package com.lgjy.woodenox.db

import androidx.room.TypeConverter
import com.lgjy.woodenox.entity.ContextConfig
import com.lgjy.woodenox.util.JsonConvertor
import com.lgjy.woodenox.util.LogP

/**
 * Created by LGJY on 2022/6/23.
 * Email：yujye@sina.com
 *
 * ROOM定义下的任务上下文表的序列反序列化工具
 */

internal object TaskContextConvertor {

    private val TAG = "===".intern() + TaskContextConvertor::class.java.simpleName
    private const val delimiter = '&'

    @JvmStatic
    @TypeConverter
    fun serializeContextConfig(contextConfig: ContextConfig): String {
        val tag = contextConfig::class.java.simpleName
        return "$tag$delimiter${JsonConvertor.toString(contextConfig)}"
    }

    @JvmStatic
    @TypeConverter
    fun deserializeContextConfig(contextConfigJson: String): ContextConfig? {
        kotlin.runCatching {
            val i = contextConfigJson.indexOf(delimiter)
            val configJson = contextConfigJson.substring(i + 1)
            return when (val clzName = contextConfigJson.substring(0, i)) {
                HttpConfig::class.java.simpleName -> JsonConvertor.fromString<HttpConfig>(configJson)
                OSSConfig::class.java.simpleName -> JsonConvertor.fromString<OSSConfig>(configJson)
                MarkConfig::class.java.simpleName -> JsonConvertor.fromString<MarkConfig>(configJson)
                else -> {
                    LogP.e(TAG, "fromContextConfigJson: $clzName not format any ContextConfig")
                    null
                }
            }
        }.onFailure { LogP.e(TAG, "fromContextConfigJson failed", it) }

        return null
    }
}
