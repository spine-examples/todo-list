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

import {async, fakeAsync, TestBed, tick} from '@angular/core/testing';
import {RouterTestingModule} from '@angular/router/testing';

import {Client, Type} from 'spine-web';

import {ActiveTasksComponent} from '../../../../src/app/task-list/active/active-tasks.component';
import {TaskService} from '../../../../src/app/task-service/task.service';
import {TaskItemComponent} from '../../../../src/app/task-list/task-item/task-item.component';
import {MyListView, TaskItem, TaskListView} from 'generated/main/js/todolist/q/projections_pb';
import {mockSpineWebClient, subscriptionDataOf} from '../../given/mock-spine-web-client';
import {
  HOUSE_TASK_1_DESC,
  HOUSE_TASK_1_ID,
  HOUSE_TASK_2_DESC,
  HOUSE_TASK_2_ID,
  houseTasks
} from '../../given/tasks';

describe('ActiveTasksComponent', () => {

  let component: ActiveTasksComponent;

  const unsubscribe = jasmine.createSpy();

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ActiveTasksComponent, TaskItemComponent],
      imports: [RouterTestingModule.withRoutes([])],
      providers: [TaskService, {provide: Client, useValue: mockSpineWebClient()}]
    })
      .compileComponents();

    mockSpineWebClient().subscribeToEntities.and.returnValue(subscriptionDataOf(
      [houseTasks()], [], [], unsubscribe
    ));
    const fixture = TestBed.createComponent(ActiveTasksComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should receive active task list on initialization', fakeAsync(() => {
    tick(); // Wait for the fake subscription fetch.
    expect(component.tasks[0].getId().getValue()).toBe(HOUSE_TASK_1_ID);
    expect(component.tasks[0].getDescription().getValue()).toBe(HOUSE_TASK_1_DESC);
    expect(component.tasks[1].getId().getValue()).toBe(HOUSE_TASK_2_ID);
    expect(component.tasks[1].getDescription().getValue()).toBe(HOUSE_TASK_2_DESC);
  }));

  it('should call unsubscribe method on destroy', fakeAsync(() => {
    tick(); // Wait for the fake subscription fetch.
    component.ngOnDestroy();
    expect(unsubscribe).toHaveBeenCalled();
  }));
});
