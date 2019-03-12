import {Component} from '@angular/core';
import {TaskService} from '../task-service/task.service';

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
