/*
 * Copyright 2018, TeamDev. All rights reserved.
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
import io.spine.examples.todolist.R
import io.spine.examples.todolist.Task
import io.spine.examples.todolist.TaskId
import io.spine.examples.todolist.TaskLabel
import io.spine.examples.todolist.model.toDetails
import io.spine.examples.todolist.view.AbstractActivity
import io.spine.examples.todolist.view.TimeFormatter
import io.spine.examples.todolist.view.newtask.ReadonlyLabelsAdapter
import kotlinx.android.synthetic.main.task_details.*

/**
 * The `Activity` displaying a single [task][Task] to the user.
 *
 * @author Dmytro Dashenkov
 * @see TaskDetailsViewModel
 */
class TaskDetailsActivity : AbstractActivity<TaskDetailsViewModel>() {

    /**
     * The companion object of the `TaskDetailsActivity`.
     *
     * The object helps to start the activity with the required parameters.
     */
    companion object {

        /**
         * The name of the Task ID param passed to the activity on creation.
         */
        private const val TASK_ID_KEY = "task_id"

        /**
         * Opens the `TaskDetailsActivity` for showing the [Task] with the given ID.
         *
         * @param context to open the activity from
         * @param taskId  the ID of the [Task] to display
         */
        fun open(context: Context, taskId: TaskId) {
            val intent = Intent(context, TaskDetailsActivity::class.java)
            val idValue = taskId.value
            intent.putExtra(TASK_ID_KEY, idValue)
            context.startActivity(intent)
        }

        /**
         * The ID of the [Task] displayed on this activity.
         *
         * This value is passed to the `activity` on creation.
         *
         * This property is defined in the companion object to make sure the [TaskDetailsActivity]
         * never works with the passed arguments on the low level (i.e. on the level of `Intent`s).
         */
        private val TaskDetailsActivity.targetId: TaskId
            get() {
                val openIntent = this.intent
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

        val taskId = targetId
        model().fetchTask(taskId, this::updateTask)
        model().fetchLabels(taskId, this::updateLabels)
    }

    /**
     * Updates the activity UI with the data from the given [Task].
     */
    private fun updateTask(task: Task?) {
        if (task !== null) {
            taskDescription.text = task.description.value
            taskPriority.text = task.priority.toString()
            bindDueDate(taskDueDate, task.dueDate)
        }
    }

    /**
     * Binds the given due date [Timestamp] to the given view.
     */
    private fun bindDueDate(target: TextView, dueDate: Timestamp) {
        if (dueDate.seconds > 0) {
            val formattedDate = TimeFormatter.format(dueDate)
            val template = resources.getString(R.string.due_date)
            target.text = String.format(template, formattedDate)
        }
    }

    /**
     * Updates the list of the task labels with the given [labels][TaskLabel].
     */
    private fun updateLabels(labels: Collection<TaskLabel>) {
        val labelDetails = labels.map(TaskLabel::toDetails)
        val adapter = ReadonlyLabelsAdapter(labelDetails)
        taskLabels.adapter = adapter
    }
}
