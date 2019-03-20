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
import {Moment} from 'moment';

import {TaskCreationWizard} from '../service/task-creation-wizard.service';
import {StringValue} from '../../pipes/string-value/string-value.pipe';
import {MomentFromTimestamp} from '../../pipes/moment-from-timestamp/momentFromTimestamp.pipe';
import {WizardStep} from '../wizard-step';

import {Timestamp} from 'google-protobuf/google/protobuf/timestamp_pb';
import {TaskPriority} from 'generated/main/js/todolist/attributes_pb';
import {TaskDescription} from 'generated/main/js/todolist/values_pb';

@Component({
  selector: 'app-task-definition',
  templateUrl: './task-definition.component.html',
  styleUrls: ['./task-definition.component.css']
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

  description: TaskDescription;
  priority: TaskPriority;
  dueDate: Timestamp;

  constructor(wizard: TaskCreationWizard, location: Location) {
    super(location, wizard);
  }

  /**
   * This method is not a property as publicly available setter for {@link description} field is
   * also required and should accept `TaskDescription`.
   */
  setDescription(value: string): void {
    this.onInputChange();
    this.description = StringValue.back(value, TaskDescription);
  }

  setPriority(value: TaskPriority): void {
    this.onInputChange();
    this.priority = value;
  }

  setDueDate(value: Moment): void {
    this.onInputChange();
    this.dueDate = MomentFromTimestamp.back(value);
  }

  protected doStep(): Promise<void> {
    return this.wizard.updateTaskDetails(this.description, this.priority, this.dueDate);
  }
}
