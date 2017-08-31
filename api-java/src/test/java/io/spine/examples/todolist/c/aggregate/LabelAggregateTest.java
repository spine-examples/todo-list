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

package io.spine.examples.todolist.c.aggregate;

import com.google.protobuf.Message;
import io.spine.change.ValueMismatch;
import io.spine.core.Command;
import io.spine.core.CommandEnvelope;
import io.spine.examples.todolist.LabelColor;
import io.spine.examples.todolist.LabelDetails;
import io.spine.examples.todolist.LabelDetailsUpdateFailed;
import io.spine.examples.todolist.LabelId;
import io.spine.examples.todolist.TaskLabel;
import io.spine.examples.todolist.c.commands.CreateBasicLabel;
import io.spine.examples.todolist.c.commands.UpdateLabelDetails;
import io.spine.examples.todolist.c.events.LabelCreated;
import io.spine.examples.todolist.c.events.LabelDetailsUpdated;
import io.spine.examples.todolist.c.failures.CannotUpdateLabelDetails;
import io.spine.examples.todolist.c.failures.Rejections;
import io.spine.server.aggregate.AggregateCommandTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.spine.Identifier.newUuid;
import static io.spine.examples.todolist.c.aggregate.LabelAggregate.DEFAULT_LABEL_COLOR;
import static io.spine.examples.todolist.testdata.TestLabelCommandFactory.LABEL_TITLE;
import static io.spine.examples.todolist.testdata.TestLabelCommandFactory.UPDATED_LABEL_TITLE;
import static io.spine.examples.todolist.testdata.TestLabelCommandFactory.createLabelInstance;
import static io.spine.examples.todolist.testdata.TestLabelCommandFactory.updateLabelDetailsInstance;
import static io.spine.protobuf.AnyPacker.pack;
import static io.spine.server.aggregate.AggregateMessageDispatcher.dispatchCommand;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Illia Shepilov
 */
class LabelAggregateTest {

    @Nested
    @DisplayName("CreateBasicLabel command should be interpreted by LabelAggregate and")
    class CreateBasicLabelCommand extends LabelAggregateCommandTest<CreateBasicLabel> {

        @Test
        @DisplayName("produce LabelCreated event")
        void produceLabel() {
            final List<? extends Message> messageList = createBasicLabel();

            assertEquals(1, messageList.size());
            assertEquals(LabelCreated.class, messageList.get(0)
                                                        .getClass());

            final LabelCreated labelCreated = (LabelCreated) messageList.get(0);

            assertEquals(getLabelId(), labelCreated.getId());
            assertEquals(LABEL_TITLE, labelCreated.getDetails()
                                                  .getTitle());
        }

        @Test
        @DisplayName("create the basic label")
        void createLabel() {
            createBasicLabel();

            final TaskLabel state = aggregate.getState();

            assertEquals(getLabelId(), state.getId());
            assertEquals(DEFAULT_LABEL_COLOR, state.getColor());
            assertEquals(LABEL_TITLE, state.getTitle());
        }
    }

    @Nested
    @DisplayName("UpdateLabelDetails command should be interpreted by LabelAggregate and")
    class UpdateLabelDetailsCommand extends LabelAggregateCommandTest<UpdateLabelDetails> {

        @Override
        public void setUp() {
            super.setUp();
            createBasicLabel();
        }

        private List<? extends Message> dispatchCommandUpdateLabelDetails(UpdateLabelDetails details) {
            final Command command = createCommand(details);
            return dispatchCommand(aggregate, CommandEnvelope.of(command));
        }

        @Test
        @DisplayName("produce LabelDetailsUpdated event")
        void produceEvent() {
            final UpdateLabelDetails updateLabelDetails = updateLabelDetailsInstance(getLabelId());
            final List<? extends Message> messageList =
                    dispatchCommandUpdateLabelDetails(updateLabelDetails);

            assertEquals(1, messageList.size());
            assertEquals(LabelDetailsUpdated.class, messageList.get(0)
                                                               .getClass());

            final LabelDetailsUpdated labelDetailsUpdated =
                    (LabelDetailsUpdated) messageList.get(0);
            final LabelDetails details = labelDetailsUpdated.getLabelDetailsChange()
                                                            .getNewDetails();
            assertEquals(getLabelId(), labelDetailsUpdated.getLabelId());
            assertEquals(LabelColor.GREEN, details.getColor());
            assertEquals(UPDATED_LABEL_TITLE, details.getTitle());
        }

        @Test
        @DisplayName("update the label details twice")
        void updateLabelDetailsTwice() {
            UpdateLabelDetails updateLabelDetails = updateLabelDetailsInstance(getLabelId());
            dispatchCommandUpdateLabelDetails(updateLabelDetails);

            TaskLabel state = aggregate.getState();
            assertEquals(getLabelId(), state.getId());
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
            updateLabelDetails = updateLabelDetailsInstance(getLabelId(), previousLabelDetails,
                                                            newLabelDetails);
            dispatchCommandUpdateLabelDetails(updateLabelDetails);

            state = aggregate.getState();
            assertEquals(getLabelId(), state.getId());
            assertEquals(updatedLabelColor, state.getColor());
            assertEquals(updatedTitle, state.getTitle());
        }

        @Test
        @DisplayName("produce CannotUpdateLabelDetails failure " +
                "when the label details does not match expected")
        void cannotUpdateLabelDetails() {
            final LabelDetails expectedLabelDetails = LabelDetails.newBuilder()
                                                                  .setColor(LabelColor.BLUE)
                                                                  .setTitle(LABEL_TITLE)
                                                                  .build();
            final LabelDetails newLabelDetails = LabelDetails.newBuilder()
                                                             .setColor(LabelColor.RED)
                                                             .setTitle(UPDATED_LABEL_TITLE)
                                                             .build();
            final UpdateLabelDetails updateLabelDetails =
                    updateLabelDetailsInstance(getLabelId(), expectedLabelDetails, newLabelDetails);
            createCommand(updateLabelDetails);
            final CannotUpdateLabelDetails failure =
                    assertThrows(CannotUpdateLabelDetails.class,
                                 () -> aggregate.handle(commandMessage().get()));
            final Rejections.CannotUpdateLabelDetails cannotUpdateLabelDetails =
                    failure.getMessageThrown();
            final LabelDetailsUpdateFailed labelDetailsUpdateFailed =
                    cannotUpdateLabelDetails.getUpdateFailed();
            final LabelId actualLabelId = labelDetailsUpdateFailed.getFailureDetails()
                                                                  .getLabelId();
            assertEquals(getLabelId(), actualLabelId);

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

    private static abstract class LabelAggregateCommandTest<C extends Message>
            extends AggregateCommandTest<C, LabelAggregate> {

        private LabelId labelId;
        LabelAggregate aggregate;

        @BeforeEach
        public void init() {
            setUp();
        }

        @Override
        public void setUp() {
            super.setUp();
            aggregate = aggregate().get();
        }

        @Override
        protected LabelAggregate createAggregate() {
            labelId = createLabelId();
            return new LabelAggregate(labelId);
        }

        LabelId getLabelId() {
            return labelId;
        }

        List<? extends Message> createBasicLabel() {
            final CreateBasicLabel createBasicLabel = createLabelInstance(labelId);
            final Command command = createDifferentCommand(createBasicLabel);
            return dispatchCommand(aggregate, CommandEnvelope.of(command));
        }

        private static LabelId createLabelId() {
            return LabelId.newBuilder()
                          .setValue(newUuid())
                          .build();
        }
    }
}
