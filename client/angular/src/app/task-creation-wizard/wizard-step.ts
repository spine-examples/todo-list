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

import {AfterViewInit, Directive, Input, ViewChild} from '@angular/core';
import {MatStepper} from '@angular/material/stepper';
import {Router} from '@angular/router';

import {TaskCreationWizard} from 'app/task-creation-wizard/service/task-creation-wizard.service';
import {ErrorViewport} from 'app/common-components/error-viewport/error-viewport.component';

/**
 * A common base for components that represent Task Creation Wizard steps.
 */
@Directive()
export abstract class WizardStep implements AfterViewInit {

  /**
   * URL to return to after leaving the wizard page.
   *
   * Visible for testing.
   */
  static readonly QUIT_TO = '';

  /**
   * A reference to the Angular Material Stepper which handles the wizard UI.
   *
   * Visible for testing.
   */
  @Input()
  stepper: MatStepper;

  /**
   * A viewport for displaying error messages.
   *
   * Visible for testing.
   */
  @ViewChild(ErrorViewport, {static: true})
  errorViewport: ErrorViewport;

  /* Fields are visible for testing. */
  protected constructor(readonly router: Router, readonly wizard: TaskCreationWizard) {
  }

  /**
   * Reports an error which cannot really be recovered in the scope of a current page.
   */
  private static reportFatalError(err) {
    throw new Error(err);
  }

  /**
   * @inheritDoc
   */
  ngAfterViewInit(): void {
    this.initOwnModel();
  }

  /**
   * Inits the component's own model, i.e. data which does not depend on a Task Creation Wizard
   * state.
   */
  protected initOwnModel(): void {
    // NO-OP by default.
  }

  /**
   * Inits the model entries with the data from Task Creation Wizard.
   *
   * This method must be called from the outside when we are sure the wizard data has been fetched.
   */
  initFromWizard(): void {
    // NO-OP by default.
  }

  /*
  * Navigation methods that can be used by the descendants as necessary.
  */

  /**
   * Navigates to the previous wizard step.
   *
   * Visible for testing.
   */
  previous(): void {
    this.stepper.previous();
  }

  /**
   * Executes an action of the current step and navigates to the next.
   *
   * Visible for testing.
   */
  next(): void {
    this.clearError();
    this.doStep()
        .then(() => {
          this.setCompleted();
          this.stepper.next();
        })
        .catch(err => {
          this.reportError(err);
        });
  }

  /**
   * Finalizes the current step execution and sends the appropriate command to the server.
   */
  protected abstract doStep(): Promise<void>;

  /**
   * Cancels the task creation and navigates away from the wizard.
   *
   * Visible for testing.
   */
  cancel(): void {
    this.wizard.cancelTaskCreation()
        .then(() => this.quitWizard())
        .catch(err => {
          this.quitWizard();
          WizardStep.reportFatalError(err);
        });
  }

  /**
   * Completes the task creation and navigates away from the wizard.
   *
   * Visible for testing.
   */
  finish() {
    this.doStep()
        .then(() => this.quitWizard())
        .catch(err => {
          this.quitWizard();
          WizardStep.reportFatalError(err);
        });
  }

  /**
   * Reports an error which can be recovered and thus can be shown to the user (like an invalid
   * task description).
   */
  protected reportError(err): void {
    this.errorViewport.text = err;
  }

  /**
   * Clears the error viewport.
   */
  private clearError(): void {
    this.errorViewport.text = '';
  }

  /**
   * The method which should be called by descendants when the user inputs any data on the UI.
   */
  protected onUserInput(): void {
    this.setNotCompleted();
  }

  /**
   * Sets the current {@link stepper} step to completed.
   */
  private setCompleted(): void {
    this.stepper.selected.completed = true;
  }

  /**
   * Sets the current {@link stepper} step to not completed.
   */
  private setNotCompleted(): void {
    this.stepper.selected.completed = false;
  }

  /**
   * Navigates away from the wizard.
   */
  private quitWizard(): void {
    // noinspection JSIgnoredPromiseFromCall No navigation result handling necessary.
    this.router.navigate([WizardStep.QUIT_TO]);
  }
}
