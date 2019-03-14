import {NgModule} from '@angular/core';

import {MatDividerModule} from '@angular/material/divider';

import {ErrorViewport} from './error-viewport/error-viewport.component';
import {PageHeader} from './page-header/page-header.component';

/**
 * The common components of the To-Do List application.
 */
@NgModule({
  declarations: [
    ErrorViewport,
    PageHeader
  ],
  imports: [
    MatDividerModule
  ],
  exports: [
    ErrorViewport,
    PageHeader
  ]
})
export class TodoListCommonsModule {
}
