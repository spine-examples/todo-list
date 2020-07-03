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

public final class DbProperties {

    private static final String NAME = "db.name";
    private static final String PASSWORD = "db.password";
    private static final String PREFIX = "db.prefix";
    private static final String INSTANCE = "db.instance";
    private static final String USERNAME = "db.username";
    private final Map<String, String> properties;

    private DbProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public static DbProperties fromResourceFile(String fileName) {
        Properties properties = loadProperties(fileName);
        ImmutableMap<String, String> map = Maps.fromProperties(properties);
        return new DbProperties(map);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public String dbName() {
        String result = value(NAME);
        return result;
    }

    public String username() {
        String result = value(USERNAME);
        return result;
    }

    public String password() {
        String result = value(PASSWORD);
        return result;
    }

    public String dbPrefix() {
        String result = value(PREFIX);
        return result;
    }

    public String dbInstance() {
        String result = value(INSTANCE);
        return result;
    }

    public String value(String key) {
        checkNotNull(key);
        return Optional.ofNullable(properties.get(key))
                       .orElseThrow(() -> newIllegalStateException(
                               "Could not read `%s` from the DB properties file.", key));
    }

    private static Properties loadProperties(String propertiesFile) {
        Properties properties = new Properties();
        InputStream stream = DbProperties.class.getClassLoader()
                                               .getResourceAsStream(propertiesFile);
        try {
            properties.load(stream);
        } catch (IOException e) {
            throw illegalStateWithCauseOf(e);
        }
        return properties;
    }

    public static class Builder {
        private final Map<String, String> properties = new HashMap<>(5);

        public Builder setDbName(String name) {
            checkNotNull(name);
            properties.put(NAME, name);
            return this;
        }

        public Builder setUsername(String username) {
            checkNotNull(username);
            properties.put(USERNAME, username);
            return this;
        }

        public Builder setPassword(String password) {
            checkNotNull(password);
            properties.put(PASSWORD, password);
            return this;
        }

        public Builder setUrlPrefix(String urlPrefix) {
            checkNotNull(urlPrefix);
            properties.put(PREFIX, urlPrefix);
            return this;
        }

        public Builder setInstanceName(String instanceName) {
            checkNotNull(instanceName);
            properties.put(INSTANCE, instanceName);
            return this;
        }

        public DbProperties build() {
            properties.forEach((k, v) -> checkNotNull(v));
            return new DbProperties(properties);
        }
    }
}
