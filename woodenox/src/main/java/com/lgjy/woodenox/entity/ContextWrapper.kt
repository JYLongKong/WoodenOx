package com.lgjy.woodenox.entity

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Created by LGJY on 2022/11/10.
 * Email：yujye@sina.com
 */

internal const val CONTEXT_ID: String = "contextId"
internal const val CONTEXT_DATA: String = "contextData"

interface ContextData

@JsonClass(generateAdapter = true)
data class ContextWrapper(
    @Json(name = CONTEXT_ID) val contextId: Long,    // 高位添加了context类型信息，与TaskContext.contextId不等同
    @Json(name = CONTEXT_DATA) val contextData: ContextData
)
