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
import {Component, Input} from '@angular/core';
import {MatStepper} from '@angular/material';

import {TaskCreationWizard} from '../service/task-creation-wizard.service';

@Component({
  selector: 'app-confirmation',
  templateUrl: './confirmation.component.html',
  styleUrls: ['./confirmation.component.css']
})
export class ConfirmationComponent {

  @Input()
  private readonly stepper: MatStepper;

  isRedirect = false;

  constructor(private readonly wizard: TaskCreationWizard,
              private readonly location: Location) {
  }

  back() {
    this.stepper.previous();
  }

  finish() {
    this.wizard.completeTaskCreation().then(() => this.backFromWizard());
  }

  /**
   * Task will be saved as draft.
   */
  cancel() {
    this.wizard.cancelTaskCreation().then(() => this.backFromWizard());
  }

  /**
   * Checking current path is not working as {@link Location} wrapper does not go back itself after
   * calling `back()`.
   */
  private backFromWizard(): void {
    this.location.back();
    if (this.isRedirect) {
      this.location.back();
    }
  }
}
