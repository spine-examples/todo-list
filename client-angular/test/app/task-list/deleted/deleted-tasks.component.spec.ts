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
import {MatListModule} from '@angular/material/list';
import {RouterModule} from '@angular/router';

import {Client} from 'spine-web';
import {DeletedTasksComponent} from 'app/task-list/deleted/deleted-tasks.component';
import {TaskLinkComponent} from 'app/task-list/task-link/task-link.component';
import {TaskService} from 'app/task-service/task.service';
import {mockSpineWebClient, subscriptionDataOf} from 'test/given/mock-spine-web-client';
import {houseTasks} from 'test/given/tasks';

describe('DeletedTasksComponent', () => {

  let component: DeletedTasksComponent;
  let fixture: ComponentFixture<DeletedTasksComponent>;
  const mockClient = mockSpineWebClient();
  const unsubscribe = jasmine.createSpy();

  mockClient.subscribeToEntities.and.returnValue(subscriptionDataOf(
    [houseTasks()], [], [], unsubscribe
  ));

  beforeEach(fakeAsync(() => {
    TestBed.configureTestingModule({
      declarations: [DeletedTasksComponent, TaskLinkComponent],
      imports: [MatListModule, RouterModule],
      providers: [TaskService, {provide: Client, useValue: mockClient}]
    })
      .compileComponents();

    fixture = TestBed.createComponent(DeletedTasksComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();

    tick(); // Wait for the fake subscription fetch.
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DeletedTasksComponent);
    component = fixture.componentInstance;

    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
