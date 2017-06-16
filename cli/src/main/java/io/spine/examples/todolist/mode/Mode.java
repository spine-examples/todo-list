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

import io.spine.examples.todolist.AppConfig;
import io.spine.examples.todolist.client.TodoClient;

import java.text.SimpleDateFormat;
import java.util.Locale;

import static io.spine.examples.todolist.mode.Mode.ModeConstants.DATE_FORMAT;

/**
 * @author Dmytro Grankin
 */
public abstract class Mode {

    private final TodoClient client = AppConfig.getClient();

    public abstract void start();

    protected TodoClient getClient() {
        return client;
    }

    static SimpleDateFormat getDateFormat() {
        final SimpleDateFormat result = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
        return result;
    }

    static class ModeConstants {
        static final String DATE_FORMAT = "yyyy-MM-dd";
        static final String LINE_SEPARATOR = System.lineSeparator();
        static final String BACK_TO_THE_MENU_MESSAGE = "Back to the previous menu.";
        static final String DEFAULT_VALUE = "default";

        private ModeConstants() {
        }
    }
}
