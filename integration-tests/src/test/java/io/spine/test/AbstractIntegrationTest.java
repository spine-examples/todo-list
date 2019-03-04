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

package io.spine.test;

import com.google.common.collect.ImmutableList;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.spine.examples.todolist.c.commands.CreateBasicLabel;
import io.spine.examples.todolist.c.commands.CreateBasicTask;
import io.spine.examples.todolist.c.commands.CreateDraft;
import io.spine.examples.todolist.client.TodoClient;
import io.spine.examples.todolist.client.builder.CommandBuilder;
import io.spine.examples.todolist.context.BoundedContexts;
import io.spine.examples.todolist.server.Server;
import io.spine.server.BoundedContext;
import io.spine.server.storage.StorageFactory;
import io.spine.server.storage.jdbc.JdbcStorageFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static io.spine.client.ConnectionConstants.DEFAULT_CLIENT_SERVICE_PORT;
import static io.spine.examples.todolist.client.TodoClient.HOST;
import static io.spine.examples.todolist.server.Server.newServer;
import static io.spine.examples.todolist.testdata.Given.newDescription;
import static io.spine.examples.todolist.testdata.TestLabelCommandFactory.LABEL_TITLE;
import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.DESCRIPTION;
import static io.spine.util.Exceptions.illegalStateWithCauseOf;
import static io.spine.util.Exceptions.newIllegalStateException;
import static java.lang.String.format;
import static java.util.Objects.nonNull;
import static junit.framework.TestCase.fail;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Base class for integration and performance tests. Encapsulates server, clients setup logic.
 *
 * <p>Contains {@link #getClients() method getClients} to access clients.
 *
 * <p>Use {@link #asyncPerformanceTest(ToDoCommand, Integer) asyncPerformanceTest method} to execute
 * operation in multithreaded environment.
 */
public abstract class AbstractIntegrationTest {

    private static final String STORAGE_TYPE_PROPERTY_KEY = "storage.type";
    private static final String STORAGE_TYPE_IN_MEMORY = "in-memory";
    private static final String STORAGE_TYPE_JDBC = "jdbc";
    private static final String DB_PROPERTIES_FILE = "jdbc-storage.properties";
    private static final int PORT = DEFAULT_CLIENT_SERVICE_PORT;
    private static final int NUMBER_OF_CLIENTS = 20;
    private static final String DB_URL_FORMAT = "%s//%s:%s/%s?useSSL=false&serverTimezone=UTC";
    private static final Logger LOGGER = getLogger(AbstractIntegrationTest.class);
    private static final Properties DB_CONFIG_PROPERTIES = getProperties(DB_PROPERTIES_FILE);

    private final TodoClient[] clients = new TodoClient[NUMBER_OF_CLIENTS];
    private Server server;
    private TodoClient client;

    protected static CreateBasicTask createBasicTask() {
        return CommandBuilder.task()
                             .createTask()
                             .setDescription(newDescription(DESCRIPTION))
                             .build();
    }

    protected static CreateBasicLabel createBasicLabel() {
        return CommandBuilder.label()
                             .createLabel()
                             .setTitle(LABEL_TITLE)
                             .build();
    }

    protected static CreateDraft createDraft() {
        return CommandBuilder.task()
                             .createDraft()
                             .build();
    }

    private static Properties getProperties(String propertiesFile) {
        Properties properties = new Properties();
        InputStream stream = AbstractIntegrationTest.class.getClassLoader()
                                                          .getResourceAsStream(propertiesFile);
        try {
            properties.load(stream);
        } catch (IOException e) {
            throw illegalStateWithCauseOf(e);
        }
        return properties;
    }

    private static StorageFactory createStorageFactory() {
        return JdbcStorageFactory.newBuilder()
                                 .setDataSource(createDataSource())
                                 .setMultitenant(false)
                                 .build();
    }

    private static DataSource createDataSource() {
        HikariConfig config = new HikariConfig();

        String prefix = DB_CONFIG_PROPERTIES.getProperty("db.prefix");
        String dbName = DB_CONFIG_PROPERTIES.getProperty("db.name");
        String username = DB_CONFIG_PROPERTIES.getProperty("db.username");
        String password = DB_CONFIG_PROPERTIES.getProperty("db.password");
        String host = DB_CONFIG_PROPERTIES.getProperty("db.host");
        String port = DB_CONFIG_PROPERTIES.getProperty("db.port");

        LOGGER.info("Start `DataSource` creation. The following parameters will be used:");
        String dbUrl = format(DB_URL_FORMAT, prefix, host, port, dbName);
        config.setJdbcUrl(dbUrl);
        LOGGER.info("JDBC URL: {}", dbUrl);

        config.setUsername(username);
        LOGGER.info("Username: {}", username);

        config.setPassword(password);
        LOGGER.info("Password: {}", password);

        DataSource dataSource = new HikariDataSource(config);
        return dataSource;
    }

    @BeforeEach
    protected void setUp() throws InterruptedException {
        BoundedContext boundedContextInMemory = createBoundedContext();
        server = newServer(PORT, boundedContextInMemory);
        startServer();
        client = TodoClient.instance(HOST, PORT);
        for (int i = 0; i < NUMBER_OF_CLIENTS; i++) {
            clients[i] = TodoClient.instance(HOST, PORT);
        }
    }

    @AfterEach
    protected void tearDown() {
        server.shutdown();
        getClient().shutdown();
        getClients().forEach(TodoClient::shutdown);
    }

    private void startServer() throws InterruptedException {
        CountDownLatch serverStartLatch = new CountDownLatch(1);
        Thread serverThread = new Thread(() -> {
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

    @SuppressWarnings("AccessOfSystemProperties") // OK for this test.
    private static BoundedContext createBoundedContext() {
        String storageType = System.getProperty(STORAGE_TYPE_PROPERTY_KEY);
        BoundedContext boundedContext = null;
        if (!nonNull(storageType) || storageType.equals(STORAGE_TYPE_IN_MEMORY)) {
            boundedContext = BoundedContexts.create();
        } else if (storageType.equals(STORAGE_TYPE_JDBC)) {
            boundedContext = BoundedContexts.create(createStorageFactory());
        } else {
            fail("Property storage.type contains not supported storage type, read README.md to " +
                         "find supported storage types.");
        }
        return boundedContext;
    }

    protected TodoClient getClient() {
        return client;
    }

    protected ImmutableList<TodoClient> getClients() {
        return ImmutableList.copyOf(clients);
    }

    protected void asyncPerformanceTest(ToDoCommand command, Integer numberOfRequests) {
        int availableProcessors = Runtime.getRuntime()
                                         .availableProcessors();
        ExecutorService pool = Executors.newFixedThreadPool(availableProcessors * 2);
        Collection<Future<?>> tasks = new ArrayList<>();
        for (int i = 0; i < numberOfRequests; i++) {
            int iterationIndex = i;
            Future<?> task = pool.submit(() -> command.execute(iterationIndex));
            tasks.add(task);
        }
        try {
            tasks.forEach(AbstractIntegrationTest::waitForCompletion);
        } finally {
            pool.shutdownNow();
        }
    }

    protected interface ToDoCommand {

        void execute(int iterationIndex);
    }

    private static void waitForCompletion(Future<?> task) {
        try {
            task.get();
        } catch (Throwable e) {
            throw newIllegalStateException(e, "Test task did not complete normally, reason: %s",
                                           e.getLocalizedMessage());
        }
    }

}
