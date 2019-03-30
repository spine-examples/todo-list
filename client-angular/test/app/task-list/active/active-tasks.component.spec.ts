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

import {ComponentFixture, fakeAsync, TestBed, tick} from '@angular/core/testing';
import {RouterTestingModule} from '@angular/router/testing';
import {ReactiveFormsModule} from '@angular/forms';
import {MatInputModule} from '@angular/material';
import {MatListModule} from '@angular/material/list';
import {MatIconModule} from '@angular/material/icon';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {By} from '@angular/platform-browser';
import {Client, Type} from 'spine-web';

import {ActiveTasksComponent} from 'app/task-list/active/active-tasks.component';
import {TaskService} from 'app/task-service/task.service';
import {ActiveTaskItemComponent} from 'app/task-list/active/active-task-item/active-task-item.component';
import {mockSpineWebClient, subscriptionDataOf} from 'test/given/mock-spine-web-client';
import {
  HOUSE_TASK_1_DESC,
  HOUSE_TASK_1_ID,
  HOUSE_TASK_2_DESC,
  HOUSE_TASK_2_ID,
  houseTasks
} from 'test/given/tasks';

import {MyListView, TaskItem, TaskListView} from 'proto/todolist/q/projections_pb';
import {TaskLinkComponent} from 'app/task-list/task-link/task-link.component';

describe('ActiveTasksComponent', () => {
  const mockClient = mockSpineWebClient();
  const unsubscribe = jasmine.createSpy('unsubscribe');

  let component: ActiveTasksComponent;
  let fixture: ComponentFixture<ActiveTasksComponent>;

  mockClient.subscribeToEntities.and.returnValue(subscriptionDataOf(
    [houseTasks()], [], [], unsubscribe
  ));

  beforeEach(fakeAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ActiveTasksComponent, ActiveTaskItemComponent, TaskLinkComponent],
      imports: [
        RouterTestingModule.withRoutes([]),
        ReactiveFormsModule,
        MatInputModule,
        MatListModule,
        MatIconModule,
        BrowserAnimationsModule
      ],
      providers: [TaskService, {provide: Client, useValue: mockClient}]
    })
      .compileComponents();

    fixture = TestBed.createComponent(ActiveTasksComponent);
    component = fixture.componentInstance;
    tick(); // Wait for the fake subscription fetch.
    fixture.detectChanges();
  }));

  it('should allow basic task creation', () => {
    const method = spyOn<any>(component, 'createBasicTask');
    const input = fixture.debugElement.query(By.css('.task-description-input')).nativeElement;
    input.value = 'Some basic task text';
    const keyPressed = new KeyboardEvent('keydown', {
      key: 'Enter'
    });
    input.dispatchEvent(keyPressed);
    expect(method).toHaveBeenCalledTimes(1);
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should receive active task list on initialization', () => {
    expect(component.tasks[0].getId().getValue()).toBe(HOUSE_TASK_1_ID);
    expect(component.tasks[0].getDescription().getValue()).toBe(HOUSE_TASK_1_DESC);
    expect(component.tasks[1].getId().getValue()).toBe(HOUSE_TASK_2_ID);
    expect(component.tasks[1].getDescription().getValue()).toBe(HOUSE_TASK_2_DESC);
  });

  it('should create `app-active-task-item` for each of the received tasks', () => {
    fixture.detectChanges();
    const elements = fixture.nativeElement.getElementsByTagName('app-active-task-item');

    expect(elements.length).toBe(2);
    expect(elements[0].textContent).toContain(HOUSE_TASK_1_DESC);
    expect(elements[1].textContent).toContain(HOUSE_TASK_2_DESC);
  });
});
