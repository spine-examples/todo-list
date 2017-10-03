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

package io.spine.test.performance;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.spine.examples.todolist.c.commands.CreateBasicTask;
import io.spine.examples.todolist.client.CommandLineTodoClient;
import io.spine.examples.todolist.client.TodoClient;
import io.spine.examples.todolist.client.builder.CommandBuilder;
import io.spine.examples.todolist.context.BoundedContexts;
import io.spine.examples.todolist.server.Server;
import io.spine.server.BoundedContext;
import io.spine.server.storage.StorageFactory;
import io.spine.server.storage.jdbc.JdbcStorageFactory;
import io.spine.util.Exceptions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static io.spine.client.ConnectionConstants.DEFAULT_CLIENT_SERVICE_PORT;
import static io.spine.examples.todolist.client.CommandLineTodoClient.HOST;
import static io.spine.examples.todolist.testdata.Given.newDescription;
import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.DESCRIPTION;
import static io.spine.util.Exceptions.illegalStateWithCauseOf;
import static java.lang.String.format;
import static org.slf4j.LoggerFactory.getLogger;

public class BasePT {
    private static final String DB_PROPERTIES_FILE = "jdbc-storage.properties";
    private static final int PORT = DEFAULT_CLIENT_SERVICE_PORT;
    private static final int NUMBER_OF_CLIENTS = 20;
    private static final String DB_URL_FORMAT = "%s/%s?useSSL=false&serverTimezone=UTC";
    private static final Logger log = getLogger(BasePT.class);
    private final TodoClient[] clients = new TodoClient[NUMBER_OF_CLIENTS];
    private Server server;
    private TodoClient client;

    protected static CreateBasicTask createBasicTask() {
        return CommandBuilder.task()
                             .createTask()
                             .setDescription(newDescription(DESCRIPTION))
                             .build();
    }

    private StorageFactory createStorageFactory() {
        return JdbcStorageFactory.newBuilder()
                                 .setDataSource(createDataSource())
                                 .setMultitenant(false)
                                 .build();
    }

    private DataSource createDataSource() {
        final HikariConfig config = new HikariConfig();
        final String[] args = getDefaultArguments();

        final String dbName = args[0];
        final String username = args[1];
        final String password = args[2];

        log.info("Start `DataSource` creation. The following parameters will be used:");
        final String dbUrl = format(DB_URL_FORMAT, getDbUrlPrefix(), dbName);
        config.setJdbcUrl(dbUrl);
        log.info("JDBC URL: {}", dbUrl);

        config.setUsername(username);
        log.info("Username: {}", username);

        config.setPassword(password);
        log.info("Password: {}", password);

        final DataSource dataSource = new HikariDataSource(config);
        return dataSource;
    }

    private String getDbUrlPrefix() {
        final String prefix = "jdbc:mysql://localhost";
        return prefix;
    }

    private String[] getDefaultArguments() {
        final Properties properties = getProperties(DB_PROPERTIES_FILE);
        final String dbName = properties.getProperty("db.name");
        final String username = properties.getProperty("db.username");
        final String password = properties.getProperty("db.password");
        return new String[]{dbName, username, password};
    }

    private Properties getProperties(String propertiesFile) {
        final Properties properties = new Properties();
        final InputStream stream = BasePT.class.getClassLoader()
                                               .getResourceAsStream(propertiesFile);
        try {
            properties.load(stream);
        } catch (IOException e) {
            throw illegalStateWithCauseOf(e);
        }
        return properties;
    }

    @BeforeEach
    protected void setUp() throws InterruptedException {
        BoundedContext boundedContextInMemory = BoundedContexts.create();
        server = new Server(PORT, boundedContextInMemory);
        startServer();
        client = new CommandLineTodoClient(HOST, PORT);
        for (int i = 0; i < NUMBER_OF_CLIENTS; i++) {
            clients[i] = new CommandLineTodoClient(HOST, PORT);
        }
    }

    @AfterEach
    protected void tearDown() {
        server.shutdown();
        getClient().shutdown();
    }

    private void startServer() throws InterruptedException {
        final CountDownLatch serverStartLatch = new CountDownLatch(1);
        final Thread serverThread = new Thread(() -> {
            try {
                server.start();
                serverStartLatch.countDown();
            } catch (IOException e) {
                throw Exceptions.illegalStateWithCauseOf(e);
            }
        });

        serverThread.start();
        serverStartLatch.await(100, TimeUnit.MILLISECONDS);
    }

    protected TodoClient getClient() {
        return client;
    }

    protected TodoClient[] getClients() {
        return clients;
    }

    protected void asyncPerformanceTest(ToDoCommand command, Integer numberOfRequests) throws
                                                                                       InterruptedException {
        ExecutorService pool = Executors.newFixedThreadPool(Runtime.getRuntime()
                                                                   .availableProcessors() * 2);
        final CountDownLatch latch = new CountDownLatch(numberOfRequests);
        for (int i = 0; i < numberOfRequests; i++) {
            final int iterationIndex = i;
            pool.submit(() -> {
                command.execute(iterationIndex);
                latch.countDown();
            });
        }
        try {
            latch.await();
        } finally {
            pool.shutdownNow();
        }
    }

    protected interface ToDoCommand {
        void execute(int iterationIndex);
    }
}
