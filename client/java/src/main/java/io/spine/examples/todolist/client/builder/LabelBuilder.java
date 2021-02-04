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

package io.spine.examples.todolist.client.builder;

import io.spine.examples.todolist.tasks.LabelId;
import io.spine.examples.todolist.tasks.command.CreateBasicLabel;

/**
 * Provides label command builders.
 */
public final class LabelBuilder {

    private LabelBuilder() {
    }

    static LabelBuilder newInstance() {
        return new LabelBuilder();
    }

    /**
     * Provides builder for the {@link CreateBasicLabel} command.
     *
     * @return the {@link CreateBasicLabelBuilder} instance
     */
    public CreateBasicLabelBuilder createLabel() {
        return new CreateBasicLabelBuilder();
    }

    /**
     * Builder for the {@link CreateBasicLabel} command.
     */
    public static final class CreateBasicLabelBuilder {

        private final CreateBasicLabel.Builder builder = CreateBasicLabel.newBuilder();

        /**
         * Sets the title to the {@link CreateBasicLabel.Builder}.
         *
         * @param title
         *         the title of the command
         * @return the {@code CreateBasicLabelBuilder} instance
         */
        public CreateBasicLabelBuilder setTitle(String title) {
            builder.setLabelTitle(title);
            return this;
        }

        /**
         * Builds {@link CreateBasicLabel} command.
         */
        public CreateBasicLabel build() {
            builder.setLabelId(LabelId.generate());
            return builder.vBuild();
        }
    }
}
