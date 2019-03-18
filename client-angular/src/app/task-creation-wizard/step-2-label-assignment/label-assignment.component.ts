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

import {Location} from '@angular/common';
import {Component} from '@angular/core';

import {UuidGenerator} from '../../uuid-generator/uuid-generator';

import {LabelId} from 'generated/main/js/todolist/identifiers_pb';
import {LabelItem} from 'generated/main/js/todolist/q/projections_pb';

export function mockLabels(): LabelItem[] {
  const label1 = new LabelItem();
  const id1 = UuidGenerator.newId(LabelId);
  label1.setId(id1);
  label1.setLabelTitle('Reds');
  label1.setLabelColor('#ff0000');

  const label2 = new LabelItem();
  const id2 = UuidGenerator.newId(LabelId);
  label2.setId(id2);
  label2.setLabelTitle('Blues');
  label2.setLabelColor('#0000ff');


  const label3 = new LabelItem();
  const id3 = UuidGenerator.newId(LabelId);
  label3.setId(id3);
  label3.setLabelTitle('Grays');
  label3.setLabelColor('#808080');
  return [label1, label2, label3];
}

@Component({
  selector: 'app-label-assignment',
  templateUrl: './label-assignment.component.html',
  styleUrls: ['./label-assignment.component.css']
})
export class LabelAssignmentComponent {

  private readonly LABELS: LabelItem[] = mockLabels();

  constructor(private readonly location: Location) {
  }

  isCompleted(): boolean {
    return true;
  }

  back() {
  }

  next() {
  }

  /**
   * Task will be saved as draft.
   */
  cancel() {
    this.location.back();
  }
}
