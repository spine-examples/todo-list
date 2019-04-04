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

import {Component} from '@angular/core';
import {async, TestBed, fakeAsync, tick} from '@angular/core/testing';
import {ActivatedRoute, convertToParamMap} from '@angular/router';
import {RouterTestingModule} from '@angular/router/testing';
import {TaskId, TaskItem} from 'proto/todolist/q/projections_pb';

import {TaskDetailsComponent} from 'app/task-list/task-item/task-details/task-details.component';
import {LayoutService} from 'app/layout/layout.service';
import {mockLayoutService} from 'test/given/layout-service';
import {taskWithId} from 'test/given/tasks';

const expectedTaskId = 'taskId';
const expectedTask: TaskItem = taskWithId(expectedTaskId);

describe('TaskDetailsComponent', () => {
  let component: TaskDetailsComponent;
  const ID = 'test-task-ID';
  const activatedRoute = {snapshot: {paramMap: convertToParamMap({id: ID})}};
  let fixture;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [TaskDetailsComponent],
      imports: [RouterTestingModule.withRoutes([])],
      providers: [
        {provide: ActivatedRoute, useValue: activatedRoute},
        {provide: LayoutService, useValue: mockLayoutService()}
      ]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TaskDetailsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

});
