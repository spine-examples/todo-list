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

import {async, TestBed} from '@angular/core/testing';
import {RouterTestingModule} from '@angular/router/testing';
import {Client} from 'spine-web';
import {MatExpansionModule} from '@angular/material/expansion';
import {Component} from '@angular/core';

import {TaskItemComponent} from 'app/task-list/task-item/task-item.component';
import {TaskService} from 'app/task-service/task.service';
import {mockSpineWebClient, subscriptionDataOf} from 'test/given/mock-spine-web-client';
import {TodoListPipesModule} from 'app/pipes/todo-list-pipes.module';
import {TaskDetailsComponent} from 'app/task-list/task-item/task-details/task-details.component';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {taskWithId} from 'test/given/tasks';
import {TaskItem} from 'proto/todolist/q/projections_pb';
import {LayoutService} from 'app/layout/layout.service';
import {NotificationService} from "app/layout/notification.service";
import {LayoutModule} from "app/layout/layout.module";

const expectedTask = taskWithId('some id');

describe('TaskItemComponent', () => {

  const mockClient = mockSpineWebClient();
  let hostFixture;
  let childComponent: TaskDetailsComponent;

  mockClient.subscribeToEntities.and.returnValue(subscriptionDataOf(
    [], [], [], jasmine.createSpy()
  ));

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [TestHostComponent, TaskItemComponent, TaskDetailsComponent],
      imports: [
        RouterTestingModule.withRoutes([]),
        MatExpansionModule, TodoListPipesModule,
        BrowserAnimationsModule, LayoutModule],
      providers: [TaskService, {provide: Client, useValue: mockClient}, LayoutService, NotificationService]
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
  selector: `app-test-host-component`,
  template: `<app-task-item [task]="task"></app-task-item>`
})
class TestHostComponent {

  private task: TaskItem = expectedTask;
}

