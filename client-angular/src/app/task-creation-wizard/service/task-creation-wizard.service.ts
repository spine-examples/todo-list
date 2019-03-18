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

import {Injectable} from '@angular/core';
import {Moment} from 'moment';
import {Client} from 'spine-web';

import {UuidGenerator} from '../../uuid-generator/uuid-generator';

import {Timestamp} from 'google-protobuf/google/protobuf/timestamp_pb';
import {TaskPriority} from 'generated/main/js/todolist/attributes_pb';
import {TaskCreationId, TaskId} from 'generated/main/js/todolist/identifiers_pb';
import {TaskDescription} from 'generated/main/js/todolist/values_pb';
import {SetTaskDetails, StartTaskCreation} from 'generated/main/js/todolist/c/commands_pb';

/**
 * A service which executes commands specific to the Task Creation Wizard process.
 *
 * The service is stateful and is re-instantiated every time the user navigates to the wizard.
 *
 * Is not injected in-place ("`providedIn(...)`") to avoid circular dependency.
 */
@Injectable()
export class TaskCreationWizard {

  private taskId;
  private taskCreationId;

  constructor(private readonly spineWebClient: Client) {
  }

  setTaskDetails(details: TaskDetails): Promise<void> {
    if (!this.taskCreationId) {
      return this.startTaskCreation().then(() => this.updateTaskDetails(details));
    }
    return this.updateTaskDetails(details);
  }

  private startTaskCreation(): Promise<void> {
    const taskCreationId = UuidGenerator.newId(TaskCreationId);
    const taskId = UuidGenerator.newId(TaskId);
    const cmd = new StartTaskCreation();
    cmd.setId(taskCreationId);
    cmd.setTaskId(taskId);
    return new Promise<void>(
      (resolve, reject) => {
        const startProcess = () => {
          this.taskCreationId = taskCreationId;
          this.taskId = taskId;
          resolve();
        };
        this.spineWebClient.sendCommand(cmd, startProcess, reject, reject);
      }
    );
  }

  private updateTaskDetails(details: TaskDetails): Promise<void> {
    const cmd = new SetTaskDetails();
    cmd.setId(this.taskCreationId);

    const description = new TaskDescription();
    description.setValue(details.description);
    cmd.setDescription(description);

    if (details.priority) {
      cmd.setPriority(details.priority);
    }
    if (details.dueDate) {
      const toDate = details.dueDate.toDate();
      const dueDate = Timestamp.fromDate(toDate);
      cmd.setDueDate(dueDate);
    }
    return new Promise<void>((resolve, reject) =>
      this.spineWebClient.sendCommand(cmd, resolve, reject, reject)
    );
  }
}

export interface TaskDetails {
  description: string;
  priority: TaskPriority;
  dueDate: Moment;
}
