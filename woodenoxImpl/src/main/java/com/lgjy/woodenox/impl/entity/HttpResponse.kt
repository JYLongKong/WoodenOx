package com.lgjy.woodenox.impl.entity

/**
 * Created by LGJY on 2022/11/9.
 * Email：yujye@sina.com
 */

data class HttpResponse<T>(
    val code: String?,
    val message: String = "",
    val data: T?,
    val success: Boolean = false,
    val content: T?
)
