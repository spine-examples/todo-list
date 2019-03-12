import {NgModule} from '@angular/core';
import {LabelsComponent} from './labels.component';
import {SpineWebClientModule} from '../spine-web-client/spine-web-client.module';
import {LabelsRoutingModule} from './labels.routes';

/**
 * The module responsible for displaying and managing the task labels.
 */
@NgModule({
  declarations: [LabelsComponent],
  imports: [LabelsRoutingModule, SpineWebClientModule]
})
export class LabelsModule {
}
