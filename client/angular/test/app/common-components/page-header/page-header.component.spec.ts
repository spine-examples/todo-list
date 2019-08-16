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

import {ComponentFixture, fakeAsync, TestBed} from '@angular/core/testing';
import {MatDividerModule} from '@angular/material/divider';
import {RouterTestingModule} from '@angular/router/testing';

import {PageHeader} from 'app/common-components/page-header/page-header.component';

describe('PageHeader', () => {
  let fixture: ComponentFixture<PageHeader>;
  let component: PageHeader;

  beforeEach(fakeAsync(() => {
    TestBed.configureTestingModule({
      declarations: [PageHeader],
      imports: [
        RouterTestingModule.withRoutes([]),
        MatDividerModule
      ]
    })
           .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PageHeader);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should contain the specified text in a separate paragraph', () => {
    const text = 'the text';
    component.text = text;
    fixture.detectChanges();
    const elements = fixture.nativeElement.querySelectorAll('p');
    elements.forEach(
        element => {
          if (element.classList.contains('mat-headline')) {
            expect(element.textContent).toContain(text);
          }
        }
    );
  });
});
