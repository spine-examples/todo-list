/*
 * Copyright 2018, TeamDev Ltd. All rights reserved.
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

import uuid from "../node_modules/uuid";

import {
    ActorRequestFactory,
    BackendClient,
    FirebaseClient,
    HttpClient
} from "../node_modules/spine-js-client/index"

import {TaskId} from "../proto/main/js/todolist/identifiers_pb";
import {TaskDescription} from "../proto/main/js/todolist/values_pb";
import {CreateBasicTask} from "../proto/main/js/todolist/c/commands_pb";

const noOp = () => {};

const errorCallback = error => {
    console.error(error);
};

export class Client {

    constructor() {
        this._backendClient = new BackendClient(
            new HttpClient("localhost:8080"),
            new FirebaseClient(firebase),
            new ActorRequestFactory("web-test-actor")
        );
    }

    submitNewTask(description) {
        let command = Client._createTaskCommand(description);

        let typedCommand = new TypedMessage(
            command,
            new TypeUrl("type.spine.examples.todolist/spine.examples.todolist.CreateBasicTask")
        );
        this._backendClient.sendCommand(typedCommand, noOp, errorCallback, errorCallback);
    }

    static _createTaskCommand(description) {
        let id = Client._newId();
        let descriptionValue = Client._description(description);

        let command = new CreateBasicTask();
        command.setId(id);
        command.setDescription(descriptionValue);
    }

    static _description(value) {
        let result = new TaskDescription();
        result.setValue(value);
        return result;
    }

    static _newId() {
        let id = new TaskId();
        let value = uuid.v4();
        id.setValue(value);
        return id;
    }
}
