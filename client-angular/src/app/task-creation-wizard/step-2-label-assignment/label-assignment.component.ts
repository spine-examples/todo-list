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
import {LabelView} from 'generated/main/js/todolist/q/projections_pb';

@Component({
  selector: 'app-label-assignment',
  templateUrl: './label-assignment.component.html',
  styleUrls: ['./label-assignment.component.css']
})
export class LabelAssignmentComponent extends WizardStep {

  // noinspection JSMismatchedCollectionQueryUpdate Used as a part of NG model.
  private available: LabelView[];

  // noinspection JSMismatchedCollectionQueryUpdate Used as a part of NG model.
  /**
   * Is always a subset of available.
   */
  private selected: LabelView[];

  private readonly loadAllLabels: Promise<LabelView[]>;

  constructor(router: Router, wizard: TaskCreationWizard, labelService: LabelService) {
    super(router, wizard);
    this.loadAllLabels = labelService.fetchAllLabels();
  }

  protected initOwnModel(): void {
    this.loadAllLabels.then(labels => {
      this.available = labels;
    });
  }

  initFromWizard() {
    this.loadAllLabels.then(labels => {
      const findMatch = id => labels.find(label => label.getId().getValue() === id.getValue());
      this.selected = this.wizard.taskLabels.map(findMatch);
    });
  }

  switchSelectedStatus(label: LabelView): void {
    this.onInputChange();
    if (this.selected.includes(label)) {
      const index = this.selected.indexOf(label);
      this.selected.splice(index, 1);
    } else {
      this.selected.push(label);
    }
  }

  protected doStep(): Promise<void> {
    if (this.selected.length > 0) {
      return this.wizard.addLabels(this.selected.map(label => label.getId()));
    }
    return this.wizard.skipLabelAssignment();
  }
}
