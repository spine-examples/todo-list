import {Component} from '@angular/core';
import {TaskService} from '../task-service/task.service';

/**
 * The component aggregating various task list views as well as basic application navigation.
 *
 * Is, in fact, a "main page" of the client.
 */
@Component({
  selector: 'app-tasks',
  templateUrl: './task-list.component.html',
  styleUrls: ['./task-list.component.css']
})
export class TaskListComponent {

  constructor(private readonly taskService: TaskService) {
  }

  createBasicTask(): void {
    this.taskService.createBasicTask('Random task');
  }
}
