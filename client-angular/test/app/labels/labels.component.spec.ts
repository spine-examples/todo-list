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

import {Location} from '@angular/common';
import {async, TestBed} from '@angular/core/testing';
import {RouterTestingModule} from '@angular/router/testing';

import {Client} from 'spine-web';

import {LabelsComponent} from 'app/labels/labels.component';
import {LabelService} from 'app/labels/label.service';
import {mockSpineWebClient} from 'test/given/mock-spine-web-client';

describe('LabelsComponent', () => {
  let component: LabelsComponent;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [LabelsComponent],
      imports: [RouterTestingModule.withRoutes([])],
      providers: [LabelService, {provide: Client, useValue: mockSpineWebClient()}]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    const fixture = TestBed.createComponent(LabelsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should navigate back', () => {
    const location: Location = TestBed.get(Location);
    const initialPath = '/task-list/tasks/active';
    location.go(initialPath);

    const nextPath = 'labels';
    location.go(nextPath);
    component.back();

    expect(location.path()).toBe(initialPath);
  });

  it('should print warning message when trying to add a new label', () => {
    console.log = jasmine.createSpy('log');
    component.addLabel();
    expect(console.log).toHaveBeenCalledWith('To be honest I cannot add a new label');
  });
});
