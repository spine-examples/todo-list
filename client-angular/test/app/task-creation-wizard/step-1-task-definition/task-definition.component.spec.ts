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
import {FormsModule} from '@angular/forms';
import {NoopAnimationsModule} from '@angular/platform-browser/animations';
import {MatMomentDateModule} from '@angular/material-moment-adapter';
import {MatButtonModule} from '@angular/material/button';
import {MatDatepickerModule} from '@angular/material/datepicker';
import {MatInputModule} from '@angular/material/input';
import {MatSelectModule} from '@angular/material/select';
import {RouterTestingModule} from '@angular/router/testing';

import {Client} from 'spine-web';
import {TaskDefinitionComponent} from 'app/task-creation-wizard/step-1-task-definition/task-definition.component';
import {TodoListComponentsModule} from 'app/common-components/todo-list-components.module';
import {TodoListPipesModule} from 'app/pipes/todo-list-pipes.module';
import {TaskCreationWizard} from 'app/task-creation-wizard/service/task-creation-wizard.service';
import {TaskService} from 'app/task-service/task.service';
import {mockSpineWebClient, subscriptionDataOf} from 'test/given/mock-spine-web-client';
import {mockStepper} from 'test/task-creation-wizard/given/mock-stepper';
import {chores} from 'test/given/tasks';
import {initMockProcess, taskCreationProcess} from 'test/given/task-creation-process';
import {tomorrowMoment} from 'test/given/dates';
import {MomentFromTimestamp} from 'app/pipes/moment-from-timestamp/momentFromTimestamp.pipe';
import {TaskPriority} from 'proto/todolist/attributes_pb';
import {LayoutService} from 'app/layout/layout.service';
import {mockLayoutService} from 'test/given/layout-service';
import {LayoutModule} from 'app/layout/layout.module';
import {NotificationService} from 'app/layout/notification.service';
import {MatSnackBarModule} from '@angular/material/snack-bar';


describe('TaskDefinitionComponent', () => {
  const mockClient = mockSpineWebClient();
  const unsubscribe = jasmine.createSpy('unsubscribe');
  mockClient.subscribeToEntities.and.returnValue(subscriptionDataOf(
    [chores()], [], [], unsubscribe
  ));

  let component: TaskDefinitionComponent;
  let fixture: ComponentFixture<TaskDefinitionComponent>;

  beforeEach(fakeAsync(() => {
    TestBed.configureTestingModule({
      declarations: [
        TaskDefinitionComponent
      ],
      imports: [
        NoopAnimationsModule,
        FormsModule,
        RouterTestingModule.withRoutes([
          // Same component for convenience.
          {path: 'active', component: TaskDefinitionComponent}
        ]),

        TodoListComponentsModule,
        TodoListPipesModule,
        LayoutModule,

        MatSnackBarModule,
        MatMomentDateModule,
        MatButtonModule,
        MatDatepickerModule,
        MatInputModule,
        MatSelectModule
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

    mockClient.fetchById.and.callFake(initMockProcess());
    fixture = TestBed.createComponent(TaskDefinitionComponent);
    component = fixture.componentInstance;
    component.wizard.init(taskCreationProcess().getId().getValue());
    tick();
    component.stepper = mockStepper();
    component.initFromWizard();

    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should receive initial task data from wizard', () => {
    expect(component.description).toEqual(component.wizard.taskDescription);
    expect(component.priority).toEqual(component.wizard.taskPriority);
    expect(component.dueDate).toEqual(component.wizard.taskDueDate);
  });

  it('should set description to a given value and become non-completed', () => {
    component.stepper.selected.completed = true;
    component.setDescription('New description');
    expect(component.stepper.selected.completed).toBeFalsy();
  });

  it('should set priority to a given value and become non-completed', () => {
    component.stepper.selected.completed = true;
    component.setPriority(TaskPriority.LOW);
    expect(component.stepper.selected.completed).toBeFalsy();
  });

  it('should set due date to a given value and become non-completed', () => {
    component.stepper.selected.completed = true;
    component.setDueDate(tomorrowMoment());
    expect(component.stepper.selected.completed).toBeFalsy();
  });

  it('should update task details and navigate to next step', fakeAsync(() => {
    const newDescription = 'The new description';
    component.setDescription(newDescription);
    const newPriority = TaskPriority.LOW;
    component.setPriority(newPriority);
    const newDueDate = tomorrowMoment();
    component.setDueDate(newDueDate);

    mockClient.sendCommand.and.callFake((command, resolve) => {
      expect(command.getDescriptionChange().getPreviousValue())
        .toEqual(component.wizard.taskDescription);
      expect(command.getDescriptionChange().getNewValue().getValue())
        .toEqual(newDescription);

      expect(command.getPriorityChange().getPreviousValue())
        .toEqual(component.wizard.taskPriority);
      expect(command.getPriorityChange().getNewValue())
        .toEqual(newPriority);

      const dueDateForCheck = MomentFromTimestamp.back(newDueDate);
      expect(command.getDueDateChange().getPreviousValue())
        .toEqual(component.wizard.taskDueDate);
      expect(command.getDueDateChange().getNewValue())
        .toEqual(dueDateForCheck);

      resolve();
    });
    component.next();
    tick();
    expect(component.stepper.next).toHaveBeenCalledTimes(1);
  }));

  it('should forward wizard errors to error viewport', fakeAsync(() => {
    const errorMessage = 'Update task details failed';
    mockClient.sendCommand.and.callFake((command, resolve, reject) => reject(errorMessage));
    component.setDescription('The new description');
    component.next();
    tick();
    expect(component.errorViewport.text).toEqual(errorMessage);
  }));

  it('should clear error viewport when navigating to next step', fakeAsync(() => {
    mockClient.sendCommand.and.callFake((command, resolve) => resolve());
    component.errorViewport.text = 'Some error message';
    component.next();
    tick();
    expect(component.errorViewport.text).toEqual('');
  }));

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
