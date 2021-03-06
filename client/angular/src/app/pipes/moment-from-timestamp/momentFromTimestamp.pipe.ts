/*
 * Copyright 2021, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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
import * as moment from 'moment';
import {Moment} from 'moment';
import {Timestamp} from 'google-protobuf/google/protobuf/timestamp_pb';

/**
 * Obtains Moment.js `Moment` from the passed `Timestamp` and vice versa.
 *
 * Usage:
 *   timestamp | moment
 */
@Pipe({
  name: 'moment'
})
export class MomentFromTimestamp implements PipeTransform {

  /**
   * Performs the backwards transformation, obtaining the `Timestamp` from the given `Moment`.
   *
   * Returns `undefined` on `undefined` inputs (rather than producing an `Error`) for convenience
   * as in To-Do List `undefined` is often a valid value for NG model entries (e.g. task can have
   * undefined due date).
   */
  static back(value: Moment): Timestamp {
    if (!value) {
      return undefined;
    }
    const date = value.toDate();
    return Timestamp.fromDate(date);
  }

  /**
   * Performs the transformation.
   *
   * Returns `undefined` on `undefined` inputs (rather than producing an `Error`) for convenience
   * as in To-Do List `undefined` is often a valid value for NG model entries (e.g. task can have
   * undefined due date).
   */
  transform(value: Timestamp): Moment {
    if (!value) {
      return undefined;
    }
    const date = value.toDate();
    return moment(date);
  }
}
