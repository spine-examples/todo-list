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
import io.spine.examples.todolist.LabelId;
import io.spine.examples.todolist.TaskId;
import io.spine.examples.todolist.c.aggregate.LabelAggregate;
import io.spine.examples.todolist.c.aggregate.TaskAggregateRoot;
import io.spine.examples.todolist.c.aggregate.TaskLabelsPart;
import io.spine.examples.todolist.c.aggregate.TaskPart;
import io.spine.examples.todolist.repository.DraftTasksViewRepository;
import io.spine.examples.todolist.repository.LabelAggregateRepository;
import io.spine.examples.todolist.repository.LabelledTasksViewRepository;
import io.spine.examples.todolist.repository.MyListViewRepository;
import io.spine.examples.todolist.repository.TaskLabelsRepository;
import io.spine.examples.todolist.repository.TaskRepository;
import io.spine.server.BoundedContext;
import io.spine.server.aggregate.AggregatePartRepository;
import io.spine.server.aggregate.AggregateRepository;
import io.spine.server.event.EventBus;
import io.spine.server.event.EventEnricher;
import io.spine.server.storage.StorageFactory;
import io.spine.server.storage.StorageFactorySwitch;
import io.spine.server.storage.memory.InMemoryStorageFactory;

import static io.spine.server.storage.StorageFactorySwitch.newInstance;
import static io.spine.util.Exceptions.newIllegalStateException;

/**
 * Utilities for creation the {@link BoundedContext} instances.
 *
 * @author Illia Shepilov
 * @author Dmytro Grankin
 */
public class BoundedContextFactory {

    /** The name of the Bounded Context. */
    private static final String NAME = "TodoListBoundedContext";
    private static final BoundedContextName BOUNDED_CONTEXT_NAME = BoundedContext.newName(NAME);

    /**
     * Default production scope {@link StorageFactory} supplier.
     *
     * <p>Throws an {@link IllegalStateException} on any invocation, as the {@link StorageFactory}
     * for production should be specified explicitly.
     */
    @SuppressWarnings("Guava") // Spine Java 7 API.
    private static final Supplier<StorageFactory> FAILING_STORAGE_SUPPLIER = () -> {
        throw newIllegalStateException(
                "Use BoundedContextFactory.create(StorageFactory) in production code.");
    };

    /**
     * Default test scope {@link StorageFactory} supplier.
     *
     * <p>Creates a new {@link InMemoryStorageFactory}.
     */
    @SuppressWarnings("Guava") // Spine Java 7 API.
    private static final Supplier<StorageFactory> IN_MEM_STORAGE_SUPPLIER =
            () -> InMemoryStorageFactory.newInstance(BOUNDED_CONTEXT_NAME, false);

    private final StorageFactorySwitch storageFactorySwitch;

    public static BoundedContextName getDefaultName() {
        return BOUNDED_CONTEXT_NAME;
    }

    protected BoundedContextFactory(StorageFactorySwitch storageFactorySwitch) {
        this.storageFactorySwitch = storageFactorySwitch;
    }

    /**
     * Retrieves a test instance of {@code BoundedContextFactory}.
     *
     * <p>The instance uses the {@linkplain InMemoryStorageFactory in-memory storage} by default.
     *
     * <p>For the production instance call {@link #instance(StorageFactory)}.
     *
     * @return an instance of {@code BoundedContextFactory} for tests
     */
    @VisibleForTesting
    public static BoundedContextFactory instance() {
        return Default.INSTANCE.value;
    }

    /**
     * Retrieves an instance of {@code BoundedContextFactory} with the given
     * {@linkplain StorageFactory storage} implementation.
     *
     * @param storageFactory the {@link StorageFactory} to use in the created
     *                       {@linkplain BoundedContext bounded contexts}
     * @return an instance of {@code BoundedContextFactory}
     */
    public static BoundedContextFactory instance(StorageFactory storageFactory) {
        final StorageFactorySwitch storageSwitch = newInstance(BOUNDED_CONTEXT_NAME, false)
                .init(() -> storageFactory, () -> storageFactory);
        final BoundedContextFactory factory = new BoundedContextFactory(storageSwitch);
        return factory;
    }

    /**
     * Creates the {@link BoundedContext} instance using the {@code StorageFactory} from
     * the {@link #storageFactorySwitch}.
     *
     * @return new test {@link BoundedContext} instance
     */
    public final BoundedContext create() {
        final StorageFactory storageFactory = storageFactorySwitch.get();

        final AggregateRepository<LabelId, LabelAggregate> labelAggregateRepo =
                labelAggregateRepository();
        final AggregatePartRepository<TaskId, TaskPart, TaskAggregateRoot> taskRepo =
                taskRepository();
        final AggregatePartRepository<TaskId, TaskLabelsPart, TaskAggregateRoot> taskLabelsRepo =
                taskLabelsRepository();
        final MyListViewRepository myListViewRepo = new MyListViewRepository();
        final LabelledTasksViewRepository tasksViewRepo = new LabelledTasksViewRepository();
        final DraftTasksViewRepository draftTasksViewRepo = new DraftTasksViewRepository();

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

        onCreateBoundedContext(boundedContext);

        return boundedContext;
    }

    /**
     * A trigger method for extending {@link #create()} method logic.
     *
     * <p>This method is called after an instance on {@link BoundedContext} is created and all
     * the repositories are registered.
     *
     * @param bc the created {@link BoundedContext}
     * @implSpec Performs no action by default.
     */
    protected void onCreateBoundedContext(BoundedContext bc) {
        // NoOp
    }

    private static EventBus.Builder createEventBus(
            StorageFactory storageFactory,
            AggregateRepository<LabelId, LabelAggregate> labelRepo,
            AggregatePartRepository<TaskId, TaskPart, TaskAggregateRoot> taskRepo,
            AggregatePartRepository<TaskId, TaskLabelsPart, TaskAggregateRoot> labelsRepo) {
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
     * Creates an {@link AggregateRepository} for the {@link LabelAggregate}.
     *
     * <p>Override this method to inject a custom repository implementation into the created
     * {@linkplain BoundedContext bounded contexts}.
     *
     * @return an repository for {@link LabelAggregate}
     * @implSpec The default implementation creates an instance of {@link LabelAggregateRepository}.
     */
    protected AggregateRepository<LabelId, LabelAggregate> labelAggregateRepository() {
        return new LabelAggregateRepository();
    }

    /**
     * Creates an {@link AggregatePartRepository} for the {@link TaskPart}.
     *
     * <p>Override this method to inject a custom repository implementation into the created
     * {@linkplain BoundedContext bounded contexts}.
     *
     * @return an repository for {@link TaskPart}
     * @implSpec The default implementation creates an instance of {@link TaskRepository}.
     */
    protected AggregatePartRepository<TaskId, TaskPart, TaskAggregateRoot> taskRepository() {
        return new TaskRepository();
    }

    /**
     * Creates an {@link AggregatePartRepository} for the {@link TaskLabelsPart}.
     *
     * <p>Override this method to inject a custom repository implementation into the created
     * {@linkplain BoundedContext bounded contexts}.
     *
     * @return an repository for {@link TaskLabelsPart}
     * @implSpec The default implementation creates an instance of {@link TaskLabelsRepository}.
     */
    protected AggregatePartRepository<TaskId, TaskLabelsPart, TaskAggregateRoot>
    taskLabelsRepository() {
        return new TaskLabelsRepository();
    }

    @VisibleForTesting
    static BoundedContext createBoundedContext(EventBus.Builder eventBus) {
        @SuppressWarnings("Guava") // Spine Java 7 API.
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

    /**
     * A singleton enum holding the default instance of {@code BoundedContextFactory} used for
     * tests.
     *
     * @see #instance() for public access to the default instance
     * @see #instance(StorageFactory) for the production instances of {@code BoundedContextFactory}
     */
    private enum Default {
        INSTANCE;

        @SuppressWarnings("NonSerializableFieldInSerializableClass")
        private final BoundedContextFactory value = new BoundedContextFactory(defaultSwitch());

        private static StorageFactorySwitch defaultSwitch() {
            final StorageFactorySwitch result = newInstance(BOUNDED_CONTEXT_NAME, false);
            result.init(FAILING_STORAGE_SUPPLIER, IN_MEM_STORAGE_SUPPLIER);
            return result;
        }
    }
}
