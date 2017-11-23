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

package io.spine.server.given;

import io.spine.server.FRChangeCustomerName;
import io.spine.server.FRCreateCustomer;
import io.spine.server.FRCustomer;
import io.spine.server.FRCustomerCreated;
import io.spine.server.FRCustomerId;
import io.spine.server.FRCustomerNameChanged;
import io.spine.server.FRCustomerVBuilder;
import io.spine.server.aggregate.Aggregate;
import io.spine.server.aggregate.AggregateRepository;
import io.spine.server.aggregate.Apply;
import io.spine.server.command.Assign;

/**
 * Test environment for
 * the {@link io.spine.server.FirebaseSubscriptionRepeater FirebaseSubscriptionRepeater} tests.
 *
 * @author Dmytro Dashenkov
 */
public class FirebaseRepeaterTestEnv {

    public static class CustomerAggregate
            extends Aggregate<FRCustomerId, FRCustomer, FRCustomerVBuilder> {

        protected CustomerAggregate(FRCustomerId id) {
            super(id);
        }

        @Assign
        FRCustomerCreated handle(FRCreateCustomer command) {
            return FRCustomerCreated.newBuilder()
                                    .setId(command.getId())
                                    .build();
        }

        @Assign
        FRCustomerNameChanged handle(FRChangeCustomerName command) {
            return FRCustomerNameChanged.newBuilder()
                                        .setNewName(command.getNewName())
                                        .build();
        }

        @Apply
        private void on(FRCustomerCreated event) {
            getBuilder().setId(event.getId());
        }

        @Apply
        private void on(FRCustomerNameChanged event) {
            getBuilder().setName(event.getNewName());
        }
    }

    public static class CustomerRepository
            extends AggregateRepository<FRCustomerId, CustomerAggregate> {}
}
