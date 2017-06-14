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

import io.spine.examples.todolist.mode.menu.Menu;

import static io.spine.examples.todolist.mode.InteractiveMode.ModeConstants.BACK_TO_THE_MENU_MESSAGE;

/**
 * @author Illia Shepilov
 */
public class MainMenu extends Menu {

    public MainMenu() {
        super(Menu.newBuilder()
                  .setMenuExit(BACK_TO_THE_MENU_MESSAGE)
                  .addMenuItem("Create the task.", new CreateTaskMenu())
                  .addMenuItem("Create the label.", new CreateLabelMode())
                  .addMenuItem("Show the tasks in the draft state.", new DraftTasksMode())
                  .addMenuItem("Show the labelled tasks.", new LabelledTasksMode())
                  .addMenuItem("Show my tasks.", new MyTasksMode()));
    }

    static class MainModeConstants {

        static final String ENTER_LABEL_ID_MESSAGE = "Please enter the label id: ";

        private MainModeConstants() {
        }
    }
}
