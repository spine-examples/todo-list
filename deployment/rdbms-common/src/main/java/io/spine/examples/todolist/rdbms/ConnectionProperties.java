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
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import io.spine.base.Environment;
import io.spine.base.EnvironmentType;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.util.Exceptions.illegalStateWithCauseOf;
import static io.spine.util.Exceptions.newIllegalStateException;

/**
 * Properties for connecting to a database.
 */
public final class ConnectionProperties {

    @VisibleForTesting
    static final String NAME = "db.name";
    @VisibleForTesting
    static final String INSTANCE = "db.instance";

    private static final String PROTOCOL = "db.protocol";
    private static final String USERNAME = "db.username";
    private static final String PASSWORD = "db.password";

    private final ImmutableMap<String, String> properties;
    private final Class<? extends EnvironmentType> envType;

    private ConnectionProperties(ImmutableMap<String, String> properties,
                                 Class<? extends EnvironmentType> envType) {
        this.properties = properties;
        this.envType = envType;
    }

    /**
     * Tries to load the properties from a resource file with the specified name.
     *
     * <p>If there was an IO error reading the file, an {@code IllegalStateException} is thrown.
     *
     * @param fileName
     *         name of the resource file with the DB properties
     */
    public static ConnectionProperties fromResourceFile(String fileName) {
        Properties properties = loadProperties(fileName);
        ImmutableMap<String, String> map = Maps.fromProperties(properties);
        return new ConnectionProperties(map, currentEnvType());
    }

    /**
     * Tries to assemble the properties from system properties, specified with the {@code -D} Java
     * option.
     *
     * @return the connection properties assembled using system properties
     * @see System#getProperty(String)
     */
    public static ConnectionProperties fromSystemProperties() {
        ImmutableMap.Builder<String, String> properties = ImmutableMap.builder();
        Stream.of(NAME, PASSWORD, PROTOCOL, INSTANCE, USERNAME)
              .forEach(systemProperty -> insertIfSet(properties, systemProperty));
        Class<? extends EnvironmentType> envType = Environment.instance()
                                                              .type();
        return new ConnectionProperties(properties.build(), envType);
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

        DbCredentials result =
                DbCredentials
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

    /** Returns a JDBC connection protocol. */
    public JdbcConnectionProtocol connectionProtocol() {
        String stringValue = value(PROTOCOL);
        JdbcConnectionProtocol result =
                JdbcConnectionProtocol
                        .newBuilder()
                        .setValue(stringValue)
                        .vBuild();
        return result;
    }

    /**
     * Returns {@code true} if this instance of properties has a DB name set.
     */
    public boolean hasDbName() {
        return has(NAME);
    }

    /**
     * Returns {@code true} if this instance of properties has both username and password set.
     */
    public boolean hasCredentials() {
        return has(USERNAME) && has(PASSWORD);
    }

    /**
     * Returns {@code true} if this instance of properties the instance name set.
     */
    public boolean hasInstanceName() {
        return has(INSTANCE);
    }

    /**
     * Returns a new {@code Builder} based on this instance.
     *
     * <p>This instance is not affected by changes to the returned builder.
     */
    public Builder toBuilder() {
        return new Builder(new HashMap<>(properties));
    }

    /**
     * Returns the type of the environment that the server connecting to a database is in.
     */
    Class<? extends EnvironmentType> environmentType() {
        return envType;
    }

    private boolean has(String key) {
        return properties.containsKey(key);
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
        InputStream stream = ConnectionProperties.class.getClassLoader()
                                                       .getResourceAsStream(propertiesFile);
        try {
            properties.load(stream);
        } catch (RuntimeException | IOException e) {
            throw illegalStateWithCauseOf(e);
        }
        return properties;
    }

    private static void insertIfSet(ImmutableMap.Builder<String, String> mapBuilder,
                                    String systemOptionKey) {
        Optional<String> value = systemOption(systemOptionKey);
        value.ifPresent(v -> mapBuilder.put(systemOptionKey, v));
    }

    @SuppressWarnings("AccessOfSystemProperties")
    private static Optional<String> systemOption(String optionKey) {
        Optional<String> result = Optional.ofNullable(System.getProperty(optionKey));
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ConnectionProperties that = (ConnectionProperties) o;
        return Objects.equal(properties, that.properties);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(properties);
    }

    private static Class<? extends EnvironmentType> currentEnvType() {
        Environment env = Environment.instance();
        Class<? extends EnvironmentType> result = env.type();
        return result;
    }

    /**
     * A builder of DB connection properties.
     */
    public static class Builder {

        private final Map<String, String> properties;
        private Class<? extends EnvironmentType> envType;

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

        /** Sets the JDBC connection protocol to the specified one. */
        public Builder setConnectionProtocol(String connectionProtocol) {
            checkNotNull(connectionProtocol);
            properties.put(PROTOCOL, connectionProtocol);
            return this;
        }

        /** Sets the instance name to the specified one. */
        public Builder setInstanceName(String instanceName) {
            checkNotNull(instanceName);
            properties.put(INSTANCE, instanceName);
            return this;
        }

        /** Sets the environment type to the specified one. */
        public Builder setEnvType(Class<? extends EnvironmentType> envType) {
            checkNotNull(envType);
            this.envType = envType;
            return this;
        }

        /** Returns a new instance of the DB connection properties. */
        public ConnectionProperties build() {
            properties.forEach((k, v) -> checkNotNull(v));
            Class<? extends EnvironmentType> environmentType = envType != null
                                                               ? envType
                                                               : currentEnvType();
            return new ConnectionProperties(ImmutableMap.copyOf(properties), environmentType);
        }
    }
}
