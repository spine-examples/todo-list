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

import {Component} from '@angular/core';
import {Router} from '@angular/router';

import {LabelService} from '../../labels/label.service';
import {TaskCreationWizard} from '../service/task-creation-wizard.service';
import {WizardStep} from '../wizard-step';

import {LabelId} from 'generated/main/js/todolist/identifiers_pb';
import {TaskLabel} from 'generated/main/js/todolist/model_pb';
import {LabelItem} from 'generated/main/js/todolist/q/projections_pb';

@Component({
  selector: 'app-label-assignment',
  templateUrl: './label-assignment.component.html',
  styleUrls: ['./label-assignment.component.css']
})
export class LabelAssignmentComponent extends WizardStep {

  // noinspection JSMismatchedCollectionQueryUpdate Queried as a part of NG model.
  private available: TaskLabel[];
  private selected: TaskLabel[];

  private labelsFetched: boolean;

  private readonly loadAllLabels: Promise<TaskLabel[]>;

  constructor(router: Router, wizard: TaskCreationWizard, labelService: LabelService) {
    super(router, wizard);
    this.labelsFetched = false;
    this.loadAllLabels = labelService.fetchAllLabels();
  }

  protected initOwnModel(): void {
    this.loadAllLabels.then(labels => {
      this.available = labels;
      this.available.sort(LabelService.sortLabels);
      this.labelsFetched = true;
    });
  }

  initFromWizard() {
    this.selected = this.wizard.taskLabels;
  }

  switchSelectedStatus(label: TaskLabel): void {
    this.onInputChange();
    if (this.selected.includes(label)) {
      const index = this.selected.indexOf(label);
      this.selected.splice(index, 1);
    } else {
      this.selected.push(label);
    }
    this.selected.sort(LabelService.sortLabels);
  }

  protected doStep(): Promise<void> {
    if (this.selected.length > 0) {
      return this.wizard.addLabels(this.selected);
    }
    return this.wizard.skipLabelAssignment();
  }
}
