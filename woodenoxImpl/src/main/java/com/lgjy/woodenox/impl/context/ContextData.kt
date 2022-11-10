package com.lgjy.woodenox.impl.context

import com.lgjy.woodenox.entity.ContextData
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Created by LGJY on 2022/11/9.
 * Email：yujye@sina.com
 *
 * 上下文参数
 */

@JsonClass(generateAdapter = true)
data class HttpContextData(
    @Json(name = "httpMethod") val httpMethod: Int,
    @Json(name = "url") val url: String
) : ContextData

@JsonClass(generateAdapter = true)
data class OSSContextData(
    @Json(name = "targetDir") val targetDir: String
) : ContextData

@JsonClass(generateAdapter = true)
data class MarkContextData(
    @Json(name = "orientation") val orientation: Int,
    @Json(name = "imagePath") val imagePath: String,
    @Json(name = "content") val content: Map<String, String>
) : ContextData
