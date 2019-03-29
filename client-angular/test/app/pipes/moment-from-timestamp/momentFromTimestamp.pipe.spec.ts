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

import * as moment from 'moment';

import {MomentFromTimestamp} from 'app/pipes/moment-from-timestamp/momentFromTimestamp.pipe';

import {Timestamp} from 'google-protobuf/google/protobuf/timestamp_pb';

describe('MomentFromTimestamp', () => {

  const pipe = new MomentFromTimestamp();

  it('should create', () => {
    expect(pipe).toBeTruthy();
  });

  it('should convert Protobuf `Timestamp` into the Moment.js `Moment`', () => {
    const date = new Date();
    const timestamp = Timestamp.fromDate(date);
    const transform = pipe.transform(timestamp);
    expect(transform.toDate()).toEqual(date);
  });

  it('should return `undefined` if converting `undefined` `Timestamp`', () => {
    const transform = pipe.transform(undefined);
    expect(transform).toBeUndefined();
  });

  it('should convert Moment.js `Moment` to Protobuf `Timestamp`', () => {
    const date = new Date();
    const theMoment = moment(date);
    const timestamp = MomentFromTimestamp.back(theMoment);
    expect(timestamp.toDate()).toEqual(date);
  });

  it('should return `undefined` when converting `undefined` `Moment`', () => {
    const transform = MomentFromTimestamp.back(undefined);
    expect(transform).toBeUndefined();
  });
});
