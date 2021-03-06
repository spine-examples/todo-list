/*
 * Copyright 2021, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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
import {TaskView} from 'proto/todolist/views_pb';
import {first} from 'rxjs/operators';

@Component({
  selector: 'app-task-list',
  templateUrl: './task-list.component.html'
})
export class TaskListComponent implements OnInit {

  @Input()
  private filter: (t: TaskView) => boolean;

  tasks: TaskView[];

  public hasElements: boolean;

  constructor(private route: ActivatedRoute, private readonly taskService: TaskService) {
  }

  ngOnInit(): void {
    if (!this.filter) {
      this.route.data
          .pipe(first())
          .subscribe((data: any): void => {
            this.filter = data.filter;
            this.initTaskList();
          });
    } else {
      this.initTaskList();
    }
  }

  private initTaskList(): void {
    this.reloadTasks();
    this.subscribeToTaskChanges();
  }

  private reloadTasks(): void {
    this.taskService.fetchAllTasks();
  }

  private subscribeToTaskChanges(): void {
    this.taskService.tasks$.subscribe((tasks: TaskView[]) => {
      this.tasks = tasks.filter(this.filter);
      this.hasElements = this.tasks.length !== 0;
    });
  }
}
