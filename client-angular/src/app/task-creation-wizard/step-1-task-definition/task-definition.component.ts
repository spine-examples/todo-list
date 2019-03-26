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

import {TaskCreationWizard} from '../service/task-creation-wizard.service';
import {StringValue} from '../../pipes/string-value/string-value.pipe';
import {MomentFromTimestamp} from '../../pipes/moment-from-timestamp/momentFromTimestamp.pipe';
import {WizardStep} from '../wizard-step';

import {Timestamp} from 'google-protobuf/google/protobuf/timestamp_pb';
import {TaskPriority} from 'generated/main/js/todolist/attributes_pb';
import {TaskDescription} from 'generated/main/js/todolist/values_pb';

/**
 * A component which represents the first step of the Task Creation Wizard - a task definition.
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
   * Due date for tasks is allowed to be set starting from today.
   */
  private readonly today: Date = new Date();

  /** Visible for testing. */
  description: TaskDescription;

  /** Visible for testing. */
  priority: TaskPriority;

  /** Visible for testing. */
  dueDate: Timestamp;

  constructor(router: Router, wizard: TaskCreationWizard) {
    super(router, wizard);
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
