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

import {tomorrow} from 'test/given/dates';
import {TaskId} from 'proto/todolist/identifiers_pb';
import {TaskDescription} from 'proto/todolist/values_pb';
import {TaskView} from 'proto/todolist/views_pb';
import {TaskPriority, TaskStatus} from 'proto/todolist/attributes_pb';

export const CHORE_1_ID = 'task-1';
export const CHORE_1_DESC = 'Wash the dishes';

export const CHORE_2_ID = 'task-2';
export const CHORE_2_DESC = 'Clean the house';

export function chores(): TaskView[] {
  const tasks = [
    taskItem(CHORE_1_ID, CHORE_1_DESC),
    taskItem(CHORE_2_ID, CHORE_2_DESC)
  ];
  tasks.forEach(task => task.setStatus(TaskStatus.OPEN));
  return tasks;
}

export function taskWithId(desiredId: string): TaskView {
  const taskId = new TaskId();
  taskId.setUuid(desiredId);
  const description = new TaskDescription();
  description.setValue('Wash my car');
  return taskItem(taskId, description);
}

export function taskItem(id: TaskId, description: TaskDescription): TaskView {
  const result = new TaskView();
  const taskId = new TaskId();
  taskId.setUuid(id);
  result.setId(taskId);
  const taskDescription = new TaskDescription();
  taskDescription.setValue(description);
  result.setDescription(taskDescription);
  result.setDueDate(tomorrow());
  result.setStatus(TaskStatus.OPEN);
  result.setPriority(TaskPriority.NORMAL);
  return result;
}

export function chore(): TaskView {
  return taskItem(CHORE_1_ID, CHORE_1_DESC);
}
