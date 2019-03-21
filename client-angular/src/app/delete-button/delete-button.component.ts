import {Component, Input} from '@angular/core';
import {TaskService} from '../task-service/task.service';

@Component({
  selector: 'app-delete-button',
  templateUrl: './delete-button.component.html'
})
export class DeleteButtonComponent {

  constructor(private readonly taskService: TaskService) {
  }

  @Input()
  taskToDelete: string;

  private deleteTask() {
    this.taskService.deleteTask(this.taskToDelete);
  }
}
