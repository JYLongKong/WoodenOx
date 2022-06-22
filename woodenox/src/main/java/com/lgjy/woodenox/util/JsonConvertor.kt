package com.lgjy.woodenox.util

import com.squareup.moshi.Moshi

/**
 * Created by LGJY on 2022/6/22.
 * Email：yujye@sina.com
 *
 * Json转换器
 * Json convertor
 */

internal object JsonConvertor {

    val moshi: Moshi = Moshi.Builder().build()

    inline fun <reified T> fromString(string: String): T? {
        return moshi.adapter(T::class.java).fromJson(string)
    }

    inline fun <reified T> toString(t: T): String {
        return moshi.adapter(T::class.java).toJson(t)
    }
}
