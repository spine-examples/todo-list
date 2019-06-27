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

import com.google.api.gax.grpc.GrpcCallContext;
import com.google.auth.oauth2.GoogleCredentials;
import io.grpc.CallCredentials;
import io.grpc.CallOptions;
import io.grpc.ManagedChannel;
import io.grpc.auth.MoreCallCredentials;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.spine.server.ContextSpec;
import io.spine.server.trace.TracerFactory;
import io.spine.server.trace.stackdriver.StackdriverTracerFactory;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Factory of {@link io.spine.server.trace.TracerFactory} instances.
 */
final class Tracing {

    /**
     * Creates a {@link TracerFactory} for the given context.
     *
     * @param context
     *         bounded context info
     * @return new tracer factory
     */
    static TracerFactory createTracing(ContextSpec context) {
        checkNotNull(context);
        GrpcCallContext callContext = callContext();
        String projectId = Configuration.instance()
                                        .projectId();
        StackdriverTracerFactory result = StackdriverTracerFactory
                .newBuilder()
                .setGcpProjectId(projectId)
                .setCallContext(callContext)
                .setContext(context.name())
                .build();
        return result;
    }

    private static GrpcCallContext callContext() {
        GoogleCredentials credentials = GoogleAuth.serviceAccountCredentials();
        CallCredentials callCredentials = MoreCallCredentials.from(credentials);
        CallOptions options = CallOptions
                .DEFAULT
                .withCallCredentials(callCredentials);
        ManagedChannel channel = NettyChannelBuilder
                .forTarget(StackdriverTracerFactory.stackdriverEndpoint())
                .build();
        return GrpcCallContext
                .createDefault()
                .withChannel(channel)
                .withCallOptions(options);
    }

}
