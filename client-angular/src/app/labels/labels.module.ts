import {NgModule} from '@angular/core';
import {LabelsComponent} from './labels.component';
import {SpineWebClientModule} from '../spine-web-client/spine-web-client.module';
import {LabelsRoutingModule} from './labels.routes';

@NgModule({
  declarations: [LabelsComponent],
  imports: [SpineWebClientModule, LabelsRoutingModule]
})
export class LabelsModule { }
