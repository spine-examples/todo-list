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

import com.google.common.annotations.VisibleForTesting;
import io.spine.examples.todolist.AppConfig;
import io.spine.examples.todolist.UserCommunicator;
import io.spine.examples.todolist.UserCommunicatorImpl;
import io.spine.examples.todolist.client.TodoClient;

/**
 * @author Dmytro Grankin
 */
public abstract class Mode {

    private final TodoClient client = AppConfig.getClient();
    private UserCommunicator userCommunicator = new UserCommunicatorImpl();

    public abstract void start();

    protected String askUser(String question) {
        return userCommunicator.askUser(question);
    }

    protected void println(String message) {
        userCommunicator.println(message);
    }

    protected TodoClient getClient() {
        return client;
    }

    @VisibleForTesting
    public void setUserCommunicator(UserCommunicator userCommunicator) {
        this.userCommunicator = userCommunicator;
    }
}
