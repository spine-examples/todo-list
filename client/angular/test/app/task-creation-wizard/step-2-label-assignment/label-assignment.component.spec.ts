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

import {MatChipsModule, MatIconModule, MatListModule} from '@angular/material';
import {LabelAssignmentComponent} from 'app/task-creation-wizard/step-2-label-assignment/label-assignment.component';
import {TodoListComponentsModule} from 'app/common-components/todo-list-components.module';
import {TodoListPipesModule} from 'app/pipes/todo-list-pipes.module';
import {TaskCreationWizard} from 'app/task-creation-wizard/service/task-creation-wizard.service';
import {TaskService} from 'app/task-service/task.service';
import {mockSpineWebClient, subscriptionDataOf} from 'test/given/mock-spine-web-client';
import {LabelService} from 'app/labels/label.service';
import {chores} from 'test/given/tasks';
import {initMockProcessWithLabels, taskCreationProcess} from 'test/given/task-creation-process';
import {mockStepper} from 'test/task-creation-wizard/given/mock-stepper';
import {label1, label2} from 'test/given/labels';
import {LayoutService} from 'app/layout/layout.service';
import {mockLayoutService} from 'test/given/layout-service';
import {LayoutModule} from 'app/layout/layout.module';
import {NotificationService} from 'app/layout/notification.service';
import {MatSnackBarModule} from '@angular/material/snack-bar';


describe('LabelAssignmentComponent', () => {
  const mockClient = mockSpineWebClient();
  const unsubscribe = jasmine.createSpy('unsubscribe');
  mockClient.subscribe.and.returnValue(subscriptionDataOf(
    [chores()], [], [], unsubscribe
  ));

  let component: LabelAssignmentComponent;
  let fixture: ComponentFixture<LabelAssignmentComponent>;

  const availableLabels = [label1(), label2()];
  const selectedLabels = [availableLabels[0]];

  beforeEach(fakeAsync(() => {
    TestBed.configureTestingModule({
      declarations: [
        LabelAssignmentComponent
      ],
      imports: [
        RouterTestingModule.withRoutes([
          // Same component for convenience.
          {path: 'task-list/active', component: LabelAssignmentComponent}
        ]),

        TodoListComponentsModule,
        TodoListPipesModule,
        LayoutModule,

        MatSnackBarModule,
        MatChipsModule,
        MatIconModule,
        MatListModule
      ],
      providers: [
        TaskCreationWizard,
        TaskService,
        LabelService,
        NotificationService,
        {provide: Client, useValue: mockClient},
        {provide: LayoutService, useValue: mockLayoutService()}
      ]
    })
      .compileComponents();

    mockClient.fetch.and.callFake(initMockProcessWithLabels(selectedLabels, availableLabels));
    fixture = TestBed.createComponent(LabelAssignmentComponent);
    component = fixture.componentInstance;
    component.wizard.init(taskCreationProcess().getId().getUuid());
    tick();
    component.stepper = mockStepper();

    component.initFromWizard();
    component.ngAfterViewInit();

    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load a list of available labels in `afterViewInit`', fakeAsync(() => {
    expect(component.available).toEqual(availableLabels);
  }));

  it('should redirect Errors during loading labels to error viewport', fakeAsync(() => {
    const errorMessage = 'Loading available labels failed';
    mockClient.fetch.and.returnValue(Promise.reject(errorMessage));
    const newFixture = TestBed.createComponent(LabelAssignmentComponent);
    const newComponent = newFixture.componentInstance;

    newComponent.ngAfterViewInit();
    tick();
    expect(newComponent.errorViewport.text)
      .toEqual(`Error when loading available labels: ${errorMessage}`);

    newComponent.errorViewport.text = '';

    newComponent.initFromWizard();
    tick();
    expect(newComponent.errorViewport.text)
      .toEqual(`Error when loading available labels: ${errorMessage}`);
  }));

  it('should add label to selected and become non-completed', () => {
    component.stepper.selected.completed = true;
    const availableLabel = availableLabels[1];
    component.switchSelectedStatus(availableLabel);

    expect(component.selected.length).toEqual(2);
    expect(component.selected).toContain(availableLabel);
    expect(component.stepper.selected.completed).toBeFalsy();
  });

  it('should remove label from selected and become non-completed', () => {
    component.stepper.selected.completed = true;
    const selectedLabel = selectedLabels[0];
    component.switchSelectedStatus(selectedLabel);

    expect(component.selected).toEqual([]);
    expect(component.stepper.selected.completed).toBeFalsy();
  });

  it('should navigate to previous step but persist own completion status', () => {
    component.stepper.selected.completed = true;
    component.previous();
    expect(component.stepper.previous).toHaveBeenCalledTimes(1);

    // The `stepper.selected` remains intact because we are using mock `previous()` method.
    // So we can check completion status of the current step.
    expect(component.stepper.selected.completed).toBeTruthy();
  });

  it('should run `AddLabels` command with selected labels and navigate to next step',
    fakeAsync(() => {
      mockClient.sendCommand.and.callFake((command, resolve) => {
        expect(command.getExistingLabelsList())
          .toEqual(selectedLabels.map(label => label.getId()));
        resolve();
      });
      component.next();
      tick();
      expect(component.stepper.next).toHaveBeenCalledTimes(1);
    }));

  it('should run `SkipLabels` command if the selected label list is empty', fakeAsync(() => {
    component.selected = [];
    mockClient.sendCommand.and.callFake((command, resolve) => {
      expect(command.getExistingLabelsList).toBeUndefined();
      resolve();
    });
    component.next();
    tick();
    expect(component.stepper.next).toHaveBeenCalledTimes(1);
  }));

  it('should forward wizard errors to error viewport', fakeAsync(() => {
    const errorMessage = 'Adding labels failed';
    mockClient.sendCommand.and.callFake((command, resolve, reject) => reject(errorMessage));
    component.next();
    tick();
    expect(component.errorViewport.text).toEqual(errorMessage);
  }));

  it('should clear error viewport when navigating to next step', fakeAsync(() => {
    mockClient.sendCommand.and.callFake((command, resolve) => resolve());
    component.errorViewport.text = 'Adding labels failed';
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
