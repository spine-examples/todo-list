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
import {LabelAssignmentComponent} from '../../../../src/app/task-creation-wizard/step-2-label-assignment/label-assignment.component';
import {TodoListComponentsModule} from '../../../../src/app/common-components/todo-list-components.module';
import {TodoListPipesModule} from '../../../../src/app/pipes/todo-list-pipes.module';
import {TaskCreationWizard} from '../../../../src/app/task-creation-wizard/service/task-creation-wizard.service';
import {TaskService} from '../../../../src/app/task-service/task.service';
import {mockSpineWebClient, subscriptionDataOf} from '../../given/mock-spine-web-client';
import {LabelService} from '../../../../src/app/labels/label.service';
import {houseTasks} from '../../given/tasks';
import {initMockProcessWithLabels, taskCreationProcess} from '../../given/task-creation-process';
import {mockStepper} from '../given/mock-stepper';
import {label1, label2} from '../../given/labels';
import {WizardStep} from '../../../../src/app/task-creation-wizard/wizard-step';

describe('LabelAssignmentComponent', () => {
  const mockClient = mockSpineWebClient();
  const unsubscribe = jasmine.createSpy('unsubscribe');
  mockClient.subscribeToEntities.and.returnValue(subscriptionDataOf(
    [houseTasks()], [], [], unsubscribe
  ));

  const fetch = jasmine.createSpyObj<Client.Fetch>('Fetch', ['atOnce']);
  mockClient.fetchAll.and.returnValue(fetch);
  fetch.atOnce.and.returnValue(Promise.resolve());

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

        MatChipsModule,
        MatIconModule,
        MatListModule
      ],
      providers: [
        TaskCreationWizard,
        TaskService,
        LabelService,
        {provide: Client, useValue: mockClient}
      ]
    })
      .compileComponents();

    fetch.atOnce.and.returnValue(Promise.resolve(availableLabels));
    mockClient.fetchById.and.callFake(initMockProcessWithLabels(selectedLabels));
    fixture = TestBed.createComponent(LabelAssignmentComponent);
    component = fixture.componentInstance;
    component.wizard.init(taskCreationProcess().getId().getValue());
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
    fetch.atOnce.and.returnValue(Promise.reject(errorMessage));
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
    expect(component.router.url).toEqual(WizardStep.RETURN_TO);
  }));

  it('throw Error if canceling task creation failed', fakeAsync(() => {
    mockClient.sendCommand.and.callFake((command, resolve, reject) => reject());
    expect(() => {
      component.cancel();
      tick();
    }).toThrowError();
  }));
});
