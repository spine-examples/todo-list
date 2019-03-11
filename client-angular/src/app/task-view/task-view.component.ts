import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Location } from '@angular/common';

// todo how to fetch task details https://angular.io/guide/router#fetch-data-before-navigating.
@Component({
  selector: 'app-task-details',
  templateUrl: './task-view.component.html',
  styleUrls: ['./task-view.component.css']
})
export class TaskViewComponent implements OnInit {

  private readonly taskId;

  constructor(private readonly route: ActivatedRoute, private readonly location: Location) {
    this.taskId = this.route.snapshot.paramMap.get('id');
  }

  ngOnInit() {
  }

  back(): void {
    this.location.back();
  }
}
