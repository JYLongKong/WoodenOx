package com.lgjy.woodenox.entity

/**
 * Created by LGJY on 2022/6/22.
 * Email：yujye@sina.com
 *
 * 任务执行结果
 * Sealed class of task execution result
 */

sealed class TaskResult(val output :String) {

    data class Success(val out :String) : TaskResult(out)

    sealed class Failure(out: String): TaskResult(out) {

        data class Error(val o :String): TaskResult.Failure(o)

        data class Exception(val e: Throwable, val o :String): TaskResult.Failure(o)
    }
}
