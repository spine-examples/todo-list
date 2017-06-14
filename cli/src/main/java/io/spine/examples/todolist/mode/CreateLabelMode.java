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

import io.spine.examples.todolist.LabelId;
import io.spine.examples.todolist.c.commands.CreateBasicLabel;

import static io.spine.base.Identifier.newUuid;
import static io.spine.examples.todolist.mode.InteractiveMode.newLabelId;
import static io.spine.examples.todolist.mode.TodoListCommands.createBasicLabelCmd;

/**
 * @author Illia Shepilov
 */
class CreateLabelMode extends RepeatableAction {

    private static final String CREATE_ONE_MORE_LABEL_QUESTION = "Do you want to create one more label?";
    private static final String ENTER_TITLE_MESSAGE = "Please enter the label title";

    CreateLabelMode() {
        super(CREATE_ONE_MORE_LABEL_QUESTION);
    }

    @Override
    public void doAction() {
        final LabelId labelId = newLabelId(newUuid());
        final String title = askUser(ENTER_TITLE_MESSAGE);
        final CreateBasicLabel createBasicLabel = createBasicLabelCmd(labelId, title);
        getClient().create(createBasicLabel);
    }
}
