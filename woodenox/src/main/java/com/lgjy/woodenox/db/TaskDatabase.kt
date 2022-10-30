package com.lgjy.woodenox.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

/**
 * Created by LGJY on 2022/6/24.
 * Email：yujye@sina.com
 *
 * The Room database for Task
 */

@Database(entities = [Task::class, TaskContext::class], version = 1, exportSchema = false)
@TypeConverters(TaskConvertor::class, TaskContextConvertor::class)
abstract class TaskDatabase : RoomDatabase() {

    abstract fun TaskDao(): TaskDao
    abstract fun TaskContextDao(): TaskContextDao

    companion object {

        private const val DATABASE_NAME = "tasks-db"

        @Volatile
        private var instance: TaskDatabase? = null

        fun getInstance(context: Context): TaskDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context): TaskDatabase {
            return Room.databaseBuilder(context, TaskDatabase::class.java, DATABASE_NAME)
                .fallbackToDestructiveMigration()   // 当未匹配到版本的时候就会直接删除表然后重新创建
                .build()
        }
    }
}
