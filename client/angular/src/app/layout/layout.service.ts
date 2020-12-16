/*
 * Copyright 2020, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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
import {BehaviorSubject, Observable} from 'rxjs';

/**
 * Configuration of the layout.
 *
 * Defines the toolbar label value on the toolbar, whether a navbar and a quit button are supposed
 * to be visible.
 */
export interface LayoutConfig {
  toolbarLabel?: string;
  showNavigation?: boolean;
  quitButtonHandler?: () => void;
}

/**
 * A service responsible for the layout of the application.
 *
 * Allows to adjust the way the sidenav and the toolbar are displayed.
 */
@Injectable({
  providedIn: 'root'
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

  /** Executes the current value of the `quitButtonHandler`. */
  public quit(): void {
    this._config$.getValue().quitButtonHandler();
  }

  /**
   * Updates the layout config.
   *
   * Updating the config partly is possible, an object with unspecified field can be passed in
   * this case.
   *
   * @param config layout config with updated field values.
   */
  public update(config: LayoutConfig): void {
    const result = {...this._config$.getValue(), ...config};
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
