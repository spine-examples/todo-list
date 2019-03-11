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

import {Injectable} from '@angular/core';
import {MyListView, TaskItem} from 'generated/main/js/todolist/q/projections_pb';
import {TaskServiceModule} from './task-service.module';
import {MessageType, SpineWebClient} from '../spine-web-client/spine-web-client.service';

import {TaskId} from 'generated/main/js/todolist/identifiers_pb';
import {TaskDescription} from 'generated/main/js/todolist/values_pb';
import {CreateBasicTask} from 'generated/main/js/todolist/c/commands_pb';

@Injectable({
  providedIn: TaskServiceModule,
})
export class TaskService {

  constructor(private readonly spineWebClient: SpineWebClient) {
  }

  subscribeToActive(tasks: TaskItem[]): void {
    const fillTable = (view: MyListView) => {
      console.log('Obtained a new version of view');
    };
    const messageType = new MessageType<MyListView>(MyListView);
    this.spineWebClient.subscribeWithCallback(messageType, fillTable);
  }
}
