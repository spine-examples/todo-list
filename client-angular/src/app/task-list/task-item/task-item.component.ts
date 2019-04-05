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

import {Component, Input} from '@angular/core';

import {TaskItem, TaskStatus} from 'proto/todolist/q/projections_pb';
import {TaskService} from 'app/task-service/task.service';

@Component({
  selector: 'app-task-item',
  templateUrl: './task-item.component.html',
  styleUrls: ['./task-item.component.css']
})
export class TaskItemComponent {

  constructor(private readonly taskService: TaskService) {
  }

  @Input()
  private task: TaskItem;

  private expanded: boolean;

  private get displayCompleteButton(): boolean {
    return this.shouldShowButton();
  }

  private get displayDeleteButton(): boolean {
    return this.shouldShowButton();
  }

  private shouldShowButton() {
    if (this.task) {
      const status = this.task.getStatus();
      return status === TaskStatus.OPEN || status === TaskStatus.FINALIZED;
    }
    return false;
  }

  private completeTask() {
    this.taskService.completeTask(this.task.getId());
  }

  private deleteTask() {
    this.taskService.deleteTask(this.task.getId());
  }
}
