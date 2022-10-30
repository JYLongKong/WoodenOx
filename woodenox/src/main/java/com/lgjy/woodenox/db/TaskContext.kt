package com.lgjy.woodenox.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.lgjy.woodenox.entity.ContextConfig

/**
 * Created by LGJY on 2022/6/23.
 * Email：yujye@sina.com
 *
 * 任务上下文表
 */

@Entity(tableName = "task_contexts")
data class TaskContext(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "contextId") val contextId: Long,
    @ColumnInfo(name = "contextType") val contextType: Int,
    @ColumnInfo(name = "contextConfig") val contextConfig: ContextConfig
)
