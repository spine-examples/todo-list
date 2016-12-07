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

import org.spine3.examples.todolist.CompleteTask;
import org.spine3.examples.todolist.CreateBasicTask;
import org.spine3.examples.todolist.DeleteTask;
import org.spine3.examples.todolist.ReopenTask;
import org.spine3.examples.todolist.RestoreDeletedTask;
import org.spine3.examples.todolist.UpdateTaskDescription;
import org.spine3.examples.todolist.UpdateTaskDueDate;
import org.spine3.examples.todolist.UpdateTaskPriority;

public class TestTaskCommandFactory {

    private static final String DESCRIPTION = "Create command description.";

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

    public static UpdateTaskDescription updateTaskDescriptionInstance(String desciprion) {
        return UpdateTaskDescription.newBuilder()
                                    .setUpdatedDescription(desciprion)
                                    .build();
    }

    public static UpdateTaskDueDate updateTaskDueDateInstance() {
        return UpdateTaskDueDate.newBuilder()
                                .build();
    }

    public static UpdateTaskPriority updateTaskPriorityInstance() {
        return UpdateTaskPriority.newBuilder()
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

    public static DeleteTask deleteTaslInstance() {
        return DeleteTask.newBuilder()
                         .build();
    }

    public static RestoreDeletedTask restoreDeletedTaskInstance() {
        return RestoreDeletedTask.newBuilder()
                                 .build();
    }

}
