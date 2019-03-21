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

import {Timestamp} from 'google-protobuf/google/protobuf/timestamp_pb';
import {TaskPriority} from 'generated/main/js/todolist/attributes_pb';
import {TaskCreationId, TaskId} from 'generated/main/js/todolist/identifiers_pb';
import {TaskCreation, TaskLabel} from 'generated/main/js/todolist/model_pb';
import {TaskDescription} from 'generated/main/js/todolist/values_pb';
import {SetTaskDetails, StartTaskCreation} from 'generated/main/js/todolist/c/commands_pb';

@Component({
  selector: 'app-task-creation-wizard',
  templateUrl: './task-creation-wizard.component.html',
  styleUrls: ['./task-creation-wizard.component.css'],
  providers: [
    TaskCreationWizard
  ]
})
export class TaskCreationWizardComponent implements AfterViewInit {

  private static readonly STEPS: Map<TaskCreation.Stage, number> = new Map([
    [TaskCreation.Stage.TASK_DEFINITION, 0],
    [TaskCreation.Stage.LABEL_ASSIGNMENT, 1],
    [TaskCreation.Stage.CONFIRMATION, 2]
  ]);

  @ViewChild(MatHorizontalStepper)
  stepper: MatHorizontalStepper;

  @ViewChild(TaskDefinitionComponent)
  taskDefinition: TaskDefinitionComponent;

  @ViewChild(LabelAssignmentComponent)
  labelAssignment: LabelAssignmentComponent;

  @ViewChild(ConfirmationComponent)
  confirmation: ConfirmationComponent;

  private isLoading: boolean;

  private readonly initWizard: Promise<void>;

  constructor(private readonly wizard: TaskCreationWizard,
              private readonly changeDetector: ChangeDetectorRef,
              private readonly location: Location,
              route: ActivatedRoute) {
    this.isLoading = true;
    const taskCreationId = route.snapshot.paramMap.get('taskCreationId');
    this.initWizard = wizard.init(taskCreationId);
  }

  /**
   * Reports an error which cannot really be recovered for the wizard.
   */
  private static reportFatalError(err): void {
    throw new Error(err);
  }

  ngAfterViewInit(): void {
    this.initWizard
      .then(() => {
        this.ensureLocation();
        this.moveToCurrentStep();

        this.taskDefinition.description = this.wizard.taskDescription;
        this.taskDefinition.priority = this.wizard.taskPriority;
        this.taskDefinition.dueDate = this.wizard.taskDueDate;
        this.labelAssignment.selected = this.wizard.taskLabels;

        this.changeDetector.detectChanges();

        this.isLoading = false;
      })
      .catch(err => TaskCreationWizardComponent.reportFatalError(err));
  }

  /**
   * Re-navigates from '/wizard' to '/wizard:*taskCreationId*'.
   *
   * To create a new task, user can just navigate to '/wizard'. A task creation process will then
   * be started and assigned a new ID.
   *
   * This method changes the current location, so the user sees the "correct" URL with a task
   * creation ID param.
   */
  private ensureLocation(): void {
    const idString = this.wizard.id.getValue();
    const urlWithId = `/wizard/${idString}`;
    const alreadyWithId = this.location.isCurrentPathEqualTo(urlWithId);
    if (!alreadyWithId) {
      this.location.go(urlWithId);
    }
  }

  /**
   * Selects the appropriate step in {@link stepper} according to the current wizard stage.
   *
   * Completes all the preceding steps (they still can be returned to).
   */
  private moveToCurrentStep(): void {
    const currentStage = this.wizard.stage;
    const index = TaskCreationWizardComponent.STEPS.get(currentStage);
    if (index === undefined) {
      TaskCreationWizardComponent.reportFatalError(`There is no wizard step for stage ${currentStage}`);
      return;
    }
    for (let i = 0; i < index; i++) {
      this.stepper.steps.toArray()[i].completed = true;
    }
    this.stepper.selectedIndex = index;
  }
}
