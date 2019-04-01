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

import {LabelService} from 'app/labels/label.service';
import {TaskCreationWizard} from 'app/task-creation-wizard/service/task-creation-wizard.service';
import {WizardStep} from 'app/task-creation-wizard/wizard-step';

import {LabelId} from 'proto/todolist/identifiers_pb';
import {LabelView} from 'proto/todolist/q/projections_pb';
import {LayoutService} from "app/layout/layout.service";

/**
 * A component which represents the second step of the Task Creation Wizard - a label assignment.
 *
 * The "aim" of this step is to send either `AddLabels` (in case the user assigned labels to the
 * task) or `SkipLabels` (in case no labels were assigned) command to the server.
 *
 * The wizard will then proceed to the confirmation.
 *
 * It will still be possible to return to this step and modify the task labels in future.
 */
@Component({
  selector: 'app-label-assignment',
  templateUrl: './label-assignment.component.html',
  styleUrls: ['./label-assignment.component.css']
})
export class LabelAssignmentComponent extends WizardStep {

  /**
   * The list of all available labels.
   *
   * Visible for testing.
   */
  available: LabelView[];

  /**
   * The list of labels selected for the current task.
   *
   * For comparison brevity, is always a subset of entities from {@link available}.
   *
   * Visible for testing.
   */
  selected: LabelView[];

  /**
   * A promise to load the available labels list.
   */
  private readonly loadAvailableLabels: Promise<LabelView[]>;

  constructor(router: Router, wizard: TaskCreationWizard, labelService: LabelService) {
    super(router, wizard);
    this.loadAvailableLabels = labelService.fetchAllLabels();
  }

  /**
   * @inheritDoc
   */
  protected initOwnModel(): void {
    this.loadAvailableLabels
      .then(labels => {
        this.available = labels;
      })
      .catch(err => {
        this.reportError(`Error when loading available labels: ${err}`);
      });
  }

  /**
   * @inheritDoc
   */
  initFromWizard(): void {
    this.loadAvailableLabels
      .then(labels => {
        const findMatch = id => labels.find(label => label.getId().getValue() === id.getValue());
        this.selected = this.wizard.taskLabels.map(findMatch);
      })
      .catch(err => {
        this.reportError(`Error when loading available labels: ${err}`);
      });
  }

  /**
   * Switches 'selected'/'not-selected' status for the label.
   *
   * Visible for testing.
   */
  switchSelectedStatus(label: LabelView): void {
    this.onUserInput();
    if (this.selected.includes(label)) {
      const index = this.selected.indexOf(label);
      this.selected.splice(index, 1);
    } else {
      this.selected.push(label);
    }
  }

  /**
   * @inheritDoc
   */
  protected doStep(): Promise<void> {
    if (this.selected.length > 0) {
      return this.wizard.addLabels(this.selected.map(label => label.getId()));
    }
    return this.wizard.skipLabelAssignment();
  }
}
