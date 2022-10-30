package com.lgjy.woodenox.db

import android.content.ContextWrapper
import androidx.room.TypeConverter
import com.lgjy.woodenox.entity.ContextType
import com.lgjy.woodenox.entity.TaskState
import com.lgjy.woodenox.util.JsonConvertor
import com.lgjy.woodenox.util.LogP

/**
 * Created by LGJY on 2022/6/24.
 * Email：yujye@sina.com
 *
 * ROOM定义下的任务表的序列反序列化工具
 */

internal object TaskConvertor {

    private val TAG = "===".intern() + TaskConvertor::class.java.simpleName
    // 高于该位的内容决定Context的类型 todo
    private const val CONTEXT_TYPE_BIT: Int = 50
    private const val delimiter = '&'

    @JvmStatic
    @TypeConverter
    fun serializeTaskState(taskState: TaskState): Int {
        return taskState.ordinal
    }

    @JvmStatic
    @TypeConverter
    fun deserializeTaskState(stateOrdinal: Int): TaskState {
        return TaskState.values().getOrElse(stateOrdinal) { TaskState.NONE }
    }

    @JvmStatic
    @TypeConverter
    fun serializeContext(contextWrapper: ContextWrapper): String {
        with(contextWrapper) {
            val content = JsonConvertor.moshi.adapter(contextData.javaClass).toJson(contextData)
            return "${contextId}$delimiter$content"
        }
    }

    @JvmStatic
    @TypeConverter
    fun deserializeContext(contextJson: String): ContextWrapper? {
        kotlin.runCatching {
            val i = contextJson.indexOf(delimiter)
            val contextData = contextJson.substring(i + 1)
            val contextId = contextJson.substring(0, i).toLong()
            val contextType = (contextId shr CONTEXT_TYPE_BIT).toInt()
            return when (contextType) {
                ContextType.HTTP.ordinal -> JsonConvertor.fromString<HttpContextData>(contextData)
                ContextType.OSS.ordinal -> JsonConvertor.fromString<OSSContextData>(contextData)
                ContextType.MARK.ordinal -> JsonConvertor.fromString<MarkContextData>(contextData)
                else -> {
                    LogP.e(TAG, "fromJson error: contextType->$contextType")
                    null
                }
            }?.let {
                return ContextWrapper(contextId, it)
            }
        }.onFailure { LogP.e(TAG, "fromContextWrapperJson failed", it) }

        return null
    }
}
