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
import {TaskItem} from 'proto/todolist/q/projections_pb';
import {TaskService} from 'app/task-service/task.service';

/**
 * The view of a single `Active` task in the list.
 */
@Component({
  selector: 'app-active-task-item',
  templateUrl: './active-task-item.component.html',
  styleUrls: ['./active-task-item.component.css']
})
export class ActiveTaskItemComponent {

  constructor(readonly taskService: TaskService) {
  }

  @Input()
  task: TaskItem;

  /**
   * Marks this task as `Complete`, changing its status respectively.
   */
  completeTask(): void {
    this.taskService.completeTask(this.task.getId());
  }

  /**
   * Deletes this task from the list.
   */
  deleteTask(): void {
    this.taskService.deleteTask(this.task.getId());
  }
}
