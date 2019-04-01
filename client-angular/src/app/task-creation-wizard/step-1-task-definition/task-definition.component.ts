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
import {Moment} from 'moment';

import {TaskCreationWizard} from 'app/task-creation-wizard/service/task-creation-wizard.service';
import {StringValue} from 'app/pipes/string-value/string-value.pipe';
import {MomentFromTimestamp} from 'app/pipes/moment-from-timestamp/momentFromTimestamp.pipe';
import {WizardStep} from 'app/task-creation-wizard/wizard-step';

import {Timestamp} from 'google-protobuf/google/protobuf/timestamp_pb';
import {TaskPriority} from 'proto/todolist/attributes_pb';
import {TaskDescription} from 'proto/todolist/values_pb';

/**
 * A component which represents the first step of the Task Creation Wizard - a task definition.
 *
 * The "aim" of this step is to send an `UpdateTaskDetails` command with the new task properties
 * specified by the user.
 *
 * It is possible to return to this step at any stage of the task creation in the future, but it
 * must be completed at least one time before proceeding to the next step.
 */
@Component({
  selector: 'app-task-definition',
  templateUrl: './task-definition.component.html'
})
export class TaskDefinitionComponent extends WizardStep {

  /**
   * Possible task priorities.
   */
  private readonly TASK_PRIORITIES: TaskPriority =
    [TaskPriority.HIGH, TaskPriority.NORMAL, TaskPriority.LOW];

  /**
   * Due date for tasks is allowed to be set starting from tomorrow.
   *
   * As the due date receives default '00:00' time, it's the closest value that is reasonable.
   */
  private readonly tomorrow: Date = TaskDefinitionComponent.tomorrow();

  /** Visible for testing. */
  description: TaskDescription;

  /** Visible for testing. */
  priority: TaskPriority;

  /** Visible for testing. */
  dueDate: Timestamp;

  constructor(router: Router, wizard: TaskCreationWizard) {
    super(router, wizard);
  }

  private static tomorrow(): Date {
    const result = new Date();
    result.setDate(result.getDate() + 1);
    return result;
  }

  /**
   * @inheritDoc
   */
  initFromWizard() {
    this.description = this.wizard.taskDescription;
    this.priority = this.wizard.taskPriority;
    this.dueDate = this.wizard.taskDueDate;
  }

  /**
   * Handles the task description change done on the UI.
   *
   * Visible for testing.
   */
  setDescription(value: string): void {
    this.onUserInput();
    this.description = StringValue.back(value, TaskDescription);
  }

  /**
   * Handles the task priority change done on the UI.
   *
   * Visible for testing.
   */
  setPriority(value: TaskPriority): void {
    this.onUserInput();
    this.priority = value;
  }

  /**
   * Handles the due date change done on the UI.
   *
   * Visible for testing.
   */
  setDueDate(value: Moment): void {
    this.onUserInput();
    this.dueDate = MomentFromTimestamp.back(value);
  }

  /**
   * @inheritDoc
   */
  protected doStep(): Promise<void> {
    return this.wizard.updateTaskDetails(this.description, this.priority, this.dueDate);
  }
}
