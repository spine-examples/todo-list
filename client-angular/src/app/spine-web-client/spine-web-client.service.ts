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
import {FirebaseClient} from '../firebase-client/firebase-client.service';
import * as spineWeb from 'spine-web';
import {ActorProvider, Client} from 'spine-web';
import {UserId} from '../../../proto/main/js/spine/core/user_id_pb';
import * as knownTypes from '../../../proto/main/js/index';

@Injectable()
export class SpineWebClient {

  constructor(private firebaseClient: FirebaseClient) {
    this.client = spineWeb.init({
      protoIndexFiles: [knownTypes],
      endpointUrl: environment.host,
      firebaseDatabase: firebaseClient.app.database(),
      actorProvider: SpineWebClient.actorProvider()
    });
  }

  private readonly client: Client;

  private static actorProvider(): ActorProvider {
    const userId = new UserId();
    userId.setValue(environment.actor);
    return new spineWeb.ActorProvider(userId);
  }

  private static logSuccess(): void {
    console.log('Command sent');
  }

  private static errorCallback(error): void {
    console.error(error);
  }

  sendCommand(commandMessage): void {
    this.client.sendCommand(commandMessage,
      SpineWebClient.logSuccess, SpineWebClient.errorCallback, SpineWebClient.errorCallback);
  }
}
