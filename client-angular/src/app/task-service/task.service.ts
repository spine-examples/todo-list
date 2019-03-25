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

import {TaskServiceModule} from './task-service.module';
import {UuidGenerator} from '../uuid-generator/uuid-generator';

import {TaskId} from 'generated/main/js/todolist/identifiers_pb';
import {TaskDescription} from 'generated/main/js/todolist/values_pb';
import {CompleteTask, CreateBasicTask, DeleteTask} from 'generated/main/js/todolist/c/commands_pb';
import {MyListView, TaskItem, TaskView} from 'generated/main/js/todolist/q/projections_pb';
import {TaskStatus} from 'generated/main/js/todolist/attributes_pb';

/**
 * A service which performs operations on To-Do List tasks.
 */
@Injectable({
  providedIn: TaskServiceModule,
})
export class TaskService {

  private readonly _tasks: TaskItem[] = [];
  private _unsubscribe: () => void;

  /**
   * @param spineWebClient a client for accessing Spine backend
   */
  constructor(private readonly spineWebClient: Client) {
    this.subscribeToTasks()
      .then((unsubscribeFn) => this._unsubscribe = unsubscribeFn);
  }

  /**
   * Obtains a current view of tasks.
   */
  get tasks(): TaskItem {
    return this._tasks;
  }

  /**
   * Obtains a function to unsubscribe from changes to the view of the task list.
   */
  get unsubscribe(): () => void {
    return this._unsubscribe;
  }

  /** Visible for testing. */
  static logCmdAck() {
    console.log('Command acknowledged by the server');
  }

  /** Visible for testing. */
  static logCmdErr(err) {
    console.log('Error when sending command to the server: %s', err);
  }

  /**
   * Deletes the task with the specified ID.
   *
   * @param taskId ID of the task to delete.
   */
  deleteTask(taskId: string): void {
    const cmd = new DeleteTask();
    const id = new TaskId();
    id.setValue(taskId);
    cmd.setId(id);
    this.spineWebClient.sendCommand(cmd, TaskService.logCmdAck, TaskService.logCmdErr);
  }

  /**
   * Completes the task with the specified ID, changing its status respectively.
   *
   * @param taskId ID of the task to complete
   */
  completeTask(taskId: string): void {
    const cmd = new CompleteTask();
    const id = new TaskId();
    id.setValue(taskId);
    cmd.setId(id);
    this.spineWebClient.sendCommand(cmd, TaskService.logCmdAck, TaskService.logCmdErr);
  }

  /**
   * Creates a task with a given description and a randomly generated ID.
   *
   * @param description a description of the new task
   */
  createBasicTask(description: string): void {
    const cmd = new CreateBasicTask();
    const id = UuidGenerator.newId(TaskId);
    cmd.setId(id);

    const taskDescription = new TaskDescription();
    taskDescription.setValue(description);
    cmd.setDescription(taskDescription);

    this.spineWebClient.sendCommand(cmd, TaskService.logCmdAck, TaskService.logCmdErr);
  }

  fetchById(id: TaskId): Promise<TaskView> {
    return new Promise<TaskView>((resolve, reject) => {
      const dataCallback = task => {
        if (!task) {
          reject(`No task view found for ID: ${id}`);
        } else {
          resolve(task);
        }
      };
      // noinspection JSIgnoredPromiseFromCall Method wrongly resolved by IDEA.
      this.spineWebClient.fetchById(Type.forClass(TaskView), id, dataCallback, reject);
    });
  }

  /**
   * Subscribes to the active tasks and reflects them to a given array.
   *
   * Active tasks are those which are not in draft state, completed, or deleted.
   *
   * The tasks are retrieved from the `MyListView` projection, which is an application-wide
   * singleton storing active and completed task items.
   *
   * Subscription can be cancelled via the method return value, which is a `Promise` resolving to
   * the `unsubscribe` function.
   *
   * @returns a `Promise` which resolves to an `unsubscribe` function
   */
  subscribeToTasks(): Promise<() => void> {
    const refreshTasks = {
      next: (view: MyListView): void => {
        const taskItems = view.getMyList().getItemsList();
        // Refresh the array.
        this.tasks.length = 0;
        this.tasks.push(...taskItems);
      }
    };
    const type = Type.forClass(MyListView);
    return new Promise((resolve, reject) =>
      this.spineWebClient.subscribeToEntities({ofType: type})
        .then((subscriptionObject) => {
          subscriptionObject.itemAdded.subscribe(refreshTasks);
          subscriptionObject.itemChanged.subscribe(refreshTasks);

          resolve(subscriptionObject.unsubscribe);
        })
        .catch(err => {
          console.log(
            'Cannot subscribe to entities of type (`%s`): %s',
            MyListView.typeUrl(), err
          );
          reject(err);
        })
    );
  }
}
