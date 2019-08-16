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

import {StringValue} from 'app/pipes/string-value/string-value.pipe';

import {LabelDetails, TaskDescription} from 'proto/todolist/values_pb';

describe('StringValue', () => {

  const pipe = new StringValue();

  it('should create', () => {
    expect(pipe).toBeTruthy();
  });

  it('should convert suitable Proto message to its string value', () => {
    const stringValue = 'the task description';
    const taskDescription = new TaskDescription();
    taskDescription.setValue(stringValue);
    const transform = pipe.transform(taskDescription);
    expect(transform).toEqual(stringValue);
  });

  it('should always return `undefined` when given `undefined` Proto message', () => {
    const transform = pipe.transform(undefined);
    expect(transform).toBeUndefined();
  });

  it('should produce an error when given a non-`stringValue` Proto type', () => {
    const labelDetails = new LabelDetails();
    expect(() => pipe.transform(labelDetails)).toThrowError(
        `${StringValue.ERROR_MESSAGE}${labelDetails}`
    );
  });

  it('should convert a string value to a specified Proto type', () => {
    const stringValue = 'some task description';
    const message = StringValue.back<TaskDescription>(stringValue, TaskDescription);
    expect(message.getValue()).toEqual(stringValue);
  });

  it('should always return `undefined` when given an `undefined` string value', () => {
    const transform = StringValue.back(undefined, TaskDescription);
    expect(transform).toBeUndefined();
  });

  it('should produce an error when conversion to a non-`stringValue` type is requested', () => {
    const stringValue = 'some string value';
    expect(() => StringValue.back<LabelDetails>(stringValue, LabelDetails)).toThrowError(
        `${StringValue.ERROR_MESSAGE}${LabelDetails}`
    );
  });
});
