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

package io.spine.examples.todolist.q.projection;

import com.google.protobuf.Timestamp;
import io.spine.examples.todolist.TaskDescription;
import io.spine.examples.todolist.TaskId;
import io.spine.examples.todolist.TaskPriority;
import io.spine.examples.todolist.c.events.TaskCompleted;
import io.spine.examples.todolist.c.events.TaskDescriptionUpdated;
import io.spine.examples.todolist.c.events.TaskDueDateUpdated;
import io.spine.examples.todolist.c.events.TaskPriorityUpdated;
import io.spine.examples.todolist.c.events.TaskReopened;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static io.spine.examples.todolist.q.projection.TaskItem.newBuilder;

/**
 * A utility for working with views.
 *
 * @author Illia Shepilov
 */
final class Projections {

    /**
     * The {@code private} constructor prevents the utility class instantiation.
     */
    private Projections() {}

    /**
     * Removes the matching {@linkplain TaskItem task items}
     * from the specified list by the task ID.
     *
     * @param tasks the list of the {@link TaskItem}
     * @param id    the task ID of the task view
     * @return {@link TaskListView} without deleted tasks
     */
    static TaskListView removeViewsByTaskId(List<TaskItem> tasks, TaskId id) {
        final List<TaskItem> tasksToRemove = tasks.stream()
                                                  .filter(t -> t.getId()
                                                                .equals(id))
                                                  .collect(Collectors.toList());
        return removeTasks(tasks, tasksToRemove);
    }


    private static TaskListView removeTasks(List<TaskItem> source, List<TaskItem> tasksToRemove) {
        source.removeAll(tasksToRemove);
        return newTaskListView(source);
    }

    /**
     * Marks the matching {@link TaskItem} as uncompleted according to the event data.
     *
     * @param tasks the list of the {@link TaskItem}
     * @param event {@link TaskReopened} instance
     * @return the list of the {@link TaskItem}
     */
    static List<TaskItem> updateTaskItemList(List<TaskItem> tasks, TaskReopened event) {
        final TaskId targetTaskId = event.getTaskId();

        final TaskTransformation updateFn = builder -> builder.setCompleted(false);
        return transformWithUpdate(tasks, targetTaskId, updateFn);
    }

    /**
     * Marks the matching {@link TaskItem} as completed according to the event data.
     *
     * @param tasks the list of the {@link TaskItem}
     * @param event {@link TaskCompleted} instance
     * @return the list of the {@link TaskItem}
     */
    static List<TaskItem> updateTaskItemList(List<TaskItem> tasks, TaskCompleted event) {
        final TaskId targetTaskId = event.getTaskId();

        final TaskTransformation updateFn = builder -> builder.setCompleted(true);
        return transformWithUpdate(tasks, targetTaskId, updateFn);
    }

    /**
     * Updates task due date of the matching {@link TaskItem} according to the event data.
     *
     * @param tasks the list of the {@link TaskItem}
     * @param event {@link TaskDueDateUpdated} instance
     * @return the list of the {@link TaskItem} with updated task due date
     */
    static List<TaskItem> updateTaskItemList(List<TaskItem> tasks, TaskDueDateUpdated event) {
        final TaskId targetTaskId = event.getTaskId();

        final TaskTransformation updateFn = builder -> {
            final Timestamp newDueDate = event.getDueDateChange()
                                              .getNewValue();
            return builder.setDueDate(newDueDate);
        };
        return transformWithUpdate(tasks, targetTaskId, updateFn);
    }

    /**
     * Updates the task priority of the matching {@link TaskItem} according to the event data.
     *
     * @param tasks the list of the {@link TaskItem}
     * @param event {@link TaskPriorityUpdated} instance
     * @return the list of the {@link TaskItem} with updated task priority
     */
    static List<TaskItem> updateTaskItemList(List<TaskItem> tasks, TaskPriorityUpdated event) {
        final TaskId targetTaskId = event.getTaskId();

        final TaskTransformation updateFn = builder -> {
            final TaskPriority newPriority = event.getPriorityChange()
                                                  .getNewValue();
            return builder.setPriority(newPriority);
        };
        return transformWithUpdate(tasks, targetTaskId, updateFn);
    }

    /**
     * Updates the task description of the matching {@link TaskItem} according to the event data.
     *
     * @param tasks the list of the {@link TaskItem}
     * @param event {@link TaskDescriptionUpdated} instance
     * @return the list of the {@link TaskItem} with updated task description
     */
    static List<TaskItem> updateTaskItemList(List<TaskItem> tasks, TaskDescriptionUpdated event) {
        final TaskId targetTaskId = event.getTaskId();

        final TaskTransformation updateFn = builder -> {
            final String newDescriptionValue = event.getDescriptionChange()
                                                    .getNewValue();
            final TaskDescription newDescription = TaskDescription.newBuilder()
                                                                  .setValue(newDescriptionValue)
                                                                  .build();
            return builder.setDescription(newDescription);
        };
        return transformWithUpdate(tasks, targetTaskId, updateFn);
    }

    @SuppressWarnings("MethodWithMultipleLoops") // It's fine, as there aren't a
                                                 // lot of transformations per task.
    private static List<TaskItem> transformWithUpdate(List<TaskItem> tasks,
                                                      TaskId targetTaskId,
                                                      TaskTransformation transformation) {
        final int listSize = tasks.size();
        final List<TaskItem> updatedList = new ArrayList<>(listSize);
        for (TaskItem task : tasks) {
            TaskItem addedView = task;
            final boolean willUpdate = task.getId()
                                           .equals(targetTaskId);
            if (willUpdate) {
                TaskItem.Builder resultBuilder = newBuilder();

                resultBuilder = resultBuilder.mergeFrom(task);
                resultBuilder = transformation.apply(resultBuilder);

                addedView = resultBuilder.build();
            }
            updatedList.add(addedView);
        }
        return updatedList;
    }

    static TaskListView newTaskListView(List<TaskItem> tasks) {
        return TaskListView.newBuilder()
                           .addAllItems(tasks)
                           .build();
    }

    /**
     * A common interface for the {@link TaskItem} transformations.
     */
    private interface TaskTransformation extends Function<TaskItem.Builder, TaskItem.Builder> {
    }
}
