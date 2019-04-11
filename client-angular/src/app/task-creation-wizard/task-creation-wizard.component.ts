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
import {AfterViewInit, ChangeDetectorRef, Component, OnDestroy, ViewChild} from '@angular/core';
import {MatStepper} from '@angular/material';
import {ActivatedRoute} from '@angular/router';

import {TaskCreationWizard} from 'app/task-creation-wizard/service/task-creation-wizard.service';
import {TaskDefinitionComponent} from 'app/task-creation-wizard/step-1-task-definition/task-definition.component';
import {LabelAssignmentComponent} from 'app/task-creation-wizard/step-2-label-assignment/label-assignment.component';
import {ConfirmationComponent} from 'app/task-creation-wizard/step-3-confirmation/confirmation.component';

import {Timestamp} from 'google-protobuf/google/protobuf/timestamp_pb';
import {TaskPriority} from 'proto/todolist/attributes_pb';
import {TaskCreationId, TaskId} from 'proto/todolist/identifiers_pb';
import {TaskCreation} from 'proto/todolist/model_pb';
import {TaskDescription} from 'proto/todolist/values_pb';
import {SetTaskDetails, StartTaskCreation} from 'proto/todolist/c/commands_pb';
import {LayoutService} from 'app/layout/layout.service';

/**
 * The main component of the Task Creation Wizard.
 *
 * Aggregates wizard steps into {@link MatStepper} and does the common initialization.
 *
 * User can navigate to this component by visiting either `/wizard` or
 * `/wizard/*creation-process-ID*` route.
 *
 * In the first scenario the Task Creation Wizard will be initialized from scratch and a new task
 * draft will be created. In the second scenario, the wizard will try to fetch an existing
 * `TaskCreation` process instance from the server and load its data.
 *
 * The first scenario is a "main" scenario of the wizard usage while the second one allows to return
 * to the wizard after the user navigated somewhere else.
 *
 * The steps in the wizard are sequential but returning back and modifying already completed step
 * is allowed at any point of time.
 *
 * The step navigation is done in such way that user cannot leave the current step until the
 * correct data is specified in all required inputs.
 */
@Component({
  selector: 'app-task-creation-wizard',
  templateUrl: './task-creation-wizard.component.html',
  styleUrls: ['./task-creation-wizard.component.css'],
  providers: [
    TaskCreationWizard
  ]
})
export class TaskCreationWizardComponent implements AfterViewInit, OnDestroy {

  /**
   * The task creation stages with a corresponding {@link stepper} page indices.
   */
  private static readonly STEPS: Map<TaskCreation.Stage, number> = new Map([
    [TaskCreation.Stage.TASK_DEFINITION, 0],
    [TaskCreation.Stage.LABEL_ASSIGNMENT, 1],
    [TaskCreation.Stage.CONFIRMATION, 2]
  ]);

  /**
   * A reference to the Angular Material Stepper which controls the wizard UI.
   *
   * Visible for testing.
   */
  @ViewChild(MatStepper)
  stepper: MatStepper;

  /** Visible for testing. */
  @ViewChild(TaskDefinitionComponent)
  taskDefinition: TaskDefinitionComponent;

  /** Visible for testing. */
  @ViewChild(LabelAssignmentComponent)
  labelAssignment: LabelAssignmentComponent;

  /** Visible for testing. */
  @ViewChild(ConfirmationComponent)
  confirmation: ConfirmationComponent;

  /**
   * Is `true` while the wizard is fetching its data from the server, then becomes `false`.
   *
   * Used for manipulations with UI elements.
   */
  private isLoading: boolean;

  /**
   * A promise to initialize the injected {@link TaskCreationWizard} either "from scratch" or with
   * the data from the pre-loaded process.
   */
  private readonly initWizard: Promise<void>;

  constructor(private readonly wizard: TaskCreationWizard,
              private readonly changeDetector: ChangeDetectorRef,
              private readonly location: Location,
              route: ActivatedRoute,
              private readonly layoutService: LayoutService) {
    this.isLoading = true;
    const taskCreationId = route.snapshot.paramMap.get('taskCreationId');
    this.initWizard = wizard.init(taskCreationId);
    this.layoutService.updateToolbar('Wizard');
    this.layoutService.updateShowNav(false);
  }

  /**
   * Reports an error which cannot really be recovered in the scope of a current page.
   */
  private static reportFatalError(err): void {
    throw new Error(err);
  }

  /**
   * @inheritDoc
   *
   * Initializes this component and the child components with the data from
   * {@link TaskCreationWizard}.
   */
  ngAfterViewInit(): void {
    this.initWizard
      .then(() => {
        this.ensureRouteHasId();
        this.moveToCurrentStep();

        this.taskDefinition.initFromWizard();
        this.labelAssignment.initFromWizard();

        this.isLoading = false;
        this.changeDetector.detectChanges();
      })
      .catch(err => TaskCreationWizardComponent.reportFatalError(err));
  }

  /**
   * Ensures the current route is '/wizard/*current-creation-process-ID*'.
   *
   * To create a new task, user can just navigate to '/wizard'. A task creation process will then
   * be started and assigned a new ID.
   *
   * This method changes the current location, so the user sees the "correct" URL with a task
   * creation ID part.
   */
  private ensureRouteHasId(): void {
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
   * Marks all the preceding {@link stepper} steps as completed (they still can be returned to).
   */
  private moveToCurrentStep(): void {
    const currentStage = this.wizard.stage;
    const index = TaskCreationWizardComponent.STEPS.get(currentStage);
    if (index === undefined) {
      TaskCreationWizardComponent.reportFatalError(
        `There is no wizard step for stage ${currentStage}`
      );
      return;
    }
    this.completeVisitedSteps(index);
    this.stepper.selectedIndex = index;
  }

  /**
   * Completes all {@link stepper} steps prior to the `index`.
   */
  private completeVisitedSteps(index) {
    for (let i = 0; i < this.stepper.steps.length; i++) {
      this.stepper.steps.toArray()[i].completed = i < index;
    }
  }

  ngOnDestroy(): void {
    this.layoutService.defaultLayout();
  }
}
