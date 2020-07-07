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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.util.Exceptions.illegalStateWithCauseOf;
import static io.spine.util.Exceptions.newIllegalStateException;

/**
 * Properties for connecting to a database.
 */
public final class DbConnectionProperties {

    private static final String NAME = "db.name";
    private static final String PASSWORD = "db.password";
    private static final String PREFIX = "db.prefix";
    private static final String INSTANCE = "db.instance";
    private static final String USERNAME = "db.username";

    private final Map<String, String> properties;

    private DbConnectionProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    /**
     * Tries to load the properties from a resource file with the specified name.
     *
     * <p>If there was an IO error reading the file, an {@code IllegalStateException} is thrown.
     *
     * @param fileName
     *         name of the resource file with the DB properties
     */
    public static DbConnectionProperties fromResourceFile(String fileName) {
        Properties properties = loadProperties(fileName);
        ImmutableMap<String, String> map = Maps.fromProperties(properties);
        return new DbConnectionProperties(map);
    }

    /**
     * Returns a new builder for manual composition of the DB options.
     *
     * @return a new builder instance
     */
    public static Builder newBuilder() {
        return new Builder();
    }

    /** Returns the name of the database. */
    public String dbName() {
        String result = value(NAME);
        return result;
    }

    /**
     * Returns the credentials for connecting to the database.
     *
     * <p>Credentials are assembled from 2 values: username and password, which are specified
     * separately when building the connection properties object.
     */
    public DbCredentials credentials() {
        String username = value(USERNAME);
        String password = value(PASSWORD);

        DbCredentials result = DbCredentials
                .newBuilder()
                .setUsername(username)
                .setPassword(password)
                .vBuild();
        return result;
    }

    /**
     * Returns the name of the instance to connect to.
     *
     * <p>Applicable to non-local databases only.
     */
    public String instanceName() {
        String result = value(INSTANCE);
        return result;
    }

    /** Returns a prefix for the DB connection URL. */
    public DbUrlPrefix connectionUrlPrefix() {
        String stringValue = value(PREFIX);
        DbUrlPrefix result = new DbUrlPrefix(stringValue);
        return result;
    }

    public Builder toBuilder() {
        return new Builder(new HashMap<>(properties));
    }

    private String value(String key) {
        checkNotNull(key);
        return Optional.ofNullable(properties.get(key))
                       .orElseThrow(() -> newIllegalStateException(
                               "Could not read `%s` from the database connection properties.",
                               key));
    }

    private static Properties loadProperties(String propertiesFile) {
        Properties properties = new Properties();
        InputStream stream = DbConnectionProperties.class.getClassLoader()
                                                         .getResourceAsStream(propertiesFile);
        try {
            properties.load(stream);
        } catch (IOException e) {
            throw illegalStateWithCauseOf(e);
        }
        return properties;
    }

    /**
     * A builder of DB connection properties.
     */
    public static class Builder {

        private final Map<String, String> properties;

        private Builder(Map<String, String> props) {
            this.properties = props;
        }

        private Builder() {
            this.properties = new HashMap<>(5);
        }

        /** Sets the database name to specified one. */
        public Builder setDbName(String name) {
            checkNotNull(name);
            properties.put(NAME, name);
            return this;
        }

        /** Sets username to the to the specified one. */
        public Builder setUsername(String username) {
            checkNotNull(username);
            properties.put(USERNAME, username);
            return this;
        }

        /** Sets the password to the specified one. */
        public Builder setPassword(String password) {
            checkNotNull(password);
            properties.put(PASSWORD, password);
            return this;
        }

        /** Sets the connection URL prefix to the specified one. */
        public Builder setUrlPrefix(String urlPrefix) {
            checkNotNull(urlPrefix);
            properties.put(PREFIX, urlPrefix);
            return this;
        }

        /** Sets the instance name to the specified one. */
        public Builder setInstanceName(String instanceName) {
            checkNotNull(instanceName);
            properties.put(INSTANCE, instanceName);
            return this;
        }

        /** Returns a new instance of the DB connection properties. */
        public DbConnectionProperties build() {
            properties.forEach((k, v) -> checkNotNull(v));
            return new DbConnectionProperties(new HashMap<>(properties));
        }
    }
}
