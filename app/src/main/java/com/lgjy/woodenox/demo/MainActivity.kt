package com.lgjy.woodenox.demo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.lgjy.woodenox.api.WoodenOx
import com.lgjy.woodenox.db.Task
import com.lgjy.woodenox.entity.ContextWrapper
import com.lgjy.woodenox.entity.TaskParameters
import com.lgjy.woodenox.entity.TaskState
import com.lgjy.woodenox.impl.context.HttpContextData
import com.lgjy.woodenox.impl.task.TestTask
import com.lgjy.woodenox.util.LogP
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.random.Random
import kotlin.system.measureTimeMillis

class MainActivity : AppCompatActivity() {

    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun init(view: View) {
        WoodenOx.initialize(view.context)
    }

    fun submitChain(view: View) {
        coroutineScope.launch {
            val random = Random.nextInt(2, 10)
            measureTimeMillis {
                var tempId: Long = 0L
                for (i in 1..random) {
                    val task = createTask().apply {
                        prevTaskId = tempId
                        nextTaskId = if (i == random) 0L else -1L
                    }
                    val testTask = TestTask(TaskParameters(task, WoodenOx.getInstance().getTaskExecutor()))
                    tempId = WoodenOx.getInstance().submit(testTask)
                }
            }.let { LogP.d(TAG, "submitChain × $random: take $it ms") }
        }
    }

    fun submitSingle(view: View) {
        coroutineScope.launch {
            val task = createTask().apply {
                prevTaskId = 0L
                nextTaskId = 0L
            }
            val testTask = TestTask(TaskParameters(task, WoodenOx.getInstance().getTaskExecutor()))
            WoodenOx.getInstance().submit(testTask)
        }
    }

    private fun createTask(): Task = Task(
        0L,
        -1L,
        -1L,
        -1L,
        "appId",
        0,
        ContextWrapper(1L, HttpContextData(1, "https://www.baidu.com")),
        "data",
        "tag101",
        System.currentTimeMillis(),
        "",
        "",
        "",
        TaskState.NONE
    )

    companion object {
        private const val TAG = "===MainActivity"
    }
}
