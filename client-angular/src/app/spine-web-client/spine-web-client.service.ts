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

import {environment} from '../../environments/environment';
import {Injectable} from '@angular/core';
import {SpineWebClientModule} from './spine-web-client.module';
import {FirebaseApp} from '../firebase-app/firebase-app.service';
import * as spineWeb from 'spine-web';
import * as spineWebTypes from 'spine-web/proto/index';
import {ActorProvider, Client} from 'spine-web';

import * as knownTypes from 'generated/main/js/index';
import {UserId} from 'spine-web/proto/spine/core/user_id_pb';
import {Message} from 'google-protobuf';

@Injectable({
  providedIn: SpineWebClientModule
})
export class SpineWebClient {

  private readonly client: Client;

  constructor(private readonly firebaseApp: FirebaseApp) {
    this.client = spineWeb.init({
      protoIndexFiles: [knownTypes, spineWebTypes],
      endpointUrl: environment.host,
      firebaseDatabase: this.firebaseApp.database(),
      actorProvider: SpineWebClient.actorProvider()
    });
  }

  private static actorProvider(): ActorProvider {
    const userId = new UserId();
    userId.setValue(environment.actor);
    return new spineWeb.ActorProvider(userId);
  }

  private static logSuccess(): void {
    console.log('Command sent');
  }

  private static logError(error): void {
    console.error(error);
  }

  sendCommand(commandMessage: Message): void {
    this.client.sendCommand(commandMessage,
      SpineWebClient.logSuccess, SpineWebClient.logError, SpineWebClient.logError);
  }
}
