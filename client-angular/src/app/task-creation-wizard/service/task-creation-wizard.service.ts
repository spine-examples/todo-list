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
import {Client, Type} from 'spine-web';

import {StringValue} from '../../pipes/string-value/string-value.pipe';
import {UuidGenerator} from '../../uuid-generator/uuid-generator';
import {LabelService} from '../../labels/label.service';
import {TaskService} from '../../task-service/task.service';

import {Message} from 'google-protobuf';
import {Timestamp} from 'google-protobuf/google/protobuf/timestamp_pb';
import {LabelColor, TaskPriority} from 'generated/main/js/todolist/attributes_pb';
import {LabelId, TaskCreationId, TaskId} from 'generated/main/js/todolist/identifiers_pb';
import {Task, TaskCreation, TaskLabel, TaskLabels} from 'generated/main/js/todolist/model_pb';
import {TaskDescription} from 'generated/main/js/todolist/values_pb';
import {
  AddLabels,
  CancelTaskCreation,
  CompleteTaskCreation,
  SetTaskDetails,
  SkipLabels,
  StartTaskCreation
} from 'generated/main/js/todolist/c/commands_pb';

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
  private _taskDescription: TaskDescription;
  private _taskPriority: TaskPriority;
  private _taskDueDate: Timestamp;
  private _taskLabels: TaskLabel[];

  constructor(private readonly spineWebClient: Client,
              private readonly taskService: TaskService,
              private readonly labelService: LabelService) {
  }

  init(taskCreationId: string): Promise<void> {
    if (taskCreationId) {
      const processId = StringValue.back(taskCreationId, TaskCreationId);
      return this.restore(processId);
    } else {
      return this.start();
    }
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
      this._taskLabels = [];
      resolve();
    };
    return new Promise<TaskCreationId>((resolve, reject) =>
      this.spineWebClient.sendCommand(cmd, startProcess(resolve), reject, reject)
    );
  }

  private restore(taskCreationId: TaskCreationId): Promise<void> {
    return this.restoreProcessDetails(taskCreationId)
      .then(() => this.restoreTaskDetails())
      .then(() => this.restoreTaskLabels());
  }

  private restoreProcessDetails(taskCreationId: TaskCreationId): Promise<void> {
    return this.fetchProcessDetails()
      .then(taskCreation => {
        this._id = taskCreationId;
        this._taskId = taskCreation.getTaskId();
        this._stage = taskCreation.getStage();
      });
  }

  private restoreTaskDetails(): Promise<void> {
    return this.taskService.fetchById(this._taskId)
      .then(task => {
        if (task.getDescription()) {
          this._taskDescription = task.getDescription();
        }
        if (task.getPriority()) {
          this._taskPriority = task.getPriority();
        }
        if (task.getDueDate()) {
          this._taskDueDate = task.getDueDate();
        }
      });
  }

  private restoreTaskLabels(): Promise<void> {
    return this.labelService.fetchTaskLabels(this._taskId)
      .then(labels => {
        this._taskLabels = labels;
      });
  }

  private fetchProcessDetails(): Promise<TaskCreation> {
    return new Promise<TaskCreation>((resolve, reject) => {
      const dataCallback = processDetails => {
        if (!processDetails) {
          reject(`No task creation process found for ID: ${this._id}`);
        } else {
          resolve(processDetails);
        }
      };
      this.spineWebClient.fetchById(TaskCreation, this._id, dataCallback, reject);
    });
  }

  updateTaskDetails(description: TaskDescription, priority?: TaskPriority, dueDate?: Timestamp)
    : Promise<void> {
    const cmd = new SetTaskDetails();
    cmd.setId(this._id);
    cmd.setDescription(description);
    if (priority) {
      cmd.setPriority(priority);
    }
    if (dueDate) {
      cmd.setDueDate(dueDate);
    }
    const updateTask = new Promise<void>((resolve, reject) =>
      this.spineWebClient.sendCommand(cmd, resolve, reject, reject)
    );
    return updateTask.then(() => {
      this._taskDescription = description;
      this._taskPriority = priority;
      this._taskDueDate = dueDate;
    });
  }

  /**
   * The stub method currently used because of the issue with `Task` and `TaskLabels` aggregate
   * parts.
   *
   * @see addLabelsReal
   */
  addLabelsFake(labels: TaskLabel[]): Promise<void> {
    return this.skipLabelAssignment().then(() => {
      this._taskLabels = labels;
    });
  }

  /**
   * Adds labels to the created task.
   *
   * This method is a back-up method for when the bug with aggregate parts overriding one another
   * is fixed and the real method assigning process can be used.
   *
   * For now, the {@linkplain addLabels stub method} is used.
   */
  // noinspection JSUnusedGlobalSymbols See doc.
  addLabels(labels: TaskLabel[]): Promise<void> {
    const cmd = new AddLabels();
    cmd.setId(this._id);
    const labelIds = labels.map(label => label.getId());
    cmd.setExistingLabelsList(labelIds);

    const addLabels = new Promise<void>((resolve, reject) =>
      this.spineWebClient.sendCommand(cmd, resolve, reject, reject)
    );
    return addLabels.then(() => {
      this._taskLabels = labels;
    });
  }

  skipLabelAssignment(): Promise<void> {
    const cmd = new SkipLabels();
    cmd.setId(this._id);
    return new Promise<void>((resolve, reject) =>
      this.spineWebClient.sendCommand(cmd, resolve, reject, reject)
    );
  }

  completeTaskCreation(): Promise<void> {
    const cmd = new CompleteTaskCreation();
    cmd.setId(this._id);
    return new Promise<void>((resolve, reject) =>
      this.spineWebClient.sendCommand(cmd, resolve, reject, reject)
    );
  }

  cancelTaskCreation(): Promise<void> {
    const cmd = new CancelTaskCreation();
    cmd.setId(this._id);
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

  get taskDescription(): TaskDescription {
    return this._taskDescription;
  }

  get taskPriority(): TaskPriority {
    return this._taskPriority;
  }

  get taskDueDate(): Timestamp {
    return this._taskDueDate;
  }

  get taskLabels(): TaskLabel[] {
    return this._taskLabels;
  }
}
