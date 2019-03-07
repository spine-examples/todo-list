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

import {Inject, Injectable, Optional} from '@angular/core';
import {FirebaseAppModule} from './firebase-app.module';

import {environment} from '../../environments/environment';

import * as firebase from 'firebase/app';
import 'firebase/database';

@Injectable({
  providedIn: FirebaseAppModule
})
export class FirebaseApp {

  private static readonly DEFAULT_APP_NAME = 'default-app-name';

  constructor(@Inject('firebaseAppName') @Optional() name?: string) {
    const appName = name != null ? name : FirebaseApp.DEFAULT_APP_NAME;
    this.app = firebase.initializeApp(environment.firebaseConfig, appName);
  }

  private readonly app: firebase.app.App;

  database(): firebase.database.Database {
    return this.app.database();
  }
}
