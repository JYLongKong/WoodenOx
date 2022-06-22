package com.lgjy.woodenox.entity

/**
 * Created by LGJY on 2022/6/22.
 * Email：yujye@sina.com
 *
 * 任务的状态
 * State of task
 */

enum class TaskState {
    /**
     * 任务不存在
     * The task doesn't exist
     */
    NONE,

    /**
     * 任务处于缓冲区
     * The task is in buffered area
     */
    BUFFERED,

    /**
     * 任务已经入队等待调度
     * The task has enqueued and waiting for dispatching
     */
    ENQUEUED,

    /**
     * 任务当前正在被执行
     * The task is running
     */
    RUNNING,

    /**
     * 任务执行成功
     * The task has executed succeed
     */
    SUCCEEDED,

    /**
     * 由于前置任务未完成而阻塞
     * The task is blocked because of prev task hasn't completed
     */
    BLOCKED,

    /**
     * 任务被取消
     * The task is cancelled
     */
    CANCELLED,

    /**
     * 任务由于失败超过一定次数，被标记为回收，不再进行重试
     * The task is marked for recycling due to failure for more than a certain number of times
     * and will not be retried.
     */
    RECYCLED
}
