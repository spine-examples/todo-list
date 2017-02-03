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
import org.spine3.examples.todolist.repositories.TaskDefinitionRepository;
import org.spine3.examples.todolist.repositories.TaskLabelsRepository;
import org.spine3.server.BoundedContext;
import org.spine3.server.command.CommandBus;
import org.spine3.server.storage.StorageFactory;
import org.spine3.server.storage.StorageFactorySwitch;

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
        final BoundedContext boundedContext = initBoundedContext();
        final StorageFactorySwitch factorySwitch = StorageFactorySwitch.getInstance();
        final StorageFactory storageFactory = factorySwitch.get();
        boundedContext.register(getTaskDefinitionRepository(boundedContext, storageFactory));
        boundedContext.register(getTaskLabelsRepository(boundedContext, storageFactory));
        return boundedContext;
    }

    private static BoundedContext initBoundedContext() {
        final BoundedContext boundedContext = BoundedContext.newBuilder()
                                                            .setName(NAME)
                                                            .build();
        return boundedContext;
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
