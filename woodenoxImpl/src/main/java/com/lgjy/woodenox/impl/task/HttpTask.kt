package com.lgjy.woodenox.impl.task

import com.lgjy.woodenox.entity.TaskParameters
import com.lgjy.woodenox.entity.TaskResult
import com.lgjy.woodenox.framework.task.CoroutineTask

/**
 * Created by LGJY on 2022/11/9.
 * Email：yujye@sina.com
 */

class HttpTask(taskParameters: TaskParameters) : CoroutineTask(taskParameters) {

    override suspend fun doTask(): TaskResult {
//        kotlin.runCatching {
//            // 检查网络上下文资源
//            val contextId = taskData.context.contextId
//            val httpClient = ContextManager.okHttpClients[contextId]
//                ?: throw NullPointerException("不存在id为${contextId}的httpClient")
//
//            // 检查对应的网络上下文数据
//            val httpData = taskData.context.contextData as? HttpContextData
//                ?: throw IllegalArgumentException("HTTP参数错误")
//
//            // 执行网络请求
//            val resp = httpClient.newCall(Request.Builder().url(httpData.url).build()).execute()
//
//            // 检查返回HTTP响应
//            if (!resp.isSuccessful) {
//                throw IllegalArgumentException("HTTP Response code: ${resp.code}")
//            }
//
//            // 检查HTTP返回结果
//            val httpResp = resp.body?.string()?.let {
//                JsonConvertor.fromString<HttpResponse<String>>(it)
//            }
//            if (httpResp?.success != true) {
//                throw IllegalArgumentException("HTTP Response failed!")
//            }
//
//            httpResp
//        }.onFailure {
//            taskData.attemptTimes++
//            LogP.e(TAG, "onHandle: ", it)
//            return TaskResult.Failure.Exception(it)
//        }.onSuccess {
//            LogP.d(TAG, "onHandle: onSuccess()")
//            TaskDatabase.getInstance(TaskApplication.instance).runInTransaction {
//                // TODO: 2021/2/7 执行成功后的操作
//            }
//        }
        return TaskResult.Success("")
    }

    companion object {
        private const val TAG = "===HttpTask"
    }
}
