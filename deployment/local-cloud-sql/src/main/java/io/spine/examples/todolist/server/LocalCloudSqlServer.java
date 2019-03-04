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
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.spine.base.Environment;
import io.spine.examples.todolist.context.BoundedContexts;
import io.spine.logging.Logging;
import io.spine.server.BoundedContext;
import io.spine.server.storage.StorageFactory;
import io.spine.server.storage.jdbc.JdbcStorageFactory;
import org.slf4j.Logger;

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
 * JdbcStorageFactory} with {@code Cloud SQL} instance as a data source.
 *
 * <p>To run the server successfully (for the detailed explanation see {@code README.md}):
 * <ol>
 *     <li>Install {@code gcloud} tool.</li>
 *     <li>Authenticate using {@code gcloud}. {@code Cloud SQL client} role is required.</li>
 *     <li>Create a Cloud SQL instance.</li>
 *     <li>Create a database.</li>
 * </ol>
 *
 * <p>To run the server from a command-line run the command as follows:
 * {@code gradle :local-cloud-sql:runServer -Pconf=instance_connection_name,db_name,username,password}
 *
 * <p>If the parameters were not specified to a command or the server was ran directly,
 * {@linkplain #getDefaultArguments() default arguments} will be used.
 * The arguments are stored in the properties file {@code cloud-sql.properties}.
 *
 * <p>The server exposes its {@code gRPC API} at
 * {@linkplain io.spine.client.ConnectionConstants#DEFAULT_CLIENT_SERVICE_PORT default port}.
 *
 * @see <a href="https://cloud.google.com/sdk/gcloud/">gcloud tool</a>
 * @see <a href="https://cloud.google.com/sql/docs/mysql/quickstart">Cloud SQL instance
 *         creation</a>
 */
@SuppressWarnings("DuplicateStringLiteralInspection" /* To avoid creation of a dumb base module
                                                        for servers in different modules. */)
public class LocalCloudSqlServer {

    private static final String DB_PROPERTIES_FILE = "cloud-sql.properties";
    private static final Properties properties = getProperties(DB_PROPERTIES_FILE);

    private static final String DB_URL_FORMAT = "%s//google/%s?cloudSqlInstance=%s&" +
            "useSSL=false&socketFactory=com.google.cloud.sql.mysql.SocketFactory";

    /** Prevents instantiation of this class. */
    private LocalCloudSqlServer() {
    }

    public static void main(String[] args) throws IOException {
        String[] actualArguments = getActualArguments(args);
        BoundedContext boundedContext = createBoundedContext(actualArguments);
        Server server = newServer(DEFAULT_CLIENT_SERVICE_PORT, boundedContext);
        server.start();
    }

    @VisibleForTesting
    static String[] getActualArguments(String[] commandLineArguments) {
        Logger log = Logging.get(LocalCloudSqlServer.class);
        String[] defaultArguments = getDefaultArguments();
        if (commandLineArguments.length != defaultArguments.length) {
            log.info("The specified arguments don't match the length requirement. " +
                             "Required arguments size: {}. Default arguments will be used: {}.",
                     defaultArguments.length, defaultArguments);
            return defaultArguments;
        } else {
            return commandLineArguments;
        }
    }

    @VisibleForTesting
    static BoundedContext createBoundedContext(String[] args) {
        StorageFactory storageFactory = createStorageFactory(args);
        return BoundedContexts.create(storageFactory);
    }

    private static StorageFactory createStorageFactory(String[] args) {
        return JdbcStorageFactory.newBuilder()
                                 .setDataSource(createDataSource(args))
                                 .setMultitenant(false)
                                 .build();
    }

    private static DataSource createDataSource(String[] args) {
        Logger log = Logging.get(LocalCloudSqlServer.class);
        HikariConfig config = new HikariConfig();

        String instanceConnectionName = args[0];
        String dbName = args[1];
        String username = args[2];
        String password = args[3];

        log.info("Start `DataSource` creation. The following parameters will be used:");
        String dbUrl =
                format(DB_URL_FORMAT, getDbUrlPrefix(), dbName, instanceConnectionName);
        config.setJdbcUrl(dbUrl);
        log.info("JDBC URL: {}", dbUrl);

        config.setUsername(username);
        log.info("Username: {}", username);

        config.setPassword(password);
        log.info("Password: {}", password);

        DataSource dataSource = new HikariDataSource(config);
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
        Environment environment = Environment.getInstance();
        String prefix = environment.isTests()
                        ? "jdbc:h2:mem:"
                        : properties.getProperty("db.prefix");
        return prefix;
    }

    @VisibleForTesting
    static String[] getDefaultArguments() {
        String instance = properties.getProperty("db.instance");
        String dbName = properties.getProperty("db.name");
        String username = properties.getProperty("db.username");
        String password = properties.getProperty("db.password");
        return new String[]{instance, dbName, username, password};
    }

    private static Properties getProperties(String propertiesFile) {
        Properties properties = new Properties();
        InputStream stream = LocalCloudSqlServer.class.getClassLoader()
                                                      .getResourceAsStream(propertiesFile);
        try {
            properties.load(stream);
        } catch (IOException e) {
            throw illegalStateWithCauseOf(e);
        }
        return properties;
    }
}
