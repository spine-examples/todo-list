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

import {async, ComponentFixture, TestBed} from '@angular/core/testing';
import {RouterTestingModule} from '@angular/router/testing';
import {Client} from 'spine-web';

import {TaskLinkComponent} from '../../../../src/app/task-list/task-link/task-link.component';
import {TaskService} from '../../../../src/app/task-service/task.service';
import {mockSpineWebClient, subscriptionDataOf} from '../../given/mock-spine-web-client';
import {HOUSE_TASK_1_DESC, HOUSE_TASK_1_ID, houseTasks, task} from '../../given/tasks';

describe('TaskLinkComponent', () => {

  const mockClient = mockSpineWebClient();
  let component: TaskLinkComponent;
  let fixture: ComponentFixture<TaskLinkComponent>;

  mockClient.subscribeToEntities.and.returnValue(subscriptionDataOf(
    [], [], [], jasmine.createSpy()
  ));

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [TaskLinkComponent],
      imports: [RouterTestingModule.withRoutes([])],
      providers: [TaskService, {provide: Client, useValue: mockClient}]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    const taskItem = task(HOUSE_TASK_1_ID, HOUSE_TASK_1_DESC);
    fixture = TestBed.createComponent(TaskLinkComponent);
    component = fixture.componentInstance;
    component.task = taskItem;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
