package com.lgjy.woodenox.impl.context

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Created by LGJY on 2022/11/9.
 * Email：yujye@sina.com
 *
 * 上下文参数
 */

internal const val CONTEXT_ID: String = "contextId"
internal const val CONTEXT_DATA: String = "contextData"

@JsonClass(generateAdapter = true)
data class ContextWrapper(
    @Json(name = CONTEXT_ID) val contextId: Long,    // 高位添加了context类型信息，与TaskContext.contextId不等同
    @Json(name = CONTEXT_DATA) val contextData: ContextData
)

sealed class ContextData

@JsonClass(generateAdapter = true)
data class HttpContextData(
    @Json(name = "httpMethod") val httpMethod: Int,
    @Json(name = "url") val url: String
) : ContextData()

@JsonClass(generateAdapter = true)
data class OSSContextData(
    @Json(name = "targetDir") val targetDir: String
) : ContextData()

@JsonClass(generateAdapter = true)
data class MarkContextData(
    @Json(name = "orientation") val orientation: Int,
    @Json(name = "imagePath") val imagePath: String,
    @Json(name = "content") val content: Map<String, String>
) : ContextData()
