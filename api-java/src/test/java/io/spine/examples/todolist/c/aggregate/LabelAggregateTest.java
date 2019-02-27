/*
 * Copyright 2018, TeamDev. All rights reserved.
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

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.protobuf.Message;
import io.spine.base.CommandMessage;
import io.spine.change.ValueMismatch;
import io.spine.core.Command;
import io.spine.examples.todolist.LabelColor;
import io.spine.examples.todolist.LabelDetails;
import io.spine.examples.todolist.LabelDetailsUpdateRejected;
import io.spine.examples.todolist.LabelDetailsVBuilder;
import io.spine.examples.todolist.LabelId;
import io.spine.examples.todolist.TaskLabel;
import io.spine.examples.todolist.c.commands.CreateBasicLabel;
import io.spine.examples.todolist.c.commands.UpdateLabelDetails;
import io.spine.examples.todolist.c.events.LabelCreated;
import io.spine.examples.todolist.c.events.LabelDetailsUpdated;
import io.spine.examples.todolist.c.rejection.CannotUpdateLabelDetails;
import io.spine.examples.todolist.c.rejection.Rejections;
import io.spine.examples.todolist.repository.LabelAggregateRepository;
import io.spine.server.entity.Repository;
import io.spine.server.type.CommandEnvelope;
import io.spine.testing.client.TestActorRequestFactory;
import io.spine.testing.server.aggregate.AggregateCommandTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.spine.examples.todolist.c.aggregate.LabelAggregate.DEFAULT_LABEL_COLOR;
import static io.spine.examples.todolist.testdata.TestLabelCommandFactory.LABEL_ID;
import static io.spine.examples.todolist.testdata.TestLabelCommandFactory.LABEL_TITLE;
import static io.spine.examples.todolist.testdata.TestLabelCommandFactory.UPDATED_LABEL_TITLE;
import static io.spine.examples.todolist.testdata.TestLabelCommandFactory.createLabelInstance;
import static io.spine.examples.todolist.testdata.TestLabelCommandFactory.updateLabelDetailsInstance;
import static io.spine.protobuf.AnyPacker.pack;
import static io.spine.testing.server.aggregate.AggregateMessageDispatcher.dispatchCommand;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("LabelAggregate should")
class LabelAggregateTest {

    @Nested
    @DisplayName("interpret CreateBasicLabel command and")
    class CreateBasicLabelCommand extends LabelAggregateCommandTest<CreateBasicLabel> {

        CreateBasicLabelCommand() {
            super(createLabelInstance());
        }

        @Test
        @DisplayName("produce LabelCreated event")
        void produceLabel() {
            final List<? extends Message> messageList = createBasicLabel();

            assertEquals(1, messageList.size());
            assertEquals(LabelCreated.class, messageList.get(0)
                                                        .getClass());

            final LabelCreated labelCreated = (LabelCreated) messageList.get(0);

            assertEquals(entityId(), labelCreated.getId());
            assertEquals(LABEL_TITLE, labelCreated.getDetails()
                                                  .getTitle());
        }

        @Test
        @DisplayName("create the basic label")
        void createLabel() {
            createBasicLabel();

            final TaskLabel state = aggregate.state();

            assertEquals(entityId(), state.getId());
            assertEquals(DEFAULT_LABEL_COLOR, state.getColor());
            assertEquals(LABEL_TITLE, state.getTitle());
        }
    }

    @Nested
    @DisplayName("interpret UpdateLabelDetails command and")
    class UpdateLabelDetailsCommand extends LabelAggregateCommandTest<UpdateLabelDetails> {

        UpdateLabelDetailsCommand() {
            super(updateLabelDetailsInstance());
        }

        @Override
        public void setUp() {
            super.setUp();
            createBasicLabel();
        }

        @CanIgnoreReturnValue
        private List<? extends Message> dispatchUpdateLabelDetails(UpdateLabelDetails details) {
            return dispatchCommand(aggregate, envelopeOf(details));
        }

        @Test
        @DisplayName("produce LabelDetailsUpdated event")
        void produceEvent() {
            final UpdateLabelDetails updateLabelDetails = updateLabelDetailsInstance(entityId());
            final List<? extends Message> messageList =
                    dispatchUpdateLabelDetails(updateLabelDetails);

            assertEquals(1, messageList.size());
            assertEquals(LabelDetailsUpdated.class, messageList.get(0)
                                                               .getClass());

            final LabelDetailsUpdated labelDetailsUpdated =
                    (LabelDetailsUpdated) messageList.get(0);
            final LabelDetails details = labelDetailsUpdated.getLabelDetailsChange()
                                                            .getNewDetails();
            assertEquals(entityId(), labelDetailsUpdated.getLabelId());
            assertEquals(LabelColor.GREEN, details.getColor());
            assertEquals(UPDATED_LABEL_TITLE, details.getTitle());
        }

        @Test
        @DisplayName("update the label details twice")
        void updateLabelDetailsTwice() {
            UpdateLabelDetails updateLabelDetails = updateLabelDetailsInstance(entityId());
            dispatchUpdateLabelDetails(updateLabelDetails);

            TaskLabel state = aggregate.state();
            assertEquals(entityId(), state.getId());
            assertEquals(LabelColor.GREEN, state.getColor());
            assertEquals(UPDATED_LABEL_TITLE, state.getTitle());

            final LabelColor previousLabelColor = LabelColor.GREEN;
            final LabelDetails previousLabelDetails = LabelDetailsVBuilder
                    .newBuilder()
                    .setTitle(UPDATED_LABEL_TITLE)
                    .setColor(previousLabelColor)
                    .build();
            final LabelColor updatedLabelColor = LabelColor.BLUE;
            final String updatedTitle = "updated title";
            final LabelDetails newLabelDetails = LabelDetailsVBuilder
                    .newBuilder()
                    .setColor(updatedLabelColor)
                    .setTitle(updatedTitle)
                    .build();
            updateLabelDetails = updateLabelDetailsInstance(entityId(), previousLabelDetails,
                                                            newLabelDetails);
            dispatchUpdateLabelDetails(updateLabelDetails);

            state = aggregate.state();
            assertEquals(entityId(), state.getId());
            assertEquals(updatedLabelColor, state.getColor());
            assertEquals(updatedTitle, state.getTitle());
        }

        @SuppressWarnings({"CheckReturnValue", "ResultOfMethodCallIgnored"})
        // Method called to throw exception.
        @Test
        @DisplayName("produce CannotUpdateLabelDetails rejection " +
                "when the label details does not match expected")
        void cannotUpdateLabelDetails() {
            final LabelDetails expectedLabelDetails = LabelDetailsVBuilder
                    .newBuilder()
                    .setColor(LabelColor.BLUE)
                    .setTitle(LABEL_TITLE)
                    .build();
            final LabelDetails newLabelDetails = LabelDetailsVBuilder
                    .newBuilder()
                    .setColor(LabelColor.RED)
                    .setTitle(UPDATED_LABEL_TITLE)
                    .build();
            UpdateLabelDetails updateLabelDetails =
                    updateLabelDetailsInstance(entityId(), expectedLabelDetails, newLabelDetails);
            final CannotUpdateLabelDetails rejection =
                    assertThrows(CannotUpdateLabelDetails.class,
                                 () -> aggregate.handle(updateLabelDetails));
            final Rejections.CannotUpdateLabelDetails cannotUpdateLabelDetails =
                    rejection.getMessageThrown();
            final LabelDetailsUpdateRejected rejectionDetails =
                    cannotUpdateLabelDetails.getRejectionDetails();
            final LabelId actualLabelId = rejectionDetails.getCommandDetails()
                                                          .getLabelId();
            assertEquals(entityId(), actualLabelId);

            final ValueMismatch mismatch = rejectionDetails.getLabelDetailsMismatch();
            assertEquals(pack(expectedLabelDetails), mismatch.getExpected());
            assertEquals(pack(newLabelDetails), mismatch.getNewValue());

            final LabelDetails actualLabelDetails = LabelDetailsVBuilder
                    .newBuilder()
                    .setColor(LabelColor.GRAY)
                    .setTitle(LABEL_TITLE)
                    .build();
            assertEquals(pack(actualLabelDetails), mismatch.getActual());
        }
    }

    @SuppressWarnings("PackageVisibleField") // for brevity of descendants.
    private abstract static class LabelAggregateCommandTest<C extends CommandMessage>
            extends AggregateCommandTest<LabelId, C, TaskLabel, LabelAggregate> {

        private final TestActorRequestFactory requestFactory =
                new TestActorRequestFactory(getClass());

        LabelAggregate aggregate;

        LabelAggregateCommandTest(C commandMessage) {
            super(LABEL_ID, commandMessage);
        }

        @BeforeEach
        public void init() {
            setUp();
        }

        @Override
        public void setUp() {
            super.setUp();
            aggregate = new LabelAggregate(entityId());
        }

        @Override
        protected Repository<LabelId, LabelAggregate> createRepository() {
            return new LabelAggregateRepository();
        }

        @CanIgnoreReturnValue
        List<? extends Message> createBasicLabel() {
            final CreateBasicLabel createBasicLabel = createLabelInstance();
            final Command command = createNewCommand(createBasicLabel);
            return dispatchCommand(aggregate, CommandEnvelope.of(command));
        }

        CommandEnvelope envelopeOf(CommandMessage commandMessage) {
            Command command = createNewCommand(commandMessage);
            CommandEnvelope envelope = CommandEnvelope.of(command);
            return envelope;
        }

        private Command createNewCommand(CommandMessage commandMessage) {
            Command command = requestFactory.command()
                                            .create(commandMessage);
            return command;
        }
    }
}
