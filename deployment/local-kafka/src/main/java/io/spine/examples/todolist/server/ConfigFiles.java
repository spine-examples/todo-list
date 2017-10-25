/*
 * Copyright 2017, TeamDev Ltd. All rights reserved.
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

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * A utility for working with the config files contained in the classpath.
 *
 * @author Dmytro Dashenkov
 */
final class ConfigFiles {

    private ConfigFiles() {
        // Prevent utility class instantiation.
    }

    /**
     * Reads a {@code .properties} file from the classpath by the given path.
     *
     * @param path the file path (including extension)
     * @return the loaded {@link Properties}
     */
    static Properties loadConfig(String path) {
        final Properties props = new Properties();
        try (InputStream in = ConfigFiles.class.getClassLoader().getResourceAsStream(path)) {
            props.load(in);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        return props;
    }
}
