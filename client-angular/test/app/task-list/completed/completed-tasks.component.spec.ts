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
import {RouterModule} from '@angular/router';
import {MatListModule} from '@angular/material/list';
import {By} from '@angular/platform-browser';
import {RouterTestingModule} from '@angular/router/testing';

import {Client} from 'spine-web';
import {CompletedTasksComponent} from 'app/task-list/completed/completed-tasks.component';
import {TaskService} from 'app/task-service/task.service';
import {mockSpineWebClient, subscriptionDataOf} from 'test/given/mock-spine-web-client';
import {TaskLinkComponent} from 'app/task-list/task-link/task-link.component';
import {completedTasks} from 'test/given/tasks';

describe('CompletedTasksComponent', () => {

  const mockClient = mockSpineWebClient();
  let fixture: ComponentFixture<CompletedTasksComponent>;
  let component: CompletedTasksComponent;
  const unsubscribe = jasmine.createSpy('unsubscribe');

  mockClient.subscribeToEntities.and.returnValue(subscriptionDataOf(
    [completedTasks()], [], [], unsubscribe
  ));

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [CompletedTasksComponent, TaskLinkComponent],
      imports: [MatListModule, RouterModule, RouterTestingModule],
      providers: [TaskService, {provide: Client, useValue: mockClient}]
    })
      .compileComponents();

    fixture = TestBed.createComponent(CompletedTasksComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should contain completed tasks', async(() => {
    // The timeout allows the page to fully render before checking the DOM for presence of the
    // necessary element.
    fixture.whenStable().then(() => {
      fixture.detectChanges();
      const tasks = fixture.debugElement.queryAll(By.css('.list-item'));
      expect(tasks.length).toBe(2);
    });
  }));
});
