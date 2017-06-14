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

import io.spine.examples.todolist.client.TodoClient;
import io.spine.examples.todolist.mode.menu.Menu;
import jline.console.ConsoleReader;

import static io.spine.examples.todolist.mode.InteractiveMode.ModeConstants.BACK_TO_THE_MENU_MESSAGE;
import static io.spine.examples.todolist.mode.InteractiveMode.ModeConstants.LINE_SEPARATOR;

/**
 * @author Illia Shepilov
 */
public class MainMenu extends Menu {

    public MainMenu(TodoClient client, ConsoleReader reader) {
        super(Menu.newBuilder()
                  .setClient(client)
                  .setReader(reader)
                  .setMenuExit(BACK_TO_THE_MENU_MESSAGE)
                  .addMenuItem("Create the task.", new CreateTaskMenu(client, reader))
                  .addMenuItem("Create the label.", new CreateLabelMode(client, reader))
                  .addMenuItem("Show the tasks in the draft state.",
                               new DraftTasksMode(client, reader))
                  .addMenuItem("Show the labelled tasks.",
                               new LabelledTasksMode(client, reader))
                  .addMenuItem("Show my tasks.", new MyTasksMode(client, reader)));
    }

    static class MainModeConstants {
        static final String EXIT = "exit";
        static final String TODO_PROMPT = "todo>";
        static final String HELP_ADVICE =
                "Enter 'help' or '0' to view all commands." + LINE_SEPARATOR;
        static final String ENTER_LABEL_ID_MESSAGE = "Please enter the label id: ";
        static final String HELP_MESSAGE = "0:    Help." + LINE_SEPARATOR +
                "1:    Create the task." + LINE_SEPARATOR +
                "2:    Create the label." + LINE_SEPARATOR +
                "3:    Show the tasks in the draft state." + LINE_SEPARATOR +
                "4:    Show the labelled tasks." + LINE_SEPARATOR +
                "5:    Show my tasks." + LINE_SEPARATOR +
                "exit: Exit from the application.";

        private MainModeConstants() {
        }
    }
}
