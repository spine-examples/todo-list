import {Component, OnInit} from '@angular/core';
import {TaskService} from 'app/task-service/task.service';

import {TaskItem, TaskStatus} from 'proto/todolist/q/projections_pb';

/**
 * A component that represents a subset of all tasks, such as `Active` or `Deleted`.
 */
@Component({
  selector: 'app-task-list-category',
  template: ``
})
export class TaskListCategoryComponent implements OnInit {

  /** Visible for testing. */
  public tasks: TaskItem[];

  constructor(protected readonly taskService: TaskService,
              private readonly filter: (task: TaskItem) => boolean) {
  }

  ngOnInit() {
    this.taskService.tasks$.subscribe((taskItems: TaskItem[]) => {
      this.tasks = taskItems.filter(this.filter);
    });
  }
}
