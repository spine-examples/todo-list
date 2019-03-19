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
import {AfterViewInit, Component, Input} from '@angular/core';
import {MatStepper} from '@angular/material/stepper';
import {Moment} from 'moment';


import {TaskCreationWizard} from '../service/task-creation-wizard.service';

import {Timestamp} from 'google-protobuf/google/protobuf/timestamp_pb';
import {TaskPriority} from 'generated/main/js/todolist/attributes_pb';

@Component({
  selector: 'app-task-definition',
  templateUrl: './task-definition.component.html',
  styleUrls: ['./task-definition.component.css']
})
export class TaskDefinitionComponent implements AfterViewInit {

  /** Visible for testing. */
  @Input()
  stepper: MatStepper;

  description: string;
  priority: TaskPriority;
  dueDate: Moment;

  /**
   * Possible task priorities.
   */
  private readonly TASK_PRIORITIES: TaskPriority =
    [TaskPriority.HIGH, TaskPriority.NORMAL, TaskPriority.LOW];

  /**
   * Due date for tasks is allowed to be set starting from today.
   */
  private readonly today: Date = new Date();

  constructor(private readonly wizard: TaskCreationWizard, private readonly location: Location) {
  }

  ngAfterViewInit(): void {
    this.setNotCompleted();
  }

  /**
   * Handles situations when we return to the "Task Definition" page from the later stages and it's
   * initially marked as completed.
   */
  onInputChange(): void {
    this.setNotCompleted();
  }

  next(): void {
    this.wizard.updateTaskDetails(this.description, this.priority, this.dueDate)
      .then(() => {
        this.informOnDraftCreation();
        this.setCompleted();
        this.stepper.next();
      }).catch(err => {
      this.reportError(err);
    });
  }

  /**
   * Just go back.
   */
  cancel(): void {
    this.location.back();
  }

  private informOnDraftCreation(): void {
    console.log('Draft created');
  }

  private reportError(err): void {
    console.log(`Error when setting task details: ${err}`);
  }

  private setCompleted(): void {
    this.stepper.selected.completed = true;
  }

  private setNotCompleted(): void {
    this.stepper.selected.completed = false;
  }
}
