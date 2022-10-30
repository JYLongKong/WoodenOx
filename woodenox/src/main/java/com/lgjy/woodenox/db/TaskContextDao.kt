package com.lgjy.woodenox.db

import androidx.room.*

/**
 * Created by LGJY on 2022/6/24.
 * Email：yujye@sina.com
 *
 * 对任务执行上下文的数据库操作
 */

@Dao
interface TaskContextDao {

    @Query("SELECT * FROM task_contexts")
    suspend fun getTaskContexts(): List<TaskContext>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTaskContext(taskContext: TaskContext): Long

    @Delete
    fun deleteTaskContext(taskContext: TaskContext)
}
