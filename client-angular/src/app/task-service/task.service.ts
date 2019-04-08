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

import {Injectable, OnDestroy} from '@angular/core';
import {
  Client,
  ClientError,
  CommandHandlingError,
  ConnectionError,
  ServerError,
  SpineError,
  Type
} from 'spine-web';
import {BehaviorSubject, Observable} from 'rxjs';

import {TaskServiceModule} from 'app/task-service/task-service.module';
import {UuidGenerator} from 'app/uuid-generator/uuid-generator';

import {TaskId} from 'proto/todolist/identifiers_pb';
import {TaskDescription} from 'proto/todolist/values_pb';
import {CompleteTask, CreateBasicTask, DeleteTask} from 'proto/todolist/c/commands_pb';
import {MyListView, TaskItem, TaskView} from 'proto/todolist/q/projections_pb';
import {TaskPriority, TaskStatus} from 'proto/todolist/attributes_pb';
import {NotificationService} from 'app/notification-service/notification.service';

/**
 * A service which performs operations on To-Do List tasks.
 */
@Injectable({
  providedIn: TaskServiceModule,
})
export class TaskService implements OnDestroy {

  private _tasks$: BehaviorSubject<TaskItem[]>;
  private _unsubscribe: () => void;
  private optimisticallyCreatedTasks: TaskItem[] = [];

  /**
   * @param spineWebClient a client for accessing Spine backend
   * @param notificationService a service that notifies users about events that happen in
   * the application
   */
  constructor(private spineWebClient: Client, private notificationService: NotificationService) {
  }

  private static doNothing(): () => void {
    return () => {
    };
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
   * Obtains an `Observable` of tasks all tasks in the list.
   */
  get tasks$(): Observable<TaskItem[]> {
    this.assureTasksInitialized();
    return this._tasks$.asObservable();
  }

  /**
   * Obtains the current value of the task list.
   */
  get tasks(): TaskItem[] {
    this.assureTasksInitialized();
    return this._tasks$.getValue();
  }

  /**
   * Makes sure that array of tasks is initialized.
   */
  private assureTasksInitialized() {
    if (!this._tasks$) {
      this._tasks$ = new BehaviorSubject<TaskItem[]>([]);
      this.subscribeToTasks()
        .then((unsubscribeFn) => this._unsubscribe = unsubscribeFn);
    }
  }

  /**
   * Deletes the task with the specified ID.
   *
   * @param taskId ID of the task to delete.
   */
  deleteTask(taskId: TaskId): void {
    const cmd = new DeleteTask();
    cmd.setId(taskId);
    this.spineWebClient.sendCommand(cmd, TaskService.logCmdAck, TaskService.logCmdErr);
  }

  /**
   * Completes the task with the specified ID, changing its status respectively.
   *
   * @param taskId ID of the task to complete
   */
  completeTask(taskId: TaskId): void {
    const cmd = new CompleteTask();
    cmd.setId(taskId);
    this.spineWebClient.sendCommand(cmd, TaskService.logCmdAck, (err) => this.handleError(err, taskId));
  }

  /**
   * Handles an error that has occurred as a result of a sent command.
   *
   * This method might delete tasks from the `optimisticallyUpdated` list, since some errors signify
   * that that the update has not happened, and therefore an optimistic update was wrong.
   *
   * @param err error that has occurred as a result of a sent command
   * @param taskId ID of the task related to the sent command.
   */
  private handleError(err, taskId: TaskId): void {
    // TODO:2019-04-08:serhii.lekariev: handle all the lists
    if (this.shouldUndoOptimisticOperation(err)) {
      const toBroadcast = this.filterStaleOptimistic(this._tasks$.getValue());
      this.optimisticallyCreatedTasks = this.optimisticallyCreatedTasks.filter(id => id.value !== taskId.value);
      this._tasks$.next(toBroadcast);
      this.notificationService.showSnackbarWith('Task could not be added due to a connection error.');
    }
  }

  /**
   * For the given array, returns a subarray of tasks that have not been confirmed to have came from
   * the server.
   */
  private filterStaleOptimistic(tasks: TaskItem[]) {
    const intersection = tasks.filter(value => this.optimisticallyCreatedTasks.includes(value));
    return tasks.filter(value => !intersection.includes(value));
  }

  createBasicTask(description: string): void {
    const cmd = new CreateBasicTask();
    const id = UuidGenerator.newId(TaskId);
    cmd.setId(id);

    const taskDescription = new TaskDescription();
    taskDescription.setValue(description);
    cmd.setDescription(taskDescription);

    const createdTask = new TaskItem();
    createdTask.setId(id);
    createdTask.setDescription(taskDescription);
    createdTask.setStatus(TaskStatus.OPEN);

    this.spineWebClient.sendCommand(cmd, TaskService.doNothing, (err) => this.handleError(err, id));
    this.optimisticallyCreatedTasks.push(createdTask);
    const tasksToBroadcast: TaskItem[] = [...this._tasks$.getValue(), createdTask];
    this._tasks$.next(tasksToBroadcast);
    return createdTask;
  }

  /**
   * Fetches a single task details.
   *
   * If nothing is found by the specified ID, the promise is rejected.
   */
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
   * Subscribes to the active tasks and reflects them to the array stored in this instance of the
   * task service.
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
  private subscribeToTasks(): Promise<() => void> {
    const refreshTasks = {
      next: (view: MyListView): void => {
        if (view) {
          const taskItems: TaskItem[] = view.getMyList().getItemsList();
          const toBroadcast = this.filterStaleOptimistic(taskItems);
          this._tasks$.next(toBroadcast);
        }
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

  ngOnDestroy(): void {
    if (this._unsubscribe) {
      this._unsubscribe();
    }
  }

  private shouldUndoOptimisticOperation(err: CommandHandlingError) {
    const shouldUndoFns: Array<(err: CommandHandlingError) => boolean> = [
      (e: CommandHandlingError) => e.assuresCommandNeglected(),
      (e: CommandHandlingError) => e._cause.name.startsWith('ConnectionError'),
      (e: CommandHandlingError) => e instanceof ConnectionError
    ];
    for (const key in shouldUndoFns) {
      if (shouldUndoFns[key](err)) {
        return true;
      }
    }
    return false;
  }
}

