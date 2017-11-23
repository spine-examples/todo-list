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

package io.spine.server.given;

import com.google.protobuf.Duration;
import com.google.protobuf.Message;
import com.google.protobuf.Timestamp;
import com.google.protobuf.util.Timestamps;
import io.spine.client.ActorRequestFactory;
import io.spine.client.CommandFactory;
import io.spine.core.CommandEnvelope;
import io.spine.core.Event;
import io.spine.core.EventContext;
import io.spine.core.Subscribe;
import io.spine.core.TenantId;
import io.spine.core.UserId;
import io.spine.people.PersonName;
import io.spine.server.BoundedContext;
import io.spine.server.FRChangeCustomerName;
import io.spine.server.FRCreateCustomer;
import io.spine.server.FRCustomer;
import io.spine.server.FRCustomerCreated;
import io.spine.server.FRCustomerId;
import io.spine.server.FRCustomerNameChanged;
import io.spine.server.FRCustomerVBuilder;
import io.spine.server.FRSession;
import io.spine.server.FRSessionId;
import io.spine.server.FRSessionVBuilder;
import io.spine.server.aggregate.Aggregate;
import io.spine.server.aggregate.AggregateRepository;
import io.spine.server.aggregate.Apply;
import io.spine.server.command.Assign;
import io.spine.server.command.TestEventFactory;
import io.spine.server.entity.Entity;
import io.spine.server.entity.Repository;
import io.spine.server.projection.Projection;
import io.spine.server.projection.ProjectionRepository;
import io.spine.server.stand.Stand;
import io.spine.string.Stringifier;
import io.spine.string.StringifierRegistry;
import io.spine.time.Time;

import java.text.ParseException;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;
import static io.spine.Identifier.newUuid;
import static io.spine.client.TestActorRequestFactory.newInstance;
import static io.spine.server.aggregate.AggregateMessageDispatcher.dispatchCommand;
import static io.spine.server.projection.ProjectionEventDispatcher.dispatch;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Test environment for
 * the {@link io.spine.server.FirebaseSubscriptionRepeater FirebaseSubscriptionRepeater} tests.
 *
 * @author Dmytro Dashenkov
 */
public final class FirebaseRepeaterTestEnv {

    private static final UserId TEST_ACTOR = UserId.newBuilder()
                                                   .setValue("Firebase repeater test")
                                                   .build();
    private static final ActorRequestFactory defaultRequestFactory = newInstance(TEST_ACTOR);
    private static final TestEventFactory eventFactory =
            TestEventFactory.newInstance(FirebaseRepeaterTestEnv.class);

    // Prevent utility class instantiation.
    private FirebaseRepeaterTestEnv() {
    }

    public static FRCustomerId newId() {
        return FRCustomerId.newBuilder()
                           .setUid(newUuid())
                           .build();
    }

    public static FRSessionId newSessionId() {
        return FRSessionId.newBuilder()
                          .setCustomerId(newId())
                          .setStartTime(Time.getCurrentTime())
                          .build();
    }

    public static void registerSessionIdStringifier() {
        final Stringifier<FRSessionId> stringifier = new Stringifier<FRSessionId>() {
            private static final String SEPARATOR = "::";

            @Override
            protected String toString(FRSessionId genericId) {
                final String customerUid = genericId.getCustomerId().getUid();
                final String timestamp = Timestamps.toString(genericId.getStartTime());
                return customerUid + SEPARATOR + timestamp;
            }

            @Override
            protected FRSessionId fromString(String stringId) {
                @SuppressWarnings("DynamicRegexReplaceableByCompiledPattern") // OK for tests.
                final String[] parts = stringId.split(SEPARATOR);
                checkArgument(parts.length == 2);
                final FRCustomerId customerId = FRCustomerId.newBuilder()
                                                            .setUid(parts[0])
                                                            .build();
                final Timestamp timestamp;
                try {
                    timestamp = Timestamps.parse(parts[1]);
                } catch (ParseException e) {
                    throw new IllegalArgumentException(e);
                }
                final FRSessionId result = FRSessionId.newBuilder()
                                                      .setCustomerId(customerId)
                                                      .setStartTime(timestamp)
                                                      .build();
                return result;
            }
        };
        StringifierRegistry.getInstance()
                           .register(stringifier, FRSessionId.class);
    }

    public static FRCustomer createTask(FRCustomerId customerId, BoundedContext boundedContext) {
        return createTask(customerId, boundedContext, defaultRequestFactory);
    }

    public static void createSession(FRSessionId sessionId, BoundedContext boundedContext) {
        final SessionProjection projection =
                createEntity(sessionId, boundedContext, FRSession.class);
        final FRCustomerCreated eventMsg = createdEvent(sessionId.getCustomerId());
        final Event event = eventFactory.createEvent(eventMsg);
        dispatch(projection, event);
        final Stand stand = boundedContext.getStand();
        stand.post(defaultTenant(), projection);
    }

    public static FRCustomer createTask(FRCustomerId customerId,
                                        BoundedContext boundedContext,
                                        TenantId tenantId) {
        return createTask(customerId, boundedContext, requestFactory(tenantId));
    }

    private static FRCustomer createTask(FRCustomerId customerId,
                                         BoundedContext boundedContext,
                                         ActorRequestFactory requestFactory) {
        final CustomerAggregate aggregate =
                createEntity(customerId, boundedContext, FRCustomer.class);
        final CommandFactory commandFactory = requestFactory.command();
        Stream.of(createCommand(customerId), updateCommand(customerId))
              .map(commandFactory::create)
              .map(CommandEnvelope::of)
              .forEach(cmd -> dispatchCommand(aggregate, cmd));
        final Stand stand = boundedContext.getStand();
        stand.post(defaultTenant(), aggregate);
        return aggregate.getState();
    }

    private static <I, E extends Entity<I, S>, S extends Message> E
    createEntity(I id, BoundedContext boundedContext, Class<S> stateClass) {
        @SuppressWarnings("unchecked")
        final Repository<I, E> repository =
                boundedContext.findRepository(stateClass).orNull();
        assertNotNull(repository);
        final E projection = repository.create(id);
        return projection;
    }

    private static ActorRequestFactory requestFactory(TenantId tenantId) {
        final ActorRequestFactory factory = newInstance(TEST_ACTOR, tenantId);
        return factory;
    }

    private static FRCreateCustomer createCommand(FRCustomerId id) {
        final FRCreateCustomer createCmd = FRCreateCustomer.newBuilder()
                                                           .setId(id)
                                                           .build();
        return createCmd;
    }

    private static FRChangeCustomerName updateCommand(FRCustomerId id) {
        final PersonName newName = PersonName.newBuilder()
                                             .setGivenName("John")
                                             .setFamilyName("Doe")
                                             .build();
        final FRChangeCustomerName updateCmd = FRChangeCustomerName.newBuilder()
                                                                   .setId(id)
                                                                   .setNewName(newName)
                                                                   .build();
        return updateCmd;
    }

    private static FRCustomerCreated createdEvent(FRCustomerId id) {
        final FRCustomerCreated createCmd = FRCustomerCreated.newBuilder()
                                                             .setId(id)
                                                             .build();
        return createCmd;
    }

    private static TenantId defaultTenant() {
        return TenantId.newBuilder()
                       .setValue("Default tenant")
                       .build();
    }

    public static class CustomerAggregate
            extends Aggregate<FRCustomerId, FRCustomer, FRCustomerVBuilder> {

        protected CustomerAggregate(FRCustomerId id) {
            super(id);
        }

        @SuppressWarnings("unused") // Reflective access.
        @Assign
        FRCustomerCreated handle(FRCreateCustomer command) {
            return createdEvent(command.getId());
        }

        @SuppressWarnings("unused") // Reflective access.
        @Assign
        FRCustomerNameChanged handle(FRChangeCustomerName command) {
            return FRCustomerNameChanged.newBuilder()
                                        .setNewName(command.getNewName())
                                        .build();
        }

        @SuppressWarnings("unused") // Reflective access.
        @Apply
        private void on(FRCustomerCreated event) {
            getBuilder().setId(event.getId());
        }

        @SuppressWarnings("unused") // Reflective access.
        @Apply
        private void on(FRCustomerNameChanged event) {
            getBuilder().setName(event.getNewName());
        }
    }

    public static class CustomerRepository
            extends AggregateRepository<FRCustomerId, CustomerAggregate> {}

    public static class SessionProjection
            extends Projection<FRSessionId, FRSession, FRSessionVBuilder> {

        protected SessionProjection(FRSessionId id) {
            super(id);
        }

        @SuppressWarnings("unused") // Reflective access.
        @Subscribe
        void on(FRCustomerCreated event, EventContext context) {
            getBuilder().setDuration(mockLogic(context));
        }

        private static Duration mockLogic(EventContext context) {
            final Timestamp currentTime = Time.getCurrentTime();
            final Timestamp eventTime = context.getTimestamp();
            final long durationSeconds = eventTime.getSeconds() - currentTime.getSeconds();
            final Duration duration = Duration.newBuilder()
                                              .setSeconds(durationSeconds)
                                              .build();
            return duration;
        }
    }

    public static class SessionRepository
            extends ProjectionRepository<FRSessionId, SessionProjection, FRSession> {}
}
