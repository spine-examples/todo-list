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

package org.spine3.examples.todolist.client.builder;

import org.spine3.examples.todolist.LabelId;
import org.spine3.examples.todolist.c.commands.CreateBasicLabel;

import static org.spine3.base.Identifiers.newUuid;

/**
 * Provides label command builders.
 *
 * @author Illia Shepilov
 */
public final class LabelBuilder {

    private LabelBuilder() {
    }

    static LabelBuilder getInstance() {
        return new LabelBuilder();
    }

    /**
     * Provides builder for the {@link CreateBasicLabel} command.
     *
     * @return the {@link CreateBasicLabelBuilder} onstance
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
         * @param title the title of the command
         * @return the {@link CreateBasicLabelBuilder} instance
         */
        public CreateBasicLabelBuilder setTitle(String title) {
            builder.setLabelTitle(title);
            return this;
        }

        /**
         * Builds {@link CreateBasicLabel} command.
         *
         * @return the {@link CreateBasicLabel} command
         */
        public CreateBasicLabel build() {
            final LabelId id = generateId();
            builder.setLabelId(id);
            return builder.build();
        }
    }

    private static LabelId generateId() {
        final LabelId id = LabelId.newBuilder()
                                  .setValue(newUuid())
                                  .build();
        return id;
    }
}
