/*
 * Copyright 2019, TeamDev. All rights reserved.
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

package io.spine.examples.todolist.server.tasks.label;

import io.spine.examples.todolist.tasks.LabelColor;
import io.spine.examples.todolist.tasks.LabelDetails;
import io.spine.examples.todolist.tasks.LabelId;
import io.spine.examples.todolist.tasks.TaskLabel;
import io.spine.examples.todolist.tasks.command.CreateBasicLabel;
import io.spine.examples.todolist.tasks.command.UpdateLabelDetails;
import io.spine.examples.todolist.tasks.event.LabelCreated;
import io.spine.examples.todolist.tasks.event.LabelDetailsUpdated;
import io.spine.examples.todolist.tasks.rejection.Rejections;
import io.spine.testing.server.blackbox.BlackBoxBoundedContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static io.spine.examples.todolist.testdata.TestLabelCommandFactory.createLabelInstance;
import static io.spine.examples.todolist.testdata.TestLabelCommandFactory.updateLabelDetailsInstance;

@DisplayName("LabelAggregate should")
class LabelAggregateTest {

    private BlackBoxBoundedContext boundedContext;

    @BeforeEach
    void setUp() {
        boundedContext = BlackBoxBoundedContext.singleTenant()
                                               .with(new LabelAggregateRepository());
    }

    @Nested
    @DisplayName("interpret CreateBasicLabel command and")
    class CreateBasicLabelCommand {

        @Test
        @DisplayName("produce LabelCreated event")
        void produceEvent() {
            CreateBasicLabel createLabel = createLabelInstance();
            boundedContext.receivesCommand(createLabel)
                          .assertEmitted(LabelCreated.class);
        }

        @Test
        @DisplayName("create the basic label")
        void createLabel() {
            CreateBasicLabel createLabel = createLabelInstance();
            LabelId labelId = createLabel.getLabelId();

            TaskLabel expected = TaskLabel
                    .newBuilder()
                    .setId(labelId)
                    .setTitle(createLabel.getLabelTitle())
                    .build();

            boundedContext.receivesCommand(createLabel)
                          .assertEntity(LabelAggregate.class, labelId)
                          .hasStateThat()
                          .comparingExpectedFieldsOnly()
                          .isEqualTo(expected);
        }
    }

    @Nested
    @DisplayName("interpret UpdateLabelDetails command and")
    class UpdateLabelDetailsCommand {

        @Test
        @DisplayName("produce LabelDetailsUpdated event")
        void produceEvent() {
            CreateBasicLabel createLabel = createLabelInstance();
            LabelId labelId = createLabel.getLabelId();
            UpdateLabelDetails updateDetails = updateLabelDetailsInstance(labelId);
            boundedContext.receivesCommand(createLabel)
                          .receivesCommand(updateDetails)
                          .assertEmitted(LabelDetailsUpdated.class);
        }

        @Test
        @DisplayName("update the label details twice")
        void updateLabelDetailsTwice() {
            CreateBasicLabel createLabel = createLabelInstance();
            LabelId labelId = createLabel.getLabelId();
            UpdateLabelDetails firstUpdate = updateLabelDetailsInstance(labelId);
            LabelDetails afterFirstUpdate = firstUpdate.getLabelDetailsChange()
                                                       .getNewDetails();
            LabelDetails newDetails = newDetails();
            UpdateLabelDetails secondUpdate =
                    updateLabelDetailsInstance(labelId, afterFirstUpdate, newDetails);

            TaskLabel expected = TaskLabel
                    .newBuilder()
                    .setId(labelId)
                    .setTitle(newDetails.getTitle())
                    .setColor(newDetails.getColor())
                    .build();

            boundedContext.receivesCommand(createLabel)
                          .receivesCommand(firstUpdate)
                          .receivesCommand(secondUpdate)
                          .assertEntity(LabelAggregate.class, labelId)
                          .hasStateThat()
                          .isEqualTo(expected);
        }

        @DisplayName("produce CannotUpdateLabelDetails rejection " +
                "when the label details does not match expected")
        @Test
        void cannotUpdateLabelDetails() {
            CreateBasicLabel createLabel = createLabelInstance();
            LabelId labelId = createLabel.getLabelId();

            String incorrectTitle = createLabel.getLabelTitle() + "a redundant suffix";

            LabelDetails newDetails = newDetails();

            LabelDetails previousDetails = LabelDetails
                    .newBuilder()
                    .setTitle(incorrectTitle)
                    .build();

            UpdateLabelDetails updateDetails =
                    updateLabelDetailsInstance(labelId, previousDetails, newDetails);
            boundedContext.receivesCommand(createLabel)
                          .receivesCommand(updateDetails)
                          .assertRejectedWith(Rejections.CannotUpdateLabelDetails.class);
        }

        private LabelDetails newDetails() {
            return LabelDetails
                    .newBuilder()
                    .setTitle("New title")
                    .setColor(LabelColor.RED)
                    .build();
        }
    }
}
