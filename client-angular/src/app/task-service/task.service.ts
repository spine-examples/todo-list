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
 * An operation that involves a task with the specified ID.
 *
 * `undoOperation` specifies a way to undo this operation, i.e. to bring the task list to the state
 * which it had before the operation occurred.
 */
interface TaskOperation {
  taskId: TaskId;
  undoOperation: (t: TaskItem) => void;
}

/**
 * A service which performs operations on To-Do List tasks.
 */
@Injectable({
  providedIn: TaskServiceModule,
})
export class TaskService implements OnDestroy {

  private _tasks$: BehaviorSubject<TaskItem[]>;
  private _unsubscribe: () => void;
  private optimisticallyChanged: TaskItem[] = [];

  /**
   * @param spineWebClient a client for accessing Spine backend
   * @param notificationService a service that notifies users about events that happen in
   * the application
   */
  constructor(private spineWebClient: Client, private notificationService: NotificationService) {
  }

  /** Visible for testing. */
  static doNothing(): () => void {
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
   *
   * Visible for testing.
   */
  public assureTasksInitialized() {
    if (!this._tasks$) {
      this._tasks$ = new BehaviorSubject<TaskItem[]>([]);
      this.subscribeToTasks()
        .then((unsubscribeFn) => this._unsubscribe = unsubscribeFn);
    }
  }

  /**
   * Deletes the task with the specified ID, immediately broadcasting the new task list via the
   * `tasks$`.
   *
   * If handling of the task deletion command fails, task deletion is undone and the `tasks$`
   * observable is updated accordingly.
   *
   * @param taskId ID of the task to delete.
   */
  deleteTask(taskId: TaskId): void {
    const cmd = new DeleteTask();
    cmd.setId(taskId);
    const toBroadcast = this.tasks.map(task => {
      if (task.getId() === taskId) {
        task.setStatus(TaskStatus.DELETED);
        this.optimisticallyChanged.push(task);
      }
      return task;
    });
    this._tasks$.next(toBroadcast);
    this.spineWebClient.sendCommand(cmd, TaskService.logCmdAck,
      err => this.handleError(err, {
        taskId,
        undoOperation: () => {
          this.recoverTask(taskId);
        }
      }));
  }

  /**
   * Completes the task with the specified ID, changing its status respectively, immediately
   * broadcasting the new task list via the `tasks$` observable.
   *
   * If handling of the task completion command fails, the completion is undone and the `tasks$`
   * observable is updated accordingly
   *
   * @param taskId ID of the task to complete
   */
  completeTask(taskId: TaskId): void {
    const cmd = new CompleteTask();
    cmd.setId(taskId);
    const toBroadcast = this.tasks.map(task => {
      if (task.getId() === taskId) {
        task.setStatus(TaskStatus.COMPLETED);
        this.optimisticallyChanged.push(task);
      }
      return task;
    });
    this._tasks$.next(toBroadcast);
    this.spineWebClient.sendCommand(cmd, TaskService.logCmdAck,
      (err) => this.handleError(err, {
        taskId,
        undoOperation: () => this.recoverTask(taskId)
      }));
  }

  /**
   * Creates a task with the specified description and `OPENED` status.
   *
   * The created task is immediately broadcast via `tasks$` observable.
   *
   * If the command to create a task could not be handled, task creation gets undone.
   *
   * @param description description of a new task
   */
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

    this.optimisticallyChanged.push(createdTask);
    const tasksToBroadcast: TaskItem[] = [...this._tasks$.getValue(), createdTask];
    this._tasks$.next(tasksToBroadcast);
    this.spineWebClient.sendCommand(cmd, TaskService.doNothing,
      (err) => {
        return this.handleError(err, {
          taskId: id,
          undoOperation: () => {
            const undoneBasicTaskCreation = this.tasks.filter(task => task.getId() !== id);
            this._tasks$.next(undoneBasicTaskCreation);
          }
        });
      });
  }

  /**
   * Sets the status of the specified task to `OPEN` and updates the `tasks$` observable
   * with a list with the updated task
   *
   * @param taskId ID of the task to recover
   */
  private recoverTask(taskId) {
    const tasksWithDeleteUndone = this.tasks.map(task => {
      if (task.getId() === taskId) {
        task.setStatus(TaskStatus.OPEN);
      }
      return task;
    });
    this._tasks$.next(tasksWithDeleteUndone);
  }

  /**
   * Handles an error that has occurred as a result of a sent command.
   *
   * This method might delete tasks from the `optimisticallyUpdated` list, since some errors signify
   * that that the update has not happened, and therefore an optimistic update was wrong.
   *
   * @param err error that has occurred as a result of a sent command
   * @param taskOperation task operation which was the result of a failed command
   *
   * Visible for testing
   */
  handleError(err, taskOperation: TaskOperation): void {
    if (this.shouldUndoOptimisticOperation(err)) {
      this.undoOptimisticOperation(taskOperation);
      this.notificationService.showSnackbarWith('Could not handle your request due to a connection error.');
    }
  }

  private undoOptimisticOperation(taskOperation: TaskOperation) {
    const idToUndo = taskOperation.taskId;
    const taskToUndo = this.optimisticallyChanged.find(task => task.getId() === idToUndo);
    if (taskToUndo) {
      taskOperation.undoOperation(taskToUndo);
      this.optimisticallyChanged = this.optimisticallyChanged.filter(t => t.getId() !== idToUndo);
    }
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
          const intersection = taskItems.filter(value => this.optimisticallyChanged.includes(value));
          const toBroadcast = taskItems.filter(value => !intersection.includes(value));
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

  /**
   * Based on the error that has occurred during command handling, decides whether the optimistic
   * operation associated with the command should be undone.
   *
   * @param err error that has occurred during command handling.
   *
   * Visible for testing.
   */
  shouldUndoOptimisticOperation(err: CommandHandlingError) {
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

