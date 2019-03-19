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
import {Component, Input} from '@angular/core';
import {MatStepper} from '@angular/material';

import {LabelService} from '../../labels/label.service';

import {LabelId} from 'generated/main/js/todolist/identifiers_pb';
import {TaskLabel} from 'generated/main/js/todolist/model_pb';
import {LabelItem} from 'generated/main/js/todolist/q/projections_pb';

@Component({
  selector: 'app-label-assignment',
  templateUrl: './label-assignment.component.html',
  styleUrls: ['./label-assignment.component.css']
})
export class LabelAssignmentComponent {

  @Input()
  private readonly stepper: MatStepper;


  available: TaskLabel[];
  selected: TaskLabel[];

  constructor(private readonly location: Location, private readonly labelService: LabelService) {
    labelService.fetchAllLabels().then(labels => this.available = labels);
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

  createBasicLabel() {
    this.labelService.createBasicLabel();
  }
}
