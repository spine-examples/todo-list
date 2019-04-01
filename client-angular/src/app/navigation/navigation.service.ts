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
import {NavigationServiceModule} from 'app/navigation/navigation-service.module';
import {BehaviorSubject, Observable} from 'rxjs';

@Injectable({
  providedIn: NavigationServiceModule
})
export class NavigationService {

  private _currentLocation$: BehaviorSubject<string>;
  private _showNavigation$: BehaviorSubject<boolean>;

  constructor() {
    // TODO:2019-04-01:serhii.lekariev: not supposed to be hardcoded
    this._currentLocation$ = new BehaviorSubject<string>('To-do list');
    this._showNavigation$ = new BehaviorSubject<boolean>(true);
  }

  get showNav$(): Observable<boolean> {
    return this._showNavigation$.asObservable();
  }

  get currentLocation$(): Observable<string> {
    return this._currentLocation$.asObservable();
  }

  public changeLocation(newLocation: string) {
    this._currentLocation$.next(newLocation);
  }

  public showNav(): void {
    this._showNavigation$.next(true);
  }

  public hideNav() {
    this._showNavigation$.next(false);
  }
}
