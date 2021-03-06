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

import com.google.common.annotations.VisibleForTesting;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A command-line application.
 *
 * <p>Contains the elements that are common for the entire application, e.g. {@link Screen}.
 */
public final class Application {

    private static final Application INSTANCE = new Application();

    private Screen screen;

    /** Prevents instantiation of this class from outside. */
    private Application() {
    }

    /**
     * Obtains {@link Screen} of the application.
     *
     * @return the screen
     */
    public Screen screen() {
        return screen;
    }

    /**
     * Initializes the application with the specified screen.
     *
     * @param screen
     *         the screen to use
     */
    public void init(Screen screen) {
        if (this.screen != null) {
            throw new IllegalStateException("Application is already initialized.");
        } else {
            this.screen = checkNotNull(screen);
        }
    }

    @VisibleForTesting
    public void setScreen(Screen screen) {
        this.screen = screen;
    }

    /**
     * Obtains the application instance.
     *
     * @return the singleton instance
     */
    public static Application instance() {
        return INSTANCE;
    }
}
