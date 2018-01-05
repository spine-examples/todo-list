/*
 * Copyright 2018, TeamDev Ltd. All rights reserved.
 *
 * Redistribution and use in source and/or binary forms, with or without
 * modification, must retain the above copyright notice and the following
 * disclaimer.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package io.spine.examples.todolist.view.task

import android.content.Context
import android.content.Intent
import android.support.v7.widget.GridLayoutManager
import android.widget.TextView
import com.google.protobuf.Timestamp
import com.google.protobuf.util.Timestamps.toMillis
import io.spine.examples.todolist.R
import io.spine.examples.todolist.Task
import io.spine.examples.todolist.TaskId
import io.spine.examples.todolist.TaskLabel
import io.spine.examples.todolist.model.Labels
import io.spine.examples.todolist.view.AbstractActivity
import io.spine.examples.todolist.view.newtask.ReadonlyLabelsAdapter
import kotlinx.android.synthetic.main.task_details.*
import java.text.SimpleDateFormat
import java.util.*

class TaskDetailsActivity : AbstractActivity<TaskDetailsViewModel>() {

    companion object {

        private const val TASK_ID_KEY = "task_id"
        private val FORMAT = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

        fun open(context: Context, taskId: TaskId) {
            val intent = Intent(context, TaskDetailsActivity::class.java)
            val idValue = taskId.value
            intent.putExtra(TASK_ID_KEY, idValue)
            context.startActivity(intent)
        }

        private fun getTargetId(activity: TaskDetailsActivity): TaskId {
            val openIntent = activity.intent
            val taskIdValue = openIntent.getStringExtra(TASK_ID_KEY)
            val result = TaskId.newBuilder()
                               .setValue(taskIdValue)
                               .build()
            return result
        }
    }

    override fun getContentViewResource() = R.layout.activity_task_details

    override fun getViewModelClass() = TaskDetailsViewModel::class.java

    override fun initializeView() {
        val columnCount = resources.getInteger(R.integer.label_list_column_count)
        taskLabels.layoutManager = GridLayoutManager(applicationContext, columnCount)

        val taskId = getTargetId(this)
        model().fetchTask(taskId, this::updateTask)
        model().fetchLabels(taskId, this::updateLabels)
    }

    private fun updateTask(task: Task?) {
        if (task !== null) {
            taskDescription.text = task.description.value
            taskPriority.text = task.priority.toString()
            bindTimestamp(taskDueDate, task.dueDate)
        }
    }

    private fun bindTimestamp(target: TextView, timestamp: Timestamp) {
        if (timestamp.seconds > 0) {
            val date = Date(toMillis(timestamp))
            val formattedDate = FORMAT.format(date)
            val dueDate = String.format(resources.getString(R.string.due_date), formattedDate)
            target.text = dueDate
        }
    }

    private fun updateLabels(labels: Collection<TaskLabel>) {
        val labelDetails = labels.map(Labels::packDetails)
        val adapter = ReadonlyLabelsAdapter(labelDetails)
        taskLabels.adapter = adapter
    }
}


