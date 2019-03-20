import {Location} from '@angular/common';
import {AfterViewInit, Input} from '@angular/core';
import {MatStepper} from '@angular/material';

import {TaskCreationWizard} from './service/task-creation-wizard.service';

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

export abstract class WizardStep implements AfterViewInit {

  @Input()
  stepper: MatStepper;

  isRedirect = false;

  protected constructor(private readonly location: Location,
                        protected readonly wizard: TaskCreationWizard) {
  }

  private static reportError(err): void {
    console.log(`Error when setting task details: ${err}`);
  }

  ngAfterViewInit(): void {
    this.setNotCompleted();
    this.initModel();
  }

  protected initModel(): void {
    // NO-OP by default.
  }

  /**
   * Handles situations when we return to the step from the later stages and it's initially marked
   * as completed.
   *
   * Should be called in all descendant methods that change the inputs of the `TaskCreationWizard`.
   */
  protected onInputChange(): void {
    this.setNotCompleted();
  }

  private setCompleted(): void {
    this.stepper.selected.completed = true;
  }

  private setNotCompleted(): void {
    this.stepper.selected.completed = false;
  }

  /*
  * Navigation methods all of which are optional for the component.
  */

  protected previous(): void {
    this.stepper.previous();
  }

  protected next(): void {
    this.doStep()
      .then(() => {
        this.setCompleted();
        this.stepper.next();
      })
      .catch(err => {
        WizardStep.reportError(err);
      });
  }

  /**
   * Cancel draft creation.
   */
  protected cancel(): void {
    this.wizard.cancelTaskCreation().then(() => this.backFromWizard());
  }

  protected finish() {
    this.doStep().then(() => this.backFromWizard());
  }

  protected abstract doStep(): Promise<void>;

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
