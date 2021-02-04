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

package io.spine.examples.todolist.server.appengine.given;

import com.google.auth.Credentials;

import java.net.URI;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyMap;

/** Test environment for {@link io.spine.examples.todolist.server.StorageTest}. */
public class StorageTestEnv {

    /** Prevents instantiation of the test environment. */
    private StorageTestEnv() {
    }

    public static class EmptyCredentials extends Credentials {

        private static final long serialVersionUID = 0;

        @Override
        public String getAuthenticationType() {
            return "";
        }

        @Override
        public Map<String, List<String>> getRequestMetadata(URI uri) {
            return emptyMap();
        }

        @Override
        public boolean hasRequestMetadata() {
            return false;
        }

        @Override
        public boolean hasRequestMetadataOnly() {
            return false;
        }

        @Override
        public void refresh() {
        }
    }
}
