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

import jline.console.ConsoleReader;
import io.spine.examples.todolist.LabelColor;
import io.spine.examples.todolist.LabelDetails;
import io.spine.examples.todolist.LabelDetailsChange;
import io.spine.examples.todolist.LabelId;
import io.spine.examples.todolist.c.commands.CreateBasicLabel;
import io.spine.examples.todolist.c.commands.UpdateLabelDetails;
import io.spine.examples.todolist.client.TodoClient;

import java.io.IOException;

import static io.spine.base.Identifier.newUuid;
import static io.spine.examples.todolist.mode.CreateLabelMode.CreateLabelModeConstants.CREATE_LABEL_PROMPT;
import static io.spine.examples.todolist.mode.CreateLabelMode.CreateLabelModeConstants.CREATE_ONE_MORE_LABEL_QUESTION;
import static io.spine.examples.todolist.mode.CreateLabelMode.CreateLabelModeConstants.ENTER_COLOR_MESSAGE;
import static io.spine.examples.todolist.mode.CreateLabelMode.CreateLabelModeConstants.ENTER_TITLE_MESSAGE;
import static io.spine.examples.todolist.mode.CreateLabelMode.CreateLabelModeConstants.LABEL_CREATED_MESSAGE;
import static io.spine.examples.todolist.mode.CreateLabelMode.CreateLabelModeConstants.SET_LABEL_COLOR_QUESTION;
import static io.spine.examples.todolist.mode.GeneralMode.MainModeConstants.TODO_PROMPT;
import static io.spine.examples.todolist.mode.Mode.ModeConstants.CANCEL_HINT;
import static io.spine.examples.todolist.mode.Mode.ModeConstants.LINE_SEPARATOR;
import static io.spine.examples.todolist.mode.Mode.ModeConstants.NEGATIVE_ANSWER;
import static io.spine.examples.todolist.mode.TodoListCommands.createBasicLabelCmd;
import static io.spine.examples.todolist.mode.TodoListCommands.createLabelDetails;
import static io.spine.examples.todolist.mode.TodoListCommands.createLabelDetailsChange;
import static io.spine.examples.todolist.mode.TodoListCommands.createUpdateLabelDetailsCmd;

/**
 * @author Illia Shepilov
 */
class CreateLabelMode extends Mode {

    private final TodoClient client;
    private final ConsoleReader reader;

    CreateLabelMode(TodoClient client, ConsoleReader reader) {
        super(reader);
        this.reader = reader;
        this.client = client;
    }

    @Override
    public void start() throws IOException {
        reader.setPrompt(CREATE_LABEL_PROMPT);
        String line = "";
        while (!line.equals(NEGATIVE_ANSWER)) {
            createLabel();
            line = obtainApproveValue(CREATE_ONE_MORE_LABEL_QUESTION);
        }
        reader.setPrompt(TODO_PROMPT);
    }

    private void createLabel() throws IOException {
        final LabelId labelId = createLabelId(newUuid());
        final String title;
        try {
            title = obtainLabelTitle(ENTER_TITLE_MESSAGE);
        } catch (InputCancelledException ignored) {
            return;
        }
        final CreateBasicLabel createBasicLabel = createBasicLabelCmd(labelId, title);
        client.create(createBasicLabel);

        final LabelDetails labelDetails = updateLabelDetailsIfNeeded(labelId, title);
        final String message = String.format(LABEL_CREATED_MESSAGE, labelId.getValue(), title, labelDetails.getColor());
        sendMessageToUser(message);
    }

    private LabelDetails updateLabelDetailsIfNeeded(LabelId labelId, String title) throws IOException {
        final String approveValue = obtainApproveValue(SET_LABEL_COLOR_QUESTION);
        final LabelDetails defaultInstance = LabelDetails.getDefaultInstance();
        if (approveValue.equals(NEGATIVE_ANSWER)) {
            return defaultInstance;
        }

        final LabelColor labelColor;
        try {
            labelColor = obtainLabelColor(ENTER_COLOR_MESSAGE);
        } catch (InputCancelledException ignored) {
            return defaultInstance;
        }
        final LabelDetails newLabelDetails = createLabelDetails(title, labelColor);
        final LabelDetailsChange labelDetailsChange = createLabelDetailsChange(newLabelDetails);
        final UpdateLabelDetails updateLabelDetails = createUpdateLabelDetailsCmd(labelId, labelDetailsChange);
        client.update(updateLabelDetails);
        return newLabelDetails;
    }

    static class CreateLabelModeConstants {
        static final String CREATE_LABEL_PROMPT = "create-label>";
        static final String CREATE_ONE_MORE_LABEL_QUESTION = "Do you want to create one more label?(y/n)";
        static final String SET_LABEL_COLOR_QUESTION = "Do you want to set the label color?(y/n)";
        static final String LABEL_CREATED_MESSAGE = "Created label with id: %s, title: %s, color: %s";
        static final String ENTER_COLOR_MESSAGE = "Please enter the label color: " + LINE_SEPARATOR + CANCEL_HINT;
        static final String ENTER_TITLE_MESSAGE = "Please enter the label title: " + LINE_SEPARATOR + CANCEL_HINT;

        private CreateLabelModeConstants() {
        }
    }
}
