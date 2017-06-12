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
import jline.console.ConsoleReader;
import io.spine.examples.todolist.client.TodoClient;

import java.io.IOException;
import java.util.Map;

import static io.spine.examples.todolist.mode.GeneralMode.MainModeConstants.EXIT;
import static io.spine.examples.todolist.mode.GeneralMode.MainModeConstants.HELP_MESSAGE;
import static io.spine.examples.todolist.mode.GeneralMode.MainModeConstants.TODO_PROMPT;
import static io.spine.examples.todolist.mode.Mode.ModeConstants.INCORRECT_COMMAND;
import static io.spine.examples.todolist.mode.Mode.ModeConstants.LINE_SEPARATOR;

/**
 * @author Illia Shepilov
 */
public class GeneralMode extends Mode {

    private final Map<String, Mode> modeMap = Maps.newHashMap();

    private final TodoClient client;
    private final ConsoleReader reader;

    public GeneralMode(TodoClient client, ConsoleReader reader) {
        super(reader);
        this.client = client;
        this.reader = reader;
        initModeMap();
    }

    private void initModeMap() {
        modeMap.put("0", new HelpMode(reader, HELP_MESSAGE));
        modeMap.put("1", new CreateTaskMode(client, reader));
        modeMap.put("2", new CreateLabelMode(client, reader));
        modeMap.put("3", new DraftTasksMode(client, reader));
        modeMap.put("4", new LabelledTasksMode(client, reader));
        modeMap.put("5", new MyTasksMode(client, reader));
    }

    @Override
    public void start() throws IOException {
        sendMessageToUser(HELP_MESSAGE);
        reader.setPrompt(TODO_PROMPT);
        String line = "";
        while (!line.equals(EXIT)) {
            line = reader.readLine();

            final Mode mode = modeMap.get(line);

            if (mode == null) {
                sendMessageToUser(INCORRECT_COMMAND);
                continue;
            }

            mode.start();
            sendMessageToUser(HELP_MESSAGE);
        }
    }

    public static class MainModeConstants {
        static final String EXIT = "exit";
        static final String TODO_PROMPT = "todo>";
        static final String HELP_ADVICE = "Enter 'help' or '0' to view all commands." + LINE_SEPARATOR;
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
