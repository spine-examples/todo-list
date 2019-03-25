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

import {Pipe, PipeTransform} from '@angular/core';
import {Message} from 'google-protobuf';

@Pipe({
  name: 'stringValue'
})
export class StringValue implements PipeTransform {

  /** Visible for testing. */
  static readonly ERROR_MESSAGE =
    'Expected Proto Message type which contains a single `string` field named `value`, received: ';

  /**
   * ...
   *
   * Returns `undefined` on `undefined` inputs (rather than producing an `Error`) for convenience
   * as in To-Do List `undefined` is often a valid value for NG model entries (e.g. task can have
   * undefined due date).
   */
  static back<T extends Message>(value: string, type: new() => T): T {
    if (!value) {
      return undefined;
    }
    const result = new type();
    if (!result.setValue) {
      throw new Error(`${StringValue.ERROR_MESSAGE}${type}`);
    }
    result.setValue(value);
    return result;
  }

  /**
   * ...
   *
   * Returns `undefined` on `undefined` inputs (rather than producing an `Error`) for convenience
   * as in To-Do List `undefined` is often a valid value for NG model entries (e.g. task can have
   * undefined due date).
   */
  transform(value: any): string {
    if (!value) {
      return undefined;
    }
    if (!value.getValue) {
      throw new Error(`${StringValue.ERROR_MESSAGE}${value}`);
    }
    return value.getValue();
  }
}
