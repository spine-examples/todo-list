import {NgModule} from '@angular/core';
import {FirebaseAppModule} from '../firebase-app/firebase-app.module';

/**
 * A module which provides the {@link SpineWebClient} service.
 */
@NgModule({
  imports: [FirebaseAppModule]
})
export class SpineWebClientModule {
}
