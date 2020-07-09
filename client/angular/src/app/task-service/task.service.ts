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

import {Inject, Injectable, OnDestroy} from '@angular/core';
import {Client} from 'spine-web';
import {BehaviorSubject, Observable} from 'rxjs';

import {TaskServiceModule} from 'app/task-service/task-service.module';
import {UuidGenerator} from 'app/uuid-generator/uuid-generator';

import {TaskId} from 'proto/todolist/identifiers_pb';
import {TaskDescription} from 'proto/todolist/values_pb';
import {CompleteTask, CreateBasicTask, DeleteTask} from 'proto/todolist/commands_pb';
import {TaskView} from 'proto/todolist/views_pb';
import {TaskStatus} from 'proto/todolist/attributes_pb';
import {NotificationService} from 'app/layout/notification.service';

/**
 * A state of the task before the change.
 *
 * If `previousState` is not set, the task hasn't existed prior to change.
 *
 * Is used to restore an optimistically-executed operation, such as adding a task, to rollback to
 * the state before the operation.
 */
interface TaskState {
  taskId: TaskId;
  previousState?: TaskView;
}

/**
 * A service which performs operations on To-Do List tasks.
 */
@Injectable({
  providedIn: TaskServiceModule,
})
export class TaskService implements OnDestroy {

  /**
   * @param spineWebClient a client for accessing Spine backend
   * @param notificationService a service that notifies users about events that happen in
   * the application
   */
  constructor(@Inject(Client) private spineWebClient: Client, private notificationService: NotificationService) {
  }

  /**
   * Obtains an `Observable` of tasks all tasks in the list.
   */
  get tasks$(): Observable<TaskView[]> {
    this.assureTasksInitialized();
    return this._tasks$.asObservable();
  }

  /**
   * Obtains the current value of the task list.
   */
  get tasks(): TaskView[] {
    this.assureTasksInitialized();
    return this._tasks$.getValue();
  }

  private _tasks$: BehaviorSubject<TaskView[]>;
  private _unsubscribe: () => void;
  private optimisticallyChanged: TaskState[] = [];

  /** Obtains a new task item with all the fields taken from the specified one. */
  private static copy(task: TaskView): TaskView {
    const result = new TaskView();
    result.setId(task.getId());
    result.setDescription(task.getDescription());
    result.setStatus(task.getStatus());
    result.setPriority(task.getPriority());
    return result;
  }

  /**
   * Makes sure that array of tasks is initialized.
   *
   * Visible for testing.
   */
  private assureTasksInitialized(): void {
    if (!this._tasks$) {
      this._tasks$ = new BehaviorSubject<TaskView[]>([]);
      this.fetchAllTasks();
      this.subscribeToTaskUpdates().then((unsubscribeFn) => this._unsubscribe = unsubscribeFn);
    }
  }

  /**
   * Loads all currently existing tasks and reflects them to the corresponding array in this
   * instance of the task service.
   */
  fetchAllTasks(): void {
    this.fetchAll().then(tasks => this._tasks$.next(tasks));
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
    const toBroadcast: TaskView[] = this.tasks.map(task => {
      if (task.getId() === taskId) {
        const changedTask: TaskView = TaskService.copy(task);
        task.setStatus(TaskStatus.DELETED);
        this.optimisticallyChanged.push({
          taskId,
          previousState: changedTask
        });
      }
      return task;
    }, this);
    this._tasks$.next(toBroadcast);
    this.sendTaskCommand(cmd, taskId);
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
        const changedTask = TaskService.copy(task);
        task.setStatus(TaskStatus.COMPLETED);
        this.optimisticallyChanged.push({
          taskId,
          previousState: changedTask
        });
      }
      return task;
    }, this);
    this._tasks$.next(toBroadcast);
    this.sendTaskCommand(cmd, taskId);
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

    const createdTask = new TaskView();
    createdTask.setId(id);
    createdTask.setDescription(taskDescription);
    createdTask.setStatus(TaskStatus.OPEN);

    this.optimisticallyChanged.push({
      taskId: id
    });
    const tasksToBroadcast: TaskView[] = [...this._tasks$.getValue(), createdTask];
    this._tasks$.next(tasksToBroadcast);
    this.sendTaskCommand(cmd, id);
  }

  /**
   * Sends the specified task-related command, which is related to the task with the specified ID.
   *
   * If the handling of the command fails, the changes implied by the given command
   * may be undone (see `recoverPreviousState docs`).
   *
   * @param cmd command to send
   * @param taskId ID of the task that is related to the sent command
   */
  private sendTaskCommand(cmd, taskId): void {
    const onSuccess = () => this.removeFromOptimisticallyChanged(taskId);
    const onError = err => this.recoverPreviousState(err, taskId);
    this.spineWebClient.command(cmd)
                       .onOk(onSuccess)
                       .onError(onError)
                       .onRejection(onError)
                       .post();
  }

  /** Updates the `optimisticallyChanged` list by removing the state with the specified ID. */
  private removeFromOptimisticallyChanged(taskId: TaskId): void {
    this.optimisticallyChanged = this.optimisticallyChanged.filter(t => t.taskId !== taskId);
  }

  /**
   * Handles an error that has occurred as a result of a sent command.
   *
   * This method might delete tasks from the `optimisticallyUpdated` list, since some errors signify
   * that that the update has not happened, and therefore an optimistic update was wrong.
   *
   * @param err error that has occurred as a result of a sent command
   * @param taskId ID of the task related to the operation that needs to be undone
   *
   * Visible for testing
   */
  recoverPreviousState(err, taskId: TaskId): void {
    this.undoOptimisticOperation(taskId);
    this.notificationService.showSnackbarWith('Could not handle your request due to a connection error.');
    this.removeFromOptimisticallyChanged(taskId);
  }

  /**
   * Finds the task with the specified ID in the list of optimistically changed tasks, updates it
   * to its previous state (as per `TaskState` interface).
   *
   * Then broadcasts the updated task list via `tasks$`.
   *
   * @param taskToUndo ID of the task to rollback to the previous state
   */
  private undoOptimisticOperation(taskToUndo: TaskId): void {
    if (taskToUndo) {
      const lastBroadcastTasks = this.tasks;
      const toBroadcast: TaskView[] = [];
      const toUndo: TaskState = this.optimisticallyChanged.find(value => value.taskId === taskToUndo);
      lastBroadcastTasks.forEach(task => {
        if (task.getId() === toUndo.taskId) {
          if (toUndo.previousState) {
            toBroadcast.push(toUndo.previousState);
          }
        } else {
          toBroadcast.push(task);
        }
      }, this);
      this._tasks$.next(toBroadcast);
    }
  }

  /**
   * Fetches the details of all existing tasks.
   */
  fetchAll(): Promise<TaskView[]> {
    return this.spineWebClient.select(TaskView).run();
  }

  /**
   * Fetches a single task details.
   *
   * If nothing is found by the specified ID, the promise is rejected.
   */
  fetchById(id: TaskId): Promise<TaskView> {
    return new Promise<TaskView>((resolve, reject) => {
      const dataCallback = tasks => {
        if (tasks.length < 1) {
          reject(`No task view found for ID: ${id}`);
        } else {
          resolve(tasks[0]);
        }
      };
      this.spineWebClient.select(TaskView)
                         .byId(id)
                         .run()
                         .then(tasks => dataCallback(tasks))
                         .catch(err => reject(err));
    });
  }

  /**
   * Subscribes to the task updates and reflects them to the array stored in this instance of the
   * task service.
   *
   * Active tasks are those which are not in draft state, completed, or deleted.
   *
   * The tasks are retrieved are `TaskView` projections.
   *
   * Subscription can be cancelled via the method return value, which is a `Promise` resolving to
   * the `unsubscribe` function.
   *
   * @returns a `Promise` which resolves to an `unsubscribe` function
   */
  private subscribeToTaskUpdates(): Promise<() => void> {
    return new Promise((resolve, reject) =>
        this.spineWebClient
            .subscribeTo(TaskView)
            .post()
            .then((subscriptionObject) => {
              subscriptionObject.itemAdded.subscribe(this.taskAdded());
              subscriptionObject.itemChanged.subscribe(this.taskChanged());
              subscriptionObject.itemRemoved.subscribe(this.taskRemoved());
              resolve(subscriptionObject.unsubscribe);
            })
            .catch(err => {
              console.log(
                  'Cannot subscribe to entities of type (`%s`): %s',
                  (TaskView).typeUrl(), err
              );
              reject(err);
            })
    );
  }

  /**
   * Returns a processor of the `itemAdded` Spine web client callback for tasks.
   *
   * Adds a new task to the task list or does nothing if the task with same ID is already present.
   */
  private taskAdded(): (taskView) => void {
    return taskView => {
      if (!taskView) {
        return;
      }
      const index = this.findIndex(taskView);
      const alreadyBroadcast = index > -1;
      if (!alreadyBroadcast) {
        const presentItems: TaskView[] = this.tasks.slice();
        presentItems.push(taskView);
        this._tasks$.next(presentItems);
      }
    };
  }

  /**
   * Returns a processor of the `itemChanged` Spine web client callback for tasks.
   *
   * Either updates an existing task in the list or, if it's not present, adds a new one.
   */
  private taskChanged(): (taskView) => void {
    return taskView => {
      if (!taskView) {
        return;
      }

      const index = this.tasks.findIndex(t => t.getId().getUuid() === taskView.getId().getUuid());
      const presentItems: TaskView[] = this.tasks.slice();
      if (index > -1) {
        presentItems[index] = taskView;
      } else {
        presentItems.push(taskView);
      }
      this._tasks$.next(presentItems);
    };
  }

  /**
   * Returns a processor of the `itemRemoved` Spine web client callback for tasks.
   *
   * Removes a task from the task list or does nothing if the given task is not present in the list.
   */
  private taskRemoved(): (taskView) => void {
    return taskView => {
      if (!taskView) {
        return;
      }
      const index = this.tasks.findIndex(taskView);
      const presentItems: TaskView[] = this.tasks.slice();
      if (index > -1) {
        presentItems.splice(index, 1);
      }
      this._tasks$.next(presentItems);
    };
  }

  private findIndex(taskView: TaskView): number {
    const matchesById = task => task.getId().getUuid() === taskView.getId().getUuid();
    return this.tasks.findIndex(matchesById);
  }

  ngOnDestroy(): void {
    if (this._unsubscribe) {
      this._unsubscribe();
    }
  }
}
