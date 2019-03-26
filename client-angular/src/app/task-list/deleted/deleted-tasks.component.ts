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

import {Component, OnDestroy, OnInit} from '@angular/core';

import {TaskItem, TaskStatus} from 'generated/main/js/todolist/q/projections_pb';
import {TaskService} from '../../task-service/task.service';

/**
 * A component displaying deleted tasks view.
 */
@Component({
  selector: 'app-deleted-tasks',
  templateUrl: './deleted-tasks.component.html'
})
export class DeletedTasksComponent implements OnInit, OnDestroy {

  private tasks: TaskItem[];

  constructor(private readonly taskService: TaskService) {
  }

  ngOnDestroy(): void {
    this.taskService.unsubscribe();
  }

  ngOnInit(): void {
    this.tasks = this.taskService.tasks.getValue().filter(task => task.getStatus() === TaskStatus.DELETED);
  }
}
