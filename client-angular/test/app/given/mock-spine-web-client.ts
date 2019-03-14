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

import {Message} from 'google-protobuf';
import {Client, Type} from 'spine-web';
import {Observable} from 'rxjs';

interface SubscriptionData<T> {
  itemAdded: Observable<T>;
  itemChanged: Observable<T>;
  itemRemoved: Observable<T>;
  unsubscribe: () => void;
}

function observableOf<T>(items: T[]): Observable<T> {
  return Observable.create(observer => {
    if (items.length > 0) {
      items.forEach(item => observer.next(item));
    }
    observer.complete();
  });
}

export function subscriptionDataOf<T>(added: T[],
                                      changed: T[],
                                      removed: T[],
                                      unsubscribe: () => void)
  : Promise<SubscriptionData<T>> {
  return Promise.resolve({
    itemAdded: observableOf(added),
    itemChanged: observableOf(changed),
    itemRemoved: observableOf(removed),
    unsubscribe
  });
}

export function mockSpineWebClient() {
  return jasmine.createSpyObj<Client>('Client', ['sendCommand', 'subscribeToEntities']);
}
