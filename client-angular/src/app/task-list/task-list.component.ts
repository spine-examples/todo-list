/*
 * Copyright 2019, TeamDev. All rights reserved.
 *
 * Redistribution and use in source and/or binary forms, with or without
 * modification, must retain the above copyright notice and the following
 * disclaimer.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import {Component, Input, OnInit} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {TaskService} from 'app/task-service/task.service';
import {TaskItem} from 'proto/todolist/q/projections_pb';

@Component({
  selector: 'app-task-list',
  templateUrl: './task-list.component.html'
})
export class TaskListComponent implements OnInit {

  @Input()
  private filter: (t: TaskItem) => boolean;

  private tasks: TaskItem[];

  public hasElements: boolean;

  constructor(private route: ActivatedRoute, private readonly taskService: TaskService) {
  }

  ngOnInit(): void {
    if (!this.filter) {
      this.route.data
        .subscribe(data => {
          this.initializeFromRoutedData(data);
          this.performSubscription();
        });
    } else {
      this.performSubscription();
    }
  }

  private performSubscription() {
    this.taskService.tasks$.subscribe(tasks => {
      this.tasks = tasks.filter(this.filter);
      this.hasElements = this.tasks.length !== 0;
    });
  }

  private initializeFromRoutedData(data) {
    if (!this.filter) {
      this.filter = data.filter;
    }
  }
}
