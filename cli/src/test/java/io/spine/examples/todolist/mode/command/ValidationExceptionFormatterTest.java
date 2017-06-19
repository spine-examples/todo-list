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

package io.spine.examples.todolist.mode.command;

import com.google.protobuf.Descriptors.FieldDescriptor;
import io.spine.examples.todolist.c.commands.CreateBasicTask;
import io.spine.examples.todolist.c.commands.CreateBasicTaskVBuilder;
import io.spine.validate.ConstraintViolation;
import io.spine.validate.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.spine.examples.todolist.mode.command.ValidationExceptionFormatter.ERROR_MSG_FORMAT;
import static io.spine.examples.todolist.mode.command.ValidationExceptionFormatter.format;
import static io.spine.test.Tests.assertHasPrivateParameterlessCtor;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Dmytro Grankin
 */
@DisplayName("ValidationExceptionFormatter should")
class ValidationExceptionFormatterTest {

    @Test
    @DisplayName("have the private constructor")
    void hasPrivateCtor() {
        assertHasPrivateParameterlessCtor(ValidationExceptionFormatter.class);
    }

    @Test
    @DisplayName("not accept `ValidationException` with two or more violations")
    void notAcceptValidationExceptionWithToManyViolations() {
        final List<ConstraintViolation> violations = asList(null, null);
        final ValidationException validationException = new ValidationException(violations);
        assertThrows(IllegalArgumentException.class, () -> format(validationException));
    }

    @Test
    @DisplayName("format `ValidationException` as expected")
    void returnProperMessage() {
        final CreateBasicTaskVBuilder builder = CreateBasicTaskVBuilder.newBuilder();
        final int fieldIndexToBeUpdated = CreateBasicTask.DESCRIPTION_FIELD_NUMBER - 1;
        final FieldDescriptor fieldDescriptor = CreateBasicTask.getDescriptor()
                                                               .getFields()
                                                               .get(fieldIndexToBeUpdated);
        final String fieldNameToBeUpdated = fieldDescriptor.getName();
        final String expectedErrorMsg = format(ERROR_MSG_FORMAT, fieldNameToBeUpdated);
        final String invalidFieldValue = "";

        final ValidationException ex =
                assertThrows(ValidationException.class,
                             () -> builder.setDescription(invalidFieldValue));
        assertEquals(expectedErrorMsg, format(ex));
    }
}
