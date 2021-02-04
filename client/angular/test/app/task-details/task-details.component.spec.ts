/*
 * Copyright 2021, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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

import {Component} from '@angular/core';
import {async, TestBed} from '@angular/core/testing';
import {ActivatedRoute, convertToParamMap} from '@angular/router';
import {RouterTestingModule} from '@angular/router/testing';
import {TaskView} from 'proto/todolist/views_pb';

import {TaskDetailsComponent} from 'app/task-list/task-item/task-details/task-details.component';
import {LayoutService} from 'app/layout/layout.service';
import {mockLayoutService} from 'test/given/layout-service';
import {taskWithId} from 'test/given/tasks';
import {TodoListPipesModule} from 'app/pipes/todo-list-pipes.module';

const expectedTaskId = 'taskId';
const expectedTask: TaskView = taskWithId(expectedTaskId);

describe('TaskDetailsComponent', () => {
  let hostFixture;
  let childComponent: TaskDetailsComponent;
  const ID = 'test-task-ID';
  const activatedRoute = {snapshot: {paramMap: convertToParamMap({id: ID})}};

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [TestHostComponent, TaskDetailsComponent],
      imports: [RouterTestingModule.withRoutes([]), TodoListPipesModule],
      providers: [
        {provide: ActivatedRoute, useValue: activatedRoute},
        {provide: LayoutService, useValue: mockLayoutService()}
      ]
    })
           .compileComponents();
  }));

  beforeEach(() => {
    hostFixture = TestBed.createComponent(TestHostComponent);
    childComponent = hostFixture.debugElement.children[0].componentInstance;
    hostFixture.detectChanges();
  });

  it('should create', () => {
    expect(childComponent).toBeTruthy();
  });
});

@Component({
  selector: 'app-test-host-component',
  template: `
      <app-task-details [(task)]="task"></app-task-details>`
})
class TestHostComponent {

  private readonly task: TaskView = expectedTask;
}
