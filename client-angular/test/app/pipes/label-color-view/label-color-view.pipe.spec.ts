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

import {LabelColorView} from '../../../../src/app/pipes/label-color-view/label-color-view.pipe';

import {LabelColor} from 'generated/main/js/todolist/attributes_pb';

describe('LabelColorView', () => {

  const pipe = new LabelColorView();

  it('should create', () => {
    expect(pipe).toBeTruthy();
  });

  it('should convert `LabelColor` to its string representation', () => {
    const transform = pipe.transform(LabelColor.RED);
    expect(transform).toEqual('#ff0000');
  });

  it('should throw an error on unknown label color', () => {
    expect(() => pipe.transform(LabelColor.LC_UNDEFINED)).toThrowError(
      `There is no known string representation for color ${LabelColor.LC_UNDEFINED}`
    );
  });
});
