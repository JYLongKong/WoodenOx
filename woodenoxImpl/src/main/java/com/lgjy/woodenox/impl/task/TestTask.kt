package com.lgjy.woodenox.impl.task

import com.lgjy.woodenox.entity.TaskParameters
import com.lgjy.woodenox.entity.TaskResult
import com.lgjy.woodenox.framework.task.CoroutineTask
import com.lgjy.woodenox.util.LogP

/**
 * Created by LGJY on 2022/11/9.
 * Email：yujye@sina.com
 *
 * 用于测试的模拟耗时任务
 */

class TestTask(taskParameters: TaskParameters) : CoroutineTask(taskParameters) {

    override suspend fun doTask(): TaskResult {
        LogP.d(TAG, "doTask(): taskId->${taskParamters.task.taskId}")
        Thread.sleep(2000L)
        return TaskResult.Success("我是TestTask，看到这个说明执行成功")
    }

    companion object {
        private const val TAG = "===TestTask"
    }
}