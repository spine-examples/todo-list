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
import {LabelColor, TaskPriority} from 'generated/main/js/todolist/attributes_pb';
import {LabelId, TaskCreationId, TaskId} from 'generated/main/js/todolist/identifiers_pb';
import {TaskCreation, TaskLabel} from 'generated/main/js/todolist/model_pb';
import {TaskDescription} from 'generated/main/js/todolist/values_pb';
import {SetTaskDetails, StartTaskCreation} from 'generated/main/js/todolist/c/commands_pb';

export function mockLabels(): TaskLabel[] {
  const label1 = new TaskLabel();
  const id1 = UuidGenerator.newId(LabelId);
  label1.setId(id1);
  label1.setTitle('Reds');
  label1.setColor(LabelColor.RED);

  const label2 = new TaskLabel();
  const id2 = UuidGenerator.newId(LabelId);
  label2.setId(id2);
  label2.setTitle('Blues');
  label2.setColor(LabelColor.BLUE);


  const label3 = new TaskLabel();
  const id3 = UuidGenerator.newId(LabelId);
  label3.setId(id3);
  label3.setTitle('Grays');
  label3.setColor(LabelColor.GRAY);
  return [label1, label2, label3];
}

/**
 * A service which executes commands specific to the Task Creation Wizard process.
 *
 * The service is stateful and is re-instantiated every time the user navigates to the wizard.
 *
 * The service is not injected in-place ("`providedIn(...)`") to avoid circular dependency.
 */
@Injectable()
export class TaskCreationWizard {

  private _id: TaskCreationId;
  private _stage: TaskCreation.Stage;

  private _taskId: TaskId;
  private _description: string;
  private _priority: TaskPriority;
  private _dueDate: Moment;
  private _taskLabels: TaskLabel[];

  constructor(private readonly spineWebClient: Client) {
  }

  init(taskCreationId: TaskCreationId): Promise<void> {
    if (taskCreationId) {
      return this.restore(taskCreationId);
    } else {
      return this.start();
    }
  }

  private restore(taskCreationId: TaskCreationId): Promise<void> {
    return Promise.resolve();
  }

  private start(): Promise<void> {
    const taskCreationId = UuidGenerator.newId(TaskCreationId);
    const taskId = UuidGenerator.newId(TaskId);
    const cmd = new StartTaskCreation();
    cmd.setId(taskCreationId);
    cmd.setTaskId(taskId);

    const startProcess = resolve => {
      this._id = taskCreationId;
      this._taskId = taskId;
      this._stage = TaskCreation.Stage.TASK_DEFINITION;
      this._taskLabels = mockLabels();
      resolve();
    };
    return new Promise<TaskCreationId>((resolve, reject) =>
      this.spineWebClient.sendCommand(cmd, startProcess(resolve), reject, reject)
    );
  }

  updateTaskDetails(description: string, priority?: TaskPriority, dueDate?: Moment)
    : Promise<void> {
    const cmd = new SetTaskDetails();
    cmd.setId(this._id);

    const taskDescription = new TaskDescription();
    taskDescription.setValue(description);
    cmd.setDescription(taskDescription);

    if (priority) {
      cmd.setPriority(priority);
    }
    if (dueDate) {
      const toDate = dueDate.toDate();
      const taskDueDate = Timestamp.fromDate(toDate);
      cmd.setDueDate(taskDueDate);
    }
    return new Promise<void>((resolve, reject) =>
      this.spineWebClient.sendCommand(cmd, resolve, reject, reject)
    );
  }

  get id(): TaskCreationId {
    return this._id;
  }

  get stage(): TaskCreation.Stage {
    return this._stage;
  }

  get description(): string {
    return this._description;
  }

  get priority(): TaskPriority {
    return this._priority;
  }

  get dueDate(): Moment {
    return this._dueDate;
  }

  get taskLabels(): TaskLabel[] {
    return this._taskLabels;
  }
}
