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

import {TaskCreationWizard} from '../service/task-creation-wizard.service';

@Injectable()
export class TaskCreationWizardCache {

  /**
   * For the fast navigation we only really need to cache the last visited wizard.
   */
  private static readonly CAPACITY = 1;

  private readonly cache: Map<string, TaskCreationWizard> = new Map();

  contains(id: string): boolean {
    return this.cache.has(id);
  }

  get(id: string): TaskCreationWizard {
    const cached = this.cache.get(id);
    if (!cached) {
      throw new Error(`No task creation process with ID ${id} cached`);
    }
    return cached;
  }

  store(id: string, wizard: TaskCreationWizard): void {
    if (this.cache.size >= TaskCreationWizardCache.CAPACITY) {
      this.cache.clear();
    }
    this.cache.set(id, wizard);
  }
}
