/*
 * Copyright 2020, TeamDev. All rights reserved.
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
import io.spine.base.Environment;
import io.spine.base.Tests;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A prefix to the database connection URL string.
 *
 * <p>Get the actual value with {@link #toString()}.
 *
 * <p>If the value is obtained during tests, returns a predefined test value. Otherwise, attempts
 * to get the value from the specified {@link DbConnectionProperties}.
 */
public final class DbUrlPrefix {

    @VisibleForTesting
    public static final String LOCAL_H2 = "jdbc:h2:mem:";

    private final DbConnectionProperties properties;
    private final String testValue;

    /**
     * Creates a new instance of the connection URL prefix.
     *
     * @param properties
     *         properties to get the prefix value from
     * @param testsValue
     *         value to use for tests
     */
    public DbUrlPrefix(DbConnectionProperties properties, String testsValue) {
        checkNotNull(properties);
        checkNotNull(testsValue);
        this.properties = properties;
        this.testValue = testsValue;
    }

    /**
     * Returns a new connection URL prefix known to be usable to connect to local h2 databases.
     */
    public static DbUrlPrefix propsOrLocalH2(DbConnectionProperties properties) {
        return new DbUrlPrefix(properties, LOCAL_H2);
    }

    @Override
    public String toString() {
        Environment environment = Environment.instance();
        String result = environment.is(Tests.class)
                        ? testValue
                        : properties.connectionUrlPrefix();
        return result;
    }
}
