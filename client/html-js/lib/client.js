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

let uuid = require("uuid");

let knownTypes =  require("../generated/main/js/index");

let UserId = require("spine-web/proto/spine/core/user_id_pb").UserId;
let spineWeb = require("spine-web/index");
let spineWebTypes = require('spine-web/proto/index');


let TaskId = require("../generated/main/js/todolist/identifiers_pb").TaskId;
let TaskDescription = require("../generated/main/js/todolist/values_pb").TaskDescription;
let CreateBasicTask = require("../generated/main/js/todolist/commands_pb").CreateBasicTask;
let TaskView = require("../generated/main/js/todolist/views_pb").TaskView;

let firebase = require("./firebase_client.js");

const logSuccess = () => {
    console.log("Command sent");
};

const errorCallback = (error) => {
    console.error(error);
};

const HOST = "http://localhost:8080";
const ACTOR = "TodoList-actor";

/**
 * The client of the TodoList application.
 */
export class Client {

    constructor() {
        this._spineWebClient = spineWeb.init({
            protoIndexFiles: [knownTypes, spineWebTypes],
            endpointUrl: HOST,
            firebaseDatabase: firebase.application.database(),
            actorProvider: Client._actorProvider()
        });
    }

    /**
     * Creates a new basic task with the given description.
     *
     * @param description the description of the new task
     */
    submitNewTask(description) {
        let command = Client._createTaskCommand(description);
        this._spineWebClient.sendCommand(command, logSuccess, errorCallback, errorCallback);
    }

    /**
     * Renders the task list to the given view.
     *
     * All changes to the list are received via subscriptions mechanism and are rendered
     * immediately.
     *
     * @param table the view to display the tasks in
     */
    renderTasksTo(table) {
        this.loadTasks(table);
        this.subscribeToTaskChanges(table);
    }

    /**
     * Loads all existing tasks and shows them at the given view.
     *
     * @param table the view to display the tasks in
     */
    loadTasks(table) {
        this._spineWebClient.fetch({entity: TaskView})
            .then(tasks => tasks.forEach(task => Client._addToTable(table, task)))
    }

    /**
     * Subscribes to all task list changes on the server and displays them on the UI.
     *
     * @param table the view to display the tasks in
     */
    subscribeToTaskChanges(table) {
        this._spineWebClient.subscribe({entity: TaskView})
            .then(({itemAdded, itemChanged, itemRemoved, unsubscribe}) => {
                itemAdded.subscribe(task => Client._addToTable(table, task));
            })
            .catch(errorCallback);
    }

    static _actorProvider() {
        let userId = new UserId();
        userId.setValue(ACTOR);
        return new spineWeb.ActorProvider(userId);
    }

    static _addToTable(table, taskView) {
        let description = taskView.getDescription().getValue();
        table.innerHTML += `<div class="task_item">${description}</div>`;
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
        id.setUuid(value);
        return id;
    }
}
