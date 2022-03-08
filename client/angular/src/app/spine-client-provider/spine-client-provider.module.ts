/*
 * Copyright 2021, TeamDev. All rights reserved.
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

import {NgModule} from '@angular/core';
import {AngularFireModule} from '@angular/fire/compat';
import {AngularFireDatabase, AngularFireDatabaseModule} from '@angular/fire/compat/database';
import {ActorProvider, Client, init} from 'spine-web';

import {environment} from 'environments/environment';

import * as spineWebTypes from 'spine-web/proto/index';
import * as todoListTypes from 'proto/index';

/**
 * Creates a {@link Client} instance based on the environment config.
 *
 * @param angularFire the Angular Fire database to use
 */
export function clientFactory(angularFire: AngularFireDatabase): Client {
  return init({
    protoIndexFiles: [todoListTypes, spineWebTypes],
    endpointUrl: environment.host,
    firebaseDatabase: angularFire.database,
    actorProvider: new ActorProvider()
  });
}

/**
 * A provider of the {@linkplain Client Spine Web Client} for the application.
 */
@NgModule({
  imports: [
    AngularFireModule.initializeApp(environment.firebaseConfig),
    AngularFireDatabaseModule
  ],
  providers: [{
    provide: Client,
    useFactory: clientFactory,
    deps: [AngularFireDatabase]
  }]
})
export class SpineClientProvider {
}
