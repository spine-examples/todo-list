/*
 * Copyright 2018, TeamDev. All rights reserved.
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

let client = require("spine-web-client/client/index.js").client;

let TaskId = require("../proto/main/js/todolist/identifiers_pb").TaskId;
let TaskDescription = require("../proto/main/js/todolist/values_pb").TaskDescription;
let CreateBasicTask = require("../proto/main/js/todolist/c/commands_pb").CreateBasicTask;
let MyListView = require("../proto/main/js/todolist/q/projections_pb").MyListView;

let firebase = require("./firebase_client.js");

const logSuccess = () => {
    console.log("Command sent");
};

const errorCallback = (error) => {
    console.error(error);
};

const HOST = "http://localhost:8080";

/**
 * The client of the TodoList application.
 */
export class Client {

    constructor() {
        this._backendClient = client.BackendClient.usingFirebase({
            atEndpoint: HOST,
            withFirebaseStorage: firebase.application,
            forActor: "TodoList-actor"
        });
    }

    /**
     * Creates a new basic task with the given description.
     *
     * @param description the description of the new task
     */
    submitNewTask(description) {
        let command = Client._createTaskCommand(description);

        let typeUrl = new client.TypeUrl(
            "type.spine.examples.todolist/spine.examples.todolist.CreateBasicTask"
        );
        let type = new client.Type(
            CreateBasicTask,
            typeUrl
        );
        let typedCommand = new client.TypedMessage(
            command,
            type
        );
        this._backendClient.sendCommand(typedCommand, logSuccess, errorCallback, errorCallback);
    }

    /**
     * Fetches all the tasks from the server and displays them on the UI.
     *
     * @param table the view to display the tasks in
     */
    fetchTasks(table) {
        let typeUrl = new client.TypeUrl(
            "type.spine.examples.todolist/spine.examples.todolist.MyListView"
        );
        let type = new client.Type(
            MyListView,
            typeUrl
        );
        this._backendClient.fetchAll({ofType: type}).atOnce().then(
            views => {
                let viewCount = views.length;
                if (viewCount === 1) {
                    let view = views[0];
                    Client._fillTable(table, view);
                } else if (viewCount > 1) {
                    console.error(
                        `Expected no more than one list view returned, but received ${viewCount}`
                    );
                }
            },
            errorCallback);
    }

    static _fillTable(table, myListView) {
        let items = myListView.myList.items;
        table.innerHTML = "";
        for (let item of items) {
            let description = item.description.value;
            table.innerHTML += `<div class="task_item">${description}</div>`;
        }
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
