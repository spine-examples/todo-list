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
import {LayoutServiceModule} from 'app/layout/layout-service.module';
import {BehaviorSubject, Observable} from 'rxjs';

/**
 * A service responsible for the layout of the application.
 *
 * Allows to adjust the way the sidenav and the toolbar are displayed.
 */
@Injectable({
  providedIn: LayoutServiceModule
})
export class LayoutService {

  private static readonly DEFAULT_LABEL: string = 'To-do list';
  private static readonly DEFAULT_SHOW_NAV: boolean = true;

  private _currentLabel$: BehaviorSubject<string>;
  private _showNavigation$: BehaviorSubject<boolean>;

  constructor() {
    this._currentLabel$ = new BehaviorSubject<string>('To-do list');
    this._showNavigation$ = new BehaviorSubject<boolean>(true);
  }

  /** Obtains whether the sidenav is currently being shown. */
  get showNav$(): Observable<boolean> {
    return this._showNavigation$.asObservable();
  }

  /** Obtains the value the label on the toolbar. */
  get currentLabel$(): Observable<string> {
    return this._currentLabel$.asObservable();
  }

  /** Updates the label on the toolbar with the specified value. */
  public updateLocation(newLocation: string) {
    this._currentLabel$.next(newLocation);
  }

  /** Updates whether the toolbar is supposed to be showed. */
  public updateShowNav(shouldShowNav: boolean) {
    this._showNavigation$.next(shouldShowNav);
  }

  /**
   * Sets the layout values to their defaults, i.e. a visible navbar and a label value of
   * `To-do list`.
   */
  public defaultLayout(): void {
    this._currentLabel$.next(LayoutService.DEFAULT_LABEL);
    this._showNavigation$.next(LayoutService.DEFAULT_SHOW_NAV);
  }
}
