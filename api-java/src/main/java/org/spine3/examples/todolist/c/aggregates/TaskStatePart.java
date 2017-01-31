/*
 * Copyright 2017, TeamDev Ltd. All rights reserved.
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

package org.spine3.examples.todolist.c.aggregates;

import com.google.protobuf.Message;
import org.spine3.examples.todolist.TaskId;
import org.spine3.examples.todolist.TaskLabelId;
import org.spine3.examples.todolist.TaskState;
import org.spine3.examples.todolist.TaskStatus;
import org.spine3.examples.todolist.c.commands.CompleteTask;
import org.spine3.examples.todolist.c.commands.DeleteTask;
import org.spine3.examples.todolist.c.commands.ReopenTask;
import org.spine3.examples.todolist.c.commands.RestoreDeletedTask;
import org.spine3.examples.todolist.c.events.DeletedTaskRestored;
import org.spine3.examples.todolist.c.events.LabelledTaskRestored;
import org.spine3.examples.todolist.c.events.TaskCompleted;
import org.spine3.examples.todolist.c.events.TaskDeleted;
import org.spine3.examples.todolist.c.events.TaskReopened;
import org.spine3.examples.todolist.c.failures.CannotCompleteTask;
import org.spine3.examples.todolist.c.failures.CannotDeleteTask;
import org.spine3.examples.todolist.c.failures.CannotReopenTask;
import org.spine3.examples.todolist.c.failures.CannotRestoreDeletedTask;
import org.spine3.server.aggregate.AggregatePart;
import org.spine3.server.aggregate.Apply;
import org.spine3.server.command.Assign;

import java.util.Collections;
import java.util.List;

import static com.google.common.collect.Lists.newLinkedList;
import static org.spine3.examples.todolist.c.aggregates.AggregateHelper.generateExceptionMessage;
import static org.spine3.examples.todolist.c.aggregates.FailureHelper.throwCannotCompleteTaskFailure;
import static org.spine3.examples.todolist.c.aggregates.FailureHelper.throwCannotDeleteTaskFailure;
import static org.spine3.examples.todolist.c.aggregates.FailureHelper.throwCannotReopenTaskFailure;
import static org.spine3.examples.todolist.c.aggregates.FailureHelper.throwCannotRestoreDeletedTaskFailure;

/**
 * @author Illia Shepilov
 */
public class TaskStatePart extends AggregatePart<TaskId, TaskState, TaskState.Builder> {

    /**
     * {@inheritDoc}
     *
     * @param id
     */
    protected TaskStatePart(TaskId id) {
        super(id);
    }

    @Assign
    List<? extends Message> handle(ReopenTask cmd) throws CannotReopenTask {
        final TaskState state = getState();
        final TaskStatus currentStatus = state.getTaskStatus();
        final TaskStatus newStatus = TaskStatus.OPEN;
        final boolean isValid = TaskFlowValidator.isValidTransition(currentStatus, newStatus);
        final TaskId taskId = cmd.getId();

        if (!isValid) {
            final String message = generateExceptionMessage(currentStatus, newStatus);
            throwCannotReopenTaskFailure(taskId, message);
        }

        final TaskReopened taskReopened = TaskReopened.newBuilder()
                                                      .setTaskId(taskId)
                                                      .build();
        final List<TaskReopened> result = Collections.singletonList(taskReopened);
        return result;
    }

    @Assign
    List<? extends Message> handle(DeleteTask cmd) throws CannotDeleteTask {
        final TaskState state = getState();
        final TaskStatus currentStatus = state.getTaskStatus();
        final TaskStatus newStatus = TaskStatus.DELETED;
        final TaskId taskId = cmd.getId();
        final boolean isValid = TaskFlowValidator.isValidTransition(currentStatus, newStatus);

        if (!isValid) {
            final String message = generateExceptionMessage(currentStatus, newStatus);
            throwCannotDeleteTaskFailure(taskId, message);
        }

        final TaskDeleted taskDeleted = TaskDeleted.newBuilder()
                                                   .setTaskId(taskId)
                                                   .build();
        final List<TaskDeleted> result = Collections.singletonList(taskDeleted);
        return result;
    }

    @Assign
    List<? extends Message> handle(CompleteTask cmd) throws CannotCompleteTask {
        final TaskState state = getState();
        final TaskStatus currentStatus = state.getTaskStatus();
        final TaskStatus newStatus = TaskStatus.COMPLETED;
        final TaskId taskId = cmd.getId();
        final boolean isValid = TaskFlowValidator.isValidTransition(currentStatus, newStatus);

        if (!isValid) {
            final String message = generateExceptionMessage(currentStatus, newStatus);
            throwCannotCompleteTaskFailure(taskId, message);
        }

        final TaskCompleted taskCompleted = TaskCompleted.newBuilder()
                                                         .setTaskId(taskId)
                                                         .build();
        final List<TaskCompleted> result = Collections.singletonList(taskCompleted);
        return result;
    }

    @Apply
    private void taskReopened(TaskReopened event) {
        getBuilder().setTaskStatus(TaskStatus.OPEN);
    }

    @Apply
    private void taskDeleted(TaskDeleted event) {
        getBuilder().setTaskStatus(TaskStatus.DELETED);
    }

    @Apply
    private void deletedTaskRestored(DeletedTaskRestored event) {
        getBuilder().setTaskStatus(TaskStatus.OPEN);
    }

    @Apply
    private void labelledTaskRestored(LabelledTaskRestored event) {
        getBuilder().setTaskStatus(TaskStatus.OPEN);
    }

    @Apply
    private void taskCompleted(TaskCompleted event) {
        getBuilder().setTaskStatus(TaskStatus.COMPLETED);
    }

}
