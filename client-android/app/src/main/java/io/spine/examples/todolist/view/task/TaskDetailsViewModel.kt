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

import android.util.Log
import io.spine.examples.todolist.Task
import io.spine.examples.todolist.TaskId
import io.spine.examples.todolist.TaskLabel
import io.spine.examples.todolist.view.AbstractViewModel

/**
 * The [view model][android.arch.lifecycle.ViewModel] of the [TaskDetailsActivity].
 *
 * @author Dmytro Dashenkov
 */
class TaskDetailsViewModel : AbstractViewModel() {

    /**
     * Fetches the task with the given ID.
     *
     * @param id       ID of the task to fetch
     * @param callback the callback to which the resulting [Task] is passed; the callback argument
     *                 is `null` iff the task with the given ID is not found
     */
    fun fetchTask(id: TaskId, callback: (Task?) -> Unit) {
        execute {
            val task = client().getTaskOr(id, null)
            inMainThread {
                callback(task)
            }
        }
    }

    /**
     * Fetches the labels of the task with the given ID.
     *
     * If the task does not exist or has no labels, this method results in an empty collection.
     *
     * @param id       ID of the task to find the labels for
     * @param callback the callback to which the resulting labels are passed
     */
    fun fetchLabels(id: TaskId, callback: (Collection<TaskLabel>) -> Unit) {
        execute {
            val taskLabels = client().getLabels(id)
                                     .labelIdsList
                                     .idsList
            val result = ArrayList<TaskLabel>(taskLabels.size)
            taskLabels.forEach {
                val label = client().getLabelOr(it, null)
                if (label !== null) {
                    result.add(label)
                } else {
                    Log.e(TaskDetailsViewModel::class.java.name,
                          "Unable to fetch label with ID $id")
                }
                inMainThread {
                    callback(result)
                }
            }
        }
    }
}
