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

import {CommonModule} from '@angular/common';
import {async, ComponentFixture, TestBed} from '@angular/core/testing';
import {ReactiveFormsModule} from '@angular/forms';
import {NoopAnimationsModule} from '@angular/platform-browser/animations';


import {MatMomentDateModule} from '@angular/material-moment-adapter';
import {MatButtonModule} from '@angular/material/button';
import {MatDatepickerModule} from '@angular/material/datepicker';
import {MatInputModule} from '@angular/material/input';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatSelectModule} from '@angular/material/select';
import {MatStepperModule} from '@angular/material/stepper';

import {Client} from 'spine-web';

import {TaskCreationWizardComponent} from '../../../../src/app/task-creation-wizard/task-creation-wizard.component';
import {TaskDefinitionComponent} from '../../../../src/app/task-creation-wizard/step-1/task-definition.component';
import {LabelAssignmentComponent} from '../../../../src/app/task-creation-wizard/step-2/label-assignment.component';
import {ConfirmationComponent} from '../../../../src/app/task-creation-wizard/step-3/confirmation.component';
import {TaskCreationWizardRoutingModule} from '../../../../src/app/task-creation-wizard/task-creation-wizard.routes';
import {TaskServiceModule} from '../../../../src/app/task-service/task-service.module';
import {RouterTestingModule} from '@angular/router/testing';
import {TodoListComponentsModule} from '../../../../src/app/common-components/todo-list-components.module';
import {TodoListPipesModule} from '../../../../src/app/pipes/todo-list-pipes.module';
import {TaskCreationWizard} from '../../../../src/app/task-creation-wizard/service/task-creation-wizard.service';
import {TaskService} from '../../../../src/app/task-service/task.service';
import {mockSpineWebClient} from '../../given/mock-spine-web-client';


describe('TaskDefinitionComponent', () => {
  let component: TaskDefinitionComponent;
  let fixture: ComponentFixture<TaskDefinitionComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [
        TaskCreationWizardComponent,
        TaskDefinitionComponent,
        LabelAssignmentComponent,
        ConfirmationComponent
      ],
      imports: [
        CommonModule,
        ReactiveFormsModule,
        RouterTestingModule.withRoutes([]),
        NoopAnimationsModule,

        TaskCreationWizardRoutingModule,
        TodoListComponentsModule,
        TodoListPipesModule,
        TaskServiceModule,

        MatMomentDateModule,
        MatButtonModule,
        MatDatepickerModule,
        MatInputModule,
        MatFormFieldModule,
        MatSelectModule,
        MatStepperModule
      ],
      providers: [
        TaskCreationWizard,
        TaskService,
        {provide: Client, useValue: mockSpineWebClient()}
      ]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TaskDefinitionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
