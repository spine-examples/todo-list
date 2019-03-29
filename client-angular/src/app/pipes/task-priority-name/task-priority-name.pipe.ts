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

import {Pipe, PipeTransform} from '@angular/core';

import {TaskPriority} from 'generated/main/js/todolist/attributes_pb';

/**
 * Converts task priority to its display name.
 *
 * Usage:
 *   task.getPriority() | taskPriorityName
 *
 * Example:
 *   {{ TaskPriority.HIGH | taskPriorityName }}
 *   formats to: 'High'
 */
@Pipe({
  name: 'taskPriorityName'
})
export class TaskPriorityName implements PipeTransform {

  private static readonly NAMES: Map<TaskPriority, string> = new Map([
    [TaskPriority.HIGH, 'High'],
    [TaskPriority.NORMAL, 'Normal'],
    [TaskPriority.LOW, 'Low']
  ]);

  /**
   * Does the transformation.
   *
   * If the given `TaskPriority` value is unknown to this pipe, an error is thrown.
   */
  transform(value: TaskPriority): string {
    const displayName = TaskPriorityName.NAMES.get(value);
    if (!displayName) {
      throw new Error(`Task priority ${value} is unknown`);
    }
    return displayName;
  }
}
