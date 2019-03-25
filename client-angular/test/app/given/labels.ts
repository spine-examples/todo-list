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

import {LabelId} from 'generated/main/js/todolist/identifiers_pb';
import {LabelView} from 'generated/main/js/todolist/q/projections_pb';
import {LabelColor} from 'generated/main/js/todolist/attributes_pb';

const LABEL_1_ID = 'label-1';
const LABEL_1_TITLE = 'Very important';
const LABEL_1_COLOR = LabelColor.RED;

const LABEL_2_ID = 'label-2';
const LABEL_2_TITLE = 'Not so important';
const LABEL_2_COLOR = LabelColor.GREEN;

export function label1(): LabelView {
  return label(LABEL_1_ID, LABEL_1_TITLE, LABEL_1_COLOR);
}

export function label2(): LabelView {
  return label(LABEL_2_ID, LABEL_2_TITLE, LABEL_2_COLOR);
}

function label(id: string, title: string, color: LabelColor) {
  const result = new LabelView();
  const labelId = new LabelId();
  labelId.setValue(id);
  result.setId(labelId);
  result.setTitle(title);
  result.setColor(color);
  return result;
}
