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

package io.spine.cli.view;

import com.google.protobuf.Int32Value;
import com.google.protobuf.StringValue;
import io.spine.cli.Bot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.spine.protobuf.TypeConverter.toMessage;
import static java.lang.System.lineSeparator;

@DisplayName("EntityView should")
class EntityViewTest {

    private Bot bot;
    private final AnEntityView view = new AnEntityView(Int32Value.getDefaultInstance());

    @BeforeEach
    void setUp() {
        bot = new Bot();
    }

    @Test
    @DisplayName("load and render entity state")
    void loadAndRenderEntityState() {
        view.renderBody(bot.screen());
        String expectedBody = view.renderState(AnEntityView.STATE) + lineSeparator();
        bot.assertOutput(expectedBody);
    }

    private static class AnEntityView extends EntityView<Int32Value, StringValue> {

        private static final StringValue STATE = toMessage("string", StringValue.class);

        private AnEntityView(Int32Value id) {
            super(id, "View title");
        }

        @Override
        protected StringValue load(Int32Value id) {
            return STATE;
        }

        @Override
        protected String renderState(StringValue state) {
            return state.getValue();
        }
    }
}
