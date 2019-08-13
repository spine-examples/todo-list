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

import {Client} from 'spine-web';
import {ConfirmationComponent} from 'app/task-creation-wizard/step-3-confirmation/confirmation.component';
import {TodoListComponentsModule} from 'app/common-components/todo-list-components.module';
import {TodoListPipesModule} from 'app/pipes/todo-list-pipes.module';
import {TaskCreationWizard} from 'app/task-creation-wizard/service/task-creation-wizard.service';
import {TaskService} from 'app/task-service/task.service';
import {mockSpineWebClient, subscriptionDataOf} from 'test/given/mock-spine-web-client';
import {chores} from 'test/given/tasks';
import {initMockProcess, taskCreationProcess} from 'test/given/task-creation-process';
import {mockStepper} from 'test/task-creation-wizard/given/mock-stepper';
import {TaskPriorityName} from 'app/pipes/task-priority-name/task-priority-name.pipe';
import {MomentFromTimestamp} from 'app/pipes/moment-from-timestamp/momentFromTimestamp.pipe';
import {LayoutService} from 'app/layout/layout.service';
import {mockLayoutService} from 'test/given/layout-service';
import {LayoutModule} from 'app/layout/layout.module';
import {NotificationService} from 'app/layout/notification.service';
import {MatSnackBarModule} from '@angular/material/snack-bar';


describe('ConfirmationComponent', () => {
  const mockClient = mockSpineWebClient();
  const unsubscribe = jasmine.createSpy('unsubscribe');
  mockClient.subscribe.and.returnValue(subscriptionDataOf(
    [chores()], [], [], unsubscribe
  ));

  let component: ConfirmationComponent;
  let fixture: ComponentFixture<ConfirmationComponent>;

  beforeEach(fakeAsync(() => {
    TestBed.configureTestingModule({
      declarations: [
        ConfirmationComponent
      ],
      imports: [
        RouterTestingModule.withRoutes([
          // Same component for convenience.
          {path: 'task-list/active', component: ConfirmationComponent}
        ]),

        MatSnackBarModule,
        TodoListComponentsModule,
        TodoListPipesModule,
        LayoutModule
      ],
      providers: [
        TaskCreationWizard,
        TaskService,
        NotificationService,
        {provide: Client, useValue: mockClient},
        {provide: LayoutService, useValue: mockLayoutService()}
      ]
    })
      .compileComponents();

    mockClient.fetch.and.callFake(initMockProcess());
    fixture = TestBed.createComponent(ConfirmationComponent);
    component = fixture.componentInstance;
    component.wizard.init(taskCreationProcess().getId().getUuid());
    tick();
    component.stepper = mockStepper();
    component.initFromWizard();

    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should display information about the task in component body', () => {
    const text = fixture.nativeElement.textContent;
    const descriptionText = component.wizard.taskDescription.getValue();
    expect(text).toContain(descriptionText);

    const priorityPipe = new TaskPriorityName();
    const priorityText = priorityPipe.transform(component.wizard.taskPriority);
    expect(text).toContain(priorityText);

    const dueDatePipe = new MomentFromTimestamp();
    const dueDateText = dueDatePipe.transform(component.wizard.taskDueDate).toString();
    expect(text).toContain(dueDateText);
  });

  it('should complete task creation', fakeAsync(() => {
    mockClient.sendCommand.and.callFake((command, resolve) => resolve());
    component.finish();
    tick();
    const url = component.router.url;
    const stillContainsWizard: boolean = url.includes('wizard');
    expect(stillContainsWizard).toBe(false);
  }));

  it('should throw an exception if process completion fails', fakeAsync(() => {
    const errorMessage = 'Completing task creation failed';
    mockClient.sendCommand.and.callFake((command, resolve, reject) => reject(errorMessage));
    expect(() => {
      component.finish();
      tick();
    }).toThrowError();
    const url = component.router.url;
    const stillContainsWizard: boolean = url.includes('wizard');
    expect(stillContainsWizard).toBe(false);
  }));

  it('should navigate to previous step', () => {
    component.previous();
    expect(component.stepper.previous).toHaveBeenCalledTimes(1);
  });

  it('should cancel task creation', fakeAsync(() => {
    mockClient.sendCommand.and.callFake((command, resolve) => resolve());
    component.cancel();
    tick();
    const url = component.router.url;
    const stillContainsWizard: boolean = url.includes('wizard');
    expect(stillContainsWizard).toBe(false);
  }));

  it('throw Error if canceling task creation failed', fakeAsync(() => {
    mockClient.sendCommand.and.callFake((command, resolve, reject) => reject());
    expect(() => {
      component.cancel();
      tick();
    }).toThrowError();
  }));
});
