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

import {fakeAsync, TestBed} from '@angular/core/testing';
import {Client} from 'spine-web';

import {TaskService} from '../../../src/app/task-service/task.service';
import {mockSpineWebClient, subscriptionDataOf} from '../given/mock-spine-web-client';
import {
  HOUSE_TASK_1_DESC,
  HOUSE_TASK_1_ID,
  HOUSE_TASK_2_DESC,
  HOUSE_TASK_2_ID,
  houseTask,
  houseTasks
} from '../given/tasks';

import {CreateBasicTask} from 'generated/main/js/todolist/c/commands_pb';

describe('TaskService', () => {
  const mockClient = mockSpineWebClient();
  const unsubscribe = jasmine.createSpy();
  mockClient.subscribeToEntities.and.returnValue(subscriptionDataOf(
    [houseTasks()], [], [], unsubscribe
  ));
  let service: TaskService;
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [TaskService, {provide: Client, useValue: mockClient}]
    });
    service = TestBed.get(TaskService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

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

  it('should create basic task', () => {
    service.createBasicTask(HOUSE_TASK_1_DESC);
    expect(mockClient.sendCommand).toHaveBeenCalledWith(
      jasmine.any(CreateBasicTask), TaskService.logCmdAck, TaskService.logCmdErr
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
