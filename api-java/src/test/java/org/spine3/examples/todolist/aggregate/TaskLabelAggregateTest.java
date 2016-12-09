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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.spine3.base.CommandContext;
import org.spine3.examples.todolist.AssignLabelToTask;
import org.spine3.examples.todolist.CreateBasicLabel;
import org.spine3.examples.todolist.LabelAssignedToTask;
import org.spine3.examples.todolist.LabelColor;
import org.spine3.examples.todolist.LabelCreated;
import org.spine3.examples.todolist.LabelDetailsUpdated;
import org.spine3.examples.todolist.LabelRemovedFromTask;
import org.spine3.examples.todolist.RemoveLabelFromTask;
import org.spine3.examples.todolist.TaskLabel;
import org.spine3.examples.todolist.TaskLabelId;
import org.spine3.examples.todolist.UpdateLabelDetails;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.spine3.base.Identifiers.newUuid;
import static org.spine3.examples.todolist.testdata.TestCommandContextFactory.createCommandContext;
import static org.spine3.examples.todolist.testdata.TestTaskLabelCommandFactory.assignLabelToTaskInstance;
import static org.spine3.examples.todolist.testdata.TestTaskLabelCommandFactory.createLabelInstance;
import static org.spine3.examples.todolist.testdata.TestTaskLabelCommandFactory.removeLabelFromTaskInstance;
import static org.spine3.examples.todolist.testdata.TestTaskLabelCommandFactory.updateLabelDetailsInstance;

public class TaskLabelAggregateTest {

    private static final CommandContext COMMAND_CONTEXT = createCommandContext();
    private TaskLabelAggregate aggregate;
    private CreateBasicLabel createLabelCmd;
    private UpdateLabelDetails updateLabelDetailsCmd;
    private AssignLabelToTask assignLabelToTaskCmd;
    private RemoveLabelFromTask removeLabelFromTaskCmd;
    private static final TaskLabelId ID = TaskLabelId.newBuilder()
                                                     .setValue(newUuid())
                                                     .build();

    @BeforeEach
    public void setUp() throws Exception {
        aggregate = new TaskLabelAggregate(ID);
        createLabelCmd = createLabelInstance();
        updateLabelDetailsCmd = updateLabelDetailsInstance();
        assignLabelToTaskCmd = assignLabelToTaskInstance();
        removeLabelFromTaskCmd = removeLabelFromTaskInstance();
    }

    @Test
    public void handle_create_task_label_command() {
        final int expectedListSize = 1;

        List<? extends com.google.protobuf.Message> messageList = aggregate.dispatchForTest(createLabelCmd, COMMAND_CONTEXT);

        assertEquals(expectedListSize, messageList.size());
        assertEquals(LabelCreated.class, messageList.get(0)
                                                    .getClass());
    }

    @Test
    public void handle_update_label_details_command() {
        final int expectedListSize = 1;

        List<? extends com.google.protobuf.Message> messageList = aggregate.dispatchForTest(updateLabelDetailsCmd, COMMAND_CONTEXT);

        assertEquals(expectedListSize, messageList.size());
        assertEquals(LabelDetailsUpdated.class, messageList.get(0)
                                                           .getClass());
    }

    @Test
    public void handle_assign_label_to_task_command() {
        final int expectedListSize = 1;

        List<? extends com.google.protobuf.Message> messageList = aggregate.dispatchForTest(assignLabelToTaskCmd, COMMAND_CONTEXT);

        assertEquals(expectedListSize, messageList.size());
        assertEquals(LabelAssignedToTask.class, messageList.get(0)
                                                           .getClass());
    }

    @Test
    public void handle_remove_label_from_task_command() {
        final int expectedListSize = 1;

        List<? extends com.google.protobuf.Message> messageList = aggregate.dispatchForTest(removeLabelFromTaskCmd, COMMAND_CONTEXT);

        assertEquals(expectedListSize, messageList.size());
        assertEquals(LabelRemovedFromTask.class, messageList.get(0)
                                                            .getClass());
    }

    @Test
    public void return_current_state_when_label_is_created() {
        aggregate.dispatchForTest(createLabelCmd, COMMAND_CONTEXT);

        assertEquals(LabelColor.GRAY, aggregate.getState()
                                               .getColor());
    }

    @Test
    public void return_current_state_when_label_details_updated_two_times() {
        aggregate.dispatchForTest(updateLabelDetailsCmd, COMMAND_CONTEXT);
        TaskLabel state = aggregate.getState();

        assertEquals(LabelColor.GRAY, state.getColor());
        assertEquals("label title", state.getTitle());

        LabelColor updatedLabelColor = LabelColor.GREEN;
        String updatedTitle = "updated title";

        updateLabelDetailsCmd = updateLabelDetailsInstance(updatedLabelColor, updatedTitle);
        aggregate.dispatchForTest(updateLabelDetailsCmd, COMMAND_CONTEXT);
        state = aggregate.getState();

        assertEquals(updatedLabelColor, state.getColor());
        assertEquals(updatedTitle, state.getTitle());
    }

}
