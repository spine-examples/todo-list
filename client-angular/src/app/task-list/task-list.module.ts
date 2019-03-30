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

import {CommonModule} from '@angular/common';
import {NgModule} from '@angular/core';
import {MatSidenavModule} from '@angular/material/sidenav';
import {ReactiveFormsModule} from '@angular/forms';
import {MatInputModule} from '@angular/material';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {MatListModule} from '@angular/material/list';
import {ActiveTasksComponent} from 'app/task-list/active/active-tasks.component';
import {CompletedTasksComponent} from 'app/task-list/completed/completed-tasks.component';
import {DeletedTasksComponent} from 'app/task-list/deleted/deleted-tasks.component';
import {DraftsComponent} from 'app/task-list/drafts/drafts.component';
import {TaskServiceModule} from 'app/task-service/task-service.module';
import {ActiveTaskItemComponent} from 'app/task-list/active/active-task-item/active-task-item.component';
import {TaskLinkComponent} from 'app/task-list/task-link/task-link.component';
import {TaskListCategoryComponent} from 'app/task-list/task-list-category/task-list-category.component';
import {AppRoutingModule} from 'app/app-routing.module';

/**
 * The module which displays the task list.
 */
@NgModule({
  declarations: [
    TaskLinkComponent,
    ActiveTaskItemComponent,
    ActiveTasksComponent,
    CompletedTasksComponent,
    DeletedTasksComponent,
    DraftsComponent,
    TaskListCategoryComponent
  ],
  imports: [
    AppRoutingModule,
    CommonModule,
    TaskServiceModule,
    MatSidenavModule,
    MatListModule,
    MatInputModule,
    ReactiveFormsModule,
    MatButtonModule,
    MatIconModule
  ],
  exports: [
    MatInputModule,
    ReactiveFormsModule,
    MatButtonModule,
    MatIconModule
  ]
})
export class TaskListModule {
}
