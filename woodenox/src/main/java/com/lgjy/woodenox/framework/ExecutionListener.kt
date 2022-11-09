package com.lgjy.woodenox.framework

import com.lgjy.woodenox.entity.TaskState

/**
 * Created by LGJY on 2022/11/9.
 * Email：yujye@sina.com
 *
 * 任务执行结束的监听器
 */

interface ExecutionListener {

 /**
  * 当任务执行结束时调用
  *
  * @param endedState 以何种任务状态结束
  */
 fun onExecuted(taskId: Long, endedState: TaskState)
}
