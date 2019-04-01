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
import {CompletedTasksComponent} from 'app/task-list/completed/completed-tasks.component';
import {DeletedTasksComponent} from 'app/task-list/deleted/deleted-tasks.component';
import {DraftsComponent} from 'app/task-list/drafts/drafts.component';

const routes: Routes = [
  {
    path: '',
    redirectTo: 'active',
    pathMatch: 'full'
  },
  // TODO:2019-03-12:dmytro.kuzmin: Think about getting rid of lazy loading and importing
  // todo everything statically into the main module.
  {
    path: '',
    children: [
      {path: 'active', component: ActiveTasksComponent},
      {path: 'completed', component: CompletedTasksComponent},
      {path: 'deleted', component: DeletedTasksComponent},
      {path: 'drafts', component: DraftsComponent}
    ]
  },

  {
    path: 'details',
    loadChildren: 'app/task-details/task-details.module#TaskDetailsModule'
  },

  {
    path: 'labels',
    loadChildren: 'app/labels/labels.module#LabelsModule'
  },

  {
    path: 'wizard',
    loadChildren: 'app/task-creation-wizard/task-creation-wizard.module#TaskCreationWizardModule'
  },

  {
    path: '**',
    loadChildren: 'app/page-404/page-404.module#Page404Module'
  }
];

/**
 * The module which describes the navigation routes of the application.
 *
 * All child modules are loaded dynamically (thus lazily).
 */
@NgModule({
  imports: [
    RouterModule.forRoot(routes)
  ],
  exports: [RouterModule]
})
export class AppRoutingModule {
}
