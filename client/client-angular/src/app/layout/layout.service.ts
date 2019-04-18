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

import {Injectable} from '@angular/core';
import {LayoutModule} from 'app/layout/layout.module';
import {BehaviorSubject, Observable} from 'rxjs';

/**
 * Configuration of the layout.
 *
 * Defines the toolbar label value on the toolbar as well as whether a navbar
 * is supposed to be shown.
 */
export interface LayoutConfig {
  toolbarLabel: string;
  showNavigation: boolean;
}

/**
 * A service responsible for the layout of the application.
 *
 * Allows to adjust the way the sidenav and the toolbar are displayed.
 */
@Injectable({
  providedIn: LayoutModule
})
export class LayoutService {

  private static readonly DEFAULT_LAYOUT_CONFIG: LayoutConfig = {
    toolbarLabel: 'To-do list',
    showNavigation: true
  };

  private _config$: BehaviorSubject<LayoutConfig>;

  constructor() {
    this._config$ = new BehaviorSubject<LayoutConfig>(LayoutService.DEFAULT_LAYOUT_CONFIG);
  }

  /** Obtains an observable that represents the current layout config. */
  get config$(): Observable<LayoutConfig> {
    return this._config$.asObservable();
  }

  /** Updates the label on the toolbar with the specified value. */
  public updateToolbar(toolbarValue: string): void {
    const result = {...this._config$.getValue(), ...{toolbarLabel: toolbarValue}};
    this._config$.next(result);
  }

  /** Updates whether the toolbar is supposed to be showed. */
  public updateShowNav(shouldShowNav: boolean): void {
    const result = {...this._config$.getValue(), ...{showNavigation: shouldShowNav}};
    this._config$.next(result);
  }

  /**
   * Sets the layout values to their defaults, i.e. a visible navbar and a label value of
   * `To-do list`.
   */
  public defaultLayout(): void {
    this._config$.next(LayoutService.DEFAULT_LAYOUT_CONFIG);
  }
}
