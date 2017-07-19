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
import io.spine.examples.todolist.repository.DraftTasksViewRepository;
import io.spine.examples.todolist.repository.LabelAggregateRepository;
import io.spine.examples.todolist.repository.LabelledTasksViewRepository;
import io.spine.examples.todolist.repository.MyListViewRepository;
import io.spine.examples.todolist.repository.TaskLabelsRepository;
import io.spine.examples.todolist.repository.TaskRepository;
import io.spine.server.BoundedContext;
import io.spine.server.storage.StorageFactory;
import io.spine.server.storage.memory.InMemoryStorageFactory;

import java.util.function.Supplier;

/**
 * Serves for creation the {@link BoundedContext} instances.
 *
 * @author Illia Shepilov
 */
public class TodoListBoundedContext {

    /** The name of the Bounded Context. */
    private static final String NAME = "TodoListBoundedContext";

    private static final StorageFactory storageFactory = InMemoryStorageFactory.getInstance(false);

    /**
     * Obtains the reference to the singleton {@link BoundedContext}.
     */
    public static BoundedContext getInstance() {
        return Singleton.INSTANCE.value;
    }

    /**
     * Creates and returns the {@link BoundedContext} instance.
     *
     * <p>Serves only for test needs.
     *
     * @return the {@link BoundedContext} instance
     */
    @VisibleForTesting
    public static BoundedContext createTestInstance() {
        final BoundedContext result = create();
        return result;
    }

    private TodoListBoundedContext() {
        // Disable instantiation from outside.
    }

    /**
     * Creates a new instance of the Bounded Context.
     */
    private static BoundedContext create() {
        final BoundedContext boundedContext = createBoundedContext();

        final TaskRepository taskRepo = new TaskRepository();
        final LabelAggregateRepository labelAggregateRepo = new LabelAggregateRepository();
        final TaskLabelsRepository taskLabelsRepo = new TaskLabelsRepository();
        final MyListViewRepository myListViewRepo = new MyListViewRepository();
        final LabelledTasksViewRepository tasksViewRepo = new LabelledTasksViewRepository();
        final DraftTasksViewRepository draftTasksViewRepo = new DraftTasksViewRepository();

        boundedContext.register(taskRepo);
        boundedContext.register(taskLabelsRepo);
        boundedContext.register(labelAggregateRepo);
        boundedContext.register(myListViewRepo);
        boundedContext.register(tasksViewRepo);
        boundedContext.register(draftTasksViewRepo);

        TodoListEnrichmentConfiguration.newBuilder()
                                       .setLabelRepository(labelAggregateRepo)
                                       .setTaskRepository(taskRepo)
                                       .setTaskLabelsRepository(taskLabelsRepo)
                                       .apply(boundedContext.getEventBus())
                                       .addEnrichmentFields();
        return boundedContext;
    }

    private static BoundedContext createBoundedContext() {
        final Supplier<StorageFactory> storageFactorySupplier = () -> storageFactory;
        return BoundedContext.newBuilder()
                             .setStorageFactorySupplier(storageFactorySupplier::get)
                             .setName(NAME)
                             .build();
    }

    /** The holder for the singleton reference. */
    private enum Singleton {
        INSTANCE;

        @SuppressWarnings("NonSerializableFieldInSerializableClass")
        private final BoundedContext value = create();
    }
}
