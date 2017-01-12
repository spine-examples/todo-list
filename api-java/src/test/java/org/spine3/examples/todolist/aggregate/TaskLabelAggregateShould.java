/*
 * Copyright 2016, TeamDev Ltd. All rights reserved.
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

package org.spine3.examples.todolist.aggregate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.spine3.base.CommandContext;
import org.spine3.change.ValueMismatch;
import org.spine3.examples.todolist.CannotUpdateLabelDetails;
import org.spine3.examples.todolist.CreateBasicLabel;
import org.spine3.examples.todolist.LabelColor;
import org.spine3.examples.todolist.LabelCreated;
import org.spine3.examples.todolist.LabelDetails;
import org.spine3.examples.todolist.LabelDetailsUpdated;
import org.spine3.examples.todolist.TaskLabel;
import org.spine3.examples.todolist.TaskLabelId;
import org.spine3.examples.todolist.UpdateLabelDetails;

import java.util.List;

import static com.google.protobuf.Any.pack;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.spine3.base.Identifiers.newUuid;
import static org.spine3.examples.todolist.testdata.TestCommandContextFactory.createCommandContext;
import static org.spine3.examples.todolist.testdata.TestTaskLabelCommandFactory.LABEL_ID;
import static org.spine3.examples.todolist.testdata.TestTaskLabelCommandFactory.LABEL_TITLE;
import static org.spine3.examples.todolist.testdata.TestTaskLabelCommandFactory.UPDATED_LABEL_TITLE;
import static org.spine3.examples.todolist.testdata.TestTaskLabelCommandFactory.createLabelInstance;
import static org.spine3.examples.todolist.testdata.TestTaskLabelCommandFactory.updateLabelDetailsInstance;
import static org.spine3.protobuf.AnyPacker.unpack;

/**
 * @author Illia Shepilov
 */
public class TaskLabelAggregateShould {

    private static final CommandContext COMMAND_CONTEXT = createCommandContext();
    private static final TaskLabelId ID = TaskLabelId.newBuilder()
                                                     .setValue(newUuid())
                                                     .build();
    private TaskLabelAggregate aggregate;

    @BeforeEach
    public void setUp() {
        aggregate = new TaskLabelAggregate(ID);
    }

    @Test
    public void emit_label_created_event_upon_create_task_label_command() {
        final CreateBasicLabel createLabelCmd = createLabelInstance();
        final List<? extends com.google.protobuf.Message> messageList =
                aggregate.dispatchForTest(createLabelCmd, COMMAND_CONTEXT);

        final int expectedListSize = 1;
        assertEquals(expectedListSize, messageList.size());
        assertEquals(LabelCreated.class, messageList.get(0)
                                                    .getClass());

        final LabelCreated labelCreated = (LabelCreated) messageList.get(0);

        assertEquals(LABEL_ID, labelCreated.getId());
        assertEquals(LABEL_TITLE, labelCreated.getDetails()
                                              .getTitle());
        assertEquals(LabelColor.GRAY, labelCreated.getDetails()
                                                  .getColor());
    }

    @Test
    public void emit_label_details_updated_event_upon_update_label_details_command() {
        final CreateBasicLabel createBasicLabel = createLabelInstance();
        aggregate.dispatchForTest(createBasicLabel, COMMAND_CONTEXT);

        final UpdateLabelDetails updateLabelDetailsCmd = updateLabelDetailsInstance();
        final List<? extends com.google.protobuf.Message> messageList =
                aggregate.dispatchForTest(updateLabelDetailsCmd, COMMAND_CONTEXT);

        final int expectedListSize = 1;
        assertEquals(expectedListSize, messageList.size());
        assertEquals(LabelDetailsUpdated.class, messageList.get(0)
                                                           .getClass());
        final LabelDetailsUpdated labelDetailsUpdated = (LabelDetailsUpdated) messageList.get(0);
        final LabelDetails details = labelDetailsUpdated.getLabelDetailsChange()
                                                        .getNewDetails();

        assertEquals(LABEL_ID, labelDetailsUpdated.getLabelId());
        assertEquals(LabelColor.GREEN, details.getColor());
        assertEquals(UPDATED_LABEL_TITLE, details.getTitle());
    }

    @Test
    public void emit_label_created_event_upon_create_label_command() {
        final CreateBasicLabel createLabelCmd = createLabelInstance();
        aggregate.dispatchForTest(createLabelCmd, COMMAND_CONTEXT);

        final TaskLabel state = aggregate.getState();

        assertEquals(LABEL_ID, state.getId());
        assertEquals(LabelColor.GRAY, state.getColor());
        assertEquals(LABEL_TITLE, state.getTitle());
    }

    @Test
    public void change_current_state_when_label_details_updated_two_times() {
        final CreateBasicLabel createBasicLabel = createLabelInstance();
        aggregate.dispatchForTest(createBasicLabel, COMMAND_CONTEXT);

        UpdateLabelDetails updateLabelDetailsCmd = updateLabelDetailsInstance();
        aggregate.dispatchForTest(updateLabelDetailsCmd, COMMAND_CONTEXT);

        TaskLabel state = aggregate.getState();

        assertEquals(LABEL_ID, state.getId());
        assertEquals(LabelColor.GREEN, state.getColor());
        assertEquals(UPDATED_LABEL_TITLE, state.getTitle());

        final LabelColor previousLabelColor = LabelColor.GREEN;
        final LabelDetails previousLabelDetails = LabelDetails.newBuilder()
                                                              .setTitle(UPDATED_LABEL_TITLE)
                                                              .setColor(previousLabelColor)
                                                              .build();

        final LabelColor updatedLabelColor = LabelColor.BLUE;
        final String updatedTitle = "updated title";
        final LabelDetails newLabelDetails = LabelDetails.newBuilder()
                                                         .setColor(updatedLabelColor)
                                                         .setTitle(updatedTitle)
                                                         .build();

        updateLabelDetailsCmd = updateLabelDetailsInstance(LABEL_ID, previousLabelDetails, newLabelDetails);
        aggregate.dispatchForTest(updateLabelDetailsCmd, COMMAND_CONTEXT);

        state = aggregate.getState();

        assertEquals(LABEL_ID, state.getId());
        assertEquals(updatedLabelColor, state.getColor());
        assertEquals(updatedTitle, state.getTitle());
    }

    @Test
    public void emit_cannot_update_label_details_failure_upon_update_label_details_command() {
        final CreateBasicLabel createBasicLabel = createLabelInstance();
        aggregate.dispatchForTest(createBasicLabel, COMMAND_CONTEXT);

        final LabelDetails expectedLabelDetails = LabelDetails.newBuilder()
                                                              .setColor(LabelColor.BLUE)
                                                              .setTitle(LABEL_TITLE)
                                                              .build();
        final LabelDetails newLabelDetails = LabelDetails.newBuilder()
                                                         .setColor(LabelColor.RED)
                                                         .setTitle(UPDATED_LABEL_TITLE)
                                                         .build();
        final UpdateLabelDetails updateLabelDetailsCmd =
                updateLabelDetailsInstance(LABEL_ID, expectedLabelDetails, newLabelDetails);
        final List<? extends com.google.protobuf.Message> messageList =
                aggregate.dispatchForTest(updateLabelDetailsCmd, COMMAND_CONTEXT);

        final int expectedListSize = 1;
        assertEquals(expectedListSize, messageList.size());
        assertEquals(CannotUpdateLabelDetails.class, messageList.get(0)
                                                                .getClass());
        final CannotUpdateLabelDetails cannotUpdateLabelDetails = (CannotUpdateLabelDetails) messageList.get(0);
        final ValueMismatch mismatch = cannotUpdateLabelDetails.getLabelDetailsMismatch();

        assertEquals(LABEL_ID, cannotUpdateLabelDetails.getLabelId());
        assertEquals(pack(expectedLabelDetails), mismatch.getExpected());
        assertEquals(pack(newLabelDetails), mismatch.getNewValue());

        final LabelDetails actualLabelDetails = LabelDetails.newBuilder()
                                                            .setColor(LabelColor.GRAY)
                                                            .setTitle(LABEL_TITLE)
                                                            .build();
        assertEquals(pack(actualLabelDetails), mismatch.getActual());
    }
}
