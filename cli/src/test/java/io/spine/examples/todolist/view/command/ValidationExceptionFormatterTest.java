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

package io.spine.examples.todolist.view.command;

import com.google.protobuf.Descriptors.FieldDescriptor;
import io.spine.examples.todolist.test.NaturalNumber;
import io.spine.examples.todolist.test.NaturalNumberVBuilder;
import io.spine.validate.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.spine.examples.todolist.view.command.ValidationExceptionFormatter.ERROR_MSG_FORMAT;
import static io.spine.examples.todolist.view.command.ValidationExceptionFormatter.toErrorMessages;
import static io.spine.test.Tests.assertHasPrivateParameterlessCtor;
import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Dmytro Grankin
 */
@DisplayName("ValidationExceptionFormatter should")
class ValidationExceptionFormatterTest {

    @Test
    @DisplayName("have the private constructor")
    void havePrivateCtor() {
        assertHasPrivateParameterlessCtor(ValidationExceptionFormatter.class);
    }

    @Test
    @DisplayName("obtain error messages from `ValidationException`")
    void returnProperMessage() {
        final NaturalNumberVBuilder builder = NaturalNumberVBuilder.newBuilder();
        final int fieldIndexToBeUpdated = NaturalNumber.VALUE_FIELD_NUMBER - 1;
        final FieldDescriptor fieldDescriptor = NaturalNumber.getDescriptor()
                                                             .getFields()
                                                             .get(fieldIndexToBeUpdated);
        final String fieldNameToBeUpdated = fieldDescriptor.getName();
        final String expectedErrorMsg = format(ERROR_MSG_FORMAT, fieldNameToBeUpdated);

        final int invalidFieldValue = -1;
        final ValidationException ex = assertThrows(ValidationException.class,
                                                    () -> builder.setValue(invalidFieldValue));
        final List<String> errorMessages = toErrorMessages(ex);
        assertEquals(1, errorMessages.size());
        assertEquals(expectedErrorMsg, errorMessages.get(0));
    }
}
