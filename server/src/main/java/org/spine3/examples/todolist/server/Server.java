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

package org.spine3.examples.todolist.server;

import org.spine3.examples.todolist.LabelDetails;
import org.spine3.examples.todolist.LabelIdList;
import org.spine3.examples.todolist.Task;
import org.spine3.examples.todolist.TaskDetails;
import org.spine3.examples.todolist.TaskId;
import org.spine3.examples.todolist.TaskLabel;
import org.spine3.examples.todolist.TaskLabelId;
import org.spine3.examples.todolist.c.aggregates.TaskAggregate;
import org.spine3.examples.todolist.c.aggregates.TaskLabelAggregate;
import org.spine3.examples.todolist.repositories.DraftTasksViewRepository;
import org.spine3.examples.todolist.repositories.LabelledTasksViewRepository;
import org.spine3.examples.todolist.repositories.MyListViewRepository;
import org.spine3.examples.todolist.repositories.TaskAggregateRepository;
import org.spine3.examples.todolist.repositories.TaskLabelAggregateRepository;
import org.spine3.server.BoundedContext;
import org.spine3.server.CommandService;
import org.spine3.server.QueryService;
import org.spine3.server.SubscriptionService;
import org.spine3.server.event.enrich.EventEnricher;
import org.spine3.server.storage.StorageFactory;
import org.spine3.server.transport.GrpcContainer;

import java.io.IOException;
import java.util.List;
import java.util.function.Function;

import static org.spine3.client.ConnectionConstants.DEFAULT_CLIENT_SERVICE_PORT;
import static org.spine3.server.event.EventStore.log;

/**
 * Sample gRPC server implementation.
 *
 * @author Illia Shepilov
 */
public class Server {

    private final GrpcContainer grpcContainer;
    private final BoundedContext boundedContext;
    private Function<TaskLabelId, LabelDetails> taskLabelIdToLabelDetails;
    private Function<TaskId, TaskDetails> taskIdToTaskDetails;
    private Function<TaskId, LabelIdList> taskIdToLabelList;
    private TaskAggregateRepository taskAggregateRepository;
    private TaskLabelAggregateRepository taskLabelAggregateRepository;
    private MyListViewRepository myListViewRepository;
    private LabelledTasksViewRepository labelledViewRepository;
    private DraftTasksViewRepository draftTasksViewRepository;

    public Server(StorageFactory storageFactory) {
        initiEnricherFunctions();
        final EventEnricher eventEnricher = initEventEnricher();
        this.boundedContext = initBoundedContext(storageFactory, eventEnricher);
        initRepositories(storageFactory);
        registerRepositories();
        final CommandService commandService = initCommandService();
        final QueryService queryService = initQueryService();
        final SubscriptionService subscriptionService = initSubscriptionService();
        this.grpcContainer = initGrpcContainer(commandService, subscriptionService, queryService);
    }

    private void initiEnricherFunctions() {
        initLabelIdToDetailsFunction();
        initTaskIdToDetailsFunction();
        initTaskIdToLabelListFunction();
    }

    private void initLabelIdToDetailsFunction() {
        taskLabelIdToLabelDetails = labelId -> {
            if (labelId == null) {
                return LabelDetails.getDefaultInstance();
            }
            final TaskLabelAggregate aggregate = taskLabelAggregateRepository.load(labelId);
            final TaskLabel state = aggregate.getState();
            final LabelDetails details = LabelDetails.newBuilder()
                                                     .setColor(state.getColor())
                                                     .setTitle(state.getTitle())
                                                     .build();
            return details;
        };
    }

    private void initTaskIdToDetailsFunction() {
        taskIdToTaskDetails = taskId -> {
            if (taskId == null) {
                return TaskDetails.getDefaultInstance();
            }
            final TaskAggregate aggregate = taskAggregateRepository.load(taskId);
            final Task state = aggregate.getState();
            final TaskDetails details = TaskDetails.newBuilder()
                                                   .setDescription(state.getDescription())
                                                   .setPriority(state.getPriority())
                                                   .build();
            return details;
        };
    }

    private void initTaskIdToLabelListFunction() {
        taskIdToLabelList = taskId -> {
            final TaskAggregate aggregate = taskAggregateRepository.load(taskId);
            final List<TaskLabelId> labelIdsList = aggregate.getState()
                                                            .getLabelIdsList();
            final LabelIdList result = LabelIdList.newBuilder()
                                                  .addAllLabelId(labelIdsList)
                                                  .build();
            return result;
        };
    }

    private EventEnricher initEventEnricher() {
        final EventEnricher result = EventEnricher.newBuilder()
                                                  .addFieldEnrichment(TaskLabelId.class,
                                                                      LabelDetails.class,
                                                                      taskLabelIdToLabelDetails::apply)
                                                  .addFieldEnrichment(TaskId.class,
                                                                      TaskDetails.class,
                                                                      taskIdToTaskDetails::apply)
                                                  .addFieldEnrichment(TaskId.class,
                                                                      LabelIdList.class,
                                                                      taskIdToLabelList::apply)
                                                  .build();
        return result;
    }

    private QueryService initQueryService() {
        final QueryService result = QueryService.newBuilder()
                                                .add(boundedContext)
                                                .build();
        return result;
    }

    private SubscriptionService initSubscriptionService() {
        final SubscriptionService result = SubscriptionService.newBuilder()
                                                              .add(boundedContext)
                                                              .build();
        return result;
    }

    private CommandService initCommandService() {
        final CommandService result = CommandService.newBuilder()
                                                    .add(boundedContext)
                                                    .build();
        return result;
    }

    private static GrpcContainer initGrpcContainer(CommandService commandService,
                                                   SubscriptionService subscriptionService,
                                                   QueryService queryService) {
        final GrpcContainer result = GrpcContainer.newBuilder()
                                                  .addService(commandService)
                                                  .addService(subscriptionService)
                                                  .addService(queryService)
                                                  .setPort(DEFAULT_CLIENT_SERVICE_PORT)
                                                  .build();
        return result;
    }

    private static BoundedContext initBoundedContext(StorageFactory storageFactory, EventEnricher eventEnricher) {
        final BoundedContext result = BoundedContext.newBuilder()
                                                    .setStorageFactory(storageFactory)
                                                    .setEventEnricher(eventEnricher)
                                                    .build();
        return result;
    }

    private void initRepositories(StorageFactory storageFactory) {
        taskAggregateRepository = new TaskAggregateRepository(boundedContext);
        taskAggregateRepository.initStorage(storageFactory);

        taskLabelAggregateRepository = new TaskLabelAggregateRepository(boundedContext);
        taskLabelAggregateRepository.initStorage(storageFactory);

        myListViewRepository = new MyListViewRepository(boundedContext);
        myListViewRepository.initStorage(storageFactory);

        labelledViewRepository = new LabelledTasksViewRepository(boundedContext);
        labelledViewRepository.initStorage(storageFactory);

        draftTasksViewRepository = new DraftTasksViewRepository(boundedContext);
        draftTasksViewRepository.initStorage(storageFactory);
    }

    private void registerRepositories() {
        boundedContext.register(taskAggregateRepository);
        boundedContext.register(taskLabelAggregateRepository);
        boundedContext.register(myListViewRepository);
        boundedContext.register(labelledViewRepository);
        boundedContext.register(draftTasksViewRepository);
    }

    /**
     * Starts the service.
     *
     * @throws IOException if unable to bind
     */
    public void start() throws IOException {
        startServer();
        log().info("Server started, listening to commands on the port " + DEFAULT_CLIENT_SERVICE_PORT);
        awaitTermination();
    }

    private void startServer() throws IOException {
        grpcContainer.start();
        grpcContainer.addShutdownHook();
    }

    /**
     * Waits for the service to become terminated.
     */
    public void awaitTermination() {
        grpcContainer.awaitTermination();
    }

    /**
     * Initiates an orderly shutdown of {@link GrpcContainer} and closes {@link BoundedContext}.
     *
     * <p> Closes the {@code BoundedContext} performing all necessary clean-ups.
     *
     * @throws Exception caused by closing one of the {@link BoundedContext} components
     */
    public void shutdown() throws Exception {
        grpcContainer.shutdown();
        boundedContext.close();
    }
}
