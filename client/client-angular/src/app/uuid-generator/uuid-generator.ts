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

import {Message} from 'google-protobuf';
import * as uuid from 'uuid/v4';

/**
 * A generator of UUID values.
 *
 * The UUID value is a Proto message with a single `string` field named `value`.
 */
export class UuidGenerator {

  /** Visible for testing. */
  static readonly ERROR_MESSAGE = 'Expected a valid UUID-value Protobuf type';

  constructor() {
    throw new Error('`UuidGenerator` should not be instantiated');
  }

  /**
   * Generates a new UUID value of the specified type.
   *
   * If the type does not match the UUID value pattern, an error is thrown.
   */
  static newId<T extends Message>(type: new() => T): T {
    const result = this.emptyMessageOfType(type);
    const value = uuid();
    result.setUuid(value);
    return result;
  }

  /**
   * Creates a 'UUID-value' message of the specified type with a given value.
   *
   * If the type does not match the 'UUID-value' pattern, an error is thrown.
   */
  static newIdWithValue<T extends Message>(value: string, type: new() => T): T {
    const result = this.emptyMessageOfType(type);
    result.setUuid(value);
    return result;
  }

  private static emptyMessageOfType<T extends Message>(type: new() => T): T {
    if (!type) {
      throw new Error(UuidGenerator.ERROR_MESSAGE);
    }
    const result: T = new type();
    if (!result.setUuid) {
      throw new Error(UuidGenerator.ERROR_MESSAGE);
    }
    return result;
  }
}
