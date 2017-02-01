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

package org.spine3.examples.todolist.c.aggregates.task.creation;

import com.google.protobuf.Message;
import org.spine3.examples.todolist.TaskCreation;
import org.spine3.examples.todolist.TaskDetails;
import org.spine3.examples.todolist.TaskId;
import org.spine3.examples.todolist.TaskStatus;
import org.spine3.examples.todolist.c.aggregates.TaskFlowValidator;
import org.spine3.examples.todolist.c.commands.CreateBasicTask;
import org.spine3.examples.todolist.c.commands.CreateDraft;
import org.spine3.examples.todolist.c.events.TaskCreated;
import org.spine3.examples.todolist.c.events.TaskDraftCreated;
import org.spine3.examples.todolist.c.failures.CannotCreateBasicTask;
import org.spine3.examples.todolist.c.failures.CannotCreateDraft;
import org.spine3.examples.todolist.c.failures.CannotCreateTaskWithInappropriateDescription;
import org.spine3.protobuf.Timestamps;
import org.spine3.server.aggregate.AggregatePart;
import org.spine3.server.aggregate.Apply;
import org.spine3.server.command.Assign;

import java.util.Collections;
import java.util.List;

import static org.spine3.examples.todolist.c.aggregates.FailureHelper.throwCannotCreateDraftFailure;
import static org.spine3.examples.todolist.c.aggregates.FailureHelper.throwCannotCreateTaskWithInappropriateDescription;

/**
 * @author Illia Shepilov
 */
public class TaskCreationPart extends AggregatePart<TaskId, TaskCreation, TaskCreation.Builder> {

    private static final int MIN_DESCRIPTION_LENGTH = 3;

    /**
     * {@inheritDoc}
     *
     * @param id
     */
    protected TaskCreationPart(TaskId id) {
        super(id);
    }

    @Assign
    List<? extends Message> handle(CreateBasicTask cmd)
            throws CannotCreateBasicTask, CannotCreateTaskWithInappropriateDescription {
        validateCommand(cmd);
        final TaskId taskId = cmd.getId();

        final TaskDetails.Builder taskDetails = TaskDetails.newBuilder()
                                                           .setDescription(cmd.getDescription());
        final TaskCreated result = TaskCreated.newBuilder()
                                              .setId(taskId)
                                              .setDetails(taskDetails)
                                              .build();
        return Collections.singletonList(result);
    }

    @Assign
    List<? extends Message> handle(CreateDraft cmd) throws CannotCreateDraft {
        final TaskId taskId = cmd.getId();
        final boolean isValid = TaskFlowValidator.isValidCreateDraftCommand(getState().getTaskStatus());

        if (!isValid) {
            throwCannotCreateDraftFailure(taskId);
        }

        final TaskDraftCreated draftCreated = TaskDraftCreated.newBuilder()
                                                              .setId(taskId)
                                                              .setDraftCreationTime(Timestamps.getCurrentTime())
                                                              .build();
        final List<TaskDraftCreated> result = Collections.singletonList(draftCreated);
        return result;
    }

    @Apply
    private void taskCreated(TaskCreated event) {
        final TaskDetails taskDetails = event.getDetails();
        getBuilder().setTaskId(event.getId())
                    .setDescription(taskDetails.getDescription())
                    .setTaskStatus(TaskStatus.FINALIZED);
    }

    @Apply
    private void draftCreated(TaskDraftCreated event) {
        getBuilder().setTaskId(event.getId())
                    .setDescription(event.getDetails()
                                         .getDescription())
                    .setTaskStatus(TaskStatus.DRAFT);
    }

    private static void validateCommand(CreateBasicTask cmd)
            throws CannotCreateBasicTask, CannotCreateTaskWithInappropriateDescription {
        final String description = cmd.getDescription();
        if (description != null && description.length() < MIN_DESCRIPTION_LENGTH) {
            final TaskId taskId = cmd.getId();
            throwCannotCreateTaskWithInappropriateDescription(taskId);
        }
    }
}
