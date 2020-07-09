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

package io.spine.examples.todolist.server.cloudsql;

import io.spine.examples.todolist.server.Server;

import java.io.IOException;

/**
 * A local {@link Server} that is backed by a storage based on a Google Cloud SQL emulator.
 *
 * <p>To run the server successfully (for the detailed explanation see {@code README.md}):
 * <ol>
 *     <li>Install {@code gcloud} tool.
 *     <li>Authenticate using {@code gcloud}. {@code Cloud SQL client} role is required.
 *     <li>Create a Cloud SQL instance.
 *     <li>Create a database.
 * </ol>
 *
 * <p>To run the server from a command-line run the command as follows:
 * {@code gradle :local-cloud-sql:runServer -Ddb.name=<db_name> -Ddb.instance=<db_instance>
 * -Ddb.username=<username> -Ddb.password=<password> -Ddb.protocol=<protocol>}
 *
 * <p>If the parameters were omitted, a default configuration is parsed from a configuration file
 * in resources. See {@code /resources/cloud-sql.properties}.
 *
 * <p>The server exposes its {@code gRPC API} at
 * {@linkplain io.spine.client.ConnectionConstants#DEFAULT_CLIENT_SERVICE_PORT default port}.
 *
 * @see <a href="https://cloud.google.com/sdk/gcloud/">gcloud tool</a>
 * @see <a href="https://cloud.google.com/sql/docs/mysql/quickstart">Cloud SQL instance
 *         creation</a>
 */
public class LocalCloudSqlServer {

    /** Prevents direct instantiation. */
    private LocalCloudSqlServer() {
    }

    /**
     * Starts the To-Do List application server.
     *
     * @see CloudSqlServer
     */
    public static void main(String[] args) throws IOException {
        CloudSqlServer server = new CloudSqlServer();
        server.start();
    }
}
