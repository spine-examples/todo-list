import {CommonModule} from '@angular/common';
import {NgModule} from '@angular/core';
import {TaskListComponent} from './task-list.component';
import {ActiveTasksComponent} from './active/active-tasks.component';
import {CompletedTasksComponent} from './completed/completed-tasks.component';
import {DeletedTasksComponent} from './deleted/deleted-tasks.component';
import {DraftsComponent} from './drafts/drafts.component';
import {TaskListRoutingModule} from './task-list.routes';
import {TaskServiceModule} from '../task-service/task-service.module';

/**
 * The module which displays the task list.
 */
@NgModule({
  declarations: [
    TaskListComponent,
    ActiveTasksComponent,
    CompletedTasksComponent,
    DeletedTasksComponent,
    DraftsComponent
  ],
  imports: [TaskListRoutingModule, CommonModule, TaskServiceModule]
})
export class TaskListModule {
}
