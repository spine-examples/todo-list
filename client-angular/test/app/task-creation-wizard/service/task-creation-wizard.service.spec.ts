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

import {TaskCreationWizard} from '../../../../src/app/task-creation-wizard/service/task-creation-wizard.service';
import {TaskService} from '../../../../src/app/task-service/task.service';
import {mockSpineWebClient, subscriptionDataOf} from '../../given/mock-spine-web-client';
import {houseTask1, houseTasks} from '../../given/tasks';

import {Timestamp} from 'google-protobuf/google/protobuf/timestamp_pb';
import {TaskPriority} from 'generated/main/js/todolist/attributes_pb';
import {TaskCreationId, TaskId} from 'generated/main/js/todolist/identifiers_pb';
import {TaskCreation} from 'generated/main/js/todolist/model_pb';
import {
  AddLabels,
  CancelTaskCreation,
  CompleteTaskCreation,
  SkipLabels,
  StartTaskCreation,
  UpdateTaskDetails
} from 'generated/main/js/todolist/c/commands_pb';
import {TaskView} from 'generated/main/js/todolist/q/projections_pb';

describe('TaskCreationWizard', () => {
  const mockClient = mockSpineWebClient();
  const unsubscribe = jasmine.createSpy('unsubscribe');
  mockClient.subscribeToEntities.and.returnValue(subscriptionDataOf(
    [houseTasks()], [], [], unsubscribe
  ));

  let wizard: TaskCreationWizard;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        TaskCreationWizard,
        TaskService,
        {provide: Client, useValue: mockClient}
      ]
    });
    wizard = TestBed.get(TaskCreationWizard);
  });

  it('should be created', () => {
    expect(wizard).toBeTruthy();
  });

  it('should initialize the process from scratch', () => {
    mockClient.sendCommand.and.callFake((command, resolveCallback) => resolveCallback());
    wizard.init()
      .then(() => {
        const calls = mockClient.sendCommand.calls;
        expect(calls.count()).toEqual(1);
        const callArgs = calls.argsFor(0);
        const command = callArgs[0];
        expect(wizard.id).toEqual(command.getId());
        expect(wizard.taskId).toEqual(command.getTaskId());
        expect(wizard.stage).toEqual(TaskCreation.Stage.TASK_DEFINITION);
      })
      .catch(err =>
        fail(`'StartTaskCreation' command rejected: ${err}`)
      );
  });

  it('should restore existing creation process by ID', () => {
    const creationIdValue = 'task-creation-ID';
    const taskCreationId = new TaskCreationId();
    taskCreationId.setValue(creationIdValue);
    const stage = TaskCreation.Stage.LABEL_ASSIGNMENT;
    const task = houseTask1();
    task.setPriority(TaskPriority.HIGH);
    const dueDate = Timestamp.fromDate(new Date());
    task.setDueDate(dueDate);

    const provideMockData = (type, id, resolveCallback) => {
      if (type.class() === TaskCreation) {
        const taskCreation = new TaskCreation();
        taskCreation.setId(taskCreationId);
        taskCreation.setStage(stage);
        taskCreation.setTaskId(task.getId());
        resolveCallback(taskCreation);
      } else if (type.class() === TaskView) {
        resolveCallback(task);
      }
    };
    mockClient.fetchById.and.callFake(provideMockData);
    wizard.init(creationIdValue)
      .then(() => {
        expect(wizard.id).toEqual(taskCreationId);
        expect(wizard.taskId).toEqual(task.getId());
        expect(wizard.stage).toEqual(stage);
        expect(wizard.taskDescription).toEqual(task.getDescription());
        expect(wizard.taskPriority).toEqual(task.getPriority());
        expect(wizard.taskDueDate).toEqual(task.getDueDate());
        expect(wizard.taskLabels).toEqual([]);
      })
      .catch(err =>
        fail(`Task restoration failed: ${err}`)
      );
  });

  it('should produce an error if nothing is found by the specified ID', () => {
    mockClient.fetchById.and.callFake((type, id, resolve) => resolve(null));
    const theId = 'some-ID';
    wizard.init(theId)
      .then(() => {
        fail('Task restoration should have been rejected');
      })
      .catch(err =>
        expect(err).toEqual(`No task creation process found for ID: ${theId}`)
      );
  });

  it('should propagate `init` errors as Promise rejection', () => {
    const errorMessage = 'Could not start the task creation process';
    mockClient.sendCommand.and.callFake((cmd, resolve, reject) => reject(errorMessage));
    wizard.init()
      .then(() => {
        fail('Task restoration should have been rejected');
      })
      .catch(err =>
        expect(err).toEqual(errorMessage)
      );
  });
});
