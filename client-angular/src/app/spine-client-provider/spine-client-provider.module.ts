import {NgModule} from '@angular/core';
import {AngularFireModule} from '@angular/fire';
import {AngularFireDatabase, AngularFireDatabaseModule} from '@angular/fire/database';
import {ActorProvider, Client, init} from 'spine-web';
import {UserId} from 'spine-web/proto/spine/core/user_id_pb';

import {environment} from '../../environments/environment';

import * as spineWebTypes from 'spine-web/proto/index';
import * as todoListTypes from 'generated/main/js/index';

/**
 * Wraps actor {@linkplain environment#actor data} into the form suitable for Spine Web Client.
 */
function actorProvider(): ActorProvider {
  const userId = new UserId();
  userId.setValue(environment.actor);
  return new ActorProvider(userId);
}

/**
 * Creates a {@link Client} instance based on the environment config.
 *
 * @param angularFire the Angular Fire database to use
 */
function clientFactory(angularFire: AngularFireDatabase): Client {
  return init({
    protoIndexFiles: [todoListTypes, spineWebTypes],
    endpointUrl: environment.host,
    firebaseDatabase: angularFire.database,
    actorProvider: actorProvider()
  });
}

/**
 * A provider of the {@linkplain Client Spine Web Client} for the application.
 */
@NgModule({
  declarations: [],
  imports: [
    AngularFireModule.initializeApp(environment.firebaseConfig),
    AngularFireDatabaseModule
  ],
  providers: [
    {
      provide: Client,
      useFactory: clientFactory,
      deps: [AngularFireDatabase]
    }
  ]
})
export class SpineClientProvider {
}
