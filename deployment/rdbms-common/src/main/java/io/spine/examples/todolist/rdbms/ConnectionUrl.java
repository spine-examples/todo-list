/*
 * Copyright 2021, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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

package io.spine.examples.todolist.rdbms;

import com.google.common.annotations.VisibleForTesting;
import io.spine.base.Tests;

/**
 * A URL for connecting to a database.
 */
public abstract class ConnectionUrl {

    @VisibleForTesting
    static final String LOCAL_H2_PROTOCOL = "jdbc:h2:mem:";

    private final ConnectionProperties properties;

    /**
     * Constructs a new URL based on the specified properties.
     *
     * <p>Properties are used to {@linkplain #stringValue(ConnectionProperties) compose a string
     * value}.
     */
    protected ConnectionUrl(ConnectionProperties properties) {
        this.properties = properties;
    }

    /**
     * Composes a database connection string based on the specified properties.
     */
    protected abstract String stringValue(ConnectionProperties properties);

    private String composeString(ConnectionProperties props) {
        boolean isTests = props.environmentType()
                               .equals(Tests.class);
        ConnectionProperties properties = isTests
                                          ? props.toBuilder()
                                                 .setConnectionProtocol(LOCAL_H2_PROTOCOL)
                                                 .build()
                                          : props;
        String result = stringValue(properties);
        return result;
    }

    @VisibleForTesting
    public ConnectionProperties properties() {
        return properties;
    }

    /**
     * Returns a string representation of this URL usable for connecting to a database.
     */
    @Override
    public final String toString() {
        String result = composeString(properties);
        return result;
    }
}
