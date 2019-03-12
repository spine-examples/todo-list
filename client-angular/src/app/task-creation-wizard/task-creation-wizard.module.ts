import {NgModule} from '@angular/core';
import {TaskCreationWizardComponent} from './task-creation-wizard.component';
import {TaskCreationWizardRoutingModule} from './task-creation-wizard.routes';

/**
 * The module which displays a task creation wizard.
 *
 * @see `io.spine.examples.todolist.c.procman.TaskCreationWizard` in `api-java`.
 */
@NgModule({
  declarations: [TaskCreationWizardComponent],
  imports: [TaskCreationWizardRoutingModule]
})
export class TaskCreationWizardModule {
}
