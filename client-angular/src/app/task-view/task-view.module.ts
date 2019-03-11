import {NgModule} from '@angular/core';
import {TaskViewComponent} from './task-view.component';
import {TaskViewRoutingModule} from './task-view.routes';

@NgModule({
  declarations: [TaskViewComponent],
  imports: [TaskViewRoutingModule]
})
export class TaskViewModule {
}
