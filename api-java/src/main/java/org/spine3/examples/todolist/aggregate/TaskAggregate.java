//
// Copyright 2016, TeamDev Ltd. All rights reserved.
//
// Redistribution and use in source and/or binary forms, with or without
// modification, must retain the above copyright notice and the following
// disclaimer.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
// "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
// LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
// A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
// OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
// SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
// LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
// DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
// THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
// (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
// OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
//
package org.spine3.examples.todolist.aggregate;

import com.google.protobuf.Timestamp;
import org.spine3.examples.todolist.CompleteTask;
import org.spine3.examples.todolist.CreateBasicTask;
import org.spine3.examples.todolist.CreateDraft;
import org.spine3.examples.todolist.DeleteTask;
import org.spine3.examples.todolist.DeletedTaskRestored;
import org.spine3.examples.todolist.FinalizeDraft;
import org.spine3.examples.todolist.ReopenTask;
import org.spine3.examples.todolist.RestoreDeletedTask;
import org.spine3.examples.todolist.Task;
import org.spine3.examples.todolist.TaskCompleted;
import org.spine3.examples.todolist.TaskCreated;
import org.spine3.examples.todolist.TaskDeleted;
import org.spine3.examples.todolist.TaskDescriptionUpdated;
import org.spine3.examples.todolist.TaskDetails;
import org.spine3.examples.todolist.TaskDraftCreated;
import org.spine3.examples.todolist.TaskDraftFinalized;
import org.spine3.examples.todolist.TaskDueDateUpdated;
import org.spine3.examples.todolist.TaskId;
import org.spine3.examples.todolist.TaskPriority;
import org.spine3.examples.todolist.TaskPriorityUpdated;
import org.spine3.examples.todolist.TaskReopened;
import org.spine3.examples.todolist.UpdateTaskDescription;
import org.spine3.examples.todolist.UpdateTaskDueDate;
import org.spine3.examples.todolist.UpdateTaskPriority;
import org.spine3.server.aggregate.Aggregate;
import org.spine3.server.aggregate.Apply;
import org.spine3.server.command.Assign;

import static com.google.common.base.Preconditions.checkNotNull;

public class TaskAggregate extends Aggregate<TaskId, Task, Task.Builder> {

    /**
     * Creates a new aggregate instance.
     *
     * @param id the ID for the new aggregate
     * @throws IllegalArgumentException if the ID is not of one of the supported types
     */
    public TaskAggregate(TaskId id) {
        super(id);
    }

    //TODO: Need clarification TaskID, TaskDetails
    @Assign
    public TaskCreated handle(CreateBasicTask cmd) {
        final TaskCreated result = TaskCreated.newBuilder()
                                              .setDetails(TaskDetails.newBuilder()
                                                                     .setDescription(cmd.getDescription()))
                                              .build();
        return result;
    }

    @Assign
    public TaskDescriptionUpdated handle(UpdateTaskDescription cmd) {
        validateCommand(cmd);
        final String previousDescription = getState().getDescription();
        final TaskDescriptionUpdated result = TaskDescriptionUpdated.newBuilder()
                                                                    .setId(cmd.getId())
                                                                    .setPreviousDescription(previousDescription)
                                                                    .setNewDescription(cmd.getUpdatedDescription())
                                                                    .build();
        return result;
    }

    @Assign
    public TaskDueDateUpdated handle(UpdateTaskDueDate cmd) {
        validateCommand(cmd);
        final Timestamp previousDueDate = getState().getDueDate();
        final TaskDueDateUpdated result = TaskDueDateUpdated.newBuilder()
                                                            .setId(cmd.getId())
                                                            .setPreviousDueDate(previousDueDate)
                                                            .setNewDueDate(cmd.getUpdatedDueDate())
                                                            .build();
        return result;
    }

    @Assign
    public TaskPriorityUpdated handle(UpdateTaskPriority cmd) {
        validateCommand(cmd);
        final TaskPriority previousPriority = getState().getPriority();
        final TaskPriorityUpdated result = TaskPriorityUpdated.newBuilder()
                                                              .setId(cmd.getId())
                                                              .setPreviousPriority(previousPriority)
                                                              .setNewPriority(cmd.getUpdatedPriority())
                                                              .build();
        return result;
    }

    @Assign
    public TaskReopened handle(ReopenTask cmd) {
        validateCommand(cmd);
        final TaskReopened result = TaskReopened.newBuilder()
                                                .setId(cmd.getId())
                                                .build();
        return result;
    }

    @Assign
    public TaskDeleted handle(DeleteTask cmd) {
        validateCommand(cmd);
        final TaskDeleted result = TaskDeleted.newBuilder()
                                              .setId(cmd.getId())
                                              .build();
        return result;
    }

    @Assign
    public DeletedTaskRestored handle(RestoreDeletedTask cmd) {
        validateCommand(cmd);
        final DeletedTaskRestored result = DeletedTaskRestored.newBuilder()
                                                              .setId(cmd.getId())
                                                              .build();
        return result;
    }

    @Assign
    public TaskCompleted handle(CompleteTask cmd) {
        validateCommand(cmd);
        final TaskCompleted result = TaskCompleted.newBuilder()
                                                  .setId(cmd.getId())
                                                  .build();
        return result;
    }

    @Assign
    public TaskDraftFinalized handle(FinalizeDraft cmd) {
        validateCommand(cmd);
        final TaskDraftFinalized result = TaskDraftFinalized.newBuilder()
                                                            .setId(cmd.getId())
                                                            .build();
        return result;
    }

    //TODO: should to be updated after defining draft creation
    @Assign
    public TaskDraftCreated handle(CreateDraft cmd) {
        final TaskDraftCreated result = TaskDraftCreated.newBuilder()
                                                        .build();
        return result;
    }

    @Apply
    public void event(TaskCreated event) {
        getBuilder().setId(event.getId())
                    .setCreated(Timestamp.newBuilder()
                                         .setSeconds(System.currentTimeMillis()))
                    .setPriority(TaskPriority.NORMAL);
    }

    @Apply
    public void event(TaskDescriptionUpdated event) {
        getBuilder().setId(event.getId())
                    .setDescription(event.getNewDescription());
    }

    @Apply
    public void event(TaskDueDateUpdated event) {
        getBuilder().setId(event.getId())
                    .setDueDate(event.getNewDueDate());
    }

    @Apply
    public void event(TaskPriorityUpdated event) {
        getBuilder().setId(event.getId())
                    .setPriority(event.getNewPriority());
    }

    @Apply
    public void event(TaskReopened event) {
        getBuilder().setId(event.getId());
    }

    @Apply
    public void event(TaskDeleted event) {
        getBuilder().setId(event.getId());
    }

    @Apply
    public void event(DeletedTaskRestored event) {
        getBuilder().setId(event.getId());
    }

    @Apply
    public void event(TaskCompleted event) {
        getBuilder().setId(event.getId());
    }

    @Apply
    public void event(TaskDetails event) {
        getBuilder().setPriority(event.getPriority())
                    .setDescription(event.getDescription())
                    .setCompleted(event.getCompleted());
    }

    //TODO: Need to refactor command validation

    private void validateCommand(UpdateTaskDescription cmd) {
        Task taskState = getState();
        String description = cmd.getUpdatedDescription();

        checkNotNull(description, "Description cannot be null.");

        if (description.length() < 3) {
            throw new IllegalArgumentException("Description should contains at least 3 alphanumeric symbols.");
        }

        validateCommandOnDeletionAndCompletion();
    }

    private void validateCommandOnCompletion() {
        if (getState().getCompleted()) {
            throw new IllegalArgumentException("Command cannot be applied on completed task.");
        }
    }

    private void validateCommandOnDeletion() {
        if (getState().getDeleted()) {
            throw new IllegalArgumentException("Command cannot be applied on deleted task.");
        }
    }

    private void validateCommand(RestoreDeletedTask cmd) {
        if (!getState().getDeleted()) {
            throw new IllegalArgumentException("Command cannot be applied on not deleted task.");
        }
    }

    private void validateCommand(DeleteTask cmd) {
        validateCommandOnDeletion();
    }

    private void validateCommandOnDeletionAndCompletion() {
        validateCommandOnDeletion();
        validateCommandOnCompletion();
    }

    private void validateCommand(UpdateTaskDueDate cmd) {
        validateCommandOnDeletion();
        validateCommandOnCompletion();
    }

    private void validateCommand(UpdateTaskPriority cmd) {
        validateCommandOnDeletion();
        validateCommandOnCompletion();
    }

    private void validateCommand(CompleteTask cmd) {
        validateCommandOnDeletion();

        if (getState().getDraft()) {
            throw new IllegalArgumentException("Command cannot be applied on task in draft state.");
        }
    }

    private void validateCommand(ReopenTask cmd) {
        if (!getState().getCompleted()) {
            throw new IllegalArgumentException("Command cannot be applied on not completed task.");
        }
    }

    //TODO: Need clarification
    private void validateCommand(FinalizeDraft cmd) {
        if (!getState().getDraft()) {
            throw new IllegalArgumentException("Command cannot be applied to deleted draft.");
        }
    }

}
