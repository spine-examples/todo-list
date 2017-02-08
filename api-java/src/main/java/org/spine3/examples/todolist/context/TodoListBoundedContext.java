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
import org.spine3.examples.todolist.repositories.LabelAggregateRepository;
import org.spine3.examples.todolist.repositories.LabelledTasksViewRepository;
import org.spine3.examples.todolist.repositories.MyListViewRepository;
import org.spine3.examples.todolist.repositories.TaskDefinitionRepository;
import org.spine3.examples.todolist.repositories.TaskLabelsRepository;
import org.spine3.server.BoundedContext;
import org.spine3.server.event.EventBus;

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

    @VisibleForTesting
    public static BoundedContext createTestInstance() {
        final BoundedContext result = create();
        return result;
    }

    private TodoListBoundedContext() {
        // Disable instantiation from outside.
    }

    /**
     * Creates new instance of the Tasks Bounded Context.
     */
    private static BoundedContext create() {
        final BoundedContext boundedContext = createBoundedContext();

        final TaskDefinitionRepository taskDefinitionRepository = new TaskDefinitionRepository(boundedContext);
        final LabelAggregateRepository labelAggregateRepository = new LabelAggregateRepository(boundedContext);
        final TaskLabelsRepository taskLabelsRepository = new TaskLabelsRepository(boundedContext);

        boundedContext.register(taskDefinitionRepository);
        boundedContext.register(taskLabelsRepository);
        boundedContext.register(labelAggregateRepository);
        boundedContext.register(new MyListViewRepository(boundedContext));
        boundedContext.register(new LabelledTasksViewRepository(boundedContext));
        boundedContext.register(new DraftTasksViewRepository(boundedContext));

        final TodoListRepositoryProvider repositoryProvider =
                createRepositoryProvider(taskDefinitionRepository, taskLabelsRepository, labelAggregateRepository);
        enrichEventBus(boundedContext.getEventBus(), repositoryProvider);
        return boundedContext;
    }

    private static TodoListRepositoryProvider createRepositoryProvider(TaskDefinitionRepository taskDefinitionRepo,
                                                                       TaskLabelsRepository taskLabelsRepo,
                                                                       LabelAggregateRepository labelAggregateRepo) {
        final TodoListRepositoryProvider repositoryProvider = new TodoListRepositoryProvider();
        repositoryProvider.setLabelRepository(labelAggregateRepo);
        repositoryProvider.setTaskLabelsRepository(taskLabelsRepo);
        repositoryProvider.setTaskDefinitionRepository(taskDefinitionRepo);
        return repositoryProvider;
    }

    private static void enrichEventBus(EventBus eventBus, TodoListRepositoryProvider repositoryProvider) {
        EventBusEnricher.newBuilder()
                        .setEventBus(eventBus)
                        .setRepositoryProvider(repositoryProvider)
                        .build()
                        .enrich();
    }

    private static BoundedContext createBoundedContext() {
        final BoundedContext result = BoundedContext.newBuilder()
                                                    .setName(NAME)
                                                    .build();
        return result;
    }

    /** The holder for the singleton reference. */
    private enum Singleton {
        INSTANCE;

        @SuppressWarnings("NonSerializableFieldInSerializableClass")
        private final BoundedContext value = create();
    }
}
