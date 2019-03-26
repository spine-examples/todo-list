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

import {UuidGenerator} from '../../../src/app/uuid-generator/uuid-generator';

import {TaskId} from 'generated/main/js/todolist/identifiers_pb';
import {TaskDetails} from 'generated/main/js/todolist/values_pb';

describe('UuidGenerator', () => {

  it('should throw error on instantiation', () => {
    expect(() => new UuidGenerator()).toThrowError();
  });

  it('should generate a UUID-value of a given type', () => {
    const id1 = UuidGenerator.newId(TaskId);
    const id2 = UuidGenerator.newId(TaskId);
    expect(id1).toBeTruthy();
    expect(id2).toBeTruthy();
    expect(id1).not.toEqual(id2);
  });

  it('should create a UUID-value from the given `string` value', () => {
    const id = 'the-ID';
    const created = UuidGenerator.newIdWithValue<TaskId>(id, TaskId);
    expect(created).toBeTruthy();
    expect(created.getValue()).toEqual(id);
  });

  it('should throw an error when a specified type is undefined', () => {
    expect(() => UuidGenerator.newId(undefined))
      .toThrowError(UuidGenerator.ERROR_MESSAGE);
  });

  it('should throw an error when a specified type is not a UUID-value Proto type', () => {
    expect(() => UuidGenerator.newId(TaskDetails))
      .toThrowError(UuidGenerator.ERROR_MESSAGE);
  });
});
