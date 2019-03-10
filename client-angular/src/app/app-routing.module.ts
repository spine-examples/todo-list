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
import {ActiveTasksComponent} from './tasks/active/active-tasks.component';
import {DraftsComponent} from './tasks/drafts/drafts.component';
import {CompletedTasksComponent} from './tasks/completed/completed-tasks.component';
import {DeletedTasksComponent} from './tasks/deleted/deleted-tasks.component';
import {TaskCreationWizardComponent} from './task-wizard/task-creation-wizard.component';
import {TaskListComponent} from './tasks/task-list.component';
import {LabelsComponent} from './labels/labels.component';

const routes: Routes = [
  {path: '', redirectTo: '/tasks', pathMatch: 'full'},

  {
    path: 'tasks',
    component: TaskListComponent,
    children: [
      {path: 'active', component: ActiveTasksComponent, outlet: 'tasks'},
      {path: 'drafts', component: DraftsComponent, outlet: 'tasks'},
      {path: 'completed', component: CompletedTasksComponent, outlet: 'tasks'},
      {path: 'deleted', component: DeletedTasksComponent, outlet: 'tasks'},
    ]
  },

  {path: 'labels', component: LabelsComponent},
  {path: 'wizard', component: TaskCreationWizardComponent}
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {
}
