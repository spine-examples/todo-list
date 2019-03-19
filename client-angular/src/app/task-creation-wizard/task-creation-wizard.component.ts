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
import {AfterViewInit, ChangeDetectorRef, Component, ViewChild} from '@angular/core';
import {MatHorizontalStepper} from '@angular/material';
import {ActivatedRoute} from '@angular/router';

import {TaskCreationWizard} from './service/task-creation-wizard.service';
import {TaskDefinitionComponent} from './step-1-task-definition/task-definition.component';
import {LabelAssignmentComponent} from './step-2-label-assignment/label-assignment.component';
import {ConfirmationComponent} from './step-3-confirmation/confirmation.component';

@Component({
  selector: 'app-task-creation-wizard',
  templateUrl: './task-creation-wizard.component.html',
  styleUrls: ['./task-creation-wizard.component.css'],
  providers: [
    TaskCreationWizard
  ]
})
export class TaskCreationWizardComponent implements AfterViewInit {

  @ViewChild(MatHorizontalStepper)
  stepper: MatHorizontalStepper;

  @ViewChild(TaskDefinitionComponent)
  taskDefinition: TaskDefinitionComponent;

  @ViewChild(LabelAssignmentComponent)
  labelAssignment: LabelAssignmentComponent;

  @ViewChild(ConfirmationComponent)
  confirmation: ConfirmationComponent;

  constructor(private readonly changeDetector: ChangeDetectorRef,
              private readonly wizard: TaskCreationWizard,
              location: Location,
              route: ActivatedRoute) {
    const taskCreationId = route.snapshot.paramMap.get('taskCreationId');
    console.log(`Task creation ID: ${taskCreationId}`);
    if (taskCreationId) {
      this.restoreWizard(taskCreationId);
    } else {
      wizard.startTaskCreation().then(
        creationId => location.go(`/wizard/:${creationId.getValue()}`)
      ).catch(
        err => this.reportFatalError(err)
      );
    }
  }

  ngAfterViewInit(): void {
    this.taskDefinition.description = 'dusya';
    this.changeDetector.detectChanges();
  }

  private restoreWizard(taskCreationId) {
  }

  /**
   * Fatality.
   */
  private reportFatalError(err) {
  }
}
