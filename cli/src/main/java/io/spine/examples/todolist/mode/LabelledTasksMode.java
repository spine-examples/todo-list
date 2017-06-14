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
import io.spine.examples.todolist.q.projection.LabelledTasksView;
import jline.console.ConsoleReader;

import java.util.List;
import java.util.Map;

import static io.spine.examples.todolist.mode.DisplayHelper.constructUserFriendlyLabelledTasks;
import static io.spine.examples.todolist.mode.InteractiveMode.ModeConstants.BACK;
import static io.spine.examples.todolist.mode.InteractiveMode.ModeConstants.BACK_TO_THE_MENU_MESSAGE;
import static io.spine.examples.todolist.mode.InteractiveMode.ModeConstants.LINE_SEPARATOR;
import static io.spine.examples.todolist.mode.LabelledTasksMode.LabelledTasksModeConstants.EMPTY_LABELLED_TASKS;
import static io.spine.examples.todolist.mode.LabelledTasksMode.LabelledTasksModeConstants.HELP_MESSAGE;
import static io.spine.examples.todolist.mode.LabelledTasksMode.LabelledTasksModeConstants.LABELLED_TASKS_MENU;
import static io.spine.examples.todolist.mode.LabelledTasksMode.LabelledTasksModeConstants.LABELLED_TASKS_PROMPT;
import static io.spine.examples.todolist.mode.MainMenu.MainModeConstants.TODO_PROMPT;

/**
 * @author Illia Shepilov
 */
@SuppressWarnings("unused")
public class LabelledTasksMode extends CommonMode {

    private final Map<String, Mode> modeMap = getModeMap();

    LabelledTasksMode(TodoClient client, ConsoleReader reader) {
        super(client, reader);
    }

    @Override
    public void start() {
        getReader().setPrompt(LABELLED_TASKS_PROMPT);
        println(LABELLED_TASKS_MENU);

        final ShowLabelledTasksMode showLabelledTasksMode = new ShowLabelledTasksMode(getClient(),
                                                                                      getReader());
        initModeMap(showLabelledTasksMode);

        showLabelledTasksMode.start();
        println(HELP_MESSAGE);
        String line = "";

        while (!line.equals(BACK)) {
            line = readLine();
            final Mode mode = modeMap.get(line);
            if (mode != null) {
                mode.start();
            }
        }
        getReader().setPrompt(TODO_PROMPT);
    }

    private void initModeMap(ShowLabelledTasksMode labelledTasksMode) {
        modeMap.put("1", labelledTasksMode);
    }

    private class ShowLabelledTasksMode extends InteractiveMode {

        private ShowLabelledTasksMode(TodoClient client, ConsoleReader reader) {
            super(reader, client);
        }

        @Override
        public void start() {
            final List<LabelledTasksView> labelledTasks = getClient().getLabelledTasksView();
            final String message = labelledTasks.isEmpty()
                                   ? EMPTY_LABELLED_TASKS
                                   : constructUserFriendlyLabelledTasks(labelledTasks);
            println(message);
        }
    }

    static class LabelledTasksModeConstants {
        static final String LABELLED_TASKS_MENU =
                "***************** Labelled tasks menu ****************" + LINE_SEPARATOR;
        static final String LABELLED_TASKS_PROMPT = "labelled-tasks>";
        static final String EMPTY_LABELLED_TASKS = "No labelled tasks.";
        static final String HELP_MESSAGE = "0:    Help." + LINE_SEPARATOR +
                "1:    Show the labelled tasks." + LINE_SEPARATOR +
                CommonMode.CommonModeConstants.HELP_MESSAGE +
                BACK_TO_THE_MENU_MESSAGE;

        private LabelledTasksModeConstants() {
        }
    }
}
