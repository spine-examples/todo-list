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

let uuid = require("uuid");

let client = require("spine-js-client/client/index.js").client;

let TaskId = require("../proto/main/js/todolist/identifiers_pb").TaskId;
let TaskDescription = require("../proto/main/js/todolist/values_pb").TaskDescription;
let CreateBasicTask = require("../proto/main/js/todolist/c/commands_pb").CreateBasicTask;

let firebase = require("./firebase_client.js");

const noOp = () => {};

const errorCallback = error => {
    console.error(error);
};

export class Client {

    constructor() {
        this._backendClient = new client.BackendClient(
            new client.HttpClient("http://localhost:8080"),
            new client.FirebaseClient(firebase.application),
            new client.ActorRequestFactory("web-test-actor")
        );
    }

    submitNewTask(description) {
        let command = Client._createTaskCommand(description);

        let type = new client.TypeUrl(
            "type.spine.examples.todolist/spine.examples.todolist.CreateBasicTask"
        );
        let typedCommand = new client.TypedMessage(
            command,
            type
        );
        this._backendClient.sendCommand(typedCommand, noOp, errorCallback, errorCallback);
    }

    static _createTaskCommand(description) {
        let id = Client._newId();
        let descriptionValue = Client._description(description);

        let command = new CreateBasicTask();
        command.setId(id);
        command.setDescription(descriptionValue);

        return command;
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
