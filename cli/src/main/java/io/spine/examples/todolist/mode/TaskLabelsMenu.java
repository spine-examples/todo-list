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

package io.spine.examples.todolist.mode;

import io.spine.examples.todolist.LabelColor;
import io.spine.examples.todolist.LabelDetails;
import io.spine.examples.todolist.LabelDetailsChange;
import io.spine.examples.todolist.LabelId;
import io.spine.examples.todolist.TaskId;
import io.spine.examples.todolist.c.commands.AssignLabelToTask;
import io.spine.examples.todolist.c.commands.CreateBasicLabel;
import io.spine.examples.todolist.c.commands.RemoveLabelFromTask;
import io.spine.examples.todolist.c.commands.UpdateLabelDetails;
import io.spine.examples.todolist.mode.menu.Menu;

import static io.spine.base.Identifier.newUuid;
import static io.spine.examples.todolist.mode.InteractiveMode.ModeConstants.LINE_SEPARATOR;
import static io.spine.examples.todolist.mode.TodoListCommands.createBasicLabelCmd;
import static io.spine.examples.todolist.mode.TodoListCommands.createLabelDetails;
import static io.spine.examples.todolist.mode.TodoListCommands.createLabelDetailsChange;
import static io.spine.examples.todolist.mode.TodoListCommands.createUpdateLabelDetailsCmd;
import static java.lang.String.format;

/**
 * @author Illia Shepilov
 */
class TaskLabelsMenu extends Menu {

    protected TaskLabelsMenu() {
        super(Menu.newBuilder()
                  .addMenuItem("Create the label.", new CreateLabelMode())
                  .addMenuItem("Update the label details.", new UpdateLabelDetailsMode())
                  .addMenuItem("Assign a label to a task", new AssignLabelToTaskMode())
                  .addMenuItem("Remove a label from a task.", new RemoveLabelFromTaskMode()));
    }

    private static class CreateLabelMode extends RepeatableAction {

        private static final String CREATE_ONE_MORE_LABEL_QUESTION = "Do you want to create one more label?";
        private static final String ENTER_TITLE_MESSAGE = "Please enter the label title";

        CreateLabelMode() {
            super(CREATE_ONE_MORE_LABEL_QUESTION);
        }

        @Override
        void doAction() {
            final LabelId labelId = newLabelId(newUuid());
            final String title = askUser(ENTER_TITLE_MESSAGE);
            final CreateBasicLabel createBasicLabel = createBasicLabelCmd(labelId, title);
            getClient().create(createBasicLabel);
        }
    }

    private static class UpdateLabelDetailsMode extends InteractiveMode {

        private static final String ENTER_NEW_TITLE_MESSAGE = "Please enter the new label title:";
        private static final String ENTER_PREVIOUS_TITLE_MESSAGE =
                "Please enter the previous label title:";
        private static final String ENTER_NEW_COLOR_MESSAGE = "Please enter the new label color:";
        private static final String ENTER_PREVIOUS_COLOR_MESSAGE =
                "Please enter the previous label color:";
        private static final String UPDATED_LABEL_DETAILS_MESSAGE =
                "The label details updated." + LINE_SEPARATOR +
                        "The label color: %s --> %s." + LINE_SEPARATOR +
                        "The label title: %s --> %s";

        @Override
        public void start() {
            final LabelId labelId;
            final String newTitle;
            final String previousTitle;
            final LabelColor newColor;
            final LabelColor previousColor;
            try {
                labelId = obtainLabelId();
                newTitle = obtainLabelTitle(ENTER_NEW_TITLE_MESSAGE);
                previousTitle = obtainLabelTitle(ENTER_PREVIOUS_TITLE_MESSAGE);
                newColor = obtainLabelColor(ENTER_NEW_COLOR_MESSAGE);
                previousColor = obtainLabelColor(ENTER_PREVIOUS_COLOR_MESSAGE);
            } catch (InputCancelledException ignored) {
                return;
            }
            final LabelDetails newLabelDetails = createLabelDetails(newTitle, newColor);
            final LabelDetails previousLabelDetails = createLabelDetails(previousTitle,
                                                                         previousColor);
            final LabelDetailsChange change = createLabelDetailsChange(newLabelDetails,
                                                                       previousLabelDetails);
            final UpdateLabelDetails updateLabelDetails = createUpdateLabelDetailsCmd(labelId,
                                                                                      change);
            getClient().update(updateLabelDetails);

            final String message = format(UPDATED_LABEL_DETAILS_MESSAGE,
                                          previousColor, newColor, previousTitle, newTitle);
            println(message);
        }
    }

    private static class AssignLabelToTaskMode extends InteractiveMode {

        @Override
        public void start() {
            final TaskId taskId;
            final LabelId labelId;
            try {
                taskId = obtainTaskId();
                labelId = obtainLabelId();
            } catch (InputCancelledException ignored) {
                return;
            }
            final AssignLabelToTask assignLabelToTask = createAssignLabelToTaskCmd(taskId, labelId);
            getClient().assignLabel(assignLabelToTask);
        }

        private static AssignLabelToTask createAssignLabelToTaskCmd(TaskId taskId,
                                                                    LabelId labelId) {
            return AssignLabelToTask.newBuilder()
                                    .setId(taskId)
                                    .setLabelId(labelId)
                                    .build();
        }
    }

    private static class RemoveLabelFromTaskMode extends InteractiveMode {

        @Override
        public void start() {
            final TaskId taskId;
            final LabelId labelId;
            try {
                taskId = obtainTaskId();
                labelId = obtainLabelId();
            } catch (InputCancelledException ignored) {
                return;
            }
            final RemoveLabelFromTask removeLabelFromTask = constructRemoveLabelFromTaskCmd(taskId,
                                                                                            labelId);
            getClient().removeLabel(removeLabelFromTask);
        }

        private static RemoveLabelFromTask constructRemoveLabelFromTaskCmd(TaskId taskId,
                                                                           LabelId labelId) {
            return RemoveLabelFromTask.newBuilder()
                                      .setId(taskId)
                                      .setLabelId(labelId)
                                      .build();
        }
    }
}
