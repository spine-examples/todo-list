import {Component} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {Location} from '@angular/common';

// TODO:2019-02-05:dmytro.kuzmin: How to fetch task details:
// todo https://angular.io/guide/router#fetch-data-before-navigating.
/**
 * Component responsible for displaying a single task.
 */
@Component({
  selector: 'app-task-details',
  templateUrl: './task-view.component.html',
  styleUrls: ['./task-view.component.css']
})
export class TaskViewComponent {

  private readonly taskId;

  constructor(private readonly route: ActivatedRoute, private readonly location: Location) {
    this.taskId = this.route.snapshot.paramMap.get('id');
  }

  back(): void {
    this.location.back();
  }
}
