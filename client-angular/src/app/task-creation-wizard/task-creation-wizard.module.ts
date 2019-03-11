import {NgModule} from '@angular/core';
import {TaskCreationWizardComponent} from './task-creation-wizard.component';
import {TaskCreationWizardRoutingModule} from './task-creation-wizard.routes';

@NgModule({
  declarations: [TaskCreationWizardComponent],
  imports: [TaskCreationWizardRoutingModule]
})
export class TaskCreationWizardModule { }
