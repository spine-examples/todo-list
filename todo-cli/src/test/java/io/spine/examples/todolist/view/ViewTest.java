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

package io.spine.examples.todolist.view;

import io.spine.examples.todolist.context.BoundedContextFactory;
import io.spine.examples.todolist.server.Server;
import io.spine.server.BoundedContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static io.spine.client.ConnectionConstants.DEFAULT_CLIENT_SERVICE_PORT;
import static io.spine.util.Exceptions.illegalStateWithCauseOf;

/**
 * @author Dmytro Grankin
 */
abstract class ViewTest {

    private static final int PORT = DEFAULT_CLIENT_SERVICE_PORT;

    private Server server;

    @BeforeEach
    void setUp() throws InterruptedException {
        final BoundedContext boundedContext = BoundedContextFactory.instance().create();
        server = new Server(PORT, boundedContext);
        startServer();
    }

    @AfterEach
    public void tearDown() {
        server.shutdown();
    }

    private void startServer() throws InterruptedException {
        final CountDownLatch serverStartLatch = new CountDownLatch(1);
        final Thread serverThread = new Thread(() -> {
            try {
                server.start();
                serverStartLatch.countDown();
            } catch (IOException e) {
                throw illegalStateWithCauseOf(e);
            }
        });

        serverThread.start();
        serverStartLatch.await(100, TimeUnit.MILLISECONDS);
    }
}
