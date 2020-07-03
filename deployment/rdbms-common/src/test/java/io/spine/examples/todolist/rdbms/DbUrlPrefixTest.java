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

import com.google.common.testing.NullPointerTester;
import io.spine.base.Environment;
import io.spine.base.Production;
import io.spine.base.Tests;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.examples.todolist.rdbms.DbUrlPrefix.LOCAL_H2;
import static org.junit.Assert.assertThrows;

@DisplayName("DbUrlPrefix should")
final class DbUrlPrefixTest {

    private static final String EXPECTED_PREFIX = "jdbc:pg:mem:";

    @AfterAll
    static void clear() {
        Environment.instance()
                   .reset();
    }

    @Test
    @DisplayName("throw on null ctor values")
    void throwOnNull() {
        NullPointerTester tester = new NullPointerTester();
        tester.setDefault(DbProperties.class, DbProperties.newBuilder()
                                                          .build());
        tester.testAllPublicConstructors(DbUrlPrefix.class);
    }

    @Test
    @DisplayName("return the value from the properties in the production mode")
    void propValueInProduction() {
        Environment.instance()
                   .setTo(Production.class);

        DbProperties props = DbProperties
                .newBuilder()
                .setUrlPrefix(EXPECTED_PREFIX)
                .build();

        DbUrlPrefix prefix = new DbUrlPrefix(props, LOCAL_H2);
        assertThat(prefix.toString()).isEqualTo(EXPECTED_PREFIX);
    }

    @Test
    @DisplayName("return the test value in the testing mode")
    void testValueInTestingMode() {
        Environment.instance()
                   .setTo(Tests.class);

        DbProperties properties = DbProperties.newBuilder()
                                              .setUrlPrefix(LOCAL_H2)
                                              .build();

        DbUrlPrefix prefix = new DbUrlPrefix(properties, EXPECTED_PREFIX);
        assertThat(prefix.toString()).isEqualTo(EXPECTED_PREFIX);
    }

    @Test
    @DisplayName("throw if no connection URL prefix was provided in the production mode")
    void testNoValueInProd() {
        Environment.instance()
                   .setTo(Production.class);

        DbProperties properties = DbProperties.newBuilder()
                                              .build();

        DbUrlPrefix prefix = new DbUrlPrefix(properties, LOCAL_H2);
        assertThrows(IllegalStateException.class, prefix::toString);
    }
}
