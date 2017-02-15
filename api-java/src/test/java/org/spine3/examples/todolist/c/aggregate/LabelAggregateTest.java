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

package org.spine3.examples.todolist.c.aggregate;

import com.google.common.base.Throwables;
import com.google.protobuf.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.spine3.base.CommandContext;
import org.spine3.change.ValueMismatch;
import org.spine3.examples.todolist.LabelColor;
import org.spine3.examples.todolist.LabelDetails;
import org.spine3.examples.todolist.LabelDetailsUpdateFailed;
import org.spine3.examples.todolist.LabelId;
import org.spine3.examples.todolist.TaskLabel;
import org.spine3.examples.todolist.c.commands.CreateBasicLabel;
import org.spine3.examples.todolist.c.commands.UpdateLabelDetails;
import org.spine3.examples.todolist.c.events.LabelCreated;
import org.spine3.examples.todolist.c.events.LabelDetailsUpdated;
import org.spine3.examples.todolist.c.failures.CannotUpdateLabelDetails;
import org.spine3.examples.todolist.c.failures.Failures;

import java.util.List;

import static com.google.protobuf.Any.pack;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.spine3.base.Identifiers.newUuid;
import static org.spine3.examples.todolist.testdata.TestCommandContextFactory.createCommandContext;
import static org.spine3.examples.todolist.testdata.TestLabelCommandFactory.LABEL_TITLE;
import static org.spine3.examples.todolist.testdata.TestLabelCommandFactory.UPDATED_LABEL_TITLE;
import static org.spine3.examples.todolist.testdata.TestLabelCommandFactory.createLabelInstance;
import static org.spine3.examples.todolist.testdata.TestLabelCommandFactory.updateLabelDetailsInstance;

/**
 * @author Illia Shepilov
 */
public class LabelAggregateTest {

    private static final CommandContext COMMAND_CONTEXT = createCommandContext();

    private LabelId labelId;
    private LabelAggregate aggregate;

    @BeforeEach
    public void setUp() {
        labelId = createLabelId();
        aggregate = new LabelAggregate(labelId);
    }

    private static LabelId createLabelId() {
        final LabelId result = LabelId.newBuilder()
                                      .setValue(newUuid())
                                      .build();
        return result;
    }

    @Nested
    @DisplayName("CreateBasicLabel command should be interpret by LabelAggregate and")
    class CreateBasicLabelCommand {

        @Test
        @DisplayName("produce LabelCreated event")
        public void produceLabel() {
            final CreateBasicLabel createLabelCmd = createLabelInstance(labelId);
            final List<? extends Message> messageList =
                    aggregate.dispatchForTest(createLabelCmd, COMMAND_CONTEXT);

            assertEquals(1, messageList.size());
            assertEquals(LabelCreated.class, messageList.get(0)
                                                        .getClass());

            final LabelCreated labelCreated = (LabelCreated) messageList.get(0);

            assertEquals(labelId, labelCreated.getId());
            assertEquals(LABEL_TITLE, labelCreated.getDetails()
                                                  .getTitle());
        }

        @Test
        @DisplayName("create the basic label")
        public void createLabel() {
            final CreateBasicLabel createLabelCmd = createLabelInstance(labelId);
            aggregate.dispatchForTest(createLabelCmd, COMMAND_CONTEXT);

            final TaskLabel state = aggregate.getState();

            assertEquals(labelId, state.getId());
            assertEquals(LabelColor.GRAY, state.getColor());
            assertEquals(LABEL_TITLE, state.getTitle());
        }
    }

    @Nested
    @DisplayName("UpdateLabelDetails command should be interpret by LabelAggregate and")
    class UpdateLabelDetailsCommand {

        @Test
        @DisplayName("produce LabelDetailsUpdated event")
        public void produceEvent() {
            final CreateBasicLabel createBasicLabel = createLabelInstance(labelId);
            aggregate.dispatchForTest(createBasicLabel, COMMAND_CONTEXT);

            final UpdateLabelDetails updateLabelDetailsCmd = updateLabelDetailsInstance(labelId);
            final List<? extends Message> messageList =
                    aggregate.dispatchForTest(updateLabelDetailsCmd, COMMAND_CONTEXT);

            assertEquals(1, messageList.size());
            assertEquals(LabelDetailsUpdated.class, messageList.get(0)
                                                               .getClass());

            final LabelDetailsUpdated labelDetailsUpdated = (LabelDetailsUpdated) messageList.get(0);
            final LabelDetails details = labelDetailsUpdated.getLabelDetailsChange()
                                                            .getNewDetails();
            assertEquals(labelId, labelDetailsUpdated.getLabelId());
            assertEquals(LabelColor.GREEN, details.getColor());
            assertEquals(UPDATED_LABEL_TITLE, details.getTitle());
        }

        @Test
        @DisplayName("update the label details twice")
        public void updateLabelDetailsTwice() {
            final CreateBasicLabel createBasicLabel = createLabelInstance(labelId);
            aggregate.dispatchForTest(createBasicLabel, COMMAND_CONTEXT);

            UpdateLabelDetails updateLabelDetailsCmd = updateLabelDetailsInstance(labelId);
            aggregate.dispatchForTest(updateLabelDetailsCmd, COMMAND_CONTEXT);

            TaskLabel state = aggregate.getState();
            assertEquals(labelId, state.getId());
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
            updateLabelDetailsCmd = updateLabelDetailsInstance(labelId, previousLabelDetails, newLabelDetails);
            aggregate.dispatchForTest(updateLabelDetailsCmd, COMMAND_CONTEXT);

            state = aggregate.getState();
            assertEquals(labelId, state.getId());
            assertEquals(updatedLabelColor, state.getColor());
            assertEquals(updatedTitle, state.getTitle());
        }

        @Test
        @DisplayName("produce CannotUpdateLabelDetails failure when the label details does not match expected")
        public void cannotUpdateLabelDetails() {
            final CreateBasicLabel createBasicLabel = createLabelInstance(labelId);
            aggregate.dispatchForTest(createBasicLabel, COMMAND_CONTEXT);

            final LabelDetails expectedLabelDetails = LabelDetails.newBuilder()
                                                                  .setColor(LabelColor.BLUE)
                                                                  .setTitle(LABEL_TITLE)
                                                                  .build();
            final LabelDetails newLabelDetails = LabelDetails.newBuilder()
                                                             .setColor(LabelColor.RED)
                                                             .setTitle(UPDATED_LABEL_TITLE)
                                                             .build();
            try {
                final UpdateLabelDetails updateLabelDetailsCmd =
                        updateLabelDetailsInstance(labelId, expectedLabelDetails, newLabelDetails);
                aggregate.dispatchForTest(updateLabelDetailsCmd, COMMAND_CONTEXT);
            } catch (Throwable e) {
                @SuppressWarnings("ThrowableResultOfMethodCallIgnored") // Need it for checking.
                final Throwable cause = Throwables.getRootCause(e);
                assertTrue(cause instanceof CannotUpdateLabelDetails);

                @SuppressWarnings("ConstantConditions")
                final Failures.CannotUpdateLabelDetails cannotUpdateLabelDetails =
                        ((CannotUpdateLabelDetails) cause).getFailure();
                final LabelDetailsUpdateFailed labelDetailsUpdateFailed = cannotUpdateLabelDetails.getUpdateFailed();
                final LabelId actualLabelId = labelDetailsUpdateFailed.getFailureDetails()
                                                                      .getLabelId();
                assertEquals(labelId, actualLabelId);

                final ValueMismatch mismatch = labelDetailsUpdateFailed.getLabelDetailsMismatch();
                assertEquals(pack(expectedLabelDetails), mismatch.getExpected());
                assertEquals(pack(newLabelDetails), mismatch.getNewValue());

                final LabelDetails actualLabelDetails = LabelDetails.newBuilder()
                                                                    .setColor(LabelColor.GRAY)
                                                                    .setTitle(LABEL_TITLE)
                                                                    .build();
                assertEquals(pack(actualLabelDetails), mismatch.getActual());
            }
        }
    }
}
