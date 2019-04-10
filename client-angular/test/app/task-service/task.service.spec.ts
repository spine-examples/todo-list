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

import {fakeAsync, TestBed, tick} from '@angular/core/testing';
import {Client, Message} from 'spine-web';

import {TaskService} from 'app/task-service/task.service';
import {mockSpineWebClient, observableSubscriptionDataOf} from 'test/given/mock-spine-web-client';
import {
  HOUSE_TASK_1_DESC,
  HOUSE_TASK_1_ID,
  HOUSE_TASK_2_DESC,
  HOUSE_TASK_2_ID,
  houseTask,
  houseTasks
} from 'test/given/tasks';
import {BehaviorSubject} from 'rxjs';
import {TaskId, TaskItem, TaskStatus} from 'proto/todolist/q/projections_pb';
import {CreateBasicTask} from 'proto/todolist/c/commands_pb';
import {NotificationServiceModule} from 'app/notification-service/notification-service.module';
import {mockNotificationService} from 'test/given/layout-service';
import {NotificationService} from 'app/notification-service/notification.service';

describe('TaskService', () => {
  const mockClient = mockSpineWebClient();
  const unsubscribe = jasmine.createSpy();
  const notificationService = mockNotificationService();

  const addedTasksSubject = new BehaviorSubject<TaskItem[]>(houseTasks());

  function makeCommandFail() {
    mockClient.sendCommand.and.callFake((cmd: Message, onSuccess: () => void, onError: (err) => void) => {
      const error = {
        assuresCommandNeglected: () => true
      };
      onError(error);
      tick();
    });
  }

  mockClient.subscribeToEntities.and.returnValue(observableSubscriptionDataOf(
    addedTasksSubject.asObservable(), unsubscribe
  ));
  let service: TaskService;
  beforeEach(fakeAsync(() => {
    TestBed.configureTestingModule({
      imports: [NotificationServiceModule],
      providers: [
        TaskService, {provide: Client, useValue: mockClient},
        NotificationService, {provide: NotificationService, useValue: notificationService}
      ]
    });
    service = TestBed.get(TaskService);
    service.assureTasksInitialized();
    tick();
  }));

  afterEach(() => {
    addedTasksSubject.next(houseTasks());
    mockClient.sendCommand.and.callThrough();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should update the task list without relying on server response', fakeAsync(() => {
    const expectedDescription = 'some task';
    service.createBasicTask(expectedDescription);
    const taskDescriptions = service.tasks.map(task => task.getDescription().getValue());
    expect(taskDescriptions).toContain(expectedDescription);
  }));

  it('should optimistically broadcast added tasks', () => {
    const idToComplete = service.tasks[0].getId();
    service.completeTask(idToComplete);
    const tasks: TaskItem[] = service.tasks;
    const firstHouseTask = tasks.find(task => task.getId() === idToComplete);
    expect(firstHouseTask).toBeTruthy();
    expect(firstHouseTask.getStatus()).toBe(TaskStatus.COMPLETED);
  });

  it('should roll optimistic completions back if the command handling fails', fakeAsync(() => {
    const idToComplete = service.tasks[0].getId();
    makeCommandFail();
    service.completeTask(idToComplete);
    tick();
    const noTasksAreCompleted = service.tasks.every(value => value.getStatus() === TaskStatus.OPEN);
    expect(noTasksAreCompleted).toBe(true);
  }));

  it('should update task list with a deleted task without waiting for the server response', () => {
    const idToDelete = service.tasks[0].getId();
    service.deleteTask(idToDelete);
    const tasks: TaskItem[] = service.tasks;
    const firstHouseTask = tasks.find(task => task.getId() === idToDelete);
    expect(firstHouseTask).toBeTruthy();
    expect(firstHouseTask.getStatus()).toBe(TaskStatus.DELETED);
  });

  it('should rollback deleted tasks if the deletion command fails', fakeAsync(() => {
    const idToDelete = service.tasks[0].getId();
    makeCommandFail();
    service.deleteTask(idToDelete);
    tick();
    const noneAreDeleted = service.tasks.every(value => value.getStatus() === TaskStatus.OPEN);
    expect(noneAreDeleted).toBe(true);
  }));

  it('should log command acknowledgement', () => {
    console.log = jasmine.createSpy('log');
    TaskService.logCmdAck();
    expect(console.log).toHaveBeenCalledWith('Command acknowledged by the server');
  });

  it('should log command error', () => {
    console.log = jasmine.createSpy('log');
    const errorMessage = 'Failed to process command';
    TaskService.logCmdErr(errorMessage);
    expect(console.log).toHaveBeenCalledWith(
      'Error when sending command to the server: %s', errorMessage
    );
  });

  it('should fetch an expected list of tasks', () => {
    service.tasks$.toPromise().then(fetchedTasks => {
      expect(fetchedTasks.length).toBe(2);
      expect(fetchedTasks[0].getId().getValue()).toBe(HOUSE_TASK_1_ID);
      expect(fetchedTasks[0].getDescription().getValue()).toBe(HOUSE_TASK_1_DESC);
      expect(fetchedTasks[1].getId().getValue()).toBe(HOUSE_TASK_2_ID);
      expect(fetchedTasks[1].getDescription().getValue()).toBe(HOUSE_TASK_2_DESC);
    });
  });

  it('should fetch a single task view by ID', fakeAsync(() => {
    const theTask = houseTask();
    mockClient.fetchById.and.callFake((cls, id, resolve) => resolve(theTask));
    service.fetchById(theTask.getId())
      .then(taskView => expect(taskView).toEqual(theTask))
      .catch(err =>
        fail(`Task details should have been resolved, actually rejected with an error: ${err}`)
      );
  }));

  it('should propagate errors from Spine Web Client on `fetchById`', fakeAsync(() => {
    const errorMessage = 'Task details lookup rejected';
    mockClient.fetchById.and.callFake((cls, id, resolve, reject) => reject(errorMessage));

    service.fetchById(houseTask().getId())
      .then(() => fail('Task details lookup should have been rejected'))
      .catch(err => expect(err).toEqual(errorMessage));
  }));

  it('should produce an error when no matching task is found during lookup', fakeAsync(() => {
    mockClient.fetchById.and.callFake((cls, id, resolve) => resolve(null));
    const taskId = houseTask().getId();

    service.fetchById(taskId)
      .then(() => fail('Task details lookup should have been rejected'))
      .catch(err => expect(err).toEqual(`No task view found for ID: ${taskId}`));
  }));
});
