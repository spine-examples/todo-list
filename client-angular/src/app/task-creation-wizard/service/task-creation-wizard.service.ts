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
import {UuidGenerator} from '../../uuid-generator/uuid-generator';
import {TaskService} from '../../task-service/task.service';

import {Message} from 'google-protobuf';
import {Timestamp} from 'google-protobuf/google/protobuf/timestamp_pb';
import {TimestampChange} from 'spine-web/proto/spine/change/change_pb';
import {TaskPriority} from 'generated/main/js/todolist/attributes_pb';
import {DescriptionChange, PriorityChange} from 'generated/main/js/todolist/changes_pb';
import {LabelId, TaskCreationId, TaskId} from 'generated/main/js/todolist/identifiers_pb';
import {TaskCreation} from 'generated/main/js/todolist/model_pb';
import {TaskDescription} from 'generated/main/js/todolist/values_pb';
import {
  AddLabels,
  CancelTaskCreation,
  CompleteTaskCreation,
  SkipLabels,
  StartTaskCreation,
  UpdateTaskDetails
} from 'generated/main/js/todolist/c/commands_pb';

/**
 * A service which executes commands specific to the Task Creation Wizard process.
 *
 * The service is stateful and is re-instantiated every time the user navigates to the wizard.
 *
 * All the fields are kept up-to-date with the changes on the server side.
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
  private _taskLabels: LabelId[];

  constructor(private readonly spineWebClient: Client,
              private readonly taskService: TaskService) {
  }

  /**
   * Inits the wizard for the given task creation process ID.
   *
   * If the ID is not specified, wizard starts "from scratch", creating a new task draft on
   * the server side.
   *
   * If the ID is specified, the wizard attempts to load an existing task creation process and
   * fetch its data as well as the task draft data.
   */
  init(taskCreationId?: string): Promise<void> {
    if (taskCreationId) {
      const processId = UuidGenerator.newIdWithValue(taskCreationId, TaskCreationId);
      return this.restore(processId);
    } else {
      return this.start();
    }
  }

  /**
   * Sends a command to update the task details.
   *
   * The description field is required and checked separately, throwing an error if it's not
   * specified.
   *
   * This is a temporary decision as our `spine-web` client does not support notifying
   * on rejections.
   */
  updateTaskDetails(description: TaskDescription, priority?: TaskPriority, dueDate?: Timestamp)
    : Promise<void> {
    if (!description) {
      return Promise.reject('Description value must be set.');
    }
    if (dueDate && dueDate.toDate() < new Date()) {
      return Promise.reject(
        `Task due date is allowed starting from tomorrow, specified date: ${dueDate.toDate()}`
      );
    }
    const cmd = this.prepareUpdateCommand(description, priority, dueDate);

    const sendCommand =
      !!cmd.getDescriptionChange() || !!cmd.getPriorityChange() || !!cmd.getDueDateChange();
    if (!sendCommand) {
      return Promise.resolve();
    }
    const updateTask = new Promise<void>((resolve, reject) =>
      this.spineWebClient.sendCommand(cmd, resolve, reject, reject)
    );
    return updateTask.then(() => {
      this._taskDescription = description;
      this._taskPriority = priority;
      this._taskDueDate = dueDate;
      this._stage = TaskCreation.Stage.LABEL_ASSIGNMENT;
    });
  }

  /**
   * Adds labels to the task draft.
   *
   * Empty label list is not allowed, the {@link skipLabelAssignment} method should be used for
   * such cases instead.
   *
   * Checking this "by hand" is, again, a temporary solution, until `spine-web` properly supports
   * rejections.
   */
  addLabels(labelIds: LabelId[]): Promise<void> {
    if (labelIds.length === 0) {
      return Promise.reject(
        'Empty label array is not allowed in `AddLabels` command, use `SkipLabels` instead'
      );
    }
    const cmd = new AddLabels();
    cmd.setId(this._id);
    cmd.setExistingLabelsList(labelIds);

    const addLabels = new Promise<void>((resolve, reject) =>
      this.spineWebClient.sendCommand(cmd, resolve, reject, reject)
    );
    return addLabels.then(() => {
      this._taskLabels = labelIds;
      this._stage = TaskCreation.Stage.CONFIRMATION;
    });
  }

  /**
   * Skips the label assignment stage and proceeds to `TaskCreation.Stage.CONFIRMATION`.
   */
  skipLabelAssignment(): Promise<void> {
    const cmd = new SkipLabels();
    cmd.setId(this._id);
    const skipLabels = new Promise<void>((resolve, reject) =>
      this.spineWebClient.sendCommand(cmd, resolve, reject, reject)
    );
    return skipLabels.then(() => this._stage = TaskCreation.Stage.CONFIRMATION);
  }

  /**
   * Completes the task creation, setting the draft status to `FINALIZED`.
   */
  completeTaskCreation(): Promise<void> {
    const cmd = new CompleteTaskCreation();
    cmd.setId(this._id);
    const completeProcess = new Promise<void>((resolve, reject) =>
      this.spineWebClient.sendCommand(cmd, resolve, reject, reject)
    );
    return completeProcess.then(() => this._stage = TaskCreation.Stage.COMPLETED);
  }

  /**
   * Cancels the task creation, setting its status to `CANCELED`.
   */
  cancelTaskCreation(): Promise<void> {
    const cmd = new CancelTaskCreation();
    cmd.setId(this._id);
    const cancelProcess = new Promise<void>((resolve, reject) =>
      this.spineWebClient.sendCommand(cmd, resolve, reject, reject)
    );
    return cancelProcess.then(() => this._stage = TaskCreation.Stage.CANCELED);
  }

  /**
   * Starts the task creation process "from scracth", creating a new process manager instance and a
   * new task draft on the server side.
   */
  private start(): Promise<void> {
    const taskCreationId = UuidGenerator.newId(TaskCreationId);
    const taskId = UuidGenerator.newId(TaskId);
    const cmd = new StartTaskCreation();
    cmd.setId(taskCreationId);
    cmd.setTaskId(taskId);
    return new Promise<TaskCreationId>((resolve, reject) => {
        const startProcess = () => {
          this._id = taskCreationId;
          this._taskId = taskId;
          this._stage = TaskCreation.Stage.TASK_DEFINITION;
          this._taskLabels = [];
          resolve();
        };
        this.spineWebClient.sendCommand(cmd, startProcess, reject, reject);
      }
    );
  }

  /**
   * Restores the existing task creation process by ID.
   *
   * This method queries process manager directly but it's OK as the use case
   * scenario for it is really rare.
   */
  private restore(taskCreationId: TaskCreationId): Promise<void> {
    this._id = taskCreationId;
    return this.restoreProcessDetails()
      .then(() => this.restoreTaskDetails());
  }

  private restoreProcessDetails(): Promise<void> {
    return this.fetchProcessDetails()
      .then(taskCreation => {
        this._taskId = taskCreation.getTaskId();
        this._stage = taskCreation.getStage();
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
      // noinspection JSIgnoredPromiseFromCall Method wrongly resolved by IDEA.
      this.spineWebClient.fetchById(Type.forClass(TaskCreation), this._id, dataCallback, reject);
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
        this._taskLabels = task.getLabelIdsList() ? task.getLabelIdsList().getIdsList() : [];
      });
  }

  /**
   * Prepares an `UpdateTaskDetails` command from the given NG model entries.
   */
  private prepareUpdateCommand(description, priority, dueDate) {
    const cmd = new UpdateTaskDetails();
    cmd.setId(this._id);

    if (description !== this._taskDescription) {
      const descriptionChange = new DescriptionChange();
      descriptionChange.setNewValue(description);
      if (this._taskDescription) {
        descriptionChange.setPreviousValue(this._taskDescription);
      }
      cmd.setDescriptionChange(descriptionChange);
    }

    if (priority !== this._taskPriority) {
      const priorityChange = new PriorityChange();
      priorityChange.setNewValue(priority);
      if (this._taskPriority) {
        priorityChange.setPreviousValue(this._taskPriority);
      }
      cmd.setPriorityChange(priorityChange);
    }
    if (dueDate !== this._taskDueDate) {
      const dueDateChange = new TimestampChange();
      dueDateChange.setNewValue(dueDate);
      if (this._taskDueDate) {
        dueDateChange.setPreviousValue(this._taskDueDate);
      }
      cmd.setDueDateChange(dueDateChange);
    }
    return cmd;
  }

  get id(): TaskCreationId {
    return this._id;
  }

  get taskId(): TaskId {
    return this._taskId;
  }

  get stage(): TaskCreation.Stage {
    return this._stage;
  }

  get taskDescription(): TaskDescription {
    return this._taskDescription;
  }

  /** For tests only. */
  set taskDescription(value: TaskDescription) {
    this._taskDescription = value;
  }

  get taskPriority(): TaskPriority {
    return this._taskPriority;
  }

  /** For tests only. */
  set taskPriority(value: TaskPriority) {
    this._taskPriority = value;
  }

  get taskDueDate(): Timestamp {
    return this._taskDueDate;
  }

  /** For tests only. */
  set taskDueDate(value: Timestamp) {
    this._taskDueDate = value;
  }

  get taskLabels(): LabelId[] {
    return this._taskLabels;
  }
}
