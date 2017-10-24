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

package io.spine.examples.todolist.context;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import com.google.common.base.Supplier;
import io.spine.core.BoundedContextName;
import io.spine.core.EventClass;
import io.spine.examples.todolist.repository.DraftTasksViewRepository;
import io.spine.examples.todolist.repository.LabelAggregateRepository;
import io.spine.examples.todolist.repository.LabelledTasksViewRepository;
import io.spine.examples.todolist.repository.MyListViewRepository;
import io.spine.examples.todolist.repository.TaskLabelsRepository;
import io.spine.examples.todolist.repository.TaskRepository;
import io.spine.server.BoundedContext;
import io.spine.server.catchup.KafkaCatchUp;
import io.spine.server.event.EventBus;
import io.spine.server.event.EventDispatcher;
import io.spine.server.event.EventEnricher;
import io.spine.server.projection.ProjectionRepository;
import io.spine.server.storage.StorageFactory;
import io.spine.server.storage.StorageFactorySwitch;
import io.spine.server.storage.kafka.KafkaWrapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.server.storage.StorageFactorySwitch.newInstance;
import static io.spine.util.Exceptions.newIllegalStateException;
import static java.util.stream.Collectors.toSet;

/**
 * Utilities for creation the {@link BoundedContext} instances.
 *
 * @author Illia Shepilov
 * @author Dmytro Grankin
 */
public final class BoundedContexts {

    /** The name of the Bounded Context. */
    private static final String NAME = "TodoListBoundedContext";
    private static final BoundedContextName BOUNDED_CONTEXT_NAME = BoundedContext.newName(NAME);

    private static final StorageFactorySwitch storageFactorySwitch =
            newInstance(BOUNDED_CONTEXT_NAME, false);

    @SuppressWarnings("Guava") // Spine Java 7 API.
    private static final Supplier<StorageFactory> FAILING_STORAGE_SUPPLIER = () -> {
        throw newIllegalStateException(
                "Use BoundedContexts.create(StorageFactory) in production code.");
    };

    private BoundedContexts() {
        // Disable instantiation from outside.
    }

    public static String getDefaultName() {
        return NAME;
    }

    /**
     * Creates the {@link BoundedContext} instance using the {@code StorageFactory} from
     * the {@link StorageFactorySwitch}.
     *
     * <p>Use {@link #create(StorageFactory)} method for the production environment.
     *
     * <p>Use {@link #injectStorageFactory(StorageFactory)} in tests to override the used
     * {@code StorageFactory} implementation.
     *
     * @return new test {@link BoundedContext} instance
     */
    @VisibleForTesting
    public static BoundedContext create() {
        final StorageFactory storageFactory = storageFactorySwitch.get();
        final BoundedContext result = create(storageFactory);
        return result;
    }

    /**
     * Creates a new instance of the {@link BoundedContext}
     * using the specified {@link StorageFactory}.
     *
     * @param storageFactory the storage factory to use
     * @return the bounded context created with the storage factory
     */
    public static BoundedContext create(StorageFactory storageFactory) {
        checkNotNull(storageFactory);
        final LabelAggregateRepository labelAggregateRepo = new LabelAggregateRepository();
        final TaskRepository taskRepo = new TaskRepository();
        final TaskLabelsRepository taskLabelsRepo = new TaskLabelsRepository();
        final MyListViewRepository myListViewRepo = new MyListViewRepository();
        final LabelledTasksViewRepository tasksViewRepo = new LabelledTasksViewRepository();
        final DraftTasksViewRepository draftTasksViewRepo = new DraftTasksViewRepository();

        startCatchUp(myListViewRepo, tasksViewRepo, draftTasksViewRepo);

        final EventBus.Builder eventBus = createEventBus(storageFactory,
                                                         labelAggregateRepo,
                                                         taskRepo,
                                                         taskLabelsRepo);
        final BoundedContext boundedContext = createBoundedContext(eventBus);
        boundedContext.register(taskRepo);
        boundedContext.register(taskLabelsRepo);
        boundedContext.register(labelAggregateRepo);
        boundedContext.register(myListViewRepo);
        boundedContext.register(tasksViewRepo);
        boundedContext.register(draftTasksViewRepo);

        final EventBus bus = boundedContext.getEventBus();
        bus.unregister(myListViewRepo);
        bus.unregister(tasksViewRepo);
        bus.unregister(draftTasksViewRepo);

        final EventDispatcher<?> dispatcher = createDispatcher(myListViewRepo,
                                                               tasksViewRepo,
                                                               draftTasksViewRepo);
        boundedContext.getEventBus().register(dispatcher);

        return boundedContext;
    }

    private static EventDispatcher<?> createDispatcher(EventDispatcher<?>... typeSuppliers) {
        final Set<EventClass> eventClasses =
                Stream.of(typeSuppliers)
                      .flatMap(dispatcher -> dispatcher.getMessageClasses().stream())
                      .distinct()
                      .collect(toSet());
        final KafkaWrapper kafkaWrapper = KafkaWrapper.create(
                loadProps("config/kafka-producer.properties"),
                loadProps("config/kafka-consumer.properties"));
        final EventDispatcher<?> dispatcher = KafkaCatchUp.dispatcher(eventClasses, kafkaWrapper);
        return dispatcher;
    }

    private static void startCatchUp(ProjectionRepository<?, ?, ?>... repos) {
        final Properties config = loadProps("kafka-streams.properties");
        for (ProjectionRepository<?, ?, ?> repo : repos) {
            KafkaCatchUp.start(repo, config);
        }
    }

    private static EventBus.Builder createEventBus(StorageFactory storageFactory,
                                                   LabelAggregateRepository labelRepo,
                                                   TaskRepository taskRepo,
                                                   TaskLabelsRepository labelsRepo) {
        final EventEnricher enricher = TodoListEnrichments.newBuilder()
                                                          .setLabelRepository(labelRepo)
                                                          .setTaskRepository(taskRepo)
                                                          .setTaskLabelsRepository(labelsRepo)
                                                          .build()
                                                          .createEnricher();
        final EventBus.Builder eventBus = EventBus.newBuilder()
                                                  .setEnricher(enricher)
                                                  .setStorageFactory(storageFactory);
        return eventBus;
    }

    /**
     * Injects the given {@link StorageFactory} implementation for tests.
     *
     * <p>To specify the {@link StorageFactory} implementation for the production code,
     * use {@link #create(StorageFactory)}.
     *
     * <p>After calling this method, invocation of {@link #create()} in the test environment
     * creates a {@code BoundedContext} with the passed {@code StorageFactory}.
     *
     * @param storageFactory the default {@code StorageFactory} to create the BoundedContexts for
     */
    @SuppressWarnings("unused") // Used for temporal test purposes
    @VisibleForTesting
    public static void injectStorageFactory(StorageFactory storageFactory) {
        checkNotNull(storageFactory);
        storageFactorySwitch.init(FAILING_STORAGE_SUPPLIER,
                                  () -> storageFactory);
    }

    @SuppressWarnings("Guava" /* Spine API is Java 7-based
                                 and uses `Optional` from Google Guava. */)
    @VisibleForTesting
    static BoundedContext createBoundedContext(EventBus.Builder eventBus) {
        final Optional<StorageFactory> storageFactory = eventBus.getStorageFactory();
        if (!storageFactory.isPresent()) {
            throw newIllegalStateException("EventBus does not specify a StorageFactory.");
        }
        return BoundedContext.newBuilder()
                             .setStorageFactorySupplier(storageFactory::get)
                             .setName(NAME)
                             .setEventBus(eventBus)
                             .build();
    }

    private static Properties loadProps(String path) {
        final Properties props = new Properties();
        try (InputStream in = BoundedContexts.class.getClassLoader().getResourceAsStream(path)) {
            props.load(in);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        return props;
    }
}
