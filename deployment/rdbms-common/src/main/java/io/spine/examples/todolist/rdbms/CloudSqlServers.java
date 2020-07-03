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

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A utility class that contains methods for working with Cloud SQL-backed servers.
 */
public final class CloudSqlServers {

    /** Prevent instantiation of this utility class. */
    private CloudSqlServers() {
    }

    /**
     * Returns the {@code DbConnectionProperties} object assembled from a Cloud SQL config file in
     * the resources.
     */
    public static DbConnectionProperties propertiesFromResourceFile() {
        return DbConnectionProperties.fromResourceFile("cloud-sql.properties");
    }

    /**
     * Returns the connection URL prefix that is either taken from the specified {@code
     * DbConnectionProperties} object, or falls back to the local H2-compatible prefix.
     */
    public static DbUrlPrefix prefix(DbConnectionProperties properties) {
        checkNotNull(properties);
        return DbUrlPrefix.propsOrLocalH2(properties);
    }

    /**
     * Returns a connection URL for a Google Cloud SQL database. The connection URL is composed
     * using the specified properties.
     */
    public static String dbUrl(DbConnectionProperties properties) {
        checkNotNull(properties);
        String result =
                String.format("%s//google/%s?cloudSqlInstance=%s&" +
                                      "useSSL=false&socketFactory=com.google.cloud.sql.mysql" +
                                      ".SocketFactory",
                              prefix(properties).toString(),
                              properties.dbName(),
                              properties.instanceName());
        return result;
    }
}
