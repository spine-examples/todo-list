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

import {Timestamp} from 'google-protobuf/google/protobuf/timestamp_pb';
import {TaskId, TaskListId} from 'proto/todolist/identifiers_pb';
import {TaskDescription} from 'proto/todolist/values_pb';
import {MyListView, TaskItem, TaskListView, TaskView} from 'proto/todolist/q/projections_pb';
import {TaskPriority, TaskStatus} from 'proto/todolist/attributes_pb';

export const HOUSE_TASK_1_ID = 'task-1';
export const HOUSE_TASK_1_DESC = 'Wash the dishes';

export const HOUSE_TASK_2_ID = 'task-2';
export const HOUSE_TASK_2_DESC = 'Clean the house';

export function houseTasks(): MyListView {
  const taskListId = new TaskListId();
  taskListId.setValue('task-list-ID');

  const taskListView = new TaskListView();
  const tasks = [
    taskItem(HOUSE_TASK_1_ID, HOUSE_TASK_1_DESC),
    taskItem(HOUSE_TASK_2_ID, HOUSE_TASK_2_DESC)
  ];
  tasks.forEach(taskview => taskview.setStatus(TaskStatus.OPEN));
  taskListView.setItemsList(tasks);
  const result = new MyListView();
  result.setId(taskListId);
  result.setMyList(taskListView);
  return result;
}

export function taskWithId(desiredId: string): TaskItem {
  const taskId = new TaskId();
  taskId.setValue(desiredId);
  const description = 'Wash my car';
  const result = new TaskItem();
  result.setId(taskId);
  result.setDescription(description);
  return result;
}

export function taskItem(id: TaskId, description: TaskDescription): TaskItem {
  const result = new TaskItem();
  const taskId = new TaskId();
  taskId.setValue(id);
  result.setId(taskId);
  const taskDescription = new TaskDescription();
  taskDescription.setValue(description);
  result.setDescription(taskDescription);
  return result;
}

export function houseTask(): TaskView {
  return taskView(HOUSE_TASK_1_ID, HOUSE_TASK_1_DESC);
}

function taskView(id: string, description: string): TaskView {
  const result = new TaskView();
  const taskId = new TaskId();
  taskId.setValue(id);
  result.setId(taskId);
  const taskDescription = new TaskDescription();
  taskDescription.setValue(description);
  result.setDescription(taskDescription);
  result.setPriority(TaskPriority.HIGH);

  // noinspection TypeScriptValidateJSTypes Wrong IDEA type lookup.
  result.setDueDate(tomorrow());
  return result;
}
