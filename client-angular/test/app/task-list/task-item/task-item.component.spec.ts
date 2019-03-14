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

import {async, ComponentFixture, TestBed} from '@angular/core/testing';
import {RouterTestingModule} from '@angular/router/testing';

import {TaskItemComponent} from '../../../../src/app/task-list/task-item/task-item.component';
import {HOUSE_TASK_1_DESC, HOUSE_TASK_1_ID, task} from '../../given/tasks';

describe('TaskItemComponent', () => {
  let component: TaskItemComponent;
  let fixture: ComponentFixture<TaskItemComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [TaskItemComponent],
      imports: [RouterTestingModule.withRoutes([])]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TaskItemComponent);
    component = fixture.componentInstance;
    component.task = task(HOUSE_TASK_1_ID, HOUSE_TASK_1_DESC);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should have a link to task details', () => {
    const element = fixture.nativeElement.querySelector('a');
    expect(element.href).toContain('details/' + HOUSE_TASK_1_ID);
    expect(element.textContent).toContain(HOUSE_TASK_1_DESC);
  });

  it('should allow to complete task', () => {
    window.alert = jasmine.createSpy('alert');
    component.completeTask();
    expect(window.alert).toHaveBeenCalledWith('Completing task with ID: ' + HOUSE_TASK_1_ID);
  });

  it('should allow to delete task', () => {
    window.alert = jasmine.createSpy('alert');
    component.deleteTask();
    expect(window.alert).toHaveBeenCalledWith('Deleting task with ID: ' + HOUSE_TASK_1_ID);
  });
});
