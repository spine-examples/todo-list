import {Component, Input} from '@angular/core';

import {TaskId} from 'generated/main/js/todolist/identifiers_pb';
import {TaskItem} from 'generated/main/js/todolist/q/projections_pb';

/**
 * The view of a single task in the list.
 */
@Component({
  selector: 'app-task-item',
  templateUrl: './task-item.component.html',
  styleUrls: ['./task-item.component.css']
})
export class TaskItemComponent {

  @Input()
  task: TaskItem;

  completeTask(): void {
    alert(`Completing task with ID: ${this.task.getId()}`);
  }

  deleteTask(): void {
    alert(`Deleting task with ID: ${this.task.getId()}`);
  }
}
