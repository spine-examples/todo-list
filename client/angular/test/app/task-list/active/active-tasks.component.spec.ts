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
import { MatInputModule } from '@angular/material/input';
import {MatListModule} from '@angular/material/list';
import {MatIconModule} from '@angular/material/icon';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {By} from '@angular/platform-browser';
import {Client, Message} from 'spine-web';
import {BehaviorSubject} from 'rxjs';

import {ActiveTasksComponent} from 'app/task-list/active/active-tasks.component';
import {TaskService} from 'app/task-service/task.service';
import {mockSpineWebClient, observableSubscriptionDataOf} from 'test/given/mock-spine-web-client';
import {chore} from 'test/given/tasks';

import {TaskView} from 'proto/todolist/views_pb';
import {TaskItemComponent} from 'app/task-list/task-item/task-item.component';
import {TaskListComponent} from 'app/task-list/task-list.component';
import {MatExpansionModule} from '@angular/material/expansion';
import {TodoListPipesModule} from 'app/pipes/todo-list-pipes.module';
import {TaskDetailsComponent} from 'app/task-list/task-item/task-details/task-details.component';
import {LayoutService} from 'app/layout/layout.service';
import {NotificationService} from 'app/layout/notification.service';
import {LayoutModule} from 'app/layout/layout.module';

describe('ActiveTasksComponent', () => {
  const mockClient = mockSpineWebClient();
  const unsubscribe = jasmine.createSpy('unsubscribe');

  let component: ActiveTasksComponent;
  let fixture: ComponentFixture<ActiveTasksComponent>;

  function collectDisplayedTasks(): string[] {
    const taskLists = fixture.debugElement
                             .queryAll(By.css('app-task-list'));
    const tasks = taskLists[1].queryAll(By.css('app-task-item'));
    return tasks.map(item =>
        item.query(By.css('mat-panel-title'))
            .query(By.css('div'))
            .nativeElement.innerHTML);
  }

  function addBasicTaskWith(taskDescription: string) {
    const input = fixture.debugElement
                         .query(By.css('input'))
                         .nativeElement;
    input.value = taskDescription;
    const keyPressed = new KeyboardEvent('keydown', {
      key: 'Enter'
    });
    input.dispatchEvent(keyPressed);
  }

  const addedTasksSubject = new BehaviorSubject<TaskView>(chore());

  mockClient.subscribe.and.returnValue(observableSubscriptionDataOf(
      addedTasksSubject.asObservable(), unsubscribe
  ));
  mockClient.fetch.and.returnValue(Promise.resolve([]));

  beforeEach(fakeAsync(() => {
    TestBed.configureTestingModule({
      declarations: [
        ActiveTasksComponent,
        TaskListComponent,
        TaskItemComponent,
        TaskDetailsComponent
      ],
      imports: [
        RouterTestingModule.withRoutes([]),
        ReactiveFormsModule,
        MatInputModule,
        MatListModule,
        MatIconModule,
        BrowserAnimationsModule,
        MatExpansionModule,
        TodoListPipesModule,
        LayoutModule
      ],
      providers: [TaskService, {
        provide: Client,
        useValue: mockClient
      }, LayoutService, NotificationService]
    })
           .compileComponents();

    fixture = TestBed.createComponent(ActiveTasksComponent);
    component = fixture.componentInstance;
    tick(); // Wait for the fake subscription fetch.
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should allow basic task creation', () => {
    const method = spyOn<any>(component, 'createBasicTask');
    addBasicTaskWith('some irrelevant description');
    expect(method).toHaveBeenCalledTimes(1);
  });

  it('should update the list of tasks without waiting for the response from the serve with a new task',
      fakeAsync(() => {
        const description = 'Wash my dog';
        addBasicTaskWith(description);
        tick();
        fixture.detectChanges();
        const taskDescriptions: string[] = collectDisplayedTasks();
        expect(taskDescriptions).toContain(` ${description} `);
      }));

  it('should rollback invalid optimistic task creations', fakeAsync(() => {
    const description = 'Walk my dog';
    mockClient.sendCommand.and.callFake((cmd: Message, onSuccess: () => void, onError: (err) => void) => {
      const err = {
        assuresCommandNeglected: () => true
      };
      onError(err);
    });
    addBasicTaskWith(description);
    tick(1_000);
    fixture.detectChanges();
    const taskDescriptions = collectDisplayedTasks();
    expect(taskDescriptions.includes(description)).toBe(false);
  }));
});
