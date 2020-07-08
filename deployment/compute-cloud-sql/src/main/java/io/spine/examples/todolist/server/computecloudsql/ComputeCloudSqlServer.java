/*
 * Copyright 2020, TeamDev. All rights reserved.
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

package io.spine.examples.todolist.server.computecloudsql;

import io.spine.examples.todolist.server.Server;
import io.spine.examples.todolist.server.cloudsql.CloudSqlServer;

import java.io.IOException;

/**
 * A Compute Engine {@link Server} backed by a remote Google Cloud SQL storage instance.
 *
 * <p>For the details, see the {@code README.md}.
 */
public final class ComputeCloudSqlServer {

    /** Prevents direct instantiation. */
    private ComputeCloudSqlServer() {
    }

    /**
     * Starts the Compute Engine based To-Do List application server.
     *
     * <p>All of the configuration is set by the {@code /resources/cloud-sql.properties} file.
     *
     * @see CloudSqlServer
     */
    public static void main(String[] args) throws IOException {
        CloudSqlServer server = new CloudSqlServer();
        server.start();
    }
}
