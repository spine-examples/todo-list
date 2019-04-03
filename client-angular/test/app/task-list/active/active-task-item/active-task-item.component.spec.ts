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

import {Component, ViewChild} from '@angular/core';
import {async, ComponentFixture, TestBed} from '@angular/core/testing';
import {RouterTestingModule} from '@angular/router/testing';
import {MatIconModule} from '@angular/material/icon';
import {By} from '@angular/platform-browser';

import {ActiveTaskItemComponent} from 'app/task-list/active/active-task-item/active-task-item.component';
import {HOUSE_TASK_1_DESC, HOUSE_TASK_1_ID, taskItem} from 'test/given/tasks';

import {TaskItem} from 'proto/todolist/q/projections_pb';
import {TaskService} from 'app/task-service/task.service';
import {mockSpineWebClient, subscriptionDataOf} from 'test/given/mock-spine-web-client';
import {Client} from 'spine-web';
import {TaskItemComponent} from 'app/task-list/task-item/task-item.component';

describe('ActiveTaskItemComponent', () => {
  const theTaskItem = taskItem(HOUSE_TASK_1_ID, HOUSE_TASK_1_DESC);

  const mockClient = mockSpineWebClient();
  let component: ActiveTaskItemComponent;
  let fixture: ComponentFixture<ActiveTaskItemComponent>;

  mockClient.subscribeToEntities.and.returnValue(subscriptionDataOf(
    [], [], [], jasmine.createSpy('unsubscribe')
  ));

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ActiveTaskItemComponent, TestHostComponent, TaskItemComponent],
      imports: [RouterTestingModule.withRoutes([]), MatIconModule],
      providers: [TaskService, {provide: Client, useValue: mockClient}]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ActiveTaskItemComponent);
    component = fixture.componentInstance;
    component.task = theTaskItem;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should have a link to task details', () => {
    const element = fixture.nativeElement.querySelector('a');
    expect(element.href).toContain(`details/${HOUSE_TASK_1_ID}`);
    expect(element.textContent).toContain(HOUSE_TASK_1_DESC);
  });

  it('should allow to complete task', () => {
    const completeTaskMethod = spyOn(component.taskService, 'completeTask');
    const completeButton = fixture.debugElement.query(By.css('.complete-task-button')).nativeElement;
    completeButton.click();
    expect(completeTaskMethod).toHaveBeenCalledTimes(1);
  });

  it('should allow to delete task', () => {
    const completeTaskMethod = spyOn(component.taskService, 'deleteTask');
    const deleteButton = fixture.debugElement.query(By.css('.delete-task-button')).nativeElement;
    deleteButton.click();
    expect(completeTaskMethod).toHaveBeenCalledTimes(1);
  });

  it('should receive task item injected by the host component', () => {
    const testHostFixture = TestBed.createComponent(TestHostComponent);
    testHostFixture.detectChanges();
    const testHostComponent = testHostFixture.componentInstance;
    expect(testHostComponent.childTask).toBe(theTaskItem);
  });

  @Component({
    selector: `host-component`,
    template: `
      <app-active-task-item [task]="task"></app-active-task-item>`
  })
  class TestHostComponent {

    @ViewChild(ActiveTaskItemComponent)
    private taskItemComponent: ActiveTaskItemComponent;

    private readonly task: TaskItem = theTaskItem;

    get childTask() {
      return this.taskItemComponent.task;
    }
  }
});
