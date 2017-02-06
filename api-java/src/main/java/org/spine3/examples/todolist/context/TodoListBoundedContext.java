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

package org.spine3.examples.todolist.context;

import com.google.common.annotations.VisibleForTesting;
import org.spine3.examples.todolist.repositories.DraftTasksViewRepository;
import org.spine3.examples.todolist.repositories.LabelledTasksViewRepository;
import org.spine3.examples.todolist.repositories.MyListViewRepository;
import org.spine3.examples.todolist.repositories.TaskDefinitionRepository;
import org.spine3.examples.todolist.repositories.TaskLabelsRepository;
import org.spine3.server.BoundedContext;
import org.spine3.server.command.CommandBus;
import org.spine3.server.event.EventBus;
import org.spine3.server.event.enrich.EventEnricher;
import org.spine3.server.storage.StorageFactory;
import org.spine3.server.storage.StorageFactorySwitch;

import java.util.function.Supplier;

/**
 * @author Illia Shepilov
 */
public class TodoListBoundedContext {

    /** The name of the Bounded Context. */
    private static final String NAME = "Tasks";

    /**
     * Obtains the reference to the Tasks Bounded Context.
     */
    public static BoundedContext getInstance() {
        return Singleton.INSTANCE.value;
    }

    private TodoListBoundedContext() {
        // Disable instantiation from outside.
    }

    /**
     * Creates new instance of the Tasks Bounded Context.
     */
    private static BoundedContext create() {
        final StorageFactorySwitch factorySwitch = StorageFactorySwitch.getInstance();
        final StorageFactory storageFactory = factorySwitch.get();
        final TodoListRepositoryProvider repositoryProvider = new TodoListRepositoryProvider();
        final EventEnricher eventEnricher = initEventEnricher(repositoryProvider);
        final EventBus eventBus = initEventBus(storageFactory, eventEnricher);
        final BoundedContext boundedContext = initBoundedContext(eventBus);

        final TaskDefinitionRepository taskDefinitionRepository =
                getTaskDefinitionRepository(boundedContext, storageFactory);
        boundedContext.register(taskDefinitionRepository);
        final TaskLabelsRepository taskLabelsRepository = getTaskLabelsRepository(boundedContext, storageFactory);
        boundedContext.register(taskLabelsRepository);
        boundedContext.register(getMyListViewRepository(boundedContext, storageFactory));
        boundedContext.register(getLabelledTasksViewRepository(boundedContext, storageFactory));
        boundedContext.register(getDraftTasksViewRepository(boundedContext, storageFactory));
        repositoryProvider.setTaskDefinitionRepository(taskDefinitionRepository);
        repositoryProvider.setTaskLabelsRepository(taskLabelsRepository);
        return boundedContext;
    }

    private static EventEnricher initEventEnricher(TodoListRepositoryProvider repositoryProvider) {
        final Supplier<EventEnricher> eventEnricherSupplier =
                EventEnricherSupplier.newBuilder()
                                     .setRepositoryProvider(repositoryProvider)
                                     .build();

        final EventEnricher result = eventEnricherSupplier.get();
        return result;
    }

    private static EventBus initEventBus(StorageFactory storageFactory, EventEnricher eventEnricher) {
        final EventBus result = EventBus.newBuilder()
                                        .setStorageFactory(storageFactory)
                                        .setEnricher(eventEnricher)
                                        .build();
        return result;
    }

    private static BoundedContext initBoundedContext(EventBus eventBus) {
        final BoundedContext result = BoundedContext.newBuilder()
                                                    .setName(NAME)
                                                    .setEventBus(eventBus)
                                                    .build();
        return result;
    }

    private static TaskDefinitionRepository getTaskDefinitionRepository(BoundedContext boundedContext,
                                                                        StorageFactory storageFactory) {
        final TaskDefinitionRepository taskDefinitionRepo = new TaskDefinitionRepository(boundedContext);
        taskDefinitionRepo.initStorage(storageFactory);
        return taskDefinitionRepo;
    }

    private static TaskLabelsRepository getTaskLabelsRepository(BoundedContext boundedContext,
                                                                StorageFactory storageFactory) {
        final TaskLabelsRepository taskLabelsRepo = new TaskLabelsRepository(boundedContext);
        taskLabelsRepo.initStorage(storageFactory);
        return taskLabelsRepo;
    }

    private static MyListViewRepository getMyListViewRepository(BoundedContext boundedContext,
                                                                StorageFactory storageFactory) {
        final MyListViewRepository myListViewRepo = new MyListViewRepository(boundedContext);
        myListViewRepo.initStorage(storageFactory);
        return myListViewRepo;
    }

    private static LabelledTasksViewRepository getLabelledTasksViewRepository(BoundedContext boundedContext,
                                                                              StorageFactory storageFactory) {
        final LabelledTasksViewRepository labelledTasksViewRepo = new LabelledTasksViewRepository(boundedContext);
        labelledTasksViewRepo.initStorage(storageFactory);
        return labelledTasksViewRepo;
    }

    private static DraftTasksViewRepository getDraftTasksViewRepository(BoundedContext boundedContext,
                                                                        StorageFactory storageFactory) {
        final DraftTasksViewRepository draftTasksViewRepo = new DraftTasksViewRepository(boundedContext);
        draftTasksViewRepo.initStorage(storageFactory);
        return draftTasksViewRepo;
    }

    @VisibleForTesting
    public static CommandBus getCommandBus() {
        final CommandBus result = getInstance().getCommandBus();
        return result;
    }

    /** The holder for the singleton reference. */
    private enum Singleton {
        INSTANCE;

        @SuppressWarnings("NonSerializableFieldInSerializableClass")
        private final BoundedContext value = create();
    }
}
