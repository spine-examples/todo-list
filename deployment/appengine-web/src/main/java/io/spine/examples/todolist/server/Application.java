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

package io.spine.examples.todolist.server;

import io.spine.examples.todolist.context.BoundedContexts;
import io.spine.net.Url;
import io.spine.server.BoundedContext;
import io.spine.server.CommandService;
import io.spine.server.QueryService;
import io.spine.web.firebase.DatabaseUrl;
import io.spine.web.firebase.FirebaseClient;
import io.spine.web.firebase.FirebaseCredentials;
import io.spine.web.firebase.query.FirebaseQueryBridge;
import io.spine.web.firebase.subscription.FirebaseSubscriptionBridge;
import io.spine.web.query.QueryBridge;

import static io.spine.examples.todolist.server.GoogleAuth.serviceAccountCredential;
import static io.spine.web.firebase.FirebaseClientFactory.restClient;

/**
 * The TodoList application.
 */
final class Application {

    private static final StartUpLogger log = StartUpLogger.instance();

    private final CommandService commandService;
    private final FirebaseQueryBridge queryBridge;
    private final FirebaseSubscriptionBridge subscriptionBridge;

    private static final Application INSTANCE = create();

    /**
     * Returns the application instance.
     */
    public static Application application() {
        return INSTANCE;
    }

    private Application(CommandService commandService,
                        QueryService queryService,
                        FirebaseClient firebaseClient) {
        this.commandService = commandService;
        this.queryBridge = newQueryBridge(queryService, firebaseClient);
        this.subscriptionBridge = newSubscriptionBridge(queryService, firebaseClient);
    }

    private static Application create() {
        BoundedContext boundedContext =
                BoundedContexts.create(Storage::createStorage, Tracing::createTracing);
        log.log("Initializing Command/Query services.");
        CommandService commandService =
                CommandService.newBuilder()
                              .add(boundedContext)
                              .build();
        QueryService queryService =
                QueryService.newBuilder()
                            .add(boundedContext)
                            .build();
        log.log("Initializing Firebase Realtime Database client.");
        Application application = new Application(commandService, queryService, firebaseClient());
        log.log("Application initialized");
        return application;
    }

    QueryBridge queryBridge() {
        return queryBridge;
    }

    CommandService commandService() {
        return commandService;
    }

    FirebaseSubscriptionBridge subscriptionBridge() {
        return subscriptionBridge;
    }

    private static FirebaseClient firebaseClient() {
        FirebaseCredentials credentials =
                FirebaseCredentials.fromGoogleCredentials(serviceAccountCredential());
        FirebaseClient client = restClient(databaseUrl(), credentials);
        return client;
    }

    private static FirebaseQueryBridge newQueryBridge(QueryService queryService,
                                                      FirebaseClient firebaseClient) {
        return FirebaseQueryBridge.newBuilder()
                                  .setQueryService(queryService)
                                  .setFirebaseClient(firebaseClient)
                                  .build();
    }

    private static FirebaseSubscriptionBridge newSubscriptionBridge(QueryService queryService,
                                                                    FirebaseClient firebaseClient) {
        return FirebaseSubscriptionBridge.newBuilder()
                                         .setQueryService(queryService)
                                         .setFirebaseClient(firebaseClient)
                                         .build();
    }

    private static DatabaseUrl databaseUrl() {
        String firebaseDatabaseUrl = Configuration.instance()
                                           .firebaseDatabaseUrl();
        Url url = Url
                .newBuilder()
                .setSpec(firebaseDatabaseUrl)
                .buildPartial();
        DatabaseUrl databaseUrl = DatabaseUrl
                .newBuilder()
                .setUrl(url)
                .vBuild();
        return databaseUrl;
    }
}
