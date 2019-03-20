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
import {Client, Type} from 'spine-web';
import {UuidGenerator} from '../uuid-generator/uuid-generator';

import {LabelId, TaskId} from 'generated/main/js/todolist/identifiers_pb';
import {TaskLabel, TaskLabels} from 'generated/main/js/todolist/model_pb';
import {CreateBasicLabel} from 'generated/main/js/todolist/c/commands_pb';

/**
 * A service which operates with task labels.
 */
@Injectable()
export class LabelService {

  /**
   * @param spineWebClient a client for accessing Spine backend
   */
  constructor(private readonly spineWebClient: Client) {
  }

  static sortLabels(a: TaskLabel, b: TaskLabel): number {
    return a.getTitle().localeCompare(b.getTitle());
  }

  /**
   * Temporary test-only method.
   */
  createBasicLabel(): Promise<void> {
    return new Promise<void>((resolve, reject) => {
        const cmd = new CreateBasicLabel();
        const id = UuidGenerator.newId(LabelId);
        cmd.setLabelId(id);
        cmd.setLabelTitle('TestLabel');
        this.spineWebClient.sendCommand(cmd, resolve, reject, reject);
      }
    );
  }

  fetchAllLabels(): Promise<TaskLabel[]> {
    return this.spineWebClient.fetchAll({ofType: Type.forClass(TaskLabel)}).atOnce();
  }

  fetchTaskLabels(taskId: TaskId): Promise<TaskLabel[]> {
    return this.fetchTaskLabelsInstance(taskId)
      .then(labels => {
        if (!labels) {
          return Promise.resolve([]);
        }
        const ids = labels.getLabelIdsList().getIdsList();
        const details = ids.map(id => this.fetchLabelDetails(id));
        return Promise.all(details);
      });
  }

  /**
   * If `TaskLabels` instance is not found, it just means there is no assigned labels history, and
   * not an error.
   */
  private fetchTaskLabelsInstance(taskId: TaskId): Promise<TaskLabels> {
    return new Promise<TaskLabels>((resolve, reject) =>
      this.spineWebClient.fetchById(Type.forClass(TaskLabels), taskId, resolve, reject)
    );
  }

  private fetchLabelDetails(labelId: LabelId): Promise<TaskLabel> {
    return new Promise<TaskLabel>((resolve, reject) =>
      this.spineWebClient.fetchById(Type.forClass(TaskLabel), labelId, resolve, reject)
    );
  }
}
