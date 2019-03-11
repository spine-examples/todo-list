import {CommonModule} from '@angular/common';
import {NgModule} from '@angular/core';
import {RouterModule} from '@angular/router';
import {TaskListComponent} from './task-list.component';
import {ActiveTasksComponent} from './active/active-tasks.component';
import {CompletedTasksComponent} from './completed/completed-tasks.component';
import {DeletedTasksComponent} from './deleted/deleted-tasks.component';
import {DraftsComponent} from './drafts/drafts.component';
import {TaskListRoutingModule} from './task-list.routes';

@NgModule({
  declarations: [
    TaskListComponent,
    ActiveTasksComponent,
    CompletedTasksComponent,
    DeletedTasksComponent,
    DraftsComponent
  ],
  imports: [CommonModule, RouterModule, TaskListRoutingModule]
})
export class TaskListModule {
}
