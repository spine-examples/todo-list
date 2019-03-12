import {NgModule} from '@angular/core';
import {TaskViewComponent} from './task-view.component';
import {TaskViewRoutingModule} from './task-view.routes';

/**
 * The module responsible for displaying a single task view.
 *
 * Presents the information about a task to the user and allows for task updates.
 */
@NgModule({
  declarations: [TaskViewComponent],
  imports: [TaskViewRoutingModule]
})
export class TaskViewModule {
}
