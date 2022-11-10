package com.lgjy.woodenox.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.lgjy.woodenox.entity.ContextWrapper
import com.lgjy.woodenox.entity.TaskState

/**
 * Created by LGJY on 2022/6/22.
 * Email：yujye@sina.com
 *
 * 任务实体
 * The table of task
 */

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "taskId") var taskId: Long = 0L,
    @ColumnInfo(name = "headerTaskId") var headerTaskId: Long,
    @ColumnInfo(name = "prevTaskId") var prevTaskId: Long,
    @ColumnInfo(name = "nextTaskId") var nextTaskId: Long,
    @ColumnInfo(name = "appId") val appId: String,
    @ColumnInfo(name = "condition") val condition: Int,
    @ColumnInfo(name = "context") val context: ContextWrapper?,
    @ColumnInfo(name = "data") var data: String,
    @ColumnInfo(name = "tag") val tag: String,
    @ColumnInfo(name = "submitTime") val submitTime: Long,
    @ColumnInfo(name = "output") val output: String,
    @ColumnInfo(name = "className") var className: String,
    @ColumnInfo(name = "expectations") val expectations: String,
    @ColumnInfo(name = "state") var state: TaskState,
    @ColumnInfo(name = "priority") var priority: Int = 0,
    @ColumnInfo(name = "attemptTimes") var attemptTimes: Int = 0,
)
