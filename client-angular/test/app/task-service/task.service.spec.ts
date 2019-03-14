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

import {TestBed} from '@angular/core/testing';
import {Client} from 'spine-web';

import {TaskService} from '../../../src/app/task-service/task.service';
import {mockSpineWebClient, subscriptionDataOf} from '../given/mock-spine-web-client';
import {
  HOUSE_TASK_1_DESC,
  HOUSE_TASK_1_ID,
  HOUSE_TASK_2_DESC,
  HOUSE_TASK_2_ID,
  houseTasks
} from '../given/tasks';

import {CreateBasicTask} from 'generated/main/js/todolist/c/commands_pb';
import {MyListView} from 'generated/main/js/todolist/q/projections_pb';


describe('TaskService', () => {
  let service: TaskService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [TaskService, {provide: Client, useValue: mockSpineWebClient()}]
    });
    service = TestBed.get(TaskService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should create basic task', () => {
    service.createBasicTask(HOUSE_TASK_1_DESC);
    expect(mockSpineWebClient().sendCommand).toHaveBeenCalledWith(
      jasmine.any(CreateBasicTask), TaskService.logCmdAck, TaskService.logCmdErr
    );
  });

  it('should subscribe to active tasks', () => {
    const unsubscribe = () => {
    };
    mockSpineWebClient().subscribeToEntities.and.returnValue(subscriptionDataOf(
      [houseTasks()], [], [], unsubscribe
    ));
    const taskItems = [];
    service.subscribeToActive(taskItems)
      .then(receivedUnsubscribeFunc => {
          expect(receivedUnsubscribeFunc).toBe(unsubscribe);
          expect(taskItems.length).toBe(2);
          expect(taskItems[0].getId().getValue()).toBe(HOUSE_TASK_1_ID);
          expect(taskItems[0].getDescription().getValue()).toBe(HOUSE_TASK_1_DESC);
          expect(taskItems[1].getId().getValue()).toBe(HOUSE_TASK_2_ID);
          expect(taskItems[1].getDescription().getValue()).toBe(HOUSE_TASK_2_DESC);
        }
      ).catch(() => fail('Subscription promise should have been resolved'));
  });

  it('should log and then rethrow error if subscription fails', () => {
    const errorMessage = 'Subscription failed';
    mockSpineWebClient().subscribeToEntities.and.returnValue(Promise.reject(errorMessage));
    console.log = jasmine.createSpy('log');
    const taskItems = [];
    service.subscribeToActive(taskItems)
      .then(() => fail('Subscription promise should have failed'))
      .catch(err => {
        expect(console.log).toHaveBeenCalledWith(
          'Cannot subscribe to entities of type (`%s`): %s', MyListView.typeUrl(), errorMessage
        );
        expect(err).toBe(errorMessage);
      });
  });
});
