/*
 * Copyright 2018, TeamDev Ltd. All rights reserved.
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

package io.spine.examples.todolist;

import com.google.common.annotations.VisibleForTesting;
import io.spine.examples.todolist.client.TodoClient;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * Configuration of the client application.
 *
 * @author Dmytro Grankin
 */
public class AppConfig {

    private static TodoClient todoClient = null;

    private AppConfig() {
        // Prevent instantiation of this class.
    }

    public static void init(TodoClient client) {
        if (todoClient != null) {
            throw new IllegalStateException("AppConfig is already initialized.");
        } else {
            todoClient = checkNotNull(client);
        }
    }

    @VisibleForTesting
    public static void setClient(TodoClient client) {
        todoClient = client;
    }

    /**
     * Obtains {@link TodoClient} for communication with the {@code Server}.
     *
     * @return the todoClient interface
     */
    public static TodoClient getClient() {
        checkState(todoClient != null);
        return todoClient;
    }
}
