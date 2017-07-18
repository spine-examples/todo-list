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

package io.spine.cli;

import com.google.common.annotations.VisibleForTesting;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A command-line application.
 *
 * <p>Contains the elements that are common for the entire application, e.g. {@link Screen}.
 *
 * @author Dmytro Grankin
 */
public final class Application {

    private Screen screen;

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
     * @param screen the screen to use
     */
    public void init(Screen screen) {
        if (this.screen != null) {
            throw new IllegalStateException("Application is already initialized, " +
                                                    "this function should be called only once.");
        } else {
            this.screen = checkNotNull(screen);
        }
    }

    @VisibleForTesting
    public void setScreen(Screen screen) {
        this.screen = checkNotNull(screen);
    }

    /**
     * Obtains the application instance.
     *
     * @return the singleton instance
     */
    public static Application getInstance() {
        return Singleton.INSTANCE.value;
    }

    private enum Singleton {
        INSTANCE;
        @SuppressWarnings("NonSerializableFieldInSerializableClass")
        private final Application value = new Application();
    }
}
