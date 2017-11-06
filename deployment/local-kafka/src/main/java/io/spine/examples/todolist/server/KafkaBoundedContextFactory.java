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

package io.spine.examples.todolist.server;

import com.google.common.base.Optional;
import io.spine.core.EventClass;
import io.spine.examples.todolist.LabelId;
import io.spine.examples.todolist.TaskId;
import io.spine.examples.todolist.c.aggregate.LabelAggregate;
import io.spine.examples.todolist.c.aggregate.TaskAggregateRoot;
import io.spine.examples.todolist.c.aggregate.TaskLabelsPart;
import io.spine.examples.todolist.c.aggregate.TaskPart;
import io.spine.examples.todolist.context.BoundedContextFactory;
import io.spine.examples.todolist.q.projection.DraftTasksView;
import io.spine.examples.todolist.q.projection.LabelledTasksView;
import io.spine.examples.todolist.q.projection.MyListView;
import io.spine.examples.todolist.server.repository.LabelKAggregateRepository;
import io.spine.examples.todolist.server.repository.TaskKRepository;
import io.spine.examples.todolist.server.repository.TaskLabelsKRepository;
import io.spine.server.BoundedContext;
import io.spine.server.aggregate.AggregatePartRepository;
import io.spine.server.aggregate.AggregateRepository;
import io.spine.server.catchup.KafkaCatchUp;
import io.spine.server.event.EventBus;
import io.spine.server.event.EventDispatcher;
import io.spine.server.projection.ProjectionRepository;
import io.spine.server.storage.StorageFactory;
import io.spine.server.storage.StorageFactorySwitch;
import io.spine.server.storage.kafka.KafkaStorageFactory;
import io.spine.server.storage.kafka.KafkaWrapper;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.Collection;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Stream;

import static io.spine.server.storage.kafka.Consistency.STRONG;
import static java.time.temporal.ChronoUnit.MILLIS;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

/**
 * A factory of {@link BoundedContext} instances based on Apache Kafka integration.
 *
 * <p>This implementation of {@link BoundedContextFactory} uses Kafka-based
 * {@linkplain KafkaStorageFactory storage}, {@linkplain KafkaCatchUp catch up} and
 * {@linkplain io.spine.server.aggregate.KAggregateRepository aggregate loading}.
 *
 * @author Dmytro Dashenkov
 */
public final class KafkaBoundedContextFactory extends BoundedContextFactory {

    private static final String KAFKA_STREAMS_PROPS_PATH = "config/kafka-streams.properties";
    private static final String KAFKA_PRODUCER_PROPS_PATH = "config/kafka-producer.properties";
    private static final String KAFKA_CONSUMER_PROPS_PATH = "config/kafka-consumer.properties";

    /**
     * The Kafka Streams configurations loaded from {@link #KAFKA_STREAMS_PROPS_PATH}.
     */
    private static final Properties streamsConfig;

    /**
     * The maximum time the storage poll operations may wait for the results to be returned.
     *
     * <p>Note that this is NOT the poll await used by the {@linkplain KafkaCatchUp catch up} and
     * {@linkplain io.spine.server.aggregate.KAggregateRepository aggregate loading} routines.
     * Those use the {@linkplain KafkaWrapper#DEFAULT_POLL_AWAIT default value}.
     *
     * @see KafkaStorageFactory.Builder#setMaxPollAwait(Duration)
     */
    private static final Duration STORAGE_MAX_POLL_AWAIT = Duration.of(50, MILLIS);

    /**
     * The {@link StorageFactorySwitch} containing the {@link KafkaStorageFactory} for both test
     * and production environments.
     *
     * <p>The {@linkplain io.spine.server.storage.Storage storages} produces by these factories use
     * {@link #STORAGE_MAX_POLL_AWAIT} as the max poll await and
     * {@linkplain io.spine.server.storage.kafka.Consistency#STRONG block} the thread upon write
     * operations.
     *
     * <p>The storage configurations may be found under paths {@link #KAFKA_PRODUCER_PROPS_PATH}
     * and {@link #KAFKA_CONSUMER_PROPS_PATH}.
     */
    private static final StorageFactorySwitch storageFactorySwitch;

    /**
     * The {@link KafkaWrapper} instance used while the {@linkplain KafkaCatchUp catch up} and
     * {@linkplain io.spine.server.aggregate.KAggregateRepository aggregate loading} for publishing
     * messages to Kafka.
     *
     * <p>This {@link KafkaWrapper} uses
     * the {@linkplain KafkaWrapper#create(Properties, Properties) default values} for
     * the {@link io.spine.server.storage.kafka.Consistency Consistency} and poll await.
     */
    private static final KafkaWrapper kafka;

    static {
        final Properties producerConfig = loadConfig(KAFKA_PRODUCER_PROPS_PATH);
        final Properties consumerConfig = loadConfig(KAFKA_CONSUMER_PROPS_PATH);
        final StorageFactory storageFactory =
                KafkaStorageFactory.newBuilder()
                                   .setProducerConfig(producerConfig)
                                   .setConsumerConfig(consumerConfig)
                                   .setMaxPollAwait(STORAGE_MAX_POLL_AWAIT)
                                   .setConsistencyLevel(STRONG)
                                   .build();
        storageFactorySwitch = StorageFactorySwitch.newInstance(getBoundedContextName(), false)
                                                   .init(() -> storageFactory,
                                                         () -> storageFactory);
        kafka = KafkaWrapper.create(producerConfig, consumerConfig);
        streamsConfig = loadConfig(KAFKA_STREAMS_PROPS_PATH);
    }

    private KafkaBoundedContextFactory() {
        super(storageFactorySwitch);
    }

    /**
     * {@inheritDoc}
     *
     * @implSpec Invokes the {@link KafkaCatchUp} start. See the
     * {@linkplain KafkaCatchUp#start doc} for the preconditions of a successful call.
     */
    @Override
    protected void onBoundedContestCreated(BoundedContext boundedContext) {
        setupCatchUp(boundedContext);
    }

    /**
     * {@inheritDoc}
     *
     * @implSpec Creates a new instance of {@link LabelKAggregateRepository}.
     */
    @Override
    protected AggregateRepository<LabelId, LabelAggregate> labelAggregateRepository() {
        return new LabelKAggregateRepository(streamsConfig, kafka);
    }

    @Override
    protected AggregatePartRepository<TaskId, TaskPart, TaskAggregateRoot> taskRepository() {
        return new TaskKRepository(streamsConfig, kafka);
    }

    @Override
    protected AggregatePartRepository<TaskId, TaskLabelsPart, TaskAggregateRoot>
    taskLabelsRepository() {
        return new TaskLabelsKRepository(streamsConfig, kafka);
    }

    @SuppressWarnings({
            "Guava", // Spine Java 7 API.
            "ConstantConditions" // Checked within the stream via filter(...).
    })
    private static void setupCatchUp(BoundedContext boundedContext) {
        final Collection<ProjectionRepository<?, ?, ?>> projectionRepos =
                Stream.of(MyListView.class, LabelledTasksView.class, DraftTasksView.class)
                      .map(boundedContext::findRepository)
                      .filter(Optional::isPresent)
                      .map(repositoryOptional ->
                                   (ProjectionRepository<?, ?, ?>) repositoryOptional.get())
                      .collect(toList());
        setupDispatchers(boundedContext.getEventBus(), projectionRepos);
        startCatchUp(projectionRepos);
    }

    private static void
    setupDispatchers(EventBus eventBus, Collection<? extends EventDispatcher<?>> typeSuppliers) {
        final Set<EventClass> eventClasses =
                typeSuppliers.stream()
                             .peek(eventBus::unregister)
                             .flatMap(dispatcher -> dispatcher.getMessageClasses().stream())
                             .distinct()
                             .collect(toSet());
        final EventDispatcher<?> dispatcher = KafkaCatchUp.dispatcher(eventClasses, kafka);
        eventBus.register(dispatcher);
    }

    private static void startCatchUp(Iterable<ProjectionRepository<?, ?, ?>> repos) {
        for (ProjectionRepository<?, ?, ?> repo : repos) {
            KafkaCatchUp.start(repo, streamsConfig);
        }
    }

    /**
     * Reads a {@code .properties} file from the classpath by the given path.
     *
     * @param path the file path (including extension)
     * @return the loaded {@link Properties}
     */
    private static Properties loadConfig(String path) {
        final ClassLoader loader = KafkaBoundedContextFactory.class.getClassLoader();
        final Properties props = new Properties();
        try (InputStream in = loader.getResourceAsStream(path)) {
            props.load(in);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        return props;
    }

    /**
     * Retrieves an instance of {@code KafkaBoundedContextFactory}.
     */
    public static KafkaBoundedContextFactory instance() {
        return Singleton.INSTANCE.value;
    }

    private enum Singleton {
        INSTANCE;
        @SuppressWarnings("NonSerializableFieldInSerializableClass")
        private final KafkaBoundedContextFactory value = new KafkaBoundedContextFactory();
    }
}
