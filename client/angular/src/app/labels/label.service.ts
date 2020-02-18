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
import {Client} from 'spine-web';
import {UuidGenerator} from 'app/uuid-generator/uuid-generator';

import {LabelId} from 'proto/todolist/identifiers_pb';
import {CreateBasicLabel} from 'proto/todolist/commands_pb';
import {LabelView} from 'proto/todolist/views_pb';

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

  /**
   * Temporary method for smoke tests.
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

  /**
   * Fetches a list of labels available in the application.
   */
  fetchAllLabels(): Promise<LabelView[]> {
    return this.spineWebClient.select(LabelView).run();
  }

  /**
   * Fetches the details of a single label.
   *
   * In case nothing is found by the specified ID, the promise is rejected.
   */
  fetchLabelDetails(labelId: LabelId): Promise<LabelView> {
    return new Promise<LabelView>((resolve, reject) => {
          const dataCallback = labels => {
            if (labels.length < 1) {
              reject(`No label view found for ID: ${labelId}`);
            } else {
              resolve(labels[0]);
            }
          };
          this.spineWebClient.select(LabelView)
                             .byId(labelId)
                             .run()
                             .then(labels => dataCallback(labels))
                             .catch(err => reject(err));
        }
    );
  }
}
