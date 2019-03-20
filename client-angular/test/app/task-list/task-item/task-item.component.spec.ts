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

import {TaskItemComponent} from '../../../../src/app/task-list/task-item/task-item.component';
import {HOUSE_TASK_1_DESC, HOUSE_TASK_1_ID, task} from '../../given/tasks';

import {TaskItem} from 'generated/main/js/todolist/q/projections_pb';
import {TaskService} from '../../../../src/app/task-service/task.service';
import {mockSpineWebClient} from '../../given/mock-spine-web-client';
import {Client} from 'spine-web';

describe('TaskItemComponent', () => {
  const taskItem = task(HOUSE_TASK_1_ID, HOUSE_TASK_1_DESC);

  const mockClient = mockSpineWebClient();
  let component: TaskItemComponent;
  let fixture: ComponentFixture<TaskItemComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [TaskItemComponent, TestHostComponent],
      imports: [RouterTestingModule.withRoutes([]), MatIconModule],
      providers: [TaskService, {provide: Client, useValue: mockClient}]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TaskItemComponent);
    component = fixture.componentInstance;
    component.task = taskItem;
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
    component.completeTask();
    expect(completeTaskMethod).toHaveBeenCalledWith(HOUSE_TASK_1_ID);
  });

  it('should allow to delete task', () => {
    const completeTaskMethod = spyOn(component.taskService, 'deleteTask');
    component.deleteTask();
    expect(completeTaskMethod).toHaveBeenCalledWith(HOUSE_TASK_1_ID);
  });

  it('should receive task item injected by the host component', () => {
    const testHostFixture = TestBed.createComponent(TestHostComponent);
    testHostFixture.detectChanges();
    const testHostComponent = testHostFixture.componentInstance;
    expect(testHostComponent.childTask).toBe(taskItem);
  });

  @Component({
    selector: `host-component`,
    template: `
      <app-task-item [task]="task"></app-task-item>`
  })
  class TestHostComponent {

    @ViewChild(TaskItemComponent)
    private taskItemComponent: TaskItemComponent;

    private readonly task: TaskItem = taskItem;

    get childTask() {
      return this.taskItemComponent.task;
    }
  }
});
