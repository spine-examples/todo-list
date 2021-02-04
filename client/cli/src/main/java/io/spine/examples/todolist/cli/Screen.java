/*
 * Copyright 2021, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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

package io.spine.examples.todolist.cli;

import io.spine.examples.todolist.cli.action.Shortcut;
import io.spine.examples.todolist.cli.action.TransitionAction;
import io.spine.examples.todolist.cli.view.View;

import java.util.Optional;

/**
 * A screen of an application.
 *
 * <p>Renders views and provides I/O methods.
 *
 * <p>Also provides methods related to a history of navigation
 * between views, e.g. {@link #createBackAction(String, Shortcut)}.
 */
public interface Screen {

    /**
     * Renders the specified view.
     *
     * @param view
     *         the view to renderBody
     */
    void renderView(View view);

    /**
     * Obtains the action leading from a current view to a previous view.
     *
     * @param name
     *         the name for the action
     * @param shortcut
     *         the shortcut for the action
     * @return {@code Optional} of the back action,
     *         or {@code Optional.empty} if there is no previous view
     */
    Optional<TransitionAction<View, View>> createBackAction(String name, Shortcut shortcut);

    /**
     * Prompts a user for an input and receives the input value.
     *
     * @param prompt
     *         the prompt to display
     * @return the input value
     */
    String promptUser(String prompt);

    /**
     * Prints the message and a new line after it.
     *
     * @param message
     *         the message to print
     */
    void println(String message);
}
