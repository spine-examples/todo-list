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

import com.google.common.collect.Maps;
import io.spine.examples.todolist.client.TodoClient;
import jline.console.ConsoleReader;

import java.util.Map;

import static io.spine.examples.todolist.mode.GeneralMode.MainModeConstants.EXIT;
import static io.spine.examples.todolist.mode.GeneralMode.MainModeConstants.HELP_MESSAGE;
import static io.spine.examples.todolist.mode.GeneralMode.MainModeConstants.TODO_PROMPT;
import static io.spine.examples.todolist.mode.InteractiveMode.ModeConstants.INCORRECT_COMMAND;
import static io.spine.examples.todolist.mode.InteractiveMode.ModeConstants.LINE_SEPARATOR;

/**
 * @author Illia Shepilov
 */
public class GeneralMode extends InteractiveMode {

    private final Map<String, Mode> modeMap = Maps.newHashMap();

    public GeneralMode(TodoClient client, ConsoleReader reader) {
        super(reader, client);
        initModeMap();
    }

    private void initModeMap() {
        modeMap.put("0", new HelpMode(HELP_MESSAGE));
        modeMap.put("1", new CreateTaskMode(getClient(), getReader()));
        modeMap.put("2", new CreateLabelMode(getClient(), getReader()));
        modeMap.put("3", new DraftTasksMode(getClient(), getReader()));
        modeMap.put("4", new LabelledTasksMode(getClient(), getReader()));
        modeMap.put("5", new MyTasksMode(getClient(), getReader()));
    }

    @Override
    public void start() {
        sendMessageToUser(HELP_MESSAGE);
        getReader().setPrompt(TODO_PROMPT);
        String line = "";
        while (!line.equals(EXIT)) {
            line = readLine();

            final Mode mode = modeMap.get(line);

            if (mode == null) {
                sendMessageToUser(INCORRECT_COMMAND);
                continue;
            }

            mode.start();
            sendMessageToUser(HELP_MESSAGE);
        }
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
