/*
 * Copyright 2018, TeamDev. All rights reserved.
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

package io.spine.cli.action;

import io.spine.cli.view.View;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("AbstractActionProducer should")
class AbstractActionProducerTest {

    private static final String VALID_NAME = "name";
    private static final Shortcut SHORTCUT = new Shortcut("s");

    @SuppressWarnings("ConstantConditions") // Purpose of this test.
    @Test
    @DisplayName("validate parameters are passed to the constructor")
    void validateCtorParameters() {
        assertThrows(IllegalArgumentException.class, () -> newProducer(null, SHORTCUT));
        assertThrows(IllegalArgumentException.class, () -> newProducer("", SHORTCUT));
        assertThrows(NullPointerException.class, () -> newProducer(VALID_NAME, null));
    }

    private static AbstractActionProducer<View, View, Action<View, View>> newProducer(String name,
                                                                                      Shortcut shortcut) {
        return new AnAbstractActionProducer<>(name, shortcut);
    }

    private static class AnAbstractActionProducer<S extends View,
            D extends View,
            T extends Action<S, D>>
            extends AbstractActionProducer<S, D, T> {

        private AnAbstractActionProducer(String name, Shortcut shortcut) {
            super(name, shortcut);
        }

        @Override
        public T create(S source) {
            throw new UnsupportedOperationException("Should not be tested in this test suite.");
        }
    }
}
