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

/**
 * This package contains the basic classes of the Apache Kafka based
 * {@link io.spine.server.storage.Storage Storage} implementation.
 *
 * The implementation has several limitations:
 * <ul>
 *     <li>Method {@link io.spine.server.storage.Storage#index() Storage.index()} is not
 *         implemented in any of the {@linkplain io.spine.server.storage.Storage storage} classes.
 *     <li>{@link io.spine.server.stand.StandStorage#readAllByType StandStorage.readAllByType(...)}
 *         methods are not implemented.
 *     <li>Most read operations are non-lazy and may cause memory overflow issues.
 * </ul>
 */
@Experimental
@ParametersAreNonnullByDefault
package io.spine.server.storage.kafka;

import io.spine.annotation.Experimental;

import javax.annotation.ParametersAreNonnullByDefault;
