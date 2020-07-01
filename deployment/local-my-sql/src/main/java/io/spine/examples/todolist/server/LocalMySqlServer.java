/*
 * Copyright 2019, TeamDev. All rights reserved.
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
import com.google.common.flogger.FluentLogger;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.spine.base.Environment;
import io.spine.base.Production;
import io.spine.base.Tests;
import io.spine.examples.todolist.server.tasks.TasksContextFactory;
import io.spine.server.BoundedContext;
import io.spine.server.ServerEnvironment;
import io.spine.server.storage.StorageFactory;
import io.spine.server.storage.jdbc.JdbcStorageFactory;
import io.spine.server.transport.memory.InMemoryTransportFactory;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static io.spine.client.ConnectionConstants.DEFAULT_CLIENT_SERVICE_PORT;
import static io.spine.examples.todolist.server.Server.newServer;
import static io.spine.util.Exceptions.illegalStateWithCauseOf;
import static java.lang.String.format;

/**
 * A local {@link Server} using {@link io.spine.server.storage.jdbc.JdbcStorageFactory
 * JdbcStorageFactory}, {@code MySQL} in particular.
 *
 * <p>To run the server from a command-line run the command as follows:
 * {@code gradle :local-my-sql:runServer -Pconf=db_name,username,password}
 *
 * <p>If the parameters were not specified to a command or the server was ran directly,
 * {@linkplain #defaultArguments() default arguments} will be used.
 * The arguments are stored in the properties file {@code jdbc-storage.properties}.
 *
 * <p>As the server uses {@code MySQL}, the database with the specified name should be created
 * and the username and password should be correct.
 *
 * <p>The server exposes its {@code gRPC API} at
 * {@linkplain io.spine.client.ConnectionConstants#DEFAULT_CLIENT_SERVICE_PORT default port}.
 */
@SuppressWarnings("DuplicateStringLiteralInspection" /* To avoid creation of a dumb base module
                                                        for servers in different modules. */)
public class LocalMySqlServer {

    private static final FluentLogger logger = FluentLogger.forEnclosingClass();
    private static final String DB_PROPERTIES_FILE = "jdbc-storage.properties";
    private static final Properties properties = loadProperties(DB_PROPERTIES_FILE);

    private static final String DB_URL_FORMAT = "%s/%s?useSSL=false";

    /** Prevents instantiation of this class. */
    private LocalMySqlServer() {
    }

    public static void main(String[] args) throws IOException {
        String[] actualArguments = actualArgumentsFrom(args);

        ServerEnvironment serverEnvironment = ServerEnvironment.instance();
        serverEnvironment.use(createStorageFactory(actualArguments), Production.class)
                         .use(InMemoryTransportFactory.newInstance(), Production.class);

        BoundedContext context = createContext();
        Server server = newServer(DEFAULT_CLIENT_SERVICE_PORT, context);
        server.start();
    }

    @VisibleForTesting
    static String[] actualArgumentsFrom(String[] commandLineArguments) {
        String[] defaultArguments = defaultArguments();
        if (commandLineArguments.length != defaultArguments.length) {
            logger.atInfo().log(
                    "The specified arguments do not match the length requirement. " +
                             "Required arguments size: %d. Default arguments will be used: %s.",
                     defaultArguments.length, defaultArguments);
            return defaultArguments;
        } else {
            return commandLineArguments;
        }
    }

    @VisibleForTesting
    static BoundedContext createContext() {
        return TasksContextFactory.create();
    }

    private static StorageFactory createStorageFactory(String[] args) {
        return JdbcStorageFactory
                .newBuilder()
                .setDataSource(createDataSource(args))
                .build();
    }

    private static DataSource createDataSource(String[] args) {
        FluentLogger.Api info = logger.atInfo();
        HikariConfig config = new HikariConfig();

        String dbName = args[0];
        String username = args[1];
        String password = args[2];

        info.log("Start `DataSource` creation. The following parameters will be used:");
        String dbUrl = format(DB_URL_FORMAT, dbUrlPrefix(), dbName);
        config.setJdbcUrl(dbUrl);
        info.log("JDBC URL: %s", dbUrl);

        config.setUsername(username);
        info.log("Username: %s", username);

        config.setPassword(password);
        info.log("Password: %s", password);

        DataSource dataSource = new HikariDataSource(config);
        return dataSource;
    }

    /**
     * Obtains the prefix of the connection {@code URL} for the database.
     *
     * <p>The value will be obtained from the {@link #properties}.
     *
     * <p>If the environment is {@linkplain Tests tests}, the method returns prefix for connection
     * to an in-memory database.
     *
     * @return the prefix for a connection {@code URL}
     */
    private static String dbUrlPrefix() {
        Environment environment = Environment.instance();
        String prefix = environment.is(Tests.class)
                        ? "jdbc:h2:mem:"
                        : properties.getProperty("db.prefix");
        return prefix;
    }

    @VisibleForTesting
    static String[] defaultArguments() {
        String dbName = properties.getProperty("db.name");
        String username = properties.getProperty("db.username");
        String password = properties.getProperty("db.password");
        return new String[]{dbName, username, password};
    }

    private static Properties loadProperties(String propertiesFile) {
        Properties properties = new Properties();
        InputStream stream = LocalMySqlServer.class.getClassLoader()
                                                   .getResourceAsStream(propertiesFile);
        try {
            properties.load(stream);
        } catch (IOException e) {
            throw illegalStateWithCauseOf(e);
        }
        return properties;
    }
}
