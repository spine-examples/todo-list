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
import * as uuid from 'uuid';

import {TaskServiceModule} from './task-service.module';

import {TaskId} from 'generated/main/js/todolist/identifiers_pb';
import {TaskDescription} from 'generated/main/js/todolist/values_pb';
import {CompleteTask, CreateBasicTask} from 'generated/main/js/todolist/c/commands_pb';
import {MyListView, TaskItem} from 'generated/main/js/todolist/q/projections_pb';
import {TaskStatus} from 'generated/main/js/todolist/attributes_pb';

/**
 * A service which performs operations on To-Do List tasks.
 */
@Injectable({
  providedIn: TaskServiceModule,
})
export class TaskService {

  /**
   * @param spineWebClient a client for accessing Spine backend
   */
  constructor(private readonly spineWebClient: Client) {
  }

  /**
   * Generates a new {@link TaskId}.
   */
  private static newId(): TaskId {
    const id = new TaskId();
    const value = uuid.v4();
    id.setValue(value);
    return id;
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
   * Completes a task with the specified ID.
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
    const id = TaskService.newId();
    cmd.setId(id);

    const taskDescription = new TaskDescription();
    taskDescription.setValue(description);
    cmd.setDescription(taskDescription);

    this.spineWebClient.sendCommand(cmd, TaskService.logCmdAck, TaskService.logCmdErr);
  }

  // TODO:2019-03-12:dmytro.kuzmin: Actually filter by active, will require extending `TaskItem`
  // todo projection.
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
   * @param reflectInto the array which will receive subscription updates
   * @returns a `Promise` which resolves to an `unsubscribe` function
   */
  subscribeToActive(reflectInto: TaskItem[]): Promise<() => void> {
    const refreshTasks = {
      next: (view: MyListView): void => {
        view.getMyList().getItemsList().forEach(item => console.log(item.getStatus()));
        const taskItems = view.getMyList().getItemsList()
          .filter(task => task.getStatus() === TaskStatus.OPEN);
        // Refresh the array.
        reflectInto.length = 0;
        reflectInto.push(...taskItems);
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
