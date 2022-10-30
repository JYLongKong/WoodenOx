package com.lgjy.woodenox.db

import androidx.room.*
import com.lgjy.woodenox.entity.TaskState

/**
 * Created by LGJY on 2022/6/24.
 * Email：yujye@sina.com
 *
 * 对任务的数据库操作接口
 */

@Dao
interface TaskDao {

    /**
     * 通过任务id获取任务实体
     */
    @Query("SELECT * FROM tasks WHERE taskId = :taskId")
    fun getTaskWithId(taskId: Long): Task?

    /**
     * 通过任务id获取运行次数
     */
    @Query("SELECT attemptTimes FROM tasks WHERE taskId = :taskId")
    fun getAttemptTimes(taskId: Long): Int

    /**
     * 通过任务id更新任务状态
     */
    @Query("UPDATE tasks SET state=:state WHERE taskId IN (:taskIds)")
    fun setTaskState(state: TaskState, vararg taskIds: Long)

    /**
     * 更新任务的输出数据
     */
    @Query("UPDATE tasks SET output=:output WHERE taskId=:taskId")
    fun setTaskOutput(taskId: Long, output: String)

    /**
     * 更新任务的执行状态和输出数据
     */
    @Query("UPDATE tasks SET state=:state, output=:output, attemptTimes=attemptTimes+1 WHERE taskId=:taskId")
    fun setTaskResult(taskId: Long, state: TaskState, output: String)

    /**
     * 筛选某状态下的任务
     */
    @Query("SELECT * FROM tasks WHERE state=:state")
    suspend fun getTaskWithState(state: TaskState): List<Task>

    /**
     * 按标签筛选某状态下的任务
     */
    @Query("SELECT * FROM tasks WHERE appId=:appId AND state=:state AND tag=:tag")
    suspend fun getTaskWithTagAndState(appId: String, state: TaskState, tag: String): List<Task>

    /**
     * 筛选某状态下的头任务
     */
    @Query("SELECT * FROM tasks WHERE state=:state AND prevTaskId = 0")
    suspend fun getHeaderTaskWithState(state: TaskState): List<Task>

    /**
     * 通过多任务id查询相应任务
     */
    @Query("SELECT * FROM tasks WHERE taskId IN (:taskIds)")
    suspend fun getTasksWithIds(vararg taskIds: Long): List<Task>

    /**
     * 获取未完全结束的任务id
     */
    @Query("SELECT taskId FROM tasks WHERE state IN (2, 3, 5)")
    suspend fun setUnfinishedTaskIds(): Array<Long>

    /**
     * 查询ENQUEUED状态的头节点
     */
    @Query("SELECT * FROM tasks WHERE prevTaskId = 0 AND state=2")
    suspend fun getEnqueuedHeaders(): List<Task>

    /**
     * 将RUNNING和BLOCKED状态的任务置为ENQUEUED进行重新调度
     */
    @Query("UPDATE tasks SET state=2 WHERE state IN (3, 5)")
    suspend fun setUnfinishedTaskEnqueued()

    /**
     * 通过任务id更新后置任务id
     */
    @Query("UPDATE tasks SET prevTaskId=:nextTaskId WHERE taskId=:taskId")
    fun setTaskNextTaskId(taskId: Long, nextTaskId: Long)

    /**
     * 通过头任务id获取整条任务链
     */
    @Query("SELECT * FROM tasks WHERE headerTaskId=:headerTaskId")
    suspend fun getTaskWithHeader(headerTaskId: Long): List<Task>

    @Query("UPDATE tasks SET state=:state WHERE headerTaskId=(SELECT headerTaskId FROM tasks WHERE taskId=:taskId AND headerTaskId > 0)")
    fun setChainStateWithChildId(state: TaskState, taskId: Long)

    @Query("SELECT * FROM tasks WHERE appId = :appId AND tag = :tag ORDER BY submitTime DESC")
    suspend fun getTasks(appId: String, tag: String): List<Task>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTask(task: Task): Long

    @Query("SELECT state FROM tasks WHERE taskId=:taskId")
    fun getState(taskId: Long): TaskState

    @Delete
    fun deleteTask(task: Task)
}
