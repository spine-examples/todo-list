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
package org.spine3.examples.todolist.testdata;

import com.google.protobuf.Timestamp;
import org.spine3.examples.todolist.CompleteTask;
import org.spine3.examples.todolist.CreateBasicTask;
import org.spine3.examples.todolist.CreateDraft;
import org.spine3.examples.todolist.DeleteTask;
import org.spine3.examples.todolist.FinalizeDraft;
import org.spine3.examples.todolist.ReopenTask;
import org.spine3.examples.todolist.RestoreDeletedTask;
import org.spine3.examples.todolist.TaskId;
import org.spine3.examples.todolist.TaskPriority;
import org.spine3.examples.todolist.UpdateTaskDescription;
import org.spine3.examples.todolist.UpdateTaskDueDate;
import org.spine3.examples.todolist.UpdateTaskPriority;

public class TestTaskCommandFactory {

    private static final String ID = "1";
    private static final String DESCRIPTION = "Create command description.";
    private static final long DUE_DATE = System.currentTimeMillis();

    private TestTaskCommandFactory() {
        throw new UnsupportedOperationException();
    }

    public static CreateBasicTask createTaskInstance() {
        return CreateBasicTask.newBuilder()
                              .setDescription(DESCRIPTION)
                              .build();
    }

    public static UpdateTaskDescription updateTaskDescriptionInstance() {
        return updateTaskDescriptionInstance(DESCRIPTION);
    }

    public static UpdateTaskDescription updateTaskDescriptionInstance(String desciption) {
        return UpdateTaskDescription.newBuilder()
                                    .setUpdatedDescription(desciption)
                                    .build();
    }

    public static UpdateTaskDueDate updateTaskDueDateInstance() {
        return updateTaskDueDateInstance(DUE_DATE);
    }

    public static UpdateTaskDueDate updateTaskDueDateInstance(long updatedDueDate) {
        return UpdateTaskDueDate.newBuilder()
                                .setUpdatedDueDate(Timestamp.newBuilder()
                                                            .setSeconds(updatedDueDate))
                                .build();
    }

    public static UpdateTaskPriority updateTaskPriorityInstance() {
        return updateTaskPriorityInstance(TaskPriority.HIGH);
    }

    public static UpdateTaskPriority updateTaskPriorityInstance(TaskPriority priority) {
        return UpdateTaskPriority.newBuilder()
                                 .setUpdatedPriority(priority)
                                 .build();
    }

    public static CompleteTask completeTaskInstance() {
        return CompleteTask.newBuilder()
                           .build();
    }

    public static ReopenTask reopenTaskInstance() {
        return ReopenTask.newBuilder()
                         .build();
    }

    public static DeleteTask deleteTaskInstance() {
        return DeleteTask.newBuilder()
                         .setId(TaskId.newBuilder()
                                      .setValue(ID))
                         .build();
    }

    public static RestoreDeletedTask restoreDeletedTaskInstance() {
        return RestoreDeletedTask.newBuilder()
                                 .build();
    }

    public static FinalizeDraft finalizeDraftInstance() {
        return FinalizeDraft.newBuilder()
                            .build();
    }

    public static CreateDraft createDraftInstance() {
        return CreateDraft.newBuilder()
                          .build();
    }

}
