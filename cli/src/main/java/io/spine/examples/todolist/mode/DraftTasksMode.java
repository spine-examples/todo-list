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

import io.spine.examples.todolist.TaskId;
import io.spine.examples.todolist.c.commands.FinalizeDraft;
import io.spine.examples.todolist.client.TodoClient;
import io.spine.examples.todolist.q.projection.DraftTasksView;
import jline.console.ConsoleReader;

import java.io.IOException;
import java.util.Map;

import static io.spine.examples.todolist.mode.DisplayHelper.constructUserFriendlyDraftTasks;
import static io.spine.examples.todolist.mode.DraftTasksMode.DraftTasksModeConstants.DRAFT_FINALIZED_MESSAGE;
import static io.spine.examples.todolist.mode.DraftTasksMode.DraftTasksModeConstants.DRAFT_TASKS_MENU;
import static io.spine.examples.todolist.mode.DraftTasksMode.DraftTasksModeConstants.DRAFT_TASKS_PROMPT;
import static io.spine.examples.todolist.mode.DraftTasksMode.DraftTasksModeConstants.EMPTY_DRAFT_TASKS;
import static io.spine.examples.todolist.mode.DraftTasksMode.DraftTasksModeConstants.HELP_MESSAGE;
import static io.spine.examples.todolist.mode.GeneralMode.MainModeConstants.TODO_PROMPT;
import static io.spine.examples.todolist.mode.Mode.ModeConstants.BACK;
import static io.spine.examples.todolist.mode.Mode.ModeConstants.BACK_TO_THE_MENU_MESSAGE;
import static io.spine.examples.todolist.mode.Mode.ModeConstants.LINE_SEPARATOR;
import static io.spine.examples.todolist.mode.TodoListCommands.createFinalizeDraftCmd;

/**
 * @author Illia Shepilov
 */
@SuppressWarnings("unused")
class DraftTasksMode extends CommonMode {

    private final TodoClient client;
    private final ConsoleReader reader;
    private final Map<String, Mode> modeMap = getModeMap();

    DraftTasksMode(TodoClient client, ConsoleReader reader) {
        super(client, reader);
        this.client = client;
        this.reader = reader;
    }

    @Override
    public void start() throws IOException {
        reader.setPrompt(DRAFT_TASKS_PROMPT);
        sendMessageToUser(DRAFT_TASKS_MENU);
        final ShowDraftTasksMode draftTasksMode = new ShowDraftTasksMode(client, reader);
        final FinalizeDraftMode finalizeDraftMode = new FinalizeDraftMode(client, reader);
        initModeMap(draftTasksMode, finalizeDraftMode);

        draftTasksMode.start();
        sendMessageToUser(HELP_MESSAGE);
        String line = reader.readLine();
        while (!line.equals(BACK)) {
            line = reader.readLine();
            final Mode mode = modeMap.get(line);
            if (mode != null) {
                mode.start();
            }
        }
        reader.setPrompt(TODO_PROMPT);
    }

    private void initModeMap(ShowDraftTasksMode draftTasksMode,
                             FinalizeDraftMode finalizeDraftMode) {
        modeMap.put("1", draftTasksMode);
        modeMap.put("12", finalizeDraftMode);
    }

    private class ShowDraftTasksMode extends Mode {

        private ShowDraftTasksMode(TodoClient client, ConsoleReader reader) {
            super(reader);
        }

        @Override
        public void start() throws IOException {
            final DraftTasksView draftTasksView = client.getDraftTasksView();
            final int itemsCount = draftTasksView.getDraftTasks()
                                                 .getItemsCount();
            final boolean isEmpty = itemsCount == 0;
            final String message = isEmpty
                                   ? EMPTY_DRAFT_TASKS
                                   : constructUserFriendlyDraftTasks(draftTasksView);
            sendMessageToUser(message);
        }
    }

    private class FinalizeDraftMode extends Mode {
        private FinalizeDraftMode(TodoClient client, ConsoleReader reader) {
            super(reader);
        }

        @Override
        public void start() throws IOException {
            final TaskId taskId;
            try {
                taskId = obtainTaskId();
            } catch (InputCancelledException ignored) {
                return;
            }
            final FinalizeDraft finalizeDraft = createFinalizeDraftCmd(taskId);
            client.finalize(finalizeDraft);
            final String message = String.format(DRAFT_FINALIZED_MESSAGE, taskId.getValue());
            sendMessageToUser(message);
        }
    }

    static class DraftTasksModeConstants {
        static final String DRAFT_TASKS_MENU =
                "****************** Draft tasks menu ******************" + LINE_SEPARATOR;
        static final String DRAFT_TASKS_PROMPT = "draft-tasks>";
        static final String EMPTY_DRAFT_TASKS = "No draft tasks.";
        static final String DRAFT_FINALIZED_MESSAGE = "Task with id value: %s is finalized.";
        static final String HELP_MESSAGE =
                "0:    Help." + LINE_SEPARATOR +
                "1:    Show the tasks in the draft state." + LINE_SEPARATOR +
                CommonMode.CommonModeConstants.HELP_MESSAGE + LINE_SEPARATOR +
                "12:   Finalize the draft." + LINE_SEPARATOR +
                BACK_TO_THE_MENU_MESSAGE;

        private DraftTasksModeConstants() {
        }
    }
}
