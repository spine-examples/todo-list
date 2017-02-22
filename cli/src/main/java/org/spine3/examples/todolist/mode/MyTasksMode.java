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

package org.spine3.examples.todolist.mode;

import jline.console.ConsoleReader;
import org.spine3.examples.todolist.client.TodoClient;
import org.spine3.examples.todolist.q.projection.MyListView;

import java.io.IOException;
import java.util.Map;

import static org.spine3.examples.todolist.mode.GeneralMode.MainModeConstants.TODO_PROMPT;
import static org.spine3.examples.todolist.mode.Mode.ModeConstants.BACK;
import static org.spine3.examples.todolist.mode.Mode.ModeConstants.BACK_TO_THE_MENU_MESSAGE;
import static org.spine3.examples.todolist.mode.Mode.ModeConstants.LINE_SEPARATOR;
import static org.spine3.examples.todolist.mode.MyTasksMode.MyTasksModeConstants.EMPTY_MY_LIST_TASKS;
import static org.spine3.examples.todolist.mode.MyTasksMode.MyTasksModeConstants.HELP_MESSAGE;
import static org.spine3.examples.todolist.mode.MyTasksMode.MyTasksModeConstants.MY_TASKS_MENU;
import static org.spine3.examples.todolist.mode.MyTasksMode.MyTasksModeConstants.MY_TASKS_PROMPT;

/**
 * @author Illia Shepilov
 */
public class MyTasksMode extends CommonMode {

    private final TodoClient client;
    private final ConsoleReader reader;
    private final Map<String, Mode> modeMap = getModeMap();

    MyTasksMode(TodoClient client, ConsoleReader reader) {
        super(client, reader);
        this.client = client;
        this.reader = reader;
    }

    @Override
    public void start() throws IOException {
        reader.setPrompt(MY_TASKS_PROMPT);
        sendMessageToUser(MY_TASKS_MENU);

        final ShowMyTasksMode showMyTasksMode = new ShowMyTasksMode(reader);
        initModeMap(showMyTasksMode);

        showMyTasksMode.start();
        sendMessageToUser(HELP_MESSAGE);
        String line = reader.readLine();
        while (!line.equals(BACK)) {
            final Mode mode = modeMap.get(line);
            if (mode != null) {
                mode.start();
            }

            line = reader.readLine();
        }

        reader.setPrompt(TODO_PROMPT);
    }

    private void initModeMap(ShowMyTasksMode showMyTasksMode) {
        modeMap.put("1", showMyTasksMode);
    }

    private class ShowMyTasksMode extends Mode {

        private ShowMyTasksMode(ConsoleReader reader) {
            super(reader);
        }

        @Override
        public void start() throws IOException {
            final MyListView myListView = client.getMyListView();
            final int itemsCount = myListView.getMyList()
                                             .getItemsCount();
            final boolean isEmpty = itemsCount == 0;
            final String message = isEmpty ? EMPTY_MY_LIST_TASKS : DisplayHelper.constructUserFriendlyMyList(myListView);
            sendMessageToUser(message);

        }
    }

    static class MyTasksModeConstants {

        static final String MY_TASKS_MENU = "******************** My tasks menu *******************" + LINE_SEPARATOR;
        static final String MY_TASKS_PROMPT = "my-tasks>";
        static final String EMPTY_MY_LIST_TASKS = "No tasks in the my list.";
        static final String HELP_MESSAGE = "0:    Help." + LINE_SEPARATOR +
                "1:    Show all my tasks." + LINE_SEPARATOR +
                CommonMode.CommonModeConstants.HELP_MESSAGE +
                BACK_TO_THE_MENU_MESSAGE;

        private MyTasksModeConstants() {
        }
    }
}
