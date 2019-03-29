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

import {TaskPriorityName} from 'app/pipes/task-priority-name/task-priority-name.pipe';

import {TaskPriority} from 'proto/todolist/attributes_pb';

describe('TaskPriorityName', () => {

  const pipe = new TaskPriorityName();

  it('should create', () => {
    expect(pipe).toBeTruthy();
  });

  it('should convert a `TaskPriority` to its display name', () => {
    const transform = pipe.transform(TaskPriority.HIGH);
    expect(transform).toEqual('High');
  });

  it('should produce an error when given an unknown `TaskPriority`', () => {
    expect(() => pipe.transform(TaskPriority.TC_UNDEFINED)).toThrowError(
      `Task priority ${TaskPriority.TC_UNDEFINED} is unknown`
    );
  });
});
