package com.lgjy.woodenox.impl.util

import com.lgjy.woodenox.db.Task

/**
 * Created by LGJY on 2022/6/22.
 * Email：yujye@sina.com
 *
 * 自定义校验数据
 * Check the correctness of the data
 */

object CheckData {

    /**
     * 统一检查AppId
     * Check the validity of the AppId
     */
    fun checkAppId(appId: String): Pair<Int, String>? {
        // TODO: checkAppId
        return null
    }

    /**
     * 统一检查TaskData
     * Check the correctness of the task data
     */
    fun checkTask(taskData: Task): Pair<Int, String>? {
        //        // TODO: 2021/2/4 检查appId
//        val contextData = ContextManager.taskContexts[taskData.context.contextId]
//            ?: return ERRCODE_UNKNOWN_CONTEXT_ID to (TaskApplication.instance.getString(R.string.err_unknown_context_id))
//
//        when (contextData) {
//            is HttpConfig -> {
//                taskData.data
//
//                val httpUrl = contextData.baseUrl.toHttpUrlOrNull()
//                    ?: return ERRCODE_INVALID_URL to TaskApplication.instance.getString(R.string.err_invalid_url)
//            }
//            is OSSConfig -> {
//                // TODO: 2021/2/4 校验OSSContext
//            }
//            is MarkConfig -> {
//                // TODO: 2021/2/4 校验MarkContext
//            }
//        }
        return null
    }
}
