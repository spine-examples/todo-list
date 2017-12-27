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
import java.io.InputStream;
import java.util.Properties;

import static io.spine.client.ConnectionConstants.DEFAULT_CLIENT_SERVICE_PORT;
import static io.spine.examples.todolist.server.Server.subscriptableServer;
import static io.spine.util.Exceptions.illegalStateWithCauseOf;
import static java.lang.String.format;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * A Compute Engine {@link Server} using {@link io.spine.server.storage.jdbc.JdbcStorageFactory
 * JdbcStorageFactory} for working with Cloud SQL.
 *
 * <p>If you want to run this server locally, use {@code LocalCloudSqlServer} instead.
 *
 * <p>For the details, see the {@code README.md}.
 *
 * @author Dmytro Grankin
 */
@SuppressWarnings("DuplicateStringLiteralInspection" /* To avoid creation of a dumb base module
                                                        for servers in different modules. */)
public class ComputeCloudSqlServer {

    private static final String DB_PROPERTIES_FILE = "cloud-sql.properties";
    private static final Properties properties = getProperties(DB_PROPERTIES_FILE);

    private static final String DB_URL_FORMAT = "%s//google/%s?cloudSqlInstance=%s&" +
            "useSSL=false&socketFactory=com.google.cloud.sql.mysql.SocketFactory";

    private ComputeCloudSqlServer() {
        // Prevent instantiation of this class.
    }

    public static void main(String[] args) throws IOException {
        final BoundedContext boundedContext = createBoundedContext();
        final Server server = subscriptableServer(DEFAULT_CLIENT_SERVICE_PORT, boundedContext);
        server.start();
    }

    @VisibleForTesting
    static BoundedContext createBoundedContext() {
        final StorageFactory storageFactory = createStorageFactory();
        return BoundedContexts.create(storageFactory);
    }

    private static StorageFactory createStorageFactory() {
        return JdbcStorageFactory.newBuilder()
                                 .setDataSource(createDataSource())
                                 .setMultitenant(false)
                                 .build();
    }

    private static DataSource createDataSource() {
        final HikariConfig config = new HikariConfig();

        final String instanceConnectionName = properties.getProperty("db.instance");
        final String dbName = properties.getProperty("db.name");
        final String username = properties.getProperty("db.username");
        final String password = properties.getProperty("db.password");

        log().info("Start `DataSource` creation. The following parameters will be used:");
        final String dbUrl = format(DB_URL_FORMAT, getDbUrlPrefix(), dbName, instanceConnectionName);
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

    private static Properties getProperties(String propertiesFile) {
        final Properties properties = new Properties();
        final InputStream stream = ComputeCloudSqlServer.class.getClassLoader()
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
        private final Logger value = getLogger(ComputeCloudSqlServer.class);
    }
}
