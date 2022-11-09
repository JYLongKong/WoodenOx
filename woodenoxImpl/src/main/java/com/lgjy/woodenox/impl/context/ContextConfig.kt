package com.lgjy.woodenox.impl.context

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Created by LGJY on 2022/11/9.
 * Email：yujye@sina.com
 *
 * 上下文的配置
 */

sealed class ContextConfig

/**
 * 作HTTP请求的上下文(服务)
 */
@JsonClass(generateAdapter = true)
data class HttpConfig(
    @Json(name = "baseUrl") val baseUrl: String,
    @Json(name = "headers") val headers: Map<String, String>
) : ContextConfig()

/**
 * 作OSS上传的上下文(服务)
 */
@JsonClass(generateAdapter = true)
data class OSSConfig(
    @Json(name = "region") val region: String
) : ContextConfig()

/**
 * 作图片添加水印的上下文(服务)
 */
@JsonClass(generateAdapter = true)
data class MarkConfig(
    @Json(name = "orientation") val orientation: Int
) : ContextConfig()
