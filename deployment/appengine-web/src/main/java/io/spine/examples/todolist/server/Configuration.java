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

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.util.Exceptions.illegalStateWithCauseOf;

/**
 * The configuration of the Google App Engine application.
 */
public final class Configuration {

    private static final String CONFIG_FILE = "config.properties";

    private final String firebaseDatabaseUrl;
    private final String projectId;

    private static final Configuration INSTANCE = new Configuration(readConfigFile());

    /**
     * Returns the configuration instance.
     */
    public static Configuration instance() {
        return INSTANCE;
    }

    /**
     * Prevents direct instantiation.
     */
    private Configuration(Properties properties) {
        this.firebaseDatabaseUrl = Setting.FIREBASE_DB_URL.valueFrom(properties);
        this.projectId = Setting.APP_ENGINE_PROJECT_ID.valueFrom(properties);
    }

    /**
     * Retrieves URL to Firebase database.
     */
    String firebaseDatabaseUrl() {
        return firebaseDatabaseUrl;
    }

    /**
     * Retrieves the ID of GAE project.
     */
    String projectId() {
        return projectId;
    }

    private static Properties readConfigFile() {
        Properties properties = new Properties();
        try (InputStream stream = getResource()) {
            properties.load(stream);
            return properties;
        } catch (IOException e) {
            throw illegalStateWithCauseOf(e);
        }
    }

    private static InputStream getResource() {
        return Configuration.class.getClassLoader()
                                  .getResourceAsStream(CONFIG_FILE);
    }

    /**
     * The enumeration of separate settings of a deployment configuration.
     */
    private enum Setting {

        /**
         * The URL of the Firebase Database to use for the client transport purposes, as
         * a {@code String}.
         */
        FIREBASE_DB_URL("firebase.database.url"),

        APP_ENGINE_PROJECT_ID("app-engine.project-id");

        private final String key;

        /**
         * Creates a new instance of {@code Setting}.
         *
         * @param key
         *         the name of the setting; acts as a {@link Properties} key
         */
        Setting(String key) {
            this.key = key;
        }

        @Nullable
        private String nullableValueFrom(Properties properties) {
            String value = properties.getProperty(key);
            return value;
        }

        /**
         * Retrieves the value of this setting stored in the given {@code properties}.
         *
         * @param properties
         *         {@link Properties} to get the value from
         * @return the value of the setting
         */
        private String valueFrom(Properties properties) {
            String value = nullableValueFrom(properties);
            checkNotNull(value, key);
            return value;
        }
    }
}
