import {NgModule} from '@angular/core';

import {MatDividerModule} from '@angular/material/divider';

import {ErrorViewport} from './error-viewport/error-viewport.component';
import {PageHeader} from './page-header/page-header.component';
import {TaskPriorityName} from './task-priority-display-name/task-priority-name.pipe';

/**
 * The common components, directives and pipes of the To-Do List application.
 */
@NgModule({
  declarations: [
    ErrorViewport,
    PageHeader,
    TaskPriorityName
  ],
  imports: [
    MatDividerModule
  ],
  exports: [
    ErrorViewport,
    PageHeader,
    TaskPriorityName
  ]
})
export class TodoListCommonsModule {
}
