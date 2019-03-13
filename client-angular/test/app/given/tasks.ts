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

import {TaskId, TaskListId} from 'generated/main/js/todolist/identifiers_pb';
import {TaskDescription} from 'generated/main/js/todolist/values_pb';
import {TaskItem, TaskListView, MyListView} from 'generated/main/js/todolist/q/projections_pb';

export const HOUSE_TASK_1_ID = 'task-1';
export const HOUSE_TASK_1_DESC = 'Wash the dishes';

export const HOUSE_TASK_2_ID = 'task-2';
export const HOUSE_TASK_2_DESC = 'Clean the house';

export function houseTasks(): MyListView {
  const task1 = new TaskItem();
  const id1 = new TaskId();
  id1.setValue(HOUSE_TASK_1_ID);
  task1.setId(id1);
  const description1 = new TaskDescription();
  description1.setValue(HOUSE_TASK_1_DESC);
  task1.setDescription(description1);

  const task2 = new TaskItem();
  const id2 = new TaskId();
  id2.setValue(HOUSE_TASK_2_ID);
  task2.setId(id2);
  const description2 = new TaskDescription();
  description2.setValue(HOUSE_TASK_2_DESC);
  task2.setDescription(description2);

  const taskListView = new TaskListView();
  taskListView.setItemsList([task1, task2]);

  const taskListId = new TaskListId();
  taskListId.setValue('task-list-ID');

  const result = new MyListView();
  result.setId(taskListId);
  result.setMyList(taskListView);
  return result;
}
