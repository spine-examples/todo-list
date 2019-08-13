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

import {chore} from 'test/given/tasks';

import {TaskCreationId} from 'proto/todolist/identifiers_pb';
import {TaskCreation} from 'proto/todolist/tasks_pb';
import {LabelIdsList} from 'proto/todolist/values_pb';
import {LabelView, TaskView} from 'proto/todolist/views_pb';

export function initMockProcess(stage?: TaskCreation.Stage)
  : ({entity: cls, byIds: ids}) => Promise<any> {
  const creationProcess = taskCreationProcess(stage);
  const task = chore();
  return resolveWithOneOf(creationProcess, task);
}

export function initMockProcessWithLabels(selected: LabelView[], available: LabelView[])
  : ({entity: cls, byIds: ids}) => Promise<any> {
  const creationProcess = taskCreationProcess(TaskCreation.Stage.LABEL_ASSIGNMENT);
  const task = chore();

  const labelIdsList = new LabelIdsList();
  labelIdsList.setIdsList(selected.map(label => label.getId()));
  task.setLabelIdsList(labelIdsList);
  return resolveWithOneOf(creationProcess, task, available);
}

export function taskCreationProcess(stage?: TaskCreation.Stage): TaskCreation {
  const creationIdValue = 'task-creation-ID';
  const taskCreationId = new TaskCreationId();
  taskCreationId.setUuid(creationIdValue);
  const creationStage = stage !== undefined ? stage : TaskCreation.Stage.LABEL_ASSIGNMENT;
  const taskCreation = new TaskCreation();
  taskCreation.setId(taskCreationId);
  taskCreation.setStage(creationStage);
  taskCreation.setTaskId(chore().getId());
  return taskCreation;
}

function resolveWithOneOf(creationProcess, task, labels?) {
  return ({entity: cls}) => {
    if (cls === TaskCreation) {
      return Promise.resolve([creationProcess]);
    } else if (cls === TaskView) {
      return Promise.resolve([task]);
    } else if (cls === LabelView) {
      const labelsArr = labels ? labels : [];
      return Promise.resolve(labelsArr);
    }
  };
}
