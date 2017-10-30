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
import io.spine.examples.todolist.context.BoundedContextFactory;
import io.spine.server.BoundedContext;
import io.spine.server.storage.StorageFactory;
import io.spine.server.storage.jdbc.JdbcStorageFactory;
import org.slf4j.Logger;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static io.spine.client.ConnectionConstants.DEFAULT_CLIENT_SERVICE_PORT;
import static io.spine.util.Exceptions.illegalStateWithCauseOf;
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
 * {@linkplain #getDefaultArguments() default arguments} will be used.
 * The arguments are stored in the properties file {@code jdbc-storage.properties}.
 *
 * <p>As the server uses {@code MySQL}, the database with the specified name should be created
 * and the username and password should be correct.
 *
 * <p>The server exposes its {@code gRPC API} at
 * {@linkplain io.spine.client.ConnectionConstants#DEFAULT_CLIENT_SERVICE_PORT default port}.
 *
 * @author Dmytro Grankin
 */
public class LocalJdbcServer {

    private static final String DB_PROPERTIES_FILE = "jdbc-storage.properties";
    private static final Properties properties = getProperties(DB_PROPERTIES_FILE);

    private static final String DB_URL_FORMAT = "%s/%s?useSSL=false";

    private LocalJdbcServer() {
        // Prevent instantiation of this class.
    }

    public static void main(String[] args) throws IOException {
        final String[] actualArguments;
        final int expectedArgumentsLength = 3;
        if (args.length != expectedArgumentsLength) {
            actualArguments = getDefaultArguments();
            log().info("The specified arguments don't match the length requirement. " +
                               "Required arguments size: {}. Default arguments will be used: {}.",
                       expectedArgumentsLength, actualArguments);
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
        return BoundedContextFactory.instance(storageFactory).create();
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
        final String dbUrl = format(DB_URL_FORMAT, getDbUrlPrefix(), dbName);
        config.setJdbcUrl(dbUrl);
        log().info("JDBC URL: {}", dbUrl);

        config.setUsername(username);
        log().info("Username: {}", username);

        config.setPassword(password);
        log().info("Password: {}", password);

        final DataSource dataSource = new HikariDataSource(config);
        return dataSource;
    }

    /**
     * Obtains the prefix of the connection {@code URL} for the database.
     *
     * <p>The value will be obtained from the {@link #properties}.
     *
     * <p>If the {@link Environment#isTests() environment} is tests,
     * the method returns prefix for connection to an in-memory database.
     *
     * @return the prefix for a connection {@code URL}
     */
    private static String getDbUrlPrefix() {
        final Environment environment = Environment.getInstance();
        final String prefix = environment.isTests()
                              ? "jdbc:h2:mem:"
                              : properties.getProperty("db.prefix");
        return prefix;
    }

    @VisibleForTesting
    static String[] getDefaultArguments() {
        final String dbName = properties.getProperty("db.name");
        final String username = properties.getProperty("db.username");
        final String password = properties.getProperty("db.password");
        return new String[]{dbName, username, password};
    }

    private static Properties getProperties(String propertiesFile) {
        final Properties properties = new Properties();
        final InputStream stream = LocalJdbcServer.class.getClassLoader()
                                                        .getResourceAsStream(propertiesFile);
        try {
            properties.load(stream);
        } catch (IOException e) {
            throw illegalStateWithCauseOf(e);
        }
        return properties;
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
