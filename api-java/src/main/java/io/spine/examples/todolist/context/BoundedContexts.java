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

package io.spine.examples.todolist.context;

import com.google.common.annotations.VisibleForTesting;
import io.spine.core.BoundedContextNames;
import io.spine.examples.todolist.repository.DeletedTaskProjectionRepository;
import io.spine.examples.todolist.repository.DraftTasksViewRepository;
import io.spine.examples.todolist.repository.LabelAggregateRepository;
import io.spine.examples.todolist.repository.LabelledTasksViewRepository;
import io.spine.examples.todolist.repository.MyListViewRepository;
import io.spine.examples.todolist.repository.TaskCreationWizardRepository;
import io.spine.examples.todolist.repository.TaskLabelsRepository;
import io.spine.examples.todolist.repository.TaskRepository;
import io.spine.server.BoundedContext;
import io.spine.server.enrich.Enricher;
import io.spine.server.event.EventBus;
import io.spine.server.storage.StorageFactory;
import io.spine.server.storage.memory.InMemoryStorageFactory;

import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.util.Exceptions.newIllegalStateException;

/**
 * Utilities for creation the {@link BoundedContext} instances.
 */
public final class BoundedContexts {

    /** The default name of the {@code BoundedContext}. */
    @SuppressWarnings("DuplicateStringLiteralInspection") // Duplication with tests.
    private static final String NAME = "TodoListBoundedContext";

    private static final StorageFactory IN_MEMORY_FACTORY =
            InMemoryStorageFactory.newInstance(BoundedContextNames.newName(NAME), false);

    /** Prevents instantiation of this utility class. */
    private BoundedContexts() {
    }

    /**
     * Creates the {@link BoundedContext} instance
     * using {@code InMemoryStorageFactory} for a single tenant.
     *
     * @return the {@link BoundedContext} instance
     */
    public static BoundedContext create() {
        BoundedContext result = create(IN_MEMORY_FACTORY);
        return result;
    }

    /**
     * Creates a new instance of the {@link BoundedContext}
     * using the specified {@link StorageFactory}.
     *
     * @param storageFactory
     *         the storage factory to use
     * @return the bounded context created with the storage factory
     */
    public static BoundedContext create(StorageFactory storageFactory) {
        checkNotNull(storageFactory);

        LabelAggregateRepository labelAggregateRepo = new LabelAggregateRepository();
        TaskRepository taskRepo = new TaskRepository();
        TaskLabelsRepository taskLabelsRepo = new TaskLabelsRepository();

        MyListViewRepository myListViewRepo = new MyListViewRepository();
        LabelledTasksViewRepository tasksViewRepo = new LabelledTasksViewRepository();
        DraftTasksViewRepository draftTasksViewRepo = new DraftTasksViewRepository();
        DeletedTaskProjectionRepository deletedTasksRepo = new DeletedTaskProjectionRepository();

        TaskCreationWizardRepository taskCreationRepo = new TaskCreationWizardRepository();

        EventBus.Builder eventBus = createEventBus(storageFactory,
                                                   labelAggregateRepo,
                                                   taskRepo,
                                                   taskLabelsRepo);
        BoundedContext boundedContext = createBoundedContext(eventBus);

        boundedContext.register(taskRepo);
        boundedContext.register(taskLabelsRepo);
        boundedContext.register(labelAggregateRepo);
        boundedContext.register(myListViewRepo);
        boundedContext.register(tasksViewRepo);
        boundedContext.register(draftTasksViewRepo);
        boundedContext.register(taskCreationRepo);
        boundedContext.register(deletedTasksRepo);

        return boundedContext;
    }

    private static EventBus.Builder createEventBus(StorageFactory storageFactory,
                                                   LabelAggregateRepository labelRepo,
                                                   TaskRepository taskRepo,
                                                   TaskLabelsRepository labelsRepo) {
        Enricher enricher = TodoListEnrichments
                .newBuilder()
                .setLabelRepository(labelRepo)
                .setTaskRepository(taskRepo)
                .setTaskLabelsRepository(labelsRepo)
                .build()
                .createEnricher();
        EventBus.Builder eventBus = EventBus
                .newBuilder()
                .setEnricher(enricher)
                .setStorageFactory(storageFactory);
        return eventBus;
    }

    @VisibleForTesting
    static BoundedContext createBoundedContext(EventBus.Builder eventBus) {
        Optional<StorageFactory> storageFactory = eventBus.getStorageFactory();
        if (!storageFactory.isPresent()) {
            throw newIllegalStateException("EventBus does not specify a StorageFactory.");
        }
        return BoundedContext.newBuilder()
                             .setStorageFactorySupplier(storageFactory::get)
                             .setName(NAME)
                             .setEventBus(eventBus)
                             .build();
    }
}
