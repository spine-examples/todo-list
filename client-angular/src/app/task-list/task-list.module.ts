import {NgModule} from '@angular/core';
import {TaskListComponent} from './task-list.component';
import {ActiveTasksComponent} from './active/active-tasks.component';
import {CompletedTasksComponent} from './completed/completed-tasks.component';
import {DeletedTasksComponent} from './deleted/deleted-tasks.component';
import {DraftsComponent} from './drafts/drafts.component';
import {RouterModule} from '@angular/router';
import {BrowserModule} from '@angular/platform-browser';

@NgModule({
  declarations: [
    TaskListComponent,
    ActiveTasksComponent,
    CompletedTasksComponent,
    DeletedTasksComponent,
    DraftsComponent
  ],
  imports: [BrowserModule, RouterModule]
})
export class TaskListModule { }
