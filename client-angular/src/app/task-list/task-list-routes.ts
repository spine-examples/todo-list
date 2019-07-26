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

import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';

import {ActiveTasksComponent} from 'app/task-list/active/active-tasks.component';
import {DraftsComponent} from 'app/task-list/drafts/drafts.component';
import {TaskStatus, TaskView} from 'proto/todolist/projections_pb';
import {TaskListComponent} from 'app/task-list/task-list.component';

/**
 * Task filters for components that access the `TaskListComponent` directly.
 */
const completedFilter = (task: TaskView) => task.getStatus() === TaskStatus.COMPLETED;
const deletedFilter = (task: TaskView) => task.getStatus() === TaskStatus.DELETED;


export const routes: Routes = [
  {
    path: 'tasks',
    children: [
      {
        path: 'active',
        component: ActiveTasksComponent
      },
      {
        path: 'completed',
        component: TaskListComponent,
        data: {
          filter: completedFilter
        }
      },
      {
        path: 'deleted',
        component: TaskListComponent,
        data: {
          filter: deletedFilter
        }
      },
      {path: 'drafts', component: DraftsComponent}
    ]
  }
];

/**
 * The routing configuration of the {@link TaskListModule}.
 */
@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class TaskListRoutingModule {
}
