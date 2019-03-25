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
import {houseTasks} from '../../given/tasks';

import {TaskCreationId} from 'generated/main/js/todolist/identifiers_pb';
import {TaskCreation} from 'generated/main/js/todolist/model_pb';
import {
  AddLabels,
  CancelTaskCreation,
  CompleteTaskCreation,
  SkipLabels,
  StartTaskCreation,
  UpdateTaskDetails
} from 'generated/main/js/todolist/c/commands_pb';

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
    const id = new TaskCreationId();
    id.setValue('task-creation-ID');
    wizard.init(id)
      .then()
      .catch();
  });

  it('should propagate `init` errors as Promise rejection', () => {

  });
});
