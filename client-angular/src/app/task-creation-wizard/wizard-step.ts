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

import {AfterViewInit, Input, ViewChild} from '@angular/core';
import {MatStepper} from '@angular/material';
import {Router} from '@angular/router';

import {TaskCreationWizard} from './service/task-creation-wizard.service';
import {ErrorViewport} from '../common-components/error-viewport/error-viewport.component';

export abstract class WizardStep implements AfterViewInit {

  /** Visible for testing. */
  static readonly RETURN_TO = '/task-list/active';

  /** Visible for testing. */
  @Input()
  stepper: MatStepper;

  /** Visible for testing. */
  @ViewChild(ErrorViewport)
  errorViewport: ErrorViewport;

  /* Fields are visible for testing. */
  protected constructor(readonly router: Router,
                        readonly wizard: TaskCreationWizard) {
  }

  private static reportFatalError(err) {
    throw new Error(err);
  }

  ngAfterViewInit(): void {
    this.initOwnModel();
  }

  /**
   * Inits the own model, not related to the wizard data (like the list of available labels).
   */
  protected initOwnModel(): void {
    // NO-OP by default.
  }

  /**
   * Inits the model entries which represent the data from the wizard (like task definition,
   * priority, etc.).
   *
   * Must be called from the outside when we are sure the wizard data has been fetched.
   */
  initFromWizard() {
    // NO-OP by default.
  }

  /*
  * Navigation methods which can be used by the child components as necessary.
  */

  /**
   * ...
   *
   * Is visible for testing.
   */
  previous(): void {
    this.stepper.previous();
  }

  /**
   * ...
   *
   * Is visible for testing.
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
   * Cancel draft creation.
   *
   * Is visible for testing.
   */
  cancel(): void {
    this.wizard.cancelTaskCreation()
      .then(() => this.goToActiveTasks())
      .catch(err => {
        this.goToActiveTasks();
        WizardStep.reportFatalError(err);
      });
  }

  /**
   * ...
   *
   * Is visible for testing.
   */
  finish() {
    this.doStep()
      .then(() => this.goToActiveTasks())
      .catch(err => {
        this.goToActiveTasks();
        WizardStep.reportFatalError(err);
      });
  }

  protected abstract doStep(): Promise<void>;

  protected onInputChange(): void {
    this.setNotCompleted();
  }

  private setCompleted(): void {
    this.stepper.selected.completed = true;
  }

  private setNotCompleted(): void {
    this.stepper.selected.completed = false;
  }

  private goToActiveTasks(): void {
    // noinspection JSIgnoredPromiseFromCall No navigation result handling necessary.
    this.router.navigate([WizardStep.RETURN_TO]);
  }

  private reportError(err): void {
    this.errorViewport.text = err;
  }

  private clearError(): void {
    this.errorViewport.text = '';
  }
}
