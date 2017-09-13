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

package io.spine.examples.todolist.server;

import com.google.common.annotations.VisibleForTesting;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.spine.Environment;
import io.spine.examples.todolist.context.BoundedContexts;
import io.spine.server.BoundedContext;
import io.spine.server.storage.StorageFactory;
import io.spine.server.storage.jdbc.JdbcStorageFactory;
import org.slf4j.Logger;

import javax.sql.DataSource;
import java.io.IOException;

import static io.spine.client.ConnectionConstants.DEFAULT_CLIENT_SERVICE_PORT;
import static java.lang.String.format;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * A local {@link Server} using {@link io.spine.server.storage.jdbc.JdbcStorageFactory
 * JdbcStorageFactory}, {@code MySQL} in particular.
 *
 * <p>To run the server from a command-line run the command as follows:
 * {@code gradle :local-jdbc:runServer -Pconf=db_name,username,password}
 *
 * <p>If the parameters were not specified to a command or the server was ran directly,
 * {@linkplain #DEFAULT_ARGUMENTS} will be used.
 *
 * <p>As the server uses {@code MySQL}, the data base with the specified name should be created
 * and the username and password should be correct.
 *
 * <p>The server uses the
 * {@linkplain io.spine.client.ConnectionConstants#DEFAULT_CLIENT_SERVICE_PORT default port}.
 *
 * @author Dmytro Grankin
 */
public class LocalJdbcServer {

    private static final String DB_URL_PREFIX = "jdbc:mysql:";
    private static final String DB_URL_FORMAT = "%s%s?useSSL=false";

    private static final String DB_NAME = "TodoListDB";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "root";
    @VisibleForTesting
    static final String[] DEFAULT_ARGUMENTS = {DB_NAME, USERNAME, PASSWORD};

    private LocalJdbcServer() {
        // Prevent instantiation of this class.
    }

    public static void main(String[] args) throws IOException {
        final String[] actualArguments;
        if (DEFAULT_ARGUMENTS.length != args.length) {
            log().info("The specified arguments don't match the required format. " +
                               "Default arguments will be used.");
            actualArguments = DEFAULT_ARGUMENTS;
        } else {
            actualArguments = args;
        }

        final BoundedContext boundedContext = createBoundedContext(actualArguments);
        final Server server = new Server(DEFAULT_CLIENT_SERVICE_PORT, boundedContext);
        server.start();
    }

    @VisibleForTesting
    static BoundedContext createBoundedContext(String[] args) {
        final StorageFactory storageFactory = createStorageFactory(args);
        return BoundedContexts.create(storageFactory);
    }

    private static StorageFactory createStorageFactory(String[] args) {
        return JdbcStorageFactory.newBuilder()
                                 .setDataSource(createDataSource(args))
                                 .setMultitenant(false)
                                 .build();
    }

    private static DataSource createDataSource(String[] args) {
        final HikariConfig config = new HikariConfig();

        final String dbName = args[0];
        final String username = args[1];
        final String password = args[2];

        log().info("Start `DataSource` creation. The following parameters will be used:");
        final String dbUrl = getDbUrl(dbName);
        config.setJdbcUrl(dbUrl);
        log().info("JDBC URL: {}", dbUrl);

        config.setUsername(username);
        log().info("Username: {}", username);

        config.setPassword(password);
        log().info("Password: {}", password);

        final DataSource dataSource = new HikariDataSource(config);
        return dataSource;
    }

    private static String getDbUrl(String dbName) {
        final Environment environment = Environment.getInstance();
        final String prefix = environment.isTests()
                              ? "jdbc:h2:mem:"
                              : DB_URL_PREFIX;
        return format(DB_URL_FORMAT, prefix, dbName);
    }

    private static Logger log() {
        return LogSingleton.INSTANCE.value;
    }

    private enum LogSingleton {
        INSTANCE;
        @SuppressWarnings("NonSerializableFieldInSerializableClass")
        private final Logger value = getLogger(LocalJdbcServer.class);
    }
}
